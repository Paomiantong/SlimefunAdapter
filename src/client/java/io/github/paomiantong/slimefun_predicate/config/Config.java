package io.github.paomiantong.slimefun_predicate.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.github.paomiantong.slimefun_predicate.SlimefunPredicateClient;
import io.github.paomiantong.slimefun_predicate.utils.ConfigUtils;
import lombok.Setter;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public final class Config {
    private static final Logger LOGGER = LoggerFactory.getLogger(SlimefunPredicateClient.class);
    private static final Map<String, Double> item_models = new HashMap<>();
    private static final Path CONFIG_PATH = Paths.get("config/slimefun-predicate/item-models-remap.yml");
    private static final String DEFAULT_CONFIG_PATH = "/remap.yml";

    @Configurable
    @Setter
    public static int TICKS_PER_ACTION = 1;

    @Configurable
    @Setter
    public static HashSet<String> EXCLUDE = new HashSet<>(List.of(
            "UU 物质合成表",
            "魔法水晶编年史",
            "幽灵方块",
            "攀岩镐"
    ));

    @Configurable
    @Setter
    public static HashSet<String> SPECIAL = new HashSet<>(List.of(
            "FILLED_FLASK_OF_KNOWLEDGE"
    ));

    @Configurable
    @Setter
    public static HashSet<String> EXCLUDE_WORKSTATION = new HashSet<>(List.of(
            "CULINARY_GENERATOR",
            "STARDUST_REACTOR",
            "SMART_FACTORY"
    ));

    @Configurable
    @Setter
    public static HashSet<String> SUPPORT_RECIPE_CATEGORY = new HashSet<>(List.of("MAGIC_WORKBENCH",
            "ENHANCED_CRAFTING_TABLE",
            "SMELTERY",
            "PRESSURE_CHAMBER",
            "ORE_CRUSHER",
            "ORE_WASHER",
            "COMPRESSOR",
            "JUICER",
            "GRIND_STONE",
            "ARMOR_FORGE",
            "METAL_FORGE",
            "REFINED_SMELTERY"
//            "ANCIENT_ALTAR",
    ));

    @Configurable
    @Setter
    public static HashSet<String> MENU_TITLE = new HashSet<>(List.of(
            "Slimefun 指南",
            "炼金术自传"
    ));

//    @Configurable
//    @Setter
//    public static HashSet<String> SERIES_WORKSTATION = new HashSet<>(List.of(
//            "FREEZER"
//    ));


    public static float getModel(String sf_id) {
        return item_models.getOrDefault(sf_id, 0.).floatValue();
    }

    public static void loadModel() {
        Yaml yaml = new Yaml();
        InputStream inputStream;


        if (Files.exists(CONFIG_PATH)) {
            LOGGER.info("Loading configuration from {}", CONFIG_PATH);
            try {
                inputStream = new FileInputStream(CONFIG_PATH.toFile());
            } catch (FileNotFoundException e) {
                LOGGER.error("Failed to load configuration: {}", e.getMessage());
                return;
            }
        } else {
            // 从resources加载默认配置
            LOGGER.info("Loading default configuration from resources");
            inputStream = Config.class.getResourceAsStream(DEFAULT_CONFIG_PATH);
        }
        item_models.putAll(yaml.load(inputStream));
        LOGGER.info("Loaded {} item models", item_models.size());
    }

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void loadConfig() {
        File configFile = getConfigFile();
        if (configFile == null) {
            return;
        }
        try (FileReader reader = new FileReader(configFile)) {
            var cls = Config.class;
            JsonObject config = gson.fromJson(reader, JsonObject.class);
            if (config == null) {
                saveConfig();
                return;
            }
            ConfigUtils.getConfigurableFields(cls).forEach(field -> {
                try {
                    var value = gson.fromJson(config.get(field.getName()), field.getType());
                    if (value == null) {
                        return;
                    }
                    LOGGER.info("{}: {}", field.getName(), config.get(field.getName()));
                    field.set(null, value);
                } catch (IllegalAccessException e) {
                    LOGGER.error("Failed to load configuration: {}", e.getMessage());
                }
            });
        } catch (IOException e) {
            LOGGER.error("Failed to load configuration: {}", e.getMessage());
        }
    }

    public static void saveConfig() {
        File configFile = getConfigFile();
        if (configFile == null) {
            LOGGER.error("Failed to save configuration: configuration file not found");
            return;
        }
        try (FileWriter writer = new FileWriter(configFile)) {
            JsonObject instance = new JsonObject();
            ConfigUtils.getConfigurableFields(Config.class).forEach(field -> {
                try {
                    instance.add(field.getName(), gson.toJsonTree(field.get(null)));
                } catch (IllegalAccessException e) {
                    LOGGER.error("Failed to save configuration: {}", e.getMessage());
                }
            });
            gson.toJson(instance, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to save configuration: {}", e.getMessage());
        }
    }

    public static File getConfigFile() {
        final File configFile = FabricLoader.getInstance().getConfigDir().resolve("slimefun_predicate.json").toFile();
        if (!configFile.exists()) {
            try {
                configFile.getParentFile().mkdirs();
                if (!configFile.createNewFile()) {
                    throw new IOException();
                }
            } catch (IOException | SecurityException e) {
                LOGGER.error("Failed to create configuration file: {}", e.getMessage());
                return null;
            }
        }
        return configFile;
    }
}
