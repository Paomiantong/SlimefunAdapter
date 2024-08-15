package io.github.paomiantong.slimefun_predicate.mixin;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class DebugMixin {
    @Final
    @Shadow
    protected ScreenHandler handler;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V"))
    private void renderDebugMixin(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        TextRenderer textRenderer = ((ScreenAccessor) this).getTextRenderer();
        context.getMatrices().push();
        context.getMatrices().translate(0.0F, 0.0F, 300.0F);
        handler.slots.forEach(slot -> {
            context.drawText(textRenderer, slot.id + "", slot.x, slot.y, 0xFFFFFF, false);
        });
        context.getMatrices().pop();
        // Debug code here
    }
}
