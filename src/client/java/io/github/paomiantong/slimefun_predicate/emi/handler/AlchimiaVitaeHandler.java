package io.github.paomiantong.slimefun_predicate.emi.handler;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import io.github.paomiantong.slimefun_predicate.emi.recipe.BaseRecipe;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;

import java.util.List;

import static io.github.paomiantong.slimefun_predicate.utils.InventoryUtils.get9x9Slots;

public class AlchimiaVitaeHandler implements StandardRecipeHandler<GenericContainerScreenHandler> {
    private final String type;

    public AlchimiaVitaeHandler(String type) {
        this.type = type;
    }

    @Override
    public List<Slot> getInputSources(GenericContainerScreenHandler handler) {
        final List<Slot> slots = getCraftingSlots(handler);
        slots.addAll(get9x9Slots(handler, 6));
        for (int i = 27; i < 63; i++) {
            slots.add(handler.getSlot(i));
        }
        return slots;
    }

    @Override
    public List<Slot> getCraftingSlots(GenericContainerScreenHandler handler) {
        return get9x9Slots(handler, 0);
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return recipe instanceof BaseRecipe gridRecipe && type.equals(gridRecipe.getType());
    }
}
