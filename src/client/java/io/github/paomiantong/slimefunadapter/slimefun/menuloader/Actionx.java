package io.github.paomiantong.slimefunadapter.slimefun.menuloader;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.item.ItemStack;

@Getter
public class Actionx {
    private final ActionType type;
    private final ItemStack target;
    private final int index;
    @Setter
    private ActionType postActionType;

    public Actionx(ActionType type, int index, ItemStack target) {
        this(type, index, target, null);
    }

    public Actionx(ActionType type, int index, ItemStack target, ActionType postActionType) {
        this.type = type;
        this.target = target;
        this.postActionType = postActionType;
        this.index = index;
    }

    public boolean requireAwait() {
        return type.requireAwait();
    }
}
