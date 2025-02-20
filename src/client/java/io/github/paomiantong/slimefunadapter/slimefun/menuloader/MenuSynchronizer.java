package io.github.paomiantong.slimefunadapter.slimefun.menuloader;

import io.github.paomiantong.slimefunadapter.config.Config;
import io.github.paomiantong.slimefunadapter.slimefun.SlimefunManager;
import io.github.paomiantong.slimefunadapter.slimefun.menuloader.actions.Action;
import io.github.paomiantong.slimefunadapter.slimefun.menuloader.actions.Debug;
import io.github.paomiantong.slimefunadapter.slimefun.menuloader.actions.ExecutionContext;
import io.github.paomiantong.slimefunadapter.slimefun.menuloader.actions.OpenMenu;
import io.github.paomiantong.slimefunadapter.slimefun.menuloader.addons.Addons;
import io.github.paomiantong.slimefunadapter.slimefun.menuloader.addons.WorkstationRecipeAddons;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Stack;
import java.util.function.Predicate;

@Slf4j(topic = "SlimefunAdapter")
public class MenuSynchronizer {
    @Getter
    private static boolean running = false;
    private static boolean registered = false;

    private static final Stack<Action> actionStack = new Stack<>();
    private static int lastSyncId = -1;
    private static int tickCounter = 0;
    private static boolean incrementalUpdate = false;
    @Getter
    private static final List<Addons> addons = List.of(new WorkstationRecipeAddons());

    public static final Predicate<ScreenHandler> defaultAwaitPredicate = handler -> handler.syncId == lastSyncId;

    public static void registerListener() {
        if (registered) return;
        ClientTickEvents.END_CLIENT_TICK.register(MenuSynchronizer::onClientTick);
        registered = true;
    }

    public static void startSync(boolean incrementalUpdate_) {
        if (!running) {
            MenuPath.init();
            actionStack.clear();
            actionStack.push(new OpenMenu(-1));
            lastSyncId = -1;
            running = true;
            incrementalUpdate = incrementalUpdate_;
            if (!incrementalUpdate) {
                SlimefunManager.clear();
            }
        }
    }

    public static void startDebug() {
        if (!running) {
            actionStack.clear();
            actionStack.push(new Debug(-1));
            lastSyncId = -1;
            running = true;
        }
    }

    public static void stopSync() {
        running = false;
    }

    private static ExecutionContext getExecutionContext(MinecraftClient client, ScreenHandler handler) {
        return new ExecutionContext(client, handler, actionStack, incrementalUpdate);
    }

    private static void onClientTick(MinecraftClient client) {
        if (!running) return;

        tickCounter++;
        if (tickCounter < Config.TICKS_PER_ACTION) {
            return;
        }
        tickCounter = 0;

        if (client.currentScreen instanceof HandledScreen<?> handledScreen) {
            ScreenHandler handler = handledScreen.getScreenHandler();
            Text title = handledScreen.getTitle();
            if (Config.MENU_TITLE.contains(title.getString())) {
                long limited = handler.slots.size() - 36;
                if (handler.slots.stream().limit(limited).allMatch(slot -> slot.getStack().isEmpty())) {
                    log.warn("菜单为空！");
                    return;
                }
//                log.info("CURRENT TICK SYNC ID: {}", handler.syncId);
                Action currentAction = actionStack.pop();

                // 执行当前操作
                log.info("当前操作：{}, SyncID {}", currentAction, handler.syncId);
                getExecutionContext(client, handler).dispatch(currentAction);

                // 执行更改UI的动作后需要等待UI更新
                if (lastSyncId != -1 && currentAction.await(handler)) {
                    log.info("等待UI更新...");
                    actionStack.push(currentAction);
                    return;
                }

                lastSyncId = handler.syncId;
            }
        }
    }


