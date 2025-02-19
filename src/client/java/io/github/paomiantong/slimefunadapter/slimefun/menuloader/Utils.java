package io.github.paomiantong.slimefunadapter.slimefun.menuloader;

import io.github.paomiantong.slimefunadapter.config.Config;
import io.github.paomiantong.slimefunadapter.utils.SlimefunUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Utils {
    public static boolean isExcluded(ItemStack stack) {
        String name = stack.getName().getString();
        return Config.EXCLUDE.contains(name) || name.startsWith("已锁定");
    }

    public static List<Slot> getSlots(ScreenHandler handler) {
        long limited = handler.slots.size() - 36;
        var slots = new ArrayList<>(handler.slots.stream()
                .limit(limited)
                .peek(slot -> {
                    // 当前分类有锁定物品时，标记当前分类为未解锁
                    if (SlimefunUtils.isLocked(slot.getStack())) {
                        MenuPath.current().setUnlocked(false);
                    }
                })
                .filter(slot -> (!SlimefunUtils.isUI(slot.getStack()) && !slot.getStack().isEmpty()))
                .toList());
        Collections.reverse(slots);
        return slots;
    }
}
