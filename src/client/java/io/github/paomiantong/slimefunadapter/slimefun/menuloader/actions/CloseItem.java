package io.github.paomiantong.slimefunadapter.slimefun.menuloader.actions;

import io.github.paomiantong.slimefunadapter.slimefun.menuloader.MenuPath;
import io.github.paomiantong.slimefunadapter.slimefun.menuloader.MenuSynchronizer;
import io.github.paomiantong.slimefunadapter.utils.InventoryUtils;

public class CloseItem extends Action {
    public CloseItem(int slotID) {
        super(slotID);
    }

    @Override
    public void execute(ExecutionContext context) {
        if (!MenuSynchronizer.isRunning()) return;
        InventoryUtils.clickSlot(context.handler(), 0);
        MenuPath.pop();
    }
}
