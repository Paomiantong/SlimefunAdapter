package io.github.paomiantong.slimefunadapter.slimefun.menuloader.actions;

import io.github.paomiantong.slimefunadapter.slimefun.SlimefunItemStack;
import io.github.paomiantong.slimefunadapter.slimefun.SlimefunManager;
import io.github.paomiantong.slimefunadapter.slimefun.menuloader.MenuPath;
import io.github.paomiantong.slimefunadapter.slimefun.menuloader.MenuSynchronizer;
import io.github.paomiantong.slimefunadapter.utils.SlimefunUtils;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Pair;

import java.util.List;

import static io.github.paomiantong.slimefunadapter.slimefun.menuloader.Utils.getSlots;
import static io.github.paomiantong.slimefunadapter.slimefun.menuloader.Utils.isExcluded;

@Slf4j(topic = "SlimefunAdapter")
public class ScanCategory extends Action {
    public ScanCategory(int slotID) {
        super(slotID);
    }

    @Override
    public void execute(ExecutionContext context) {
        if (!MenuSynchronizer.isRunning()) return;
        var handler = context.handler();
        // 判断是否还有下一页
        Action postAction = SlimefunUtils.hasNext(handler.slots) ? new NextPage(-1) : new CloseCategory(-1);
        List<Action> actions = getSlots(handler).stream()
                .filter(slot -> !isExcluded(slot.getStack()))
                .map(slot -> {
                    ItemStack stack = slot.getStack();
                    Action action = SlimefunUtils.isCategory(stack) ? new OpenCategory(slot.id) : new OpenItem(slot.id);
                    return new Pair<>(action, stack);
                }).filter(pair -> {
                    if (!context.incrementalUpdate())
                        return true;
                    ItemStack stack = pair.getRight();
                    Action action = pair.getLeft();
                    if (action instanceof OpenCategory) {
                        // 增量模式下完全解锁的子分类不再扫描
                        return !SlimefunManager.isCompletelyUnlocked(stack.getName().getString());
                    }
                    String id = SlimefunUtils.getSlimefunID(stack);
                    // 原版物品使用复合ID
                    if (id.startsWith("minecraft")) {
                        id = ":" + MenuPath.current().getTitle() + id + action.getSlotId();
                    }
                    // 增量模式下已解锁的物品不再扫描
                    if (!SlimefunManager.hasSlimefunItem(id)) {
                        String unlockPath = MenuPath.current().getTitle() + "/" + stack.getName().getString();
                        SlimefunManager.unlockItem(unlockPath, new SlimefunItemStack(stack.copy()));
                        log.info("解锁新物品：{} {}", unlockPath, SlimefunUtils.getSlimefunID(stack));
                        return true;
                    }
                    return false;
                }).map(Pair::getLeft).toList();

        context.pushAction(postAction);

        if (actions.isEmpty()) {
            return;
        }

        for (var action : actions) {
            context.pushAction(action);
        }
    }

    @Override
    public boolean await(ScreenHandler handler) {
        return false;
    }
}
