package io.github.paomiantong.slimefun_predicate.mixin;


import io.github.paomiantong.slimefun_predicate.slimefun.screen.SlimefunScreenHandlerType;
import io.github.paomiantong.slimefun_predicate.utils.CompatUtils;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.screen.ScreenHandlerType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class CustomScreenHandler {
    @Inject(method = "onOpenScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreens;open(Lnet/minecraft/screen/ScreenHandlerType;Lnet/minecraft/client/MinecraftClient;ILnet/minecraft/text/Text;)V"), cancellable = true)
    private void onOpenScreen(OpenScreenS2CPacket packet, CallbackInfo ci) {
        if (!CompatUtils.isEmiLoaded()) return;
        String name = packet.getName().getString();
        if (SlimefunScreenHandlerType.REGISTRY.containsKey(name)) {
            var that = (ClientCommonNetworkHandlerAccessor) this;
            HandledScreens.open(SlimefunScreenHandlerType.REGISTRY.get(name), that.getClient(), packet.getSyncId(), packet.getName());
            System.out.println("open");
            ci.cancel();
        }
    }
}
