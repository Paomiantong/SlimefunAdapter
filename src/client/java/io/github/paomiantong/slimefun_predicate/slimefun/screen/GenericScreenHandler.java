package io.github.paomiantong.slimefun_predicate.slimefun.screen;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

public class GenericScreenHandler extends GenericContainerScreenHandler {
    public GenericScreenHandler(ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, int rows) {
        super(type, syncId, playerInventory, new SimpleInventory(9 * rows), rows);
    }

    public static GenericContainerScreenHandler createAutoAncientAltar(int syncId, PlayerInventory playerInventory) {
        return new GenericScreenHandler(SlimefunScreenHandlerType.AUTO_ANCIENT_ALTAR, syncId, playerInventory, 6);
    }

    public static GenericContainerScreenHandler createDivineAltar(int syncId, PlayerInventory playerInventory) {
        return new GenericScreenHandler(SlimefunScreenHandlerType.DIVINE_ALTAR, syncId, playerInventory, 3);
    }
}
