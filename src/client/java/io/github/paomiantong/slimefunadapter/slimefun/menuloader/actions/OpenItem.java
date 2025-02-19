package io.github.paomiantong.slimefunadapter.slimefun.menuloader.actions;


import io.github.paomiantong.slimefunadapter.slimefun.SlimefunItemStack;
import io.github.paomiantong.slimefunadapter.slimefun.SlimefunManager;
import io.github.paomiantong.slimefunadapter.slimefun.menuloader.MenuPath;
import io.github.paomiantong.slimefunadapter.slimefun.menuloader.MenuSynchronizer;
import io.github.paomiantong.slimefunadapter.utils.InventoryUtils;
import io.github.paomiantong.slimefunadapter.utils.SlimefunUtils;

public class OpenItem extends Action {
    public OpenItem(int slotId) {
        super(slotId);
    }

    @Override
    public void execute(ExecutionContext context) {
        if (!MenuSynchronizer.isRunning()) return;
        var target = context.handler().getSlot(slotId).getStack();
        if (SlimefunUtils.isSlimefunItem(target)) {
            SlimefunManager.addSlimefunItem(new SlimefunItemStack(target.copy()));
        } else {
            // 使用:+路径+原版ID+槽位ID作为原版物品的ID
            String id = ":" + MenuPath.current().getTitle() + SlimefunUtils.getSlimefunID(target) + slotId;
            SlimefunManager.addSlimefunItem(new SlimefunItemStack(id, target));
        }
        MenuPath.push(
                context.handler().getSlot(slotId).getStack().getName().getString(),
                MenuPath.Type.Item);
        InventoryUtils.clickSlot(context.handler(), slotId);
        context.pushAction(new ScanItem(-1));
    }
}
