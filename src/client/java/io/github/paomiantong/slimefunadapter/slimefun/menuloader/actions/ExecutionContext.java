package io.github.paomiantong.slimefunadapter.slimefun.menuloader.actions;

import net.minecraft.client.MinecraftClient;
import net.minecraft.screen.ScreenHandler;

import java.util.Stack;

public record ExecutionContext(
        MinecraftClient client,
        ScreenHandler handler,
        Stack<Action> actionStack,
        boolean incrementalUpdate
) {
    public void dispatch(Action action) {
        action.executeOnce(this);
    }

    public void pushAction(Action action) {
        actionStack.push(action);
    }
}