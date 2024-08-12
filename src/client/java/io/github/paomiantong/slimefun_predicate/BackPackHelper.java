package io.github.paomiantong.slimefun_predicate;

import io.github.paomiantong.slimefun_predicate.utils.InventoryUtils;
import io.github.paomiantong.slimefun_predicate.utils.SlimefunUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

import java.util.regex.Pattern;

public class BackPackHelper {
    private static final KeyBinding keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.slimefun_predicate.backpack", // The translation key of the keybinding's name
            InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
            GLFW.GLFW_KEY_B, // The keycode of the key
            "category.slimefun_predicate.backpack" // The translation key of the keybinding's category.
    ));

    private static boolean BACKPACK_OPENED = false;
    private static int SWAP_SLOT = -1;
    private static final Pattern pattern = Pattern.compile(".* \\[大小 \\d+\\]");

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (keyBinding.wasPressed()) {
                if (client.player != null) {
                    swapItemToMainHandAndUse(client.player);
                }
            }
        });
        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            if (!BACKPACK_OPENED) return;
            String screenName = screen.getTitle().getString();
            System.out.println(screenName);
            if (pattern.matcher(screenName).find()) {
                System.out.println("Backpack opened");
                afterCloseBackpack(screen);
            }
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            BACKPACK_OPENED = false;
            SWAP_SLOT = -1;
        });
    }

    private static void afterCloseBackpack(Screen screen) {
        ScreenEvents.remove(screen).register(screen1 -> {
            if (screen1.equals(screen)) {
                System.out.println("Backpack closed");
                InventoryUtils.swapItemToHand(MinecraftClient.getInstance().player, SWAP_SLOT);
                BACKPACK_OPENED = false;
                SWAP_SLOT = -1;
            }
        });
    }

    private static void swapItemToMainHandAndUse(PlayerEntity player) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.interactionManager == null || player == null) return;

        PlayerInventory inventory = player.getInventory();
        int targetSlot = findTargetSlot(inventory);
        SWAP_SLOT = targetSlot;
        // 背包在热键栏中，关闭背包后应该恢复到原来的热键栏位置
        if (SWAP_SLOT > 0 && SWAP_SLOT < 9) {
            SWAP_SLOT = inventory.selectedSlot;
        }
        if (targetSlot == inventory.selectedSlot) {
            simulateRightClick(client);
        } else if (targetSlot != -1) {
            InventoryUtils.swapItemToHand(player, targetSlot);
            // 交换完成后模拟使用主手物品
            simulateRightClick(client);
        } else {
            player.sendMessage(Text.literal("没有找到背包")
                    .formatted(Formatting.DARK_RED, Formatting.BOLD), false);
        }
    }

    private static void simulateRightClick(MinecraftClient client) {
        if (client.player != null) {
            // 通过ClientPlayerEntity的interact方法模拟右键使用
            if (client.interactionManager != null) {
                client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
                BACKPACK_OPENED = true;
            }
        }
    }

    private static int findTargetSlot(PlayerInventory inventory) {
        ItemStack currentItem = inventory.getMainHandStack();

        if (SlimefunUtils.getSlimefunID(currentItem).endsWith("BACKPACK")) {
            return inventory.selectedSlot;
        }

        for (int i = 0; i < inventory.main.size(); i++) {
            ItemStack stack = inventory.main.get(i);
            if (SlimefunUtils.getSlimefunID(stack).endsWith("BACKPACK")) {
                return i;
            }
        }
        return -1;
    }
}
