package io.github.paomiantong.slimefunadapter;

import io.github.paomiantong.slimefunadapter.config.Config;
import net.minecraft.client.item.ClampedModelPredicateProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class SFPredicateProvider implements ClampedModelPredicateProvider {
    @Override
    public float unclampedCall(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity, int seed) {
        @Nullable var nbtComp = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (nbtComp == null) {
            return 0;
        }
        var values = nbtComp.copyNbt().getCompound("PublicBukkitValues");
        String sf_id = values.getString("slimefun:slimefun_item");
        if (sf_id == null || sf_id.isEmpty()) {
            if (values.contains("slimefun:slimefun_guide_mode")) {
                return 0.001f;
            } else {
                return 0;
            }
        }
        return Config.getModel(sf_id);
    }
}
