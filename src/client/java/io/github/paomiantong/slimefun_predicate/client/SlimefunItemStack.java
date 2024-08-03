package io.github.paomiantong.slimefun_predicate.client;

import com.google.gson.JsonObject;
import io.github.paomiantong.slimefun_predicate.client.utils.JsonUtils;
import net.minecraft.item.ItemStack;
import lombok.Getter;

import static io.github.paomiantong.slimefun_predicate.client.utils.SlimefunUtils.getSlimefunID;

@Getter
public class SlimefunItemStack {
    private final String id;
    private final ItemStack stack;

    public SlimefunItemStack(ItemStack stack) {
        this.id = getSlimefunID(stack);
        this.stack = stack;
    }

    public JsonObject serialize() {
        return JsonUtils.serializeItem(stack);
    }

    public String toString() {
        return stack.getName().getString() + " x" + stack.getCount();
    }

    public SlimefunItemStack copy() {
        return new SlimefunItemStack(stack.copy());
    }

    public void setAmount(int amount) {
        stack.setCount(amount);
    }

    public boolean isVanilla() {
        return id.startsWith("minecraft:");
    }
}
