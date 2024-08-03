package io.github.paomiantong.slimefun_predicate.client.menuloader;

import net.minecraft.item.ItemStack;

public class Action {
    private final ActionType type;
    private final ItemStack target;
    private final int index;
    private ActionType postActionType;
    private boolean needAwait = false;

    public Action(ActionType type, int index, ItemStack target) {
        this(type, index, target, null);
    }

    public Action(ActionType type, int index, ItemStack target, ActionType postActionType) {
        this.type = type;
        this.target = target;
        this.postActionType = postActionType;
        this.index = index;
        if (type == ActionType.CLOSE_CATEGORY ||
                type == ActionType.CLOSE_ITEM ||
                type == ActionType.OPEN_CATEGORY ||
                type == ActionType.OPEN_ITEM ||
                type == ActionType.NEXT_PAGE) {
            needAwait = true;
        }
    }

    public ActionType getType() {
        return type;
    }

    public ItemStack getTarget() {
        return target;
    }

    public int getIndex() {
        return index;
    }

    public ActionType getPostActionType() {
        return postActionType;
    }

    public void setPostActionType(ActionType postActionType) {
        this.postActionType = postActionType;
    }

    public boolean requireAwait() {
        return needAwait;
    }
}
