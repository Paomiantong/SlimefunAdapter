package io.github.paomiantong.slimefunadapter.slimefun.menuloader.actions;

import io.github.paomiantong.slimefunadapter.slimefun.menuloader.MenuSynchronizer;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.component.Component;
import net.minecraft.screen.ScreenHandler;

import java.util.stream.Collectors;

@Slf4j(topic = "SlimefunAdapter")
public class Debug extends Action {
    public Debug(int slotID) {
        super(slotID);
    }

    @Override
    public void execute(ExecutionContext context) {
        if (!MenuSynchronizer.isRunning()) return;
        var handler = context.handler();
        long limited = handler.slots.size() - 36;
        handler.slots.stream()
                .limit(limited)
                .filter(slot -> (!slot.getStack().isEmpty())).forEach(slot -> {
                    String components = slot.getStack().getComponents().stream().map(Component::toString).collect(Collectors.joining("\n"));
                    log.info("Slot ID: {}, Stack: {}\n Components: {}", slot.id, slot.getStack().getName().getString(), components);
                });
        MenuSynchronizer.stopSync();
    }

    @Override
    public boolean await(ScreenHandler handler) {
        return false;
    }
}