    /* Actions */
//    private static final ActionType OPEN_MENU = new ActionType() {
//        @Override
//        public boolean requireAwait() {
//            return false;
//        }
//
//        @Override
//        public void runAction(MinecraftClient client, ScreenHandler handler, int slotId) {
//            if (!running) return;
//            if (!SlimefunUtils.isRoot(handler.slots)) {
//                if (client.player != null) {
//                    client.player.sendMessage(
//                            Text.literal("请打开Slimefun指南主页面!")
//                                    .formatted(Formatting.DARK_RED, Formatting.BOLD),
//                            false);
//                }
//                stopSync();
//                return;
//            }
//
//            boolean first = true;
//            for (Slot slot : getSlots(handler)) {
//                ItemStack stack = slot.getStack();
//                if (isExcluded(stack) ||
//                        (incrementalUpdate && SlimefunManager.isCompletelyUnlocked(stack.getName().getString()))) {
//                    continue;
//                }
//                if (first) {
//                    first = false;
//                    actionStack.push(new Actionx(OPEN_CATEGORY, slot.id, stack, CLOSE_MENU));
//                } else {
//                    actionStack.push(new Actionx(OPEN_CATEGORY, slot.id, stack));
//                }
//            }
//        }
//
//        @Override
//        public String toString() {
//            return "OPEN_MENU";
//        }
//    };
//
//    private static final ActionType OPEN_CATEGORY = new ActionType() {
//        @Override
//        public boolean requireAwait() {
//            return true;
//        }
//
//        @Override
//        public void runAction(MinecraftClient client, ScreenHandler handler, int slotId) {
//            if (!running) return;
//            MenuPath.push(
//                    handler.getSlot(slotId).getStack().getName().getString(),
//                    MenuPath.Type.Category);
//            actionStack.push(new Actionx(SCAN_CATEGORY, -1, null));
//            InventoryUtils.clickSlot(handler, slotId);
//        }
//
//        @Override
//        public String toString() {
//            return "OPEN_CATEGORY";
//        }
//    };
//
//    private static final ActionType OPEN_ITEM = new ActionType() {
//        @Override
//        public boolean requireAwait() {
//            return true;
//        }
//
//        @Override
//        public void runAction(MinecraftClient client, ScreenHandler handler, int slotId) {
//            if (!running) return;
//            var target = handler.getSlot(slotId).getStack();
//            if (SlimefunUtils.isSlimefunItem(target)) {
//                SlimefunManager.addSlimefunItem(new SlimefunItemStack(target.copy()));
//            } else {
//                // 使用:+路径+原版ID+槽位ID作为原版物品的ID
//                String id = ":" + MenuPath.current().getTitle() + SlimefunUtils.getSlimefunID(target) + slotId;
//                SlimefunManager.addSlimefunItem(new SlimefunItemStack(id, target));
//            }
//            MenuPath.push(
//                    handler.getSlot(slotId).getStack().getName().getString(),
//                    MenuPath.Type.Item);
//            InventoryUtils.clickSlot(handler, slotId);
//            actionStack.push(new Actionx(SCAN_ITEM, -1, null));
//        }
//
//        @Override
//        public String toString() {
//            return "OPEN_ITEM";
//        }
//    };
//
//    private static final ActionType SCAN_CATEGORY = new ActionType() {
//        @Override
//        public boolean requireAwait() {
//            return false;
//        }
//
//        @Override
//        public void runAction(MinecraftClient client, ScreenHandler handler, int slotId) {
//            if (!running) return;
//            boolean first = true;
//            // 判断是否还有下一页
//            ActionType postAction = SlimefunUtils.hasNext(handler.slots) ? NEXT_PAGE : CLOSE_CATEGORY;
//            List<Actionx> actions = getSlots(handler).stream()
//                    .filter(slot -> !isExcluded(slot.getStack()))
//                    .map(slot -> {
//                        ItemStack stack = slot.getStack();
//                        ActionType actionType = SlimefunUtils.isCategory(stack) ? OPEN_CATEGORY : OPEN_ITEM;
//                        return new Actionx(actionType, slot.id, stack);
//                    }).filter(action -> {
//                        if (!incrementalUpdate)
//                            return true;
//                        ItemStack stack = action.getTarget();
//                        if (action.getType() == OPEN_CATEGORY) {
//                            // 增量模式下完全解锁的子分类不再扫描
//                            return !SlimefunManager.isCompletelyUnlocked(stack.getName().getString());
//                        }
//                        String id = SlimefunUtils.getSlimefunID(stack);
//                        // 原版物品使用复合ID
//                        if (id.startsWith("minecraft")) {
//                            id = ":" + MenuPath.current().getTitle() + id + action.getIndex();
//                        }
//                        // 增量模式下已解锁的物品不再扫描
//                        if (!SlimefunManager.hasSlimefunItem(id)) {
//                            String unlockPath = MenuPath.current().getTitle() + "/" + stack.getName().getString();
//                            SlimefunManager.unlockItem(unlockPath, new SlimefunItemStack(stack.copy()));
//                            log.info("解锁新物品：{} {}", unlockPath, SlimefunUtils.getSlimefunID(stack));
//                            return true;
//                        }
//                        return false;
//                    }).toList();
//            if (actions.isEmpty()) {
//                actionStack.push(new Actionx(postAction, -1, null));
//                return;
//            }
//            for (Actionx action : actions) {
//                if (first) {
//                    first = false;
//                    action.setPostActionType(postAction);
//                }
//                actionStack.push(action);
//            }
//        }
//
//        @Override
//        public String toString() {
//            return "SCAN_CATEGORY";
//        }
//    };
//
//    private static final ActionType SCAN_ITEM = new ActionType() {
//        @Override
//        public boolean requireAwait() {
//            return false;
//        }
//
//        @Override
//        public void runAction(MinecraftClient client, ScreenHandler handler, int slotId) {
//            if (!running) return;
//            actionStack.push(new Actionx(CLOSE_ITEM, -1, null));
//            SlimefunRecipe recipe = SlimefunUtils.extractRecipe(handler.slots);
//            final var items = SlimefunManager.getSlimefunItems();
//            int i = 0;
//            for (SlimefunItemStack input : recipe.getInputs()) {
//                int r = (i / 3) * 9;
//                int c = i % 3 + 3;
//                i++;
//                if (input.getStack().isEmpty())
//                    continue;
//                // 扫描未解锁的特殊物品
//                if (Config.SPECIAL.contains(input.getId()) && !items.containsKey(input.getId())) {
//                    SlimefunManager.addSlimefunItem(input);
//                    actionStack.push(new Actionx(OPEN_ITEM, r + c, input.getStack()));
//                }
//                // 跟踪材料未完全解锁的配方
//                if (input.isLocked()) {
//                    String unlockPath = input.getId().split("!")[1];
//                    SlimefunManager.addPartialUnlockedRecipe(unlockPath, recipe);
//                }
//            }
//            SlimefunManager.addSlimefunRecipe(recipe);
//            addons.forEach(addon -> addon.entry(actionStack, handler, slotId));
//        }
//
//        @Override
//        public String toString() {
//            return "SCAN_ITEM";
//        }
//    };
//
//    private static final ActionType CLOSE_MENU = new ActionType() {
//        @Override
//        public boolean requireAwait() {
//            return false;
//        }
//
//        @Override
//        public void runAction(MinecraftClient client, ScreenHandler handler, int slotId) {
//            if (!running) return;
//            stopSync();  // 成功后停止监听
//            SlimefunManager.finalizeData();
//            SlimefunManager.save();
//            if (client.player != null) {
//                client.player.sendMessage(
//                        Text.literal("Slimefun数据同步完成！")
//                                .formatted(Formatting.GREEN, Formatting.BOLD),
//                        false);
//            }
//        }
//
//        @Override
//        public String toString() {
//            return "CLOSE_MENU";
//        }
//    };
//
//    private static final ActionType CLOSE_CATEGORY = new ActionType() {
//        @Override
//        public boolean requireAwait() {
//            return true;
//        }
//
//        @Override
//        public void runAction(MinecraftClient client, ScreenHandler handler, int slotId) {
//            if (!running) return;
//            InventoryUtils.clickSlot(handler, 1);
//            MenuPath.pop();
//        }
//
//        @Override
//        public String toString() {
//            return "CLOSE_CATEGORY";
//        }
//    };
//
//    private static final ActionType CLOSE_ITEM = new ActionType() {
//        @Override
//        public boolean requireAwait() {
//            return true;
//        }
//
//        @Override
//        public void runAction(MinecraftClient client, ScreenHandler handler, int slotId) {
//            if (!running) return;
//            InventoryUtils.clickSlot(handler, 0);
//            MenuPath.pop();
//        }
//
//        @Override
//        public String toString() {
//            return "CLOSE_ITEM";
//        }
//    };
//
//    private static final ActionType NEXT_PAGE = new ActionType() {
//        @Override
//        public boolean requireAwait() {
//            return true;
//        }
//
//        @Override
//        public void runAction(MinecraftClient client, ScreenHandler handler, int slotId) {
//            if (!running) return;
//            InventoryUtils.clickSlot(handler, 52);
//            actionStack.push(new Actionx(SCAN_CATEGORY, -1, null));
//        }
//
//        @Override
//        public String toString() {
//            return "NEXT_PAGE";
//        }
//    };
//
//    private static final ActionType DEBUG = new ActionType() {
//        @Override
//        public boolean requireAwait() {
//            return false;
//        }
//
//        @Override
//        public void runAction(MinecraftClient client, ScreenHandler handler, int slotId) {
//            if (!running) return;
//            long limited = handler.slots.size() - 36;
//            handler.slots.stream()
//                    .limit(limited)
//                    .filter(slot -> (!slot.getStack().isEmpty())).forEach(slot -> {
//                        String components = slot.getStack().getComponents().stream().map(Component::toString).collect(Collectors.joining("\n"));
//                        log.info("Slot ID: {}, Stack: {}\n Components: {}", slot.id, slot.getStack().getName().getString(), components);
//                    });
//            stopSync();
//        }
//
//        @Override
//        public String toString() {
//            return "DEBUG";
//        }
//    };
}
