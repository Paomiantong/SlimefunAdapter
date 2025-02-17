package io.github.paomiantong.slimefunadapter.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4fStack;

import static io.github.paomiantong.slimefunadapter.render.Render.*;

public final class InventoryRender {
    public static final Identifier TEXTURE_DOUBLE_CHEST = Identifier.ofVanilla("textures/gui/container/generic_54.png");

    public static void renderBackPackPreview(Inventory inv, int baseX, int baseY, DrawContext drawContext) {
        InventoryProperties props = new InventoryProperties(inv.size());
        final MinecraftClient mc = MinecraftClient.getInstance();
        int screenWidth = getScaledWindowWidth();
        int screenHeight = getScaledWindowHeight();
        int height = props.height + 18;
        int x = MathHelper.clamp(baseX + 8, 0, screenWidth - props.width);
        int y = MathHelper.clamp(baseY - height, 0, screenHeight - height);

        color(1f, 1f, 1f, 1f);

        disableDiffuseLighting();

        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        matrix4fStack.translate(0, 0, 500);
        RenderSystem.applyModelViewMatrix();

        renderInventoryBackground(x, y, props.slotsPerRow, inv.size(), mc);

        enableDiffuseLightingGui3D();

        renderInventoryStacks(inv, x + props.slotOffsetX, y + props.slotOffsetY, props.slotsPerRow, 0, inv.size(), mc, drawContext);

        matrix4fStack.popMatrix();
        RenderSystem.applyModelViewMatrix();
    }

    public static void renderInventoryBackground(int x, int y, int slotsPerRow, int totalSlots, MinecraftClient mc) {
        setupBlend();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        BuiltBuffer builtBuffer;

        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.applyModelViewMatrix();

        bindTexture(TEXTURE_DOUBLE_CHEST);

        // Draw the slot backgrounds according to how many slots there actually are
        int rows = (int) (Math.ceil((double) totalSlots / (double) slotsPerRow));
        int bgw = Math.min(totalSlots, slotsPerRow) * 18 + 7;
        int bgh = rows * 18 + 7;

        drawTexturedRectBatched(x, y, 0, 0, 7, bgh, buffer); // left (top)
        drawTexturedRectBatched(x + 7, y, 176 - bgw, 0, bgw, 7, buffer); // top (right)
        drawTexturedRectBatched(x, y + bgh, 0, 215, bgw, 7, buffer); // bottom (left)
        drawTexturedRectBatched(x + bgw, y + 7, 169, 222 - bgh, 7, bgh, buffer); // right (bottom)

        for (int row = 0; row < rows; row++) {
            int rowLen = MathHelper.clamp(totalSlots - (row * slotsPerRow), 1, slotsPerRow);
            drawTexturedRectBatched(x + 7, y + row * 18 + 7, 7, 17, rowLen * 18, 18, buffer);

            // Render the background for the last non-existing slots on the last row,
            // in two strips of the background texture from the double chest texture's top part.
            if (rows > 1 && rowLen < slotsPerRow) {
                drawTexturedRectBatched(x + rowLen * 18 + 7, y + row * 18 + 7, 7, 3, (slotsPerRow - rowLen) * 18, 9, buffer);
                drawTexturedRectBatched(x + rowLen * 18 + 7, y + row * 18 + 16, 7, 3, (slotsPerRow - rowLen) * 18, 9, buffer);
            }
        }

        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();

        try {
            builtBuffer = buffer.end();
            BufferRenderer.drawWithGlobalProgram(builtBuffer);
            builtBuffer.close();
        } catch (Exception ignored) {
        }
    }

    public static void renderInventoryStacks(Inventory inv, int startX, int startY, int slotsPerRow, int startSlot, int maxSlots, MinecraftClient mc, DrawContext drawContext) {
        renderInventoryStacks(inv, startX, startY, slotsPerRow, startSlot, maxSlots, mc, drawContext, 0, 0);
    }

    public static void renderInventoryStacks(Inventory inv, int startX, int startY, int slotsPerRow, int startSlot, int maxSlots, MinecraftClient mc, DrawContext drawContext, double mouseX, double mouseY) {
        final int slots = inv.size();
        int x = startX;
        int y = startY;

        if (maxSlots < 0) {
            maxSlots = slots;
        }

        for (int slot = startSlot, i = 0; slot < slots && i < maxSlots; ) {
            for (int column = 0; column < slotsPerRow && slot < slots && i < maxSlots; ++column, ++slot, ++i) {
                ItemStack stack = inv.getStack(slot);

                if (!stack.isEmpty()) {
                    renderStackAt(stack, x, y, 1, mc, drawContext, mouseX, mouseY);
                }

                x += 18;
            }

            x = startX;
            y += 18;
        }
    }

    public static void renderStackAt(ItemStack stack, float x, float y, float scale, MinecraftClient mc, DrawContext drawContext) {
        renderStackAt(stack, x, y, scale, mc, drawContext, 0, 0);
    }

    public static void renderStackAt(ItemStack stack, float x, float y, float scale, MinecraftClient mc, DrawContext drawContext, double mouseX, double mouseY) {
        MatrixStack matrixStack = drawContext.getMatrices();
        matrixStack.push();
        matrixStack.translate(x, y, 0.f);
        matrixStack.scale(scale, scale, 1);

//        RenderUtils.enableDiffuseLightingGui3D();
//        RenderUtils.color(1f, 1f, 1f, 1f);

        drawContext.drawItem(stack, 0, 0);

//        RenderUtils.color(1f, 1f, 1f, 1f);
        drawContext.drawItemInSlot(mc.textRenderer, stack, 0, 0);

//        RenderUtils.color(1f, 1f, 1f, 1f);
        matrixStack.pop();
    }

    public static class InventoryProperties {
        public int totalSlots = 1;
        public int width = 176;
        public int height = 83;
        public int slotsPerRow = 9;
        public int slotOffsetX = 8;
        public int slotOffsetY = 8;

        public InventoryProperties(int totalSlots) {
            int rows = (int) (Math.ceil((double) totalSlots / (double) this.slotsPerRow));
            this.width = Math.min(this.slotsPerRow, totalSlots) * 18 + 14;
            this.height = rows * 18 + 14;
        }
    }
}
