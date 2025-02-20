package io.github.paomiantong.slimefunadapter.slimefun.menuloader.actions;

import io.github.paomiantong.slimefunadapter.slimefun.menuloader.MenuSynchronizer;
import lombok.Getter;
import net.minecraft.screen.ScreenHandler;

@Getter
public abstract class Action {
    protected final int slotId;
    protected boolean dispatched = false;

    protected Action(int slotId) {
        this.slotId = slotId;
    }

    public boolean await(ScreenHandler handler) {
        return MenuSynchronizer.defaultAwaitPredicate.test(handler);
    }

    public abstract void execute(ExecutionContext context);

    public void executeOnce(ExecutionContext context) {
        if (dispatched) return;
        dispatched = true;
        execute(context);
    }
}