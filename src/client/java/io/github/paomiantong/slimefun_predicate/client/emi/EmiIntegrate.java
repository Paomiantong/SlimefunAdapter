package io.github.paomiantong.slimefun_predicate.client.emi;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiStack;
import io.github.paomiantong.slimefun_predicate.client.*;
import io.github.paomiantong.slimefun_predicate.client.emi.recipe.BaseRecipe;
import io.github.paomiantong.slimefun_predicate.client.emi.recipe.Handler;
import io.github.paomiantong.slimefun_predicate.client.utils.SlimefunUtils;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public class EmiIntegrate implements EmiPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(SlimefunPredicateClient.class);
    private static final Comparison SLIMEFUN_ID = Comparison.compareData(stack -> SlimefunUtils.getSlimefunID(stack.getItemStack()));

    @Override
    public void register(EmiRegistry registry) {
        for (SlimefunItemStack slimefunItemStack : SlimefunManager.getSlimefunItems().values()) {
            registry.setDefaultComparison(EmiStack.of(slimefunItemStack.getStack()), SLIMEFUN_ID);
        }
        for (SlimefunRecipeCategory slimefunRecipeCategory : SlimefunManager.getSlimefunRecipeCategories().values()) {
            final SlimefunItemStack type = slimefunRecipeCategory.type();
            final String workstationId = String.valueOf(type.hashCode());
            final Identifier categoryIdentifier = Identifier.of("slimefun", workstationId.toLowerCase(Locale.ROOT));
            final EmiStack workStation = EmiStack.of(type.getStack());
            final SlimefunEmiCategory slimefunEmiCategory = new SlimefunEmiCategory(categoryIdentifier, workStation);
            registry.addCategory(slimefunEmiCategory);
            registry.addWorkstation(slimefunEmiCategory, workStation);
            for (SlimefunRecipe slimefunRecipe : slimefunRecipeCategory.recipes()) {
                registry.addRecipe(new BaseRecipe(slimefunRecipe, slimefunEmiCategory));
            }
        }

        for (SlimefunItemStack slimefunItemStack : SlimefunManager.getSlimefunItems().values()) {
            registry.addEmiStack(EmiStack.of(slimefunItemStack.getStack()));
        }

        registry.addRecipeHandler(ScreenHandlerType.GENERIC_3X3, new Handler());
    }
}
