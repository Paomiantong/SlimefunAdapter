package io.github.paomiantong.slimefunadapter.mixin;

import io.github.paomiantong.slimefunadapter.slimefun.BackPackHelper;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
@Slf4j(topic = "SlimefunAdapter")
public class MixinMinecraftClient {

    @Inject(at = @At("HEAD"), method = "onDisconnected")
    private void disconnect(CallbackInfo info) {
        log.info("Disconnecting from server, saving data...");
        BackPackHelper.save();
    }
}
