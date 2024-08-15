package io.github.paomiantong.slimefun_predicate.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import java.util.ArrayList;
import java.util.List;

public class InventoryUtils {
    public static void swapItemToHand(PlayerEntity player, int slot) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.interactionManager == null || player == null || slot < 0) return;
        PlayerInventory inventory = player.getInventory();
        ScreenHandler handler = player.playerScreenHandler;
        if (slot >= 9)
            client.interactionManager.clickSlot(handler.syncId, slot, inventory.selectedSlot, SlotActionType.SWAP, player);
        else {
            inventory.selectedSlot = slot;
            client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(inventory.selectedSlot));
        }
    }

    public static void clickSlot(ScreenHandler handler, int slot) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.interactionManager == null || slot < 0) {
            return;
        }
        client.interactionManager.clickSlot(handler.syncId, slot, 0, SlotActionType.PICKUP, client.player);
    }

    public static List<Slot> get9x9Slots(ScreenHandler handler, int start) {
        final List<Slot> slots = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            int r = (i / 3) * 9;
            int c = i % 3 + start;
            slots.add(handler.getSlot(r + c));
        }
        return slots;
    }
}
