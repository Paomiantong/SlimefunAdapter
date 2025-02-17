package io.github.paomiantong.slimefunadapter.slimefun;

import com.google.gson.JsonObject;
import io.github.paomiantong.slimefunadapter.utils.JsonUtils;
import lombok.Getter;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Objects;

import static io.github.paomiantong.slimefunadapter.utils.SlimefunUtils.getSlimefunID;

@Getter
public class SlimefunItemStack {
    private final String id;
    private final ItemStack stack;
    private final boolean locked;
    private final String unlockPath;

    public final static SlimefunItemStack EMPTY = new SlimefunItemStack(ItemStack.EMPTY);

    private SlimefunItemStack(String id, ItemStack stack, boolean locked, String unlockPath) {
        this.id = id;
        this.stack = stack;
        this.locked = locked;
        this.unlockPath = unlockPath;
    }

    public SlimefunItemStack(String id, ItemStack stack) {
        this.id = id;
        this.stack = stack;
        this.locked = false;
        this.unlockPath = null;
    }

    public SlimefunItemStack(ItemStack stack) {
        this.id = getSlimefunID(stack);
        this.stack = stack;
        this.locked = false;
        this.unlockPath = null;
    }

    public SlimefunItemStack(ItemStack stack, String unlockPath) {
        this.id = "!" + unlockPath + "!" + getSlimefunID(stack);
        this.stack = stack;
        String[] tmp = unlockPath.split("/");
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(tmp[1]));
        List<Text> lore = List.of(
                Text.literal("已锁定").formatted(Formatting.DARK_RED, Formatting.BOLD),
                Text.empty(),
                Text.literal("需要在 " + tmp[0] + " 中解锁").formatted(Formatting.WHITE)
        );
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        this.locked = true;
        this.unlockPath = unlockPath;
    }

    public JsonObject serialize() {
        return JsonUtils.serializeItem(stack);
    }

    public String toString() {
        return stack.getName().getString() + " x" + stack.getCount();
    }

    public SlimefunItemStack copy() {
        return new SlimefunItemStack(id, stack.copy(), locked, unlockPath);
    }

    public void setAmount(int amount) {
        stack.setCount(amount);
    }

    public boolean isVanilla() {
        return id.startsWith("minecraft:");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SlimefunItemStack that = (SlimefunItemStack) o;
        return Objects.equals(id, that.id) && Objects.equals(stack.getCount(), that.stack.getCount()) && Objects.equals(unlockPath, that.unlockPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, stack.getCount(), unlockPath);
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }
}
