package io.github.paomiantong.slimefunadapter.command;

import com.mojang.brigadier.CommandDispatcher;
import io.github.paomiantong.slimefunadapter.slimefun.SlimefunManager;
import io.github.paomiantong.slimefunadapter.slimefun.menuloader.MenuSynchronizer;
import io.github.paomiantong.slimefunadapter.utils.SlimefunUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class SyncSlimefunMenu {
    private static boolean monitorRegistered = false;

    public static void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            registerOpenMenuCommand(dispatcher);
        });
    }

    private static void registerOpenMenuCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("sync_menu")
                .executes(context -> {
                    FabricClientCommandSource source = context.getSource();
                    openMenu(source, false, false);
                    return 1;
                }).then(literal("stop").executes(context -> {
                    MenuSynchronizer.stopSync();
                    context.getSource().sendFeedback(Text.literal("停止同步菜单"));
                    return 1;
                }))
                .then(literal("debug").executes(context -> {
                    FabricClientCommandSource source = context.getSource();
                    openMenu(source, true, false);
                    return 1;
                })).then(literal("update").executes(context -> {
                    FabricClientCommandSource source = context.getSource();
                    openMenu(source, false, true);
                    return 1;
                })).then(literal("help").executes(context -> {
                    FabricClientCommandSource source = context.getSource();
                    source.sendFeedback(Text.literal("sync_menu [debug|update|stop|help]"));
                    source.sendFeedback(Text.literal("打开菜单并同步菜单，应该在首次使用时执行"));
                    source.sendFeedback(Text.literal("help: 显示帮助"));
                    source.sendFeedback(Text.literal("debug: 调试"));
                    source.sendFeedback(Text.literal("update: 同步菜单并更新已有的菜单，应该在解锁了新物品后执行"));
                    source.sendFeedback(Text.literal("stop: 停止同步菜单"));
                    return 1;
                }))
//                .then(literal("scan").then(
//                                argument("name", StringArgumentType.string()).executes(context -> {
//                                    FabricClientCommandSource source = context.getSource();
//                                    String name = StringArgumentType.getString(context, "name");
//                                    if (!monitorRegistered) {
//                                        MenuSynchronizer.registerListener();
//                                        monitorRegistered = true;
//                                    }
//                                    MenuSynchronizer.scan(name);
//                                    source.sendFeedback(Text.literal("扫描物品成功"));
//                                    return 1;
//                                })
//                        )
//                )
        );
    }

    private static void openMenu(FabricClientCommandSource source, boolean debug, boolean update) {
        // 在这里实现打开菜单的逻辑
        source.sendFeedback(Text.literal("正在打开菜单..."));
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
                    MenuSynchronizer.registerListener();
                    monitorRegistered = true;
                }
                if (debug) {
                    MenuSynchronizer.startDebug();
                } else {
                    if (!update)
                        SlimefunManager.clear();
                    MenuSynchronizer.startSync(update);
                }
                client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
            }
        } else {
            source.sendFeedback(Text.literal("请手持黏液科技菜单"));
        }
    }
}
