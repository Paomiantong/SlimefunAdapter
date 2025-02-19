package io.github.paomiantong.slimefunadapter.slimefun.menuloader.actions;

import io.github.paomiantong.slimefunadapter.slimefun.menuloader.MenuSynchronizer;
import io.github.paomiantong.slimefunadapter.utils.InventoryUtils;

public class NextPage extends Action {
    public NextPage(int slotId) {
        super(slotId);
    }

    @Override
    public void execute(ExecutionContext context) {
        if (!MenuSynchronizer.isRunning()) return;
        InventoryUtils.clickSlot(context.handler(), 52);
        context.pushAction(new ScanCategory(-1));
    }
}
