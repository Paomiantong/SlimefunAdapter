package io.github.paomiantong.slimefunadapter.slimefun;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.paomiantong.slimefunadapter.utils.InventoryUtils;
import io.github.paomiantong.slimefunadapter.utils.JsonUtils;
import io.github.paomiantong.slimefunadapter.utils.SlimefunUtils;
import lombok.extern.slf4j.Slf4j;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import org.lwjgl.glfw.GLFW;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static io.github.paomiantong.slimefunadapter.config.Config.CONFIG_PATH;
import static io.github.paomiantong.slimefunadapter.config.Config.writeStringToFile;

@Slf4j(topic = "SlimefunAdapter")
public class BackPackHelper {
    private static final KeyBinding keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.slimefunadapter.backpack", // The translation key of the keybinding's name
            InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
            GLFW.GLFW_KEY_B, // The keycode of the key
            "category.slimefunadapter" // The translation key of the keybinding's category.
    ));
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Pattern pattern = Pattern.compile(".* \\[大小 \\d+\\]");
    private static final Map<String, Inventory> UUID_TO_INV = new HashMap<>();
    private static boolean BACKPACK_OPENED = false;
    private static String CURRENT_UUID = null;
    private static int SWAP_SLOT = -1;
    private static ServerInfo CURRENT_SERVER = null;

    public static Inventory getInventory(ItemStack stack) {
        return UUID_TO_INV.get(SlimefunUtils.getInventoryUUID(stack));
    }

    public static void reload(MinecraftClient mc) {
        BACKPACK_OPENED = false;
        SWAP_SLOT = -1;
        CURRENT_SERVER = mc.getCurrentServerEntry();
        if (CURRENT_SERVER == null) {
            return;
        }
        try (BufferedReader json = Files.newBufferedReader(CONFIG_PATH.resolve("backpack/" + getEscapeString(CURRENT_SERVER.address) + ".json"))) {
            gson.fromJson(json, JsonObject.class).asMap().forEach((k, v) -> {
                ItemStack[] stacks = gson.fromJson(v, JsonArray.class).asList().stream().map(JsonUtils::deserializeItem).toArray(ItemStack[]::new);
                UUID_TO_INV.put(k, new SimpleInventory(stacks));
            });
        } catch (IOException ignored) {
        }
    }

    public static void save() {
        if (CURRENT_SERVER == null) {
            return;
        }
        JsonObject obj = new JsonObject();
        UUID_TO_INV.forEach((k, v) -> {
            JsonArray items = new JsonArray();
            for (int i = 0; i < v.size(); i++) {
                items.add(JsonUtils.serializeItem(v.getStack(i), true));
            }
            obj.add(k, items);
        });
        writeStringToFile("backpack/" + getEscapeString(CURRENT_SERVER.address) + ".json", gson.toJson(obj));
        log.info("Saved backpack data for server: {}", CURRENT_SERVER.address);
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (keyBinding.wasPressed()) {
                if (client.player != null) {
                    swapItemToMainHandAndUse(client.player);
                }
            }
        });
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (player != null && hand == Hand.MAIN_HAND) {
                ItemStack stack = player.getMainHandStack();
                if (SlimefunUtils.getSlimefunID(stack).endsWith("BACKPACK")) {
                    BACKPACK_OPENED = true;
                    CURRENT_UUID = SlimefunUtils.getInventoryUUID(stack);
                }

            }
            return TypedActionResult.pass(ItemStack.EMPTY);
        });
        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            if (!BACKPACK_OPENED) return;
            String screenName = screen.getTitle().getString();
            System.out.println(screenName);
            if (pattern.matcher(screenName).find()) {
                log.info("Backpack opened: {} {}", screenName, CURRENT_UUID);
                HandledScreen<?> handledScreen = (HandledScreen<?>) screen;
                UUID_TO_INV.put(CURRENT_UUID, handledScreen.getScreenHandler().getSlot(0).inventory);
                afterCloseBackpack(screen);
            }
        });
    }

    private static void afterCloseBackpack(Screen screen) {
        ScreenEvents.remove(screen).register(screen1 -> {
            if (screen1.equals(screen)) {
                log.info("Backpack closed: {}", CURRENT_UUID);
                InventoryUtils.swapItemToHand(MinecraftClient.getInstance().player, SWAP_SLOT);
                BACKPACK_OPENED = false;
                SWAP_SLOT = -1;
                CURRENT_UUID = null;
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
        if (client.player != null && client.interactionManager != null) {
            // 通过ClientPlayerEntity的interact方法模拟右键使用
            client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
            BACKPACK_OPENED = true;
            CURRENT_UUID = SlimefunUtils.getInventoryUUID(client.player.getMainHandStack());
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

    private static String getEscapeString(String str) {
        return str.replace(" ", "_").replace(":", "_").replace("/", "_");
    }
}
