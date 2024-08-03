package io.github.paomiantong.slimefun_predicate.client.menuloader;

import io.github.paomiantong.slimefun_predicate.client.*;
import io.github.paomiantong.slimefun_predicate.client.utils.SlimefunUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static io.github.paomiantong.slimefun_predicate.client.utils.SlimefunUtils.*;

public class MenuMonitor {
    private static final Logger LOGGER = LoggerFactory.getLogger(SlimefunPredicateClient.class);
    private static boolean running = false;
    private static final Stack<Action> actionStack = new Stack<>();

    private static int lastSyncId = -1;
    private static boolean awaiting = false;

    private static int tickCounter = 0;

    public static void registerListener() {
        ClientTickEvents.END_CLIENT_TICK.register(MenuMonitor::onClientTick);
    }

    public static void startMonitoring() {
        if (!running) {
            actionStack.clear();
            actionStack.push(new Action(ActionType.OPEN_MENU, -1, null));
            lastSyncId = -1;
            awaiting = false;
            running = true;
        }
    }

    public static void stopMonitoring() {
        running = false;
        ResourceLoader.save();
    }

    private static void pushAction(Slot slot, boolean first, ActionType closeType) {
        ItemStack stack = slot.getStack();
        Action action;
        if (SlimefunUtils.isCategory(stack)) {
            action = new Action(ActionType.OPEN_CATEGORY, slot.id, stack);

        } else {
            action = new Action(ActionType.OPEN_ITEM, slot.id, stack);
        }
        if (first) {
            action.setPostActionType(closeType);
        }
        actionStack.push(action);
    }

    private static boolean isExcluded(ItemStack stack) {
        String name = stack.getName().getString();
        return Config.EXCLUDE.contains(name) || name.startsWith("已锁定");
    }

    private static void clickSlot(MinecraftClient client, ScreenHandler handler, int slotId) {
        if (client.player == null || client.interactionManager == null) {
            return;
        }
        int syncId = handler.syncId;
        client.interactionManager.clickSlot(syncId, slotId, 0, SlotActionType.PICKUP, client.player);
    }

    private static List<Slot> getSlots(ScreenHandler handler) {
        long limited = handler.slots.size() - 36;
        var slots = new ArrayList<>(handler.slots.stream()
                .limit(limited)
                .filter(slot -> (!SlimefunUtils.isUI(slot.getStack()) && !slot.getStack().isEmpty()))
                .toList());
        Collections.reverse(slots);
        return slots;
    }

    private static void onOpenMenu(ScreenHandler handler) {
        if (!running) return;
        boolean first = true;
        for (Slot slot : getSlots(handler)) {
            ItemStack stack = slot.getStack();
            if (isExcluded(stack)) {
                continue;
            }
            if (first) {
                first = false;
                actionStack.push(new Action(ActionType.OPEN_CATEGORY, slot.id, stack, ActionType.CLOSE_MENU));
            } else {
                actionStack.push(new Action(ActionType.OPEN_CATEGORY, slot.id, stack));
            }
        }
    }

    private static void onOpenCategory(MinecraftClient client, ScreenHandler handler, int slotId) {
        if (!running) return;
        clickSlot(client, handler, slotId);
        actionStack.push(new Action(ActionType.SCAN_CATEGORY, -1, null));
    }

    private static void onOpenItem(MinecraftClient client, ScreenHandler handler, int slotId) {
        if (!running) return;
        clickSlot(client, handler, slotId);
        actionStack.push(new Action(ActionType.SCAN_ITEM, -1, null));
    }

    private static void onScanCategory(ScreenHandler handler, boolean hasNext) {
        if (!running) return;
        boolean first = true;
        ActionType postAction = hasNext ? ActionType.NEXT_PAGE : ActionType.CLOSE_CATEGORY;
        for (Slot slot : getSlots(handler)) {
            if (isExcluded(slot.getStack()))
                continue;
            pushAction(slot, first, postAction);
            if (first) {
                first = false;
            }
        }
    }

    private static void onScanItem(ScreenHandler handler) {
        if (!running) return;
        actionStack.push(new Action(ActionType.CLOSE_ITEM, -1, null));
        SlimefunRecipe recipe = extractRecipe(handler.slots);
        final var items = ResourceLoader.getSlimefunItems();
        int i = 0;
        for (SlimefunItemStack input : recipe.inputs()) {
            int r = (i / 3) * 9;
            int c = i % 3 + 3;
            i++;
            if (input.getStack().isEmpty())
                continue;
            if (Config.SPECIAL.contains(input.getId()) && !items.containsKey(input.getId())) {
                ResourceLoader.addSlimefunItem(input);
                actionStack.push(new Action(ActionType.OPEN_ITEM, r + c, input.getStack()));
            }
        }
        ResourceLoader.addSlimefunRecipe(recipe);

    }

    private static void onCloseMenu() {
        if (!running) return;
        stopMonitoring();  // 成功后停止监听
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
            if (title.getString().contains("Slimefun 指南")) {
                long limited = handler.slots.size() - 36;
                if (handler.slots.stream()
                        .limit(limited)
                        .allMatch(slot -> SlimefunUtils.isUI(slot.getStack()) || slot.getStack().isEmpty())) {
                    LOGGER.warn("菜单为空！");
                    return;
                }
//                LOGGER.info("CURRENT TICK SYNC ID: {}", handler.syncId);

                Action currentAction = actionStack.peek();

                // 执行更改UI的动作后需要等待UI更新
                if (lastSyncId != -1 && lastSyncId == handler.syncId && awaiting) {
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

                // 判断是否还有下一页
                boolean hasNext = hasNext(handler.slots);

                switch (currentAction.getType()) {
                    case OPEN_MENU:
                        onOpenMenu(handler);
                        break;
                    case OPEN_CATEGORY:
                        onOpenCategory(client, handler, currentAction.getIndex());
                        break;
                    case OPEN_ITEM:
                        if (isSlimefunItem(currentAction.getTarget())) {
                            ResourceLoader.addSlimefunItem(new SlimefunItemStack(currentAction.getTarget().copy()));
                        }
                        onOpenItem(client, handler, currentAction.getIndex());
                        break;
                    case SCAN_CATEGORY:
                        onScanCategory(handler, hasNext);
                        break;
                    case SCAN_ITEM:
                        onScanItem(handler);
                        break;
                    case CLOSE_MENU:
                        onCloseMenu();
                        break;
                    case CLOSE_CATEGORY:
                        clickSlot(client, handler, 1);
                        break;
                    case CLOSE_ITEM:
                        clickSlot(client, handler, 0);
                        break;
                    case NEXT_PAGE:
                        clickSlot(client, handler, 52);
                        actionStack.push(new Action(ActionType.SCAN_CATEGORY, -1, null));
                        break;
                }
            }
        }
    }
}
