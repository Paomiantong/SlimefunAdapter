package io.github.paomiantong.slimefunadapter.slimefun.menuloader;

import io.github.paomiantong.slimefunadapter.SlimefunAdapterClient;
import io.github.paomiantong.slimefunadapter.config.Config;
import io.github.paomiantong.slimefunadapter.slimefun.menuloader.addons.Addons;
import io.github.paomiantong.slimefunadapter.slimefun.menuloader.addons.WorkstationRecipeAddons;
import io.github.paomiantong.slimefunadapter.slimefun.SlimefunItemStack;
import io.github.paomiantong.slimefunadapter.slimefun.SlimefunManager;
import io.github.paomiantong.slimefunadapter.slimefun.SlimefunRecipe;
import io.github.paomiantong.slimefunadapter.utils.InventoryUtils;
import io.github.paomiantong.slimefunadapter.utils.SlimefunUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.component.Component;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MenuSynchronizer {
    private static final Logger LOGGER = LoggerFactory.getLogger(SlimefunAdapterClient.class);
    private static boolean running = false;
    private static final Stack<Action> actionStack = new Stack<>();
    private static int lastSyncId = -1;
    private static boolean awaiting = false;
    private static int tickCounter = 0;
    private static boolean incrementalUpdate = false;
    private static final List<Addons> addons = List.of(new WorkstationRecipeAddons());

    private static final Predicate<ScreenHandler> defaultAwaitPredicate = handler -> handler.syncId == lastSyncId;

    public static void registerListener() {
        ClientTickEvents.END_CLIENT_TICK.register(MenuSynchronizer::onClientTick);
    }

    public static void startSync(boolean incrementalUpdate_) {
        if (!running) {
            MenuPath.init();
            actionStack.clear();
            actionStack.push(new Action(OPEN_MENU, -1, null));
            lastSyncId = -1;
            awaiting = false;
            running = true;
            incrementalUpdate = incrementalUpdate_;
        }
    }

    public static void startDebug() {
        if (!running) {
            actionStack.clear();
            actionStack.push(new Action(DEBUG, -1, null));
            lastSyncId = -1;
            awaiting = false;
            running = true;
        }
    }

    public static void stopSync() {
        running = false;
    }

    private static boolean isExcluded(ItemStack stack) {
        String name = stack.getName().getString();
        return Config.EXCLUDE.contains(name) || name.startsWith("已锁定");
    }

    private static List<Slot> getSlots(ScreenHandler handler) {
        long limited = handler.slots.size() - 36;
        var slots = new ArrayList<>(handler.slots.stream()
                .limit(limited)
                .peek(slot -> {
                    // 当前分类有锁定物品时，标记当前分类为未解锁
                    if (SlimefunUtils.isLocked(slot.getStack())) {
                        MenuPath.current().setUnlocked(false);
                    }
                })
                .filter(slot -> (!SlimefunUtils.isUI(slot.getStack()) && !slot.getStack().isEmpty()))
                .toList());
        Collections.reverse(slots);
        return slots;
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
                    LOGGER.warn("菜单为空！");
                    return;
                }
                LOGGER.info("CURRENT TICK SYNC ID: {}", handler.syncId);

                Action currentAction = actionStack.peek();
                Predicate<ScreenHandler> predicate = currentAction.getType().getAwaitPredicate();
                if (predicate == null) {
                    predicate = defaultAwaitPredicate;
                }
                // 执行更改UI的动作后需要等待UI更新
                if (lastSyncId != -1 && awaiting && predicate.test(handler)) {
                    return;
                } else {
                    actionStack.pop();
                }

                // 纪录当前操作后是否需要等待UI更新以及当前操作的SyncID
                awaiting = currentAction.requireAwait();
                lastSyncId = handler.syncId;

                LOGGER.info("当前操作：{}, SyncID {}", currentAction.getType(), lastSyncId);
                if (currentAction.getTarget() != null) {
                    LOGGER.info("目标：{}, {}", currentAction.getTarget().getName().getString(), currentAction.getTarget());
                }
                if (currentAction.getPostActionType() != null) {
                    LOGGER.info("后续操作：{}", currentAction.getPostActionType());
                    actionStack.push(new Action(currentAction.getPostActionType(), -1, null));
                }

                // 执行当前操作
                currentAction.getType().runAction(client, handler, currentAction.getIndex());
            }
        }
    }

    public static void scan(String name) {
        actionStack.clear();
        MenuPath.init();
        lastSyncId = -1;
        awaiting = false;
        running = true;
        final ActionType closeAction = new ActionType() {
            @Override
            public boolean requireAwait() {
                return false;
            }

            @Override
            public void runAction(MinecraftClient client, ScreenHandler handler, int slotId) {
                stopSync();
            }
        };

        final ActionType scanAction = new ActionType() {
            @Override
            public boolean requireAwait() {
                return false;
            }

            @Override
            public void runAction(MinecraftClient client, ScreenHandler handler, int slotId) {
                if (!running) return;
                for (Slot slot : getSlots(handler)) {
                    ItemStack stack = slot.getStack();
                    if (isExcluded(stack) ||
                            (incrementalUpdate && SlimefunManager.isCompletelyUnlocked(stack.getName().getString()))) {
                        continue;
                    }
                    if (name.equals(stack.getName().getString())) {
                        actionStack.push(new Action(OPEN_CATEGORY, slot.id, stack, closeAction));
                    }
                }
            }

            @Override
            public String toString() {
                return "SCAN_SPEC:" + name;
            }
        };
        actionStack.push(new Action(scanAction, -1, null));
    }


    /* Actions */
    private static final ActionType OPEN_MENU = new ActionType() {
        @Override
        public boolean requireAwait() {
            return false;
        }

        @Override
        public void runAction(MinecraftClient client, ScreenHandler handler, int slotId) {
            if (!running) return;
            if (!SlimefunUtils.isRoot(handler.slots)) {
                if (client.player != null) {
                    client.player.sendMessage(
                            Text.literal("请打开Slimefun指南主页面!")
                                    .formatted(Formatting.DARK_RED, Formatting.BOLD),
                            false);
                }
                stopSync();
                return;
            }

            boolean first = true;
            for (Slot slot : getSlots(handler)) {
                ItemStack stack = slot.getStack();
                if (isExcluded(stack) ||
                        (incrementalUpdate && SlimefunManager.isCompletelyUnlocked(stack.getName().getString()))) {
                    continue;
                }
                if (first) {
                    first = false;
                    actionStack.push(new Action(OPEN_CATEGORY, slot.id, stack, CLOSE_MENU));
                } else {
                    actionStack.push(new Action(OPEN_CATEGORY, slot.id, stack));
                }
            }
        }

        @Override
        public String toString() {
            return "OPEN_MENU";
        }
    };

    private static final ActionType OPEN_CATEGORY = new ActionType() {
        @Override
        public boolean requireAwait() {
            return true;
        }

        @Override
        public void runAction(MinecraftClient client, ScreenHandler handler, int slotId) {
            if (!running) return;
            MenuPath.push(
                    handler.getSlot(slotId).getStack().getName().getString(),
                    MenuPath.Type.Category);
            actionStack.push(new Action(SCAN_CATEGORY, -1, null));
            InventoryUtils.clickSlot(handler, slotId);
        }

        @Override
        public String toString() {
            return "OPEN_CATEGORY";
        }
    };

    private static final ActionType OPEN_ITEM = new ActionType() {
        @Override
        public boolean requireAwait() {
            return true;
        }

        @Override
        public void runAction(MinecraftClient client, ScreenHandler handler, int slotId) {
            if (!running) return;
            var target = handler.getSlot(slotId).getStack();
            if (SlimefunUtils.isSlimefunItem(target)) {
                SlimefunManager.addSlimefunItem(new SlimefunItemStack(target.copy()));
            } else {
                // 使用:+路径+原版ID+槽位ID作为原版物品的ID
                String id = ":" + MenuPath.current().getTitle() + SlimefunUtils.getSlimefunID(target) + slotId;
                SlimefunManager.addSlimefunItem(new SlimefunItemStack(id, target));
            }
            MenuPath.push(
                    handler.getSlot(slotId).getStack().getName().getString(),
                    MenuPath.Type.Item);
            InventoryUtils.clickSlot(handler, slotId);
            actionStack.push(new Action(SCAN_ITEM, -1, null));
        }

        @Override
        public String toString() {
            return "OPEN_ITEM";
        }
    };

    private static final ActionType SCAN_CATEGORY = new ActionType() {
        @Override
        public boolean requireAwait() {
            return false;
        }

        @Override
        public void runAction(MinecraftClient client, ScreenHandler handler, int slotId) {
            if (!running) return;
            boolean first = true;
            // 判断是否还有下一页
            ActionType postAction = SlimefunUtils.hasNext(handler.slots) ? NEXT_PAGE : CLOSE_CATEGORY;
            List<Action> actions = getSlots(handler).stream()
                    .filter(slot -> !isExcluded(slot.getStack()))
                    .map(slot -> {
                        ItemStack stack = slot.getStack();
                        ActionType actionType = SlimefunUtils.isCategory(stack) ? OPEN_CATEGORY : OPEN_ITEM;
                        return new Action(actionType, slot.id, stack);
                    }).filter(action -> {
                        if (!incrementalUpdate)
                            return true;
                        ItemStack stack = action.getTarget();
                        if (action.getType() == OPEN_CATEGORY) {
                            // 增量模式下完全解锁的子分类不再扫描
                            return !SlimefunManager.isCompletelyUnlocked(stack.getName().getString());
                        }
                        String id = SlimefunUtils.getSlimefunID(stack);
                        // 原版物品使用复合ID
                        if (id.startsWith("minecraft")) {
                            id = ":" + MenuPath.current().getTitle() + id + action.getIndex();
                        }
                        // 增量模式下已解锁的物品不再扫描
                        if (!SlimefunManager.hasSlimefunItem(id)) {
                            String unlockPath = MenuPath.current().getTitle() + "/" + stack.getName().getString();
                            SlimefunManager.unlockItem(unlockPath, new SlimefunItemStack(stack.copy()));
                            LOGGER.info("解锁新物品：{} {}", unlockPath, SlimefunUtils.getSlimefunID(stack));
                            return true;
                        }
                        return false;
                    }).toList();
            if (actions.isEmpty()) {
                actionStack.push(new Action(postAction, -1, null));
                return;
            }
            for (Action action : actions) {
                if (first) {
                    first = false;
                    action.setPostActionType(postAction);
                }
                actionStack.push(action);
            }
        }

        @Override
        public String toString() {
            return "SCAN_CATEGORY";
        }
    };

    private static final ActionType SCAN_ITEM = new ActionType() {
        @Override
        public boolean requireAwait() {
            return false;
        }

        @Override
        public void runAction(MinecraftClient client, ScreenHandler handler, int slotId) {
            if (!running) return;
            actionStack.push(new Action(CLOSE_ITEM, -1, null));
            SlimefunRecipe recipe = SlimefunUtils.extractRecipe(handler.slots);
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
                    actionStack.push(new Action(OPEN_ITEM, r + c, input.getStack()));
                }
                // 跟踪材料未完全解锁的配方
                if (input.isLocked()) {
                    String unlockPath = input.getId().split("!")[1];
                    SlimefunManager.addPartialUnlockedRecipe(unlockPath, recipe);
                }
            }
            SlimefunManager.addSlimefunRecipe(recipe);
            addons.forEach(addon -> addon.entry(actionStack, handler, slotId));
        }

        @Override
        public String toString() {
            return "SCAN_ITEM";
        }
    };

    private static final ActionType CLOSE_MENU = new ActionType() {
        @Override
        public boolean requireAwait() {
            return false;
        }

        @Override
        public void runAction(MinecraftClient client, ScreenHandler handler, int slotId) {
            if (!running) return;
            stopSync();  // 成功后停止监听
            SlimefunManager.finalizeData();
            SlimefunManager.save();
            if (client.player != null) {
                client.player.sendMessage(
                        Text.literal("Slimefun数据同步完成！")
                                .formatted(Formatting.GREEN, Formatting.BOLD),
                        false);
            }
        }

        @Override
        public String toString() {
            return "CLOSE_MENU";
        }
    };

    private static final ActionType CLOSE_CATEGORY = new ActionType() {
        @Override
        public boolean requireAwait() {
            return true;
        }

        @Override
        public void runAction(MinecraftClient client, ScreenHandler handler, int slotId) {
            if (!running) return;
            InventoryUtils.clickSlot(handler, 1);
            MenuPath.pop();
        }

        @Override
        public String toString() {
            return "CLOSE_CATEGORY";
        }
    };

    private static final ActionType CLOSE_ITEM = new ActionType() {
        @Override
        public boolean requireAwait() {
            return true;
        }

        @Override
        public void runAction(MinecraftClient client, ScreenHandler handler, int slotId) {
            if (!running) return;
            InventoryUtils.clickSlot(handler, 0);
            MenuPath.pop();
        }

        @Override
        public String toString() {
            return "CLOSE_ITEM";
        }
    };

    private static final ActionType NEXT_PAGE = new ActionType() {
        @Override
        public boolean requireAwait() {
            return true;
        }

        @Override
        public void runAction(MinecraftClient client, ScreenHandler handler, int slotId) {
            if (!running) return;
            InventoryUtils.clickSlot(handler, 52);
            actionStack.push(new Action(SCAN_CATEGORY, -1, null));
        }

        @Override
        public String toString() {
            return "NEXT_PAGE";
        }
    };

    private static final ActionType DEBUG = new ActionType() {
        @Override
        public boolean requireAwait() {
            return false;
        }

        @Override
        public void runAction(MinecraftClient client, ScreenHandler handler, int slotId) {
            if (!running) return;
            long limited = handler.slots.size() - 36;
            handler.slots.stream()
                    .limit(limited)
                    .filter(slot -> (!slot.getStack().isEmpty())).forEach(slot -> {
                        String components = slot.getStack().getComponents().stream().map(Component::toString).collect(Collectors.joining("\n"));
                        LOGGER.info("Slot ID: {}, Stack: {}\n Components: {}", slot.id, slot.getStack().getName().getString(), components);
                    });
            stopSync();
        }

        @Override
        public String toString() {
            return "DEBUG";
        }
    };
}
