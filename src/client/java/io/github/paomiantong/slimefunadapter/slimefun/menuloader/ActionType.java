package io.github.paomiantong.slimefunadapter.slimefun.menuloader;

import net.minecraft.client.MinecraftClient;
import net.minecraft.screen.ScreenHandler;

import java.util.function.Predicate;

public abstract class ActionType {
    public abstract boolean requireAwait();

    public abstract void runAction(MinecraftClient client, ScreenHandler handler, int slotId);

    public Predicate<ScreenHandler> getAwaitPredicate() {
        return null;
    }
//    OPEN_MENU,
//    CLOSE_MENU,
//    OPEN_CATEGORY,
//    SCAN_CATEGORY,
//    NEXT_PAGE,
//    CLOSE_CATEGORY,
//    OPEN_ITEM,
//    SCAN_ITEM,
//    CLOSE_ITEM
}
