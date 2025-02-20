package io.github.paomiantong.slimefunadapter;

import io.github.paomiantong.slimefunadapter.config.Config;
import io.github.paomiantong.slimefunadapter.command.SyncSlimefunMenu;
import io.github.paomiantong.slimefunadapter.slimefun.BackPackHelper;
import io.github.paomiantong.slimefunadapter.slimefun.SlimefunManager;
import io.github.paomiantong.slimefunadapter.slimefun.SlimefunPredicateProvider;
import io.github.paomiantong.slimefunadapter.slimefun.screen.GuideBookScreen;
import io.github.paomiantong.slimefunadapter.slimefun.screen.SlimefunScreenHandlerType;
import io.github.paomiantong.slimefunadapter.utils.ShowComponents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class SlimefunAdapterClient implements ClientModInitializer {
    public static final KeyBinding tooltipKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.slimefunadapter.tooltip",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_ALT,
            KeyBinding.INVENTORY_CATEGORY
    ));

    @Override
    public void onInitializeClient() {
        Config.loadConfig();
        Config.loadModel();
        SlimefunManager.load();

        SyncSlimefunMenu.registerCommands();
        ShowComponents.registerCommands();

        BackPackHelper.register();
        ModelPredicateProviderRegistry.register(Identifier.of("sf_predict", "sf_item"), new SlimefunPredicateProvider());
        SlimefunScreenHandlerType.register();
    }
}
