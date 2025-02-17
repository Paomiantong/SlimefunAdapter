package io.github.paomiantong.slimefunadapter.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static io.github.paomiantong.slimefunadapter.SlimefunAdapterClient.tooltipKeyBinding;
import static io.github.paomiantong.slimefunadapter.utils.SlimefunUtils.getSlimefunID;
import static io.github.paomiantong.slimefunadapter.utils.SlimefunUtils.isSlimefunItem;

@Mixin(ItemStack.class)
public class MixinItemStack {
    @Inject(method = "getTooltip", at = @At("RETURN"), cancellable = true)
    protected void injectEditTooltipMethod(Item.TooltipContext context, PlayerEntity player, TooltipType type, CallbackInfoReturnable<List<Text>> info) {

        int code = InputUtil.fromTranslationKey(tooltipKeyBinding.getBoundKeyTranslationKey()).getCode();
        boolean isKeybindingPressed = InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), code);

        // If the advanced tooltips are on and the shift key is pressed the method is run.
        if (type.isAdvanced() && isKeybindingPressed == Boolean.TRUE) {
            // initialise the needed data
            ItemStack itemStack = (ItemStack) (Object) this;
            List<Text> list = info.getReturnValue();

            if (isSlimefunItem(itemStack)) {
                MutableText mutableText = Text.translatable("slimefun_predicate.tooltip").formatted(Formatting.GREEN, Formatting.BOLD);
                mutableText.append(Text.literal(getSlimefunID(itemStack)).formatted(Formatting.GRAY));
                list.add(mutableText);
                info.setReturnValue(list);
            }
        }
    }
}
