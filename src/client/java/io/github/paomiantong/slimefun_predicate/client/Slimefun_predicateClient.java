package io.github.paomiantong.slimefun_predicate.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.util.Identifier;

import java.io.FileNotFoundException;

public class Slimefun_predicateClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        try {
            Config.load();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        ModelPredicateProviderRegistry.register(new Identifier("sf_predict", "sf_item"), new SFPredicateProvider());
    }
}
