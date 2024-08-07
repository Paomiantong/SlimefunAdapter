package io.github.paomiantong.slimefun_predicate.client.menuloader;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;

public abstract class ActionType {
    public abstract boolean requireAwait();

    public abstract void runAction(MinecraftClient client, ScreenHandler handler, int slotId, boolean hasNext);

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
