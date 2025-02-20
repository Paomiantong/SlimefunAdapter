package io.github.paomiantong.slimefunadapter.slimefun.menuloader.actions;

import io.github.paomiantong.slimefunadapter.slimefun.menuloader.MenuPath;
import io.github.paomiantong.slimefunadapter.slimefun.menuloader.MenuSynchronizer;
import io.github.paomiantong.slimefunadapter.utils.InventoryUtils;


public class OpenCategory extends Action {
    public OpenCategory(int slotID) {
        super(slotID);
    }

    @Override
    public void execute(ExecutionContext context) {
        if (!MenuSynchronizer.isRunning()) return;
        MenuPath.push(
                context.handler().getSlot(slotId).getStack().getName().getString(),
                MenuPath.Type.Category);
        context.pushAction(new ScanCategory(-1));
        InventoryUtils.clickSlot(context.handler(), slotId);
    }
}
