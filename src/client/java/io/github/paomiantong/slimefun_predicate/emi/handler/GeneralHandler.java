package io.github.paomiantong.slimefun_predicate.emi.handler;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import io.github.paomiantong.slimefun_predicate.emi.recipe.BaseRecipe;
import net.minecraft.screen.Generic3x3ContainerScreenHandler;
import net.minecraft.screen.slot.Slot;

import java.util.ArrayList;
import java.util.List;

public class GeneralHandler implements StandardRecipeHandler<Generic3x3ContainerScreenHandler> {
    @Override
    public List<Slot> getInputSources(Generic3x3ContainerScreenHandler handler) {
        final List<Slot> slots = getCraftingSlots(handler);
        for (int i = 9; i < 45; i++) {
            slots.add(handler.getSlot(i));
        }
        return slots;
    }

    @Override
    public List<Slot> getCraftingSlots(Generic3x3ContainerScreenHandler handler) {
        final List<Slot> slots = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            slots.add(handler.getSlot(i));
        }
        return slots;
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return recipe instanceof BaseRecipe gridRecipe && gridRecipe.supportRecipe();
    }
}
