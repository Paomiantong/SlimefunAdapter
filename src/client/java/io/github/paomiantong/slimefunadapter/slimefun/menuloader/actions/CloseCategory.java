package io.github.paomiantong.slimefunadapter.slimefun.menuloader.actions;

import io.github.paomiantong.slimefunadapter.slimefun.menuloader.MenuPath;
import io.github.paomiantong.slimefunadapter.slimefun.menuloader.MenuSynchronizer;
import io.github.paomiantong.slimefunadapter.utils.InventoryUtils;

public class CloseCategory extends Action {
    public CloseCategory(int slotId) {
        super(slotId);
    }

    @Override
    public void execute(ExecutionContext context) {
        if (!MenuSynchronizer.isRunning()) return;
        InventoryUtils.clickSlot(context.handler(), 1);
        MenuPath.pop();
    }
}
