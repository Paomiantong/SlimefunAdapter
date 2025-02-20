package io.github.paomiantong.slimefunadapter.slimefun.screen;

import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

import java.util.HashMap;
import java.util.Map;

@Slf4j(topic = "SlimefunAdapter")
public class SlimefunScreenHandlerType {
    public static final Map<String, ScreenHandlerType<?>> REGISTRY = new HashMap<>();

    public static ScreenHandlerType<GenericContainerScreenHandler> SLIMEFUN_GUIDE_BOOK_6 = register("slimefun_guide_book_6", "Slimefun 指南", 6, GuideBookScreen::new);
    public static ScreenHandlerType<GenericContainerScreenHandler> SLIMEFUN_GUIDE_BOOK_5 = register("slimefun_guide_book_5", "Slimefun 指南", 5, GuideBookScreen::new);
    public static ScreenHandlerType<GenericContainerScreenHandler> SLIMEFUN_GUIDE_BOOK_4 = register("slimefun_guide_book_4", "Slimefun 指南", 4, GuideBookScreen::new);
    public static ScreenHandlerType<GenericContainerScreenHandler> SLIMEFUN_GUIDE_BOOK_3 = register("slimefun_guide_book_3", "Slimefun 指南", 3, GuideBookScreen::new);

    public static ScreenHandlerType<GenericContainerScreenHandler> AUTO_ANCIENT_ALTAR = register("auto_ancient_altar", "全自动远古祭坛", 6, GenericContainerScreen::new);

    public static ScreenHandlerType<GenericContainerScreenHandler> DIVINE_ALTAR = register("divine_altar", "神圣祭坛", 3, GenericContainerScreen::new);

    public static ScreenHandlerType<GenericContainerScreenHandler> ORNATE_CAULDRON = register("ornate_cauldron", "宇宙炼药锅", 3, GenericContainerScreen::new);

    public static ScreenHandlerType<GenericContainerScreenHandler> ALTAR_OF_INFUSION = register("altar_of_infusion", "注入祭坛", 3, GenericContainerScreen::new);

    public static <T extends ScreenHandler, U extends Screen & ScreenHandlerProvider<T>> ScreenHandlerType<T> register(
            String id,
            String title,
            int sourceRows,
            ScreenHandlerType.Factory<T> factory,
            HandledScreens.Provider<T, U> provider
    ) {
        var ret = Registry.register(Registries.SCREEN_HANDLER, id, new ScreenHandlerType<>(factory, FeatureFlags.VANILLA_FEATURES));
        REGISTRY.put(title + sourceRows, ret);
        HandledScreens.register(ret, provider);
        return ret;
    }

    private static ScreenHandlerType<GenericContainerScreenHandler> register(
            String id,
            String title,
            int rows,
            HandledScreens.Provider<GenericContainerScreenHandler, GenericContainerScreen> provider
    ) {
        return register(id, title, rows, buildFactory(rows), provider);
    }

    private static ScreenHandlerType.Factory<GenericContainerScreenHandler> buildFactory(int rows) {
        return (syncId, playerInventory) -> new GenericScreenHandler(null, syncId, playerInventory, rows);
    }

    public static int getRows(ScreenHandlerType<?> type) {
        if (type == ScreenHandlerType.GENERIC_9X1) {
            return 1;
        } else if (type == ScreenHandlerType.GENERIC_9X2) {
            return 2;
        } else if (type == ScreenHandlerType.GENERIC_9X3) {
            return 3;
        } else if (type == ScreenHandlerType.GENERIC_9X4) {
            return 4;
        } else if (type == ScreenHandlerType.GENERIC_9X5) {
            return 5;
        } else if (type == ScreenHandlerType.GENERIC_9X6) {
            return 6;
        } else {
            return 0;
        }
    }

    public static void register() {
        log.info("Registering Slimefun screen handler types...");
    }
}