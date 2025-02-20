package io.github.paomiantong.slimefunadapter.mixin;


import io.github.paomiantong.slimefunadapter.render.InventoryRender;
import io.github.paomiantong.slimefunadapter.slimefun.BackPackHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.github.paomiantong.slimefunadapter.SlimefunAdapterClient.tooltipKeyBinding;

@Mixin(HandledScreen.class)
public abstract class MixinHandledScreen {
    @Shadow
    @Nullable
    protected Slot focusedSlot;

    @Inject(method = "drawMouseoverTooltip", at = @At(value = "INVOKE", shift = At.Shift.AFTER,
            target = "Lnet/minecraft/client/gui/DrawContext;drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;Ljava/util/Optional;II)V"))
    private void onRenderTooltip(DrawContext drawContext, int x, int y, CallbackInfo ci) {
        if (this.focusedSlot != null && this.focusedSlot.hasStack()) {
            int code = InputUtil.fromTranslationKey(tooltipKeyBinding.getBoundKeyTranslationKey()).getCode();
            boolean isKeybindingPressed = InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), code);
            if (isKeybindingPressed) {
                Inventory inv = BackPackHelper.getInventory(this.focusedSlot.getStack());
                if (inv != null) {
                    InventoryRender.renderBackPackPreview(inv, x, y, drawContext);
                }
            }
        }
    }
}
