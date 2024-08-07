package io.github.paomiantong.slimefun_predicate.client.utils;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ShowComponents {
    public static void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            registerOpenMenuCommand(dispatcher);
        });
    }

    private static void registerOpenMenuCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("show_components")
                .executes(context -> {
                    FabricClientCommandSource source = context.getSource();
                    PlayerEntity player = source.getPlayer();
                    ItemStack itemStack = player.getMainHandStack();
                    if (itemStack.isEmpty()) {
                        source.sendFeedback(Text.of("You must hold an item in your hand to use this command."));
                        return 0;
                    }
                    itemStack.getComponents().stream().iterator().forEachRemaining(component -> {
                        source.sendFeedback(Text.of(component.toString()));
                    });
                    return 1;
                })
        );
    }
}
