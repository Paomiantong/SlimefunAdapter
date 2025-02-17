package io.github.paomiantong.slimefunadapter.emi.handler;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import io.github.paomiantong.slimefunadapter.emi.recipe.BaseRecipe;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;

import java.util.List;

import static io.github.paomiantong.slimefunadapter.utils.InventoryUtils.get9x9Slots;

public class AutoAncientAltarHandler implements StandardRecipeHandler<GenericContainerScreenHandler> {
    @Override
    public List<Slot> getInputSources(GenericContainerScreenHandler handler) {
        final List<Slot> slots = getCraftingSlots(handler);
        slots.add(handler.getSlot(33));
        slots.add(handler.getSlot(34));
        for (int i = 54; i < 90; i++) {
            slots.add(handler.getSlot(i));
        }
        return slots;
    }

    @Override
    public List<Slot> getCraftingSlots(GenericContainerScreenHandler handler) {
//        final List<Slot> slots = new ArrayList<>();
//        for (int i = 0; i < 9; i++) {
//            int r = (i / 3) * 9;
//            int c = i % 3 + 19;
//            slots.add(handler.getSlot(r + c));
//        }
        return get9x9Slots(handler, 19);
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return recipe instanceof BaseRecipe gridRecipe && "ANCIENT_ALTAR".equals(gridRecipe.getType());
    }
}
