package io.github.paomiantong.slimefun_predicate.client;

import net.minecraft.client.item.ClampedModelPredicateProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public final class SFPredicateProvider implements ClampedModelPredicateProvider {
//    public static final Logger LOGGER = LoggerFactory.getLogger(Slimefun_predicateClient.class);

    @Override
    public float unclampedCall(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity, int seed) {
        if (stack.getNbt() == null || !stack.getNbt().contains("PublicBukkitValues")) {
            return 0;
        }
        var values = stack.getNbt().getCompound("PublicBukkitValues");
        String sf_id = values.getString("slimefun:slimefun_item");
        if (sf_id == null || sf_id.isEmpty()) {
            if (values.contains("slimefun:slimefun_guide_mode")) {
                return 0.001f;
            } else {
                return 0;
            }
        }
//        float ret = Config.getModel(sf_id);
//        LOGGER.info("{}: {}", sf_id, ret);
        return Config.getModel(sf_id);
    }
}
