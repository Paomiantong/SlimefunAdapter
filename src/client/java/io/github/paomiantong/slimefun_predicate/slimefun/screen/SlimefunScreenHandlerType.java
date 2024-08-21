package io.github.paomiantong.slimefun_predicate.slimefun.screen;

import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class SlimefunScreenHandlerType {
    public static final Map<String, ScreenHandlerType<GenericContainerScreenHandler>> REGISTRY = new HashMap<>();

    @HandlerType(title = "全自动远古祭坛", rows = 6)
    public static ScreenHandlerType<GenericContainerScreenHandler> AUTO_ANCIENT_ALTAR;

    @HandlerType(title = "神圣祭坛", rows = 3)
    public static ScreenHandlerType<GenericContainerScreenHandler> DIVINE_ALTAR;

    @HandlerType(title = "宇宙炼药锅", rows = 3)
    public static ScreenHandlerType<GenericContainerScreenHandler> ORNATE_CAULDRON;

    @HandlerType(title = "注入祭坛", rows = 3)
    public static ScreenHandlerType<GenericContainerScreenHandler> ALTAR_OF_INFUSION;

    static {
        for (var field : SlimefunScreenHandlerType.class.getDeclaredFields()) {
            if (field.isAnnotationPresent(HandlerType.class)) {
                var annotation = field.getAnnotation(HandlerType.class);
                var title = annotation.title();
                var rows = annotation.rows();
                try {
                    var type = register(field.getName().toLowerCase(), title, buildFactory(rows, field));
                    field.set(null, type);
                } catch (IllegalAccessException e) {
                    log.error("Failed to register screen handler type: {}", title, e);
                }
            }
        }
    }

    public static ScreenHandlerType<GenericContainerScreenHandler> register(String id, String title, ScreenHandlerType.Factory<GenericContainerScreenHandler> factory) {
        var ret = Registry.register(Registries.SCREEN_HANDLER, id, new ScreenHandlerType<>(factory, FeatureFlags.VANILLA_FEATURES));
        REGISTRY.put(title, ret);
        return ret;
    }

    private static ScreenHandlerType.Factory<GenericContainerScreenHandler> buildFactory(int rows, Field typeField) {
        assert typeField.getType() == ScreenHandlerType.class;
        assert typeField.getGenericType() == GenericContainerScreenHandler.class;
        return (syncId, playerInventory) -> {
            try {
                return new GenericScreenHandler((ScreenHandlerType<?>) typeField.get(null), syncId, playerInventory, rows);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static void register() {
        REGISTRY.forEach((title, type) -> HandledScreens.register(type, GenericContainerScreen::new));
    }
}
