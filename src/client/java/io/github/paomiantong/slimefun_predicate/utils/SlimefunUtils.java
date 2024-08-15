package io.github.paomiantong.slimefun_predicate.utils;

import io.github.paomiantong.slimefun_predicate.slimefun.SlimefunItemStack;
import io.github.paomiantong.slimefun_predicate.slimefun.SlimefunRecipe;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
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

    public static boolean isRoot(List<Slot> slots) {
        return "_UI_MENU".equals(getSlimefunID(slots.get(1).getStack()));
    }

    public static boolean isMenu(@NotNull ItemStack stack) {
        @Nullable var nbtComp = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (nbtComp == null) {
            return false;
        }
        var values = nbtComp.copyNbt().getCompound("PublicBukkitValues");
        return values.contains("slimefun:slimefun_guide_mode");
    }

    public static boolean isLocked(@NotNull ItemStack stack) {
        return getSlimefunID(stack).startsWith("_UI_NO_PERMISSION");
    }

    public static SlimefunRecipe extractRecipe(List<Slot> slots) {
        SlimefunItemStack type = new SlimefunItemStack(slots.get(10).getStack().copy());
        ItemStack output = slots.get(16).getStack().copy();
        SlimefunItemStack[] inputs = new SlimefunItemStack[9];
        for (int i = 0; i < 9; i++) {
            int r = (i / 3) * 9;
            int c = i % 3 + 3;
            ItemStack stack = slots.get(r + c).getStack();
            String unlockPath = isLockedIngredient(stack);
            if (unlockPath != null)
                inputs[i] = new SlimefunItemStack(stack.copy(), unlockPath);
            else
                inputs[i] = new SlimefunItemStack(stack.copy());
        }
        return new SlimefunRecipe(new SlimefunItemStack(output), inputs, type);
    }

    private static final Pattern unlockPathPattern = Pattern.compile("需要在 (.+) 中解锁");

    public static String isLockedIngredient(ItemStack stack) {
        if (!stack.getItem().equals(Registries.ITEM.get(Identifier.of("minecraft:barrier")))) {
            return null;
        }
        @Nullable final var lore = stack.get(DataComponentTypes.LORE);
        @Nullable final var name = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (lore == null || name == null) {
            return null;
        }
        boolean locked = "已锁定".equals(lore.lines().get(0).getString());
        Matcher matcher = unlockPathPattern.matcher(lore.lines().get(2).getString());
        if (!matcher.find()) {
            log.error("No match:" + lore.lines().get(2).getString());
            return null;
        }
        if (locked) {
            String category = matcher.group(1);
            return category + "/" + name.getString();
        }
        return null;
    }
}
