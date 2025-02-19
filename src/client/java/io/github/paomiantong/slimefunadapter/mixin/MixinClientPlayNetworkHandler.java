package io.github.paomiantong.slimefunadapter.mixin;


import io.github.paomiantong.slimefunadapter.BackPackHelper;
import io.github.paomiantong.slimefunadapter.slimefun.screen.SlimefunScreenHandlerType;
import io.github.paomiantong.slimefunadapter.utils.CompatUtils;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.listener.TickablePacketListener;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
@Slf4j(topic = "SlimefunAdapter")
public abstract class MixinClientPlayNetworkHandler extends ClientCommonNetworkHandler implements ClientPlayPacketListener, TickablePacketListener {
    protected MixinClientPlayNetworkHandler(MinecraftClient client, ClientConnection connection, ClientConnectionState connectionState) {
        super(client, connection, connectionState);
    }

    @Inject(method = "onOpenScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreens;open(Lnet/minecraft/screen/ScreenHandlerType;Lnet/minecraft/client/MinecraftClient;ILnet/minecraft/text/Text;)V"), cancellable = true)
    private void onOpenScreen(OpenScreenS2CPacket packet, CallbackInfo ci) {
        if (!CompatUtils.isEmiLoaded()) return;
        String name = packet.getName().getString();
        if (SlimefunScreenHandlerType.REGISTRY.containsKey(name)) {
            HandledScreens.open(SlimefunScreenHandlerType.REGISTRY.get(name), this.client, packet.getSyncId(), packet.getName());
            System.out.println("open");
            ci.cancel();
        }
    }

    @Inject(at = @At("RETURN"), method = "onGameJoin")
    private void onGameJoin(GameJoinS2CPacket packet, CallbackInfo info) {
        log.info("Joining server, reloading data...");
        BackPackHelper.reload(this.client);
    }
}
