package io.github.paomiantong.slimefun_predicate.utils;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Stack;

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
                        String[] kv = component.toString().split("=>");

                        if (kv[0].endsWith("tool")) return;

                        MutableText result = Text.literal(kv[0]).formatted(Formatting.BOLD, Formatting.GOLD)
                                .append(" => ");

                        String beautified = beautifyBrackets(kv[1]);
                        result.append(Text.literal(beautified).formatted(Formatting.ITALIC, Formatting.GRAY));

                        source.sendFeedback(result);
                    });
                    return 1;
                })
        );
    }

    // 将方法定义为静态
    private static String beautifyBrackets(String input) {
        StringBuilder output = new StringBuilder();
        char[] colors = { 'c', '6', 'e', 'a', 'b', '3', 'd' }; // 彩虹色代码
        Stack<Character> bracketStack = new Stack<>();
        Stack<Integer> colorStack = new Stack<>();
        int colorIndex = 0;

        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);

            if (ch == '(' || ch == '{' || ch == '[') {
                bracketStack.push(ch);
                if (bracketStack.size() > colorStack.size()) {
                    colorStack.push(colorIndex);
                    colorIndex = (colorIndex + 1) % colors.length; // Change color at new depth level
                }
                output.append("§").append(colors[colorStack.peek()]).append(ch);
            } else if (ch == ')' || ch == '}' || ch == ']') {
                if (!bracketStack.isEmpty() && matchingBrackets(bracketStack.peek(), ch)) {
                    bracketStack.pop();
                    colorIndex = colorStack.pop();
                    output.append("§").append(colors[colorIndex]).append(ch);
                }
            } else {
                output.append(ch); // White color for non-bracket text
            }
        }

        return output.toString();
    }

    // Helper function to check if brackets match
    private static boolean matchingBrackets(char open, char close) {
        return (open == '(' && close == ')') || (open == '{' && close == '}') || (open == '[' && close == ']');
    }
}
