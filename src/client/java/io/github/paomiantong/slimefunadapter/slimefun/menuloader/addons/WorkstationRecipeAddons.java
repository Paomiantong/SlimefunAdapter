package io.github.paomiantong.slimefunadapter.slimefun.menuloader.addons;

import io.github.paomiantong.slimefunadapter.SlimefunAdapterClient;
import io.github.paomiantong.slimefunadapter.config.Config;
import io.github.paomiantong.slimefunadapter.slimefun.SlimefunItemStack;
import io.github.paomiantong.slimefunadapter.slimefun.SlimefunManager;
import io.github.paomiantong.slimefunadapter.slimefun.SlimefunRecipe;
import io.github.paomiantong.slimefunadapter.slimefun.menuloader.actions.Action;
import io.github.paomiantong.slimefunadapter.slimefun.menuloader.actions.ExecutionContext;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Stack;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static io.github.paomiantong.slimefunadapter.utils.InventoryUtils.clickSlot;
import static io.github.paomiantong.slimefunadapter.utils.SlimefunUtils.getSlimefunID;


@Slf4j
public class WorkstationRecipeAddons implements Addons {
    private static final Logger LOGGER = LoggerFactory.getLogger(SlimefunAdapterClient.class);
    private static Stack<Action> actionStack;
    private static boolean needPageTurning;
    private static String lastState;
    private final static Predicate<ScreenHandler> AWAIT = handler -> {
        LOGGER.info("lastState: {}, cur: {}", lastState, getPage(handler));
        return lastState != null && lastState.equals(getPage(handler));
    };
    private final static Pattern pattern = Pattern.compile("\\((\\d+) / (\\d+)\\)");
    private static SlimefunItemStack type = null;

    private static String getPage(ScreenHandler handler) {
        var lore = handler
                .getSlot(28)
                .getStack()
                .get(DataComponentTypes.LORE);
        if (lore == null) {
            LOGGER.error("lore is null");
            return "";
        }
        return lore.lines().get(1).getString();
    }

    private static int[] getPageInfo(String page) {
        var matcher = pattern.matcher(page);
        if (matcher.find()) {
            return new int[]{Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2))};
        }
        return new int[]{0, 0};
    }

    private static boolean hasNext(ScreenHandler handler) {
        return "_UI_NEXT_ACTIVE".equals(getSlimefunID(handler.getSlot(34).getStack()));
    }

    private static boolean isRecipeBar(ItemStack stack) {
        return stack.getName().getString().equals("⇩ 此机器可用的合成配方 ⇩");
    }

    @Override
    public void entry(Stack<Action> actionStack, ScreenHandler handler, int slotId) {
        WorkstationRecipeAddons.actionStack = actionStack;
        var slots = handler.slots;
        type = new SlimefunItemStack(slots.get(16).getStack().copy());
        if (34 >= slots.size() - 36 ||
                !isRecipeBar(handler.getSlot(33).getStack()) ||
                Config.EXCLUDE_WORKSTATION.contains(type.getId())) {
            return;
        }
        needPageTurning = hasNext(handler);
        lastState = getPage(handler);
        actionStack.push(NEXT_PAGE);
    }

    private static final Action NEXT_PAGE = new Action(-1) {
        @Override
        public boolean await(ScreenHandler handler) {
            return needPageTurning && AWAIT.test(handler);
        }

        @Override
        public void execute(ExecutionContext context) {
            var handler = context.handler();
            for (int i = 36; i < 36 + 9; i++) {
                SlimefunItemStack input = new SlimefunItemStack(handler.getSlot(i).getStack().copy());
                SlimefunItemStack output = new SlimefunItemStack(handler.getSlot(i + 9).getStack().copy());
                if (input.isEmpty() || output.isEmpty()) {
                    break;
                }
                SlimefunRecipe recipe = new SlimefunRecipe(output,
                        new SlimefunItemStack[]{
                                input, SlimefunItemStack.EMPTY, SlimefunItemStack.EMPTY,
                                SlimefunItemStack.EMPTY, SlimefunItemStack.EMPTY, SlimefunItemStack.EMPTY,
                                SlimefunItemStack.EMPTY, SlimefunItemStack.EMPTY, SlimefunItemStack.EMPTY,
                        },
                        type);
                // 此处为了方便代码编写，就不考虑解锁情况了
                if (!output.isVanilla() && !SlimefunManager.hasSlimefunItem(output.getId())) {
                    SlimefunManager.addSlimefunItem(output);
                }
                if (!input.isVanilla() && !SlimefunManager.hasSlimefunItem(input.getId())) {
                    SlimefunManager.addSlimefunItem(input);
                }
                SlimefunManager.addSlimefunRecipe(recipe);
                log.info("Add recipe: {}", recipe);
            }
            needPageTurning = hasNext(handler);
            lastState = getPage(handler);
            if (needPageTurning) {
                var pageInfo = getPageInfo(lastState);
                needPageTurning = pageInfo[0] + 1 < pageInfo[1];
                clickSlot(handler, 34);
                actionStack.push(NEXT_PAGE);
            }
        }
    };
}
