package io.github.paomiantong.slimefunadapter.slimefun.menuloader.actions;

import io.github.paomiantong.slimefunadapter.config.Config;
import io.github.paomiantong.slimefunadapter.slimefun.SlimefunItemStack;
import io.github.paomiantong.slimefunadapter.slimefun.SlimefunManager;
import io.github.paomiantong.slimefunadapter.slimefun.SlimefunRecipe;
import io.github.paomiantong.slimefunadapter.slimefun.menuloader.MenuSynchronizer;
import io.github.paomiantong.slimefunadapter.utils.SlimefunUtils;
import net.minecraft.screen.ScreenHandler;

public class ScanItem extends Action {
    public ScanItem(int slotID) {
        super(slotID);
    }

    @Override
    public void execute(ExecutionContext context) {
        if (!MenuSynchronizer.isRunning()) return;
        context.pushAction(new CloseItem(-1));
        SlimefunRecipe recipe = SlimefunUtils.extractRecipe(context.handler().slots);
        final var items = SlimefunManager.getSlimefunItems();
        int i = 0;
        for (SlimefunItemStack input : recipe.getInputs()) {
            int r = (i / 3) * 9;
            int c = i % 3 + 3;
            i++;
            if (input.getStack().isEmpty())
                continue;
            // 扫描未解锁的特殊物品
            if (Config.SPECIAL.contains(input.getId()) && !items.containsKey(input.getId())) {
                SlimefunManager.addSlimefunItem(input);
                context.pushAction(new OpenItem(r + c));
            }
            // 跟踪材料未完全解锁的配方
            if (input.isLocked()) {
                String unlockPath = input.getId().split("!")[1];
                SlimefunManager.addPartialUnlockedRecipe(unlockPath, recipe);
            }
        }
        SlimefunManager.addSlimefunRecipe(recipe);
        MenuSynchronizer.getAddons().forEach(addon -> addon.entry(context.actionStack(), context.handler(), slotId));
    }

    @Override
    public boolean await(ScreenHandler handler) {
        return false;
    }
}
