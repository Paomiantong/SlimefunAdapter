package io.github.paomiantong.slimefunadapter.slimefun.menuloader.actions;

import io.github.paomiantong.slimefunadapter.slimefun.SlimefunManager;
import io.github.paomiantong.slimefunadapter.slimefun.menuloader.MenuSynchronizer;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class CloseMenu extends Action {
    public CloseMenu(int slot_id) {
        super(slot_id);
    }

    @Override
    public void execute(ExecutionContext context) {
        if (!MenuSynchronizer.isRunning()) return;
        MenuSynchronizer.stopSync();  // 成功后停止监听
        SlimefunManager.finalizeData();
        SlimefunManager.save();
        var client = context.client();
        if (client.player != null) {
            client.player.sendMessage(
                    Text.literal("Slimefun数据同步完成！")
                            .formatted(Formatting.GREEN, Formatting.BOLD),
                    false);
        }
    }

    @Override
    public boolean await(ScreenHandler handler) {
        return false;
    }
}
