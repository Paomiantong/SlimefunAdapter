package io.github.paomiantong.slimefun_predicate.client.menuloader;

import com.mojang.brigadier.CommandDispatcher;
import io.github.paomiantong.slimefun_predicate.client.ResourceLoader;
import io.github.paomiantong.slimefun_predicate.client.utils.SlimefunUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class LoadSlimefunMenu {
    private static boolean monitorRegistered = false;

    public static void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            registerOpenMenuCommand(dispatcher);
        });
    }

    private static void registerOpenMenuCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("load_sf_menu")
                .executes(context -> {
                    FabricClientCommandSource source = context.getSource();
                    openMenu(source);
                    return 1;
                }).then(literal("stop").executes(context -> {
                    MenuMonitor.stopMonitoring();
                    context.getSource().sendFeedback(Text.literal("停止监控菜单"));
                    return 1;
                }))
        );
    }

    private static void openMenu(FabricClientCommandSource source) {
        // 在这里实现打开菜单的逻辑
        source.sendFeedback(Text.literal("打开菜单..."));
        // 可能需要进一步的逻辑来确定和打开具体的黏液科技菜单
        // 假设你已经能获取到玩家和他们的手中物品
        PlayerEntity player = source.getPlayer();
        if (player == null) return;

        ItemStack itemInHand = player.getMainHandStack();
        // 检查是否持有正确的物品
        if (SlimefunUtils.isMenu(itemInHand)) {
            MinecraftClient client = source.getClient();
            // 打开菜单的逻辑
            source.sendFeedback(Text.literal("打开菜单成功"));
            // 模拟玩家在客户端执行右键操作
            if (client.interactionManager != null) {
                if (!monitorRegistered) {
                    MenuMonitor.registerListener();
                    monitorRegistered = true;
                }
                MenuMonitor.startMonitoring();
                ResourceLoader.clear();
                client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
            }
        }
    }
}
