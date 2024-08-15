package io.github.paomiantong.slimefun_predicate.emi;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiStack;
import io.github.paomiantong.slimefun_predicate.SlimefunPredicateClient;
import io.github.paomiantong.slimefun_predicate.emi.handler.AutoAncientAltarHandler;
import io.github.paomiantong.slimefun_predicate.emi.handler.DivineAltarHandler;
import io.github.paomiantong.slimefun_predicate.emi.recipe.BaseRecipe;
import io.github.paomiantong.slimefun_predicate.emi.handler.GeneralHandler;
import io.github.paomiantong.slimefun_predicate.slimefun.SlimefunItemStack;
import io.github.paomiantong.slimefun_predicate.slimefun.SlimefunManager;
import io.github.paomiantong.slimefun_predicate.slimefun.SlimefunRecipe;
import io.github.paomiantong.slimefun_predicate.slimefun.SlimefunRecipeCategory;
import io.github.paomiantong.slimefun_predicate.slimefun.screen.SlimefunScreenHandlerType;
import io.github.paomiantong.slimefun_predicate.utils.SlimefunUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public class EmiIntegrate implements EmiPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(SlimefunPredicateClient.class);
    private static final Comparison SLIMEFUN_ID = Comparison.compareData(stack -> SlimefunUtils.getSlimefunID(stack.getItemStack()));

    @Override
    public void register(EmiRegistry registry) {
        if (!SlimefunManager.isInitialized()) {
            PlayerEntity player = MinecraftClient.getInstance().player;
            if (player != null) {
                player.sendMessage(Text
                        .translatable("message.slimefun_predicate.slimefun_data_not_loaded")
                        .formatted(Formatting.DARK_RED, Formatting.BOLD)
                );
            } else {
                LOGGER.error("Slimefun is not loaded");
            }
            return;
        }
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

        registry.addRecipeHandler(ScreenHandlerType.GENERIC_3X3, new GeneralHandler());
        registry.addRecipeHandler(SlimefunScreenHandlerType.AUTO_ANCIENT_ALTAR, new AutoAncientAltarHandler());
        registry.addRecipeHandler(SlimefunScreenHandlerType.DIVINE_ALTAR, new DivineAltarHandler());
    }
}
