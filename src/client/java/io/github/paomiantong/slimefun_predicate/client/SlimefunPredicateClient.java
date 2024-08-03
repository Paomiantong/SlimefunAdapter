package io.github.paomiantong.slimefun_predicate.client;

import io.github.paomiantong.slimefun_predicate.client.menuloader.LoadSlimefunMenu;
import io.github.paomiantong.slimefun_predicate.client.utils.ShowComponents;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.util.Identifier;

import java.io.FileNotFoundException;

public class SlimefunPredicateClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        try {
            Config.load();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        ResourceLoader.load();
        LoadSlimefunMenu.registerCommands();
        ShowComponents.registerCommands();
        ModelPredicateProviderRegistry.register(Identifier.of("sf_predict", "sf_item"), new SFPredicateProvider());
    }
}
