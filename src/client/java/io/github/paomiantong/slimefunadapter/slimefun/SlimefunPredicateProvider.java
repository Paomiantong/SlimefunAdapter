package io.github.paomiantong.slimefunadapter.slimefun;

import io.github.paomiantong.slimefunadapter.config.Config;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.item.ModelPredicateProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@Slf4j(topic = "SlimefunAdapter")
public final class SlimefunPredicateProvider implements ModelPredicateProvider {
    private static final Map<String, Integer> item_models = new HashMap<>();

    @Override
    public float call(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity, int seed) {
        float ret = (float) stack.getOrDefault(DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelDataComponent.DEFAULT).value();
        if (ret != 0) {
            return ret;
        }
        @Nullable var nbtComp = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (nbtComp == null) {
            return 0;
        }
        var values = nbtComp.copyNbt().getCompound("PublicBukkitValues");
        String sf_id = values.getString("slimefun:slimefun_item");
        if (sf_id == null || sf_id.isEmpty()) {
            if (values.contains("slimefun:slimefun_guide_mode")) {
                return getModel("SLIMEFUN_GUIDE");
            }
            return 0;
        }
        return getModel(sf_id);
    }

    public static float getModel(String sf_id) {
        return item_models.getOrDefault(sf_id, 0);
    }

    public static void loadModel() {
        Yaml yaml = new Yaml();
        InputStream inputStream;


        if (Files.exists(Config.MODEL_DATA_PATH)) {
            log.info("Loading configuration from {}", Config.MODEL_DATA_PATH);
            try {
                inputStream = new FileInputStream(Config.MODEL_DATA_PATH.toFile());
            } catch (FileNotFoundException e) {
                log.error("Failed to load model: {}", e.getMessage());
                return;
            }
        } else {
            // 从resources加载默认配置
            log.info("Loading default configuration from resources");
            inputStream = Config.class.getResourceAsStream(Config.DEFAULT_MODEL_DATA_PATH);
        }
        item_models.putAll(yaml.load(inputStream));
        log.info("Loaded {} item models", item_models.size());
    }
}
