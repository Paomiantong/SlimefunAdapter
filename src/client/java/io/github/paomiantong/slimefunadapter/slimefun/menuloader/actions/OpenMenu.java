package io.github.paomiantong.slimefunadapter.slimefun.menuloader.actions;

import io.github.paomiantong.slimefunadapter.slimefun.SlimefunManager;
import io.github.paomiantong.slimefunadapter.slimefun.menuloader.MenuSynchronizer;
import io.github.paomiantong.slimefunadapter.utils.SlimefunUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static io.github.paomiantong.slimefunadapter.slimefun.menuloader.MenuSynchronizer.stopSync;
import static io.github.paomiantong.slimefunadapter.slimefun.menuloader.Utils.getSlots;
import static io.github.paomiantong.slimefunadapter.slimefun.menuloader.Utils.isExcluded;

public class OpenMenu extends Action {
    public OpenMenu(int slotId) {
        super(slotId);
    }

    @Override
    public void execute(ExecutionContext context) {
        if (!MenuSynchronizer.isRunning()) return;
        var client = context.client();
        if (!SlimefunUtils.isRoot(context.handler().slots)) {
            if (client.player != null) {
                client.player.sendMessage(
                        Text.literal("请打开Slimefun指南主页面!")
                                .formatted(Formatting.DARK_RED, Formatting.BOLD),
                        false);
            }
            stopSync();
            return;
        }

        context.pushAction(new CloseMenu(-1));
        for (Slot slot : getSlots(context.handler())) {
            ItemStack stack = slot.getStack();
            if (isExcluded(stack) ||
                    (context.incrementalUpdate() && SlimefunManager.isCompletelyUnlocked(stack.getName().getString()))) {
                continue;
            }
            context.pushAction(new OpenCategory(slot.id));
        }
    }

    @Override
    public boolean await(ScreenHandler handler) {
        return false;
    }
}
