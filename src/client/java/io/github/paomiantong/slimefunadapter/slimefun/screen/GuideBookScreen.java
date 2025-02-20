package io.github.paomiantong.slimefunadapter.slimefun.screen;

import io.github.paomiantong.slimefunadapter.slimefun.menuloader.MenuSynchronizer;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Slf4j(topic = "SlimefunAdapter")
public class GuideBookScreen extends GenericContainerScreen {
    public GuideBookScreen(GenericContainerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    private ButtonWidget stopSyncButton;

    @Override
    protected void init() {
        super.init();

        // Sync button setup
        int buttonSize = textRenderer.fontHeight;
        int buttonX = x + titleX + textRenderer.getWidth(title) + 4;
        int buttonY = y + titleY;

        addDrawableChild(ButtonWidget.builder(
                        Text.literal("⟲"),
                        button -> onSyncPressed(hasShiftDown()))
                .dimensions(buttonX, buttonY, buttonSize, buttonSize)
                .tooltip(Tooltip.of(Text.translatable("gui.slimefunadapter.sync_button.tooltip")))
                .build());

        // Stop sync button (initially invisible)
        stopSyncButton = addDrawableChild(ButtonWidget.builder(
                        Text.literal("⏹").formatted(Formatting.RED),
                        button -> MenuSynchronizer.stopSync())
                .dimensions(width / 2 - 30, height / 2 + 20, 60, 20)
                .tooltip(Tooltip.of(Text.translatable("gui.slimefunadapter.stop_sync_button.tooltip")))
                .build());
        stopSyncButton.visible = false;
    }

    private void onSyncPressed(boolean force) {
        if (MenuSynchronizer.isRunning()) {
            log.warn("Sync already in progress, ignoring sync request");
            return;
        }
        MenuSynchronizer.registerListener();
        MenuSynchronizer.startSync(!force);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        // Render overlay when syncing
        if (MenuSynchronizer.isRunning()) {
            // Darken background
            context.fill(0, 0, width, height, 0x99000000);

            // Draw sync message
            String syncMessage = Text.translatable("gui.slimefunadapter.syncing").getString();
            int messageWidth = textRenderer.getWidth(syncMessage);
            context.drawText(textRenderer,
                    syncMessage,
                    width / 2 - messageWidth / 2,
                    height / 2 - 10,
                    0xFFFFFF,
                    true);

            // Add button background
            context.fill(
                    stopSyncButton.getX() - 2,
                    stopSyncButton.getY() - 2,
                    stopSyncButton.getX() + stopSyncButton.getWidth() + 2,
                    stopSyncButton.getY() + stopSyncButton.getHeight() + 2,
                    0x99FFFFFF);  // Light background for contrast

            // Show and render stop button on top
            stopSyncButton.visible = true;
            stopSyncButton.render(context, mouseX, mouseY, delta);
        } else {
            stopSyncButton.visible = false;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Block clicks when syncing except for the stop button
        if (MenuSynchronizer.isRunning()) {
            return stopSyncButton.mouseClicked(mouseX, mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}