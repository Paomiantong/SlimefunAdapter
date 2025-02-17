package io.github.paomiantong.slimefunadapter.menuloader.addons;

import io.github.paomiantong.slimefunadapter.menuloader.Action;
import net.minecraft.screen.ScreenHandler;

import java.util.Stack;

public interface Addons {
    void entry(Stack<Action> actionStack, ScreenHandler handler, int slotId);
}
