package io.github.paomiantong.slimefun_predicate.slimefun.screen;

import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

import java.util.HashMap;
import java.util.Map;

public class SlimefunScreenHandlerType {
    public static final Map<String, ScreenHandlerType<?>> REGISTRY = new HashMap<>();
    public static final ScreenHandlerType<GenericContainerScreenHandler> AUTO_ANCIENT_ALTAR =
            register("auto_ancient_altar", "全自动远古祭坛", GenericScreenHandler::createAutoAncientAltar);

    private static <T extends ScreenHandler> ScreenHandlerType<T> register(String id, String title, ScreenHandlerType.Factory<T> factory) {
        var ret = Registry.register(Registries.SCREEN_HANDLER, id, new ScreenHandlerType<>(factory, FeatureFlags.VANILLA_FEATURES));
        REGISTRY.put(title, ret);
        return ret;
    }

    public static void register() {
        HandledScreens.register(AUTO_ANCIENT_ALTAR, GenericContainerScreen::new);
    }
}
