package io.github.paomiantong.slimefun_predicate;

import io.github.paomiantong.slimefun_predicate.config.Config;
import io.github.paomiantong.slimefun_predicate.menuloader.SyncSlimefunMenu;
import io.github.paomiantong.slimefun_predicate.slimefun.SlimefunManager;
import io.github.paomiantong.slimefun_predicate.utils.ShowComponents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class SlimefunPredicateClient implements ClientModInitializer {
    public static final KeyBinding tooltipKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.slimefun_predicate.tooltip",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_ALT,
            KeyBinding.INVENTORY_CATEGORY)
    );

    @Override
    public void onInitializeClient() {
        Config.loadConfig();
        Config.loadModel();
        SlimefunManager.load();

        SyncSlimefunMenu.registerCommands();
        ShowComponents.registerCommands();
        BackPackHelper.register();

        ModelPredicateProviderRegistry.register(Identifier.of("sf_predict", "sf_item"), new SFPredicateProvider());
    }
}
