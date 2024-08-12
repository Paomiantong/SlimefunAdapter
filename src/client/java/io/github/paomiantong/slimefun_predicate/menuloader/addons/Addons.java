package io.github.paomiantong.slimefun_predicate.menuloader.addons;

import io.github.paomiantong.slimefun_predicate.menuloader.Action;
import net.minecraft.screen.ScreenHandler;

import java.util.Stack;

public interface Addons {
    void entry(Stack<Action> actionStack, ScreenHandler handler, int slotId);
}
