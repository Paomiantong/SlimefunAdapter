package io.github.paomiantong.slimefunadapter.slimefun.menuloader.addons;

import io.github.paomiantong.slimefunadapter.slimefun.menuloader.Action;
import net.minecraft.screen.ScreenHandler;

import java.util.Stack;

public interface Addons {
    void entry(Stack<Action> actionStack, ScreenHandler handler, int slotId);
}
