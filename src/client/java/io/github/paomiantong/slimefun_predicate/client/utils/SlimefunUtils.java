package io.github.paomiantong.slimefun_predicate.client.utils;

import io.github.paomiantong.slimefun_predicate.client.SlimefunItemStack;
import io.github.paomiantong.slimefun_predicate.client.SlimefunRecipe;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SlimefunUtils {
    public static String getSlimefunID(@NotNull ItemStack stack) {
        @Nullable var nbtComp = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (nbtComp == null) {
            return Registries.ITEM.getId(stack.getItem()).toString();
        }
        var values = nbtComp.copyNbt().getCompound("PublicBukkitValues");
        return values.getString("slimefun:slimefun_item");
    }

    public static boolean isSlimefunItem(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        @Nullable var nbtComp = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (nbtComp == null) {
            return false;
        }
        var values = nbtComp.copyNbt().getCompound("PublicBukkitValues");
        String sf_id = values.getString("slimefun:slimefun_item");
        if (sf_id == null || sf_id.isEmpty()) {
            return values.contains("slimefun:slimefun_guide_mode");
        }
        return true;
    }

    public static boolean isUI(@NotNull ItemStack stack) {
        return getSlimefunID(stack).startsWith("_UI");
    }

    public static boolean isCategory(@NotNull ItemStack stack) {
        @Nullable var loreComponent = stack.get(DataComponentTypes.LORE);
        if (loreComponent == null) {
            return false;
        }
        try {
            var lore = loreComponent.lines().get(1);
            return "⇨ 单击打开".equals(lore.getString());
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
    }

    public static boolean hasNext(List<Slot> slots) {
        if (slots.size() < 54) {
            return false;
        }
        var next = slots.get(52).getStack();
        return "_UI_NEXT_ACTIVE".equals(getSlimefunID(next));
    }

    public static boolean isMenu(@NotNull ItemStack stack) {
        @Nullable var nbtComp = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (nbtComp == null) {
            return false;
        }
        var values = nbtComp.copyNbt().getCompound("PublicBukkitValues");
        return values.contains("slimefun:slimefun_guide_mode");
    }

    public static SlimefunRecipe extractRecipe(List<Slot> slots) {
        SlimefunItemStack type = new SlimefunItemStack(slots.get(10).getStack().copy());
        ItemStack output = slots.get(16).getStack().copy();
        SlimefunItemStack[] inputs = new SlimefunItemStack[9];
        for (int i = 0; i < 9; i++) {
            int r = (i / 3) * 9;
            int c = i % 3 + 3;
            inputs[i] = new SlimefunItemStack(slots.get(r + c).getStack().copy());
        }
        return new SlimefunRecipe(new SlimefunItemStack(output), inputs, type);
    }
}
