package io.github.paomiantong.slimefun_predicate.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.util.Identifier;

public final class Render {

    public static void setupBlend()
    {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
    }

    public static void setupBlendSimple()
    {
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
    }

    public static void bindTexture(Identifier texture)
    {
        RenderSystem.setShaderTexture(0, texture);
    }

    public static void color(float r, float g, float b, float a)
    {
        RenderSystem.setShaderColor(r, g, b, a);
    }

    public static void disableDiffuseLighting()
    {
        // FIXME 1.15-pre4+
        DiffuseLighting.disableGuiDepthLighting();
    }

    public static void enableDiffuseLightingForLevel()
    {
        DiffuseLighting.enableForLevel();
    }

    public static void enableDiffuseLightingGui3D() {
        // FIXME 1.15-pre4+
        DiffuseLighting.enableGuiDepthLighting();
    }

    public static void drawTexturedRectBatched(int x, int y, int u, int v, int width, int height, BufferBuilder buffer) {
        drawTexturedRectBatched(x, y, u, v, width, height, 0, buffer);
    }

    public static void drawTexturedRectBatched(int x, int y, int u, int v, int width, int height, float zLevel, BufferBuilder buffer) {
        float pixelWidth = 0.00390625F;

        buffer.vertex(x, y + height, zLevel).texture(u * pixelWidth, (v + height) * pixelWidth);
        buffer.vertex(x + width, y + height, zLevel).texture((u + width) * pixelWidth, (v + height) * pixelWidth);
        buffer.vertex(x + width, y, zLevel).texture((u + width) * pixelWidth, v * pixelWidth);
        buffer.vertex(x, y, zLevel).texture(u * pixelWidth, v * pixelWidth);
    }

    public static int getScaledWindowWidth() {
        return MinecraftClient.getInstance().getWindow().getScaledWidth();
    }

    public static int getScaledWindowHeight() {
        return MinecraftClient.getInstance().getWindow().getScaledHeight();
    }
}
