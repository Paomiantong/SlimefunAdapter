package io.github.paomiantong.slimefun_predicate.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public final class Config {
    public static final Logger LOGGER = LoggerFactory.getLogger(Slimefun_predicateClient.class);
    private static final Map<String, Double> item_models = new HashMap<>();

    private static final Path CONFIG_PATH = Paths.get("config/item-models-remap.yml");
    private static final String DEFAULT_CONFIG_PATH = "/remap.yml";

    public static float getModel(String sf_id) {
        return item_models.getOrDefault(sf_id, 0.).floatValue();
    }

    public static void load() throws FileNotFoundException {
        Yaml yaml = new Yaml();
        InputStream inputStream;


        if (Files.exists(CONFIG_PATH)) {
            LOGGER.info("Loading configuration from {}", CONFIG_PATH);
            inputStream = new FileInputStream(CONFIG_PATH.toFile());
        } else {
            // 从resources加载默认配置
            LOGGER.info("Loading default configuration from resources");
            inputStream = Config.class.getResourceAsStream(DEFAULT_CONFIG_PATH);
            if (inputStream == null) {
                throw new FileNotFoundException("Default configuration file not found in resources.");
            }
        }
        item_models.putAll(yaml.load(inputStream));
        LOGGER.info("Loaded {} item models", item_models.size());
    }

}
