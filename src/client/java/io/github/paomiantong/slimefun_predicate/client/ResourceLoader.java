package io.github.paomiantong.slimefun_predicate.client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.paomiantong.slimefun_predicate.client.utils.JsonUtils;
import lombok.Getter;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class ResourceLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(SlimefunPredicateClient.class);
    private static final Gson gson = new Gson().newBuilder().setPrettyPrinting().create();
    private static final @Getter Map<String, SlimefunItemStack> slimefunItems = new LinkedHashMap<>();
    private static final @Getter Map<String, List<SlimefunRecipe>> slimefunRecipes = new LinkedHashMap<>();
    private static final @Getter Map<String, SlimefunRecipeCategory> slimefunRecipeCategories = new LinkedHashMap<>();
    private static final Path CONFIG_PATH = Paths.get("config/slimefun-predicate/");

    public static void writeStringToFile(String fileName, String content) {
        Path path = Paths.get(fileName);
        try {
            // 确保父目录存在
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            // 使用StandardCharsets.UTF_8确保字符编码的一致性
            Files.writeString(path, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

    public static void clear() {
        slimefunItems.clear();
        slimefunRecipes.clear();
        slimefunRecipeCategories.clear();
    }

    public static void addSlimefunItem(SlimefunItemStack item) {
        slimefunItems.put(item.getId(), item);
    }

    public static void addSlimefunRecipe(SlimefunRecipe recipe) {
        String outputId = recipe.output().getId();
        SlimefunItemStack type = recipe.type();
        String name = type.getStack().getName().getString();
        if (!slimefunRecipeCategories.containsKey(name)) {
            LOGGER.info("Adding recipe category: {} {}", name, type.getId());
            slimefunRecipeCategories.put(name, new SlimefunRecipeCategory(type, new ArrayList<>()));
        }
        slimefunRecipeCategories.get(name).recipes().add(recipe);
        if (!slimefunRecipes.containsKey(outputId)) {
            slimefunRecipes.put(outputId, new ArrayList<>());
        }
        slimefunRecipes.get(outputId).add(recipe);
    }

    public static void load() {
        clear();
        loadItems();
        loadRecipes();
    }

    public static void save() {
        JsonObject itemJson = new JsonObject();
        slimefunItems.forEach((id, item) -> {
            itemJson.add(id, item.serialize());
            System.out.println("Saved item: " + item);
        });
        writeStringToFile(CONFIG_PATH + "/slimefun_items.json", gson.toJson(itemJson));

        JsonObject recipeJson = new JsonObject();
        slimefunRecipes.forEach((id, recipe) -> {
            JsonArray recipes = new JsonArray();
            recipe.forEach(r -> recipes.add(r.serialize()));
            recipeJson.add(id, recipes);
        });
        writeStringToFile(CONFIG_PATH + "/slimefun_recipes.json", gson.toJson(recipeJson));
    }

    public static SlimefunItemStack getSlimefunItem(String id) {
        SlimefunItemStack ret = slimefunItems.get(id);
        if (ret != null) {
            return ret;
        }
        return new SlimefunItemStack(new ItemStack(Registries.ITEM.get(Identifier.of(id))));
    }

    private static void loadItems() {
        final Path ITEM_JSON_PATH = Path.of(CONFIG_PATH + "/slimefun_items.json");
        if (Files.exists(ITEM_JSON_PATH)) {
            try {
                final JsonObject itemJson = gson.fromJson(Files.newBufferedReader(ITEM_JSON_PATH), JsonObject.class);
                itemJson.entrySet().forEach(entry -> {
                    final SlimefunItemStack item = new SlimefunItemStack(JsonUtils.deserializeItem(entry.getValue().getAsJsonObject()));
                    slimefunItems.put(entry.getKey(), item);
                    //LOGGER.info("Loaded item: {} {}", item, entry.getKey());
                });
            } catch (IOException e) {
                LOGGER.warn("Error loading slimefun items: " + e.getMessage());
            }
        }
        LOGGER.info("Loaded {} slimefun items", slimefunItems.size());
    }

    private static void loadRecipes() {
        final Path RECIPE_JSON_PATH = Path.of(CONFIG_PATH + "/slimefun_recipes.json");
        if (Files.exists(RECIPE_JSON_PATH)) {
            try {
                final JsonObject recipeJson = gson.fromJson(Files.newBufferedReader(RECIPE_JSON_PATH), JsonObject.class);
                recipeJson.entrySet().forEach(entry -> {
                    JsonArray recipes = entry.getValue().getAsJsonArray();
                    recipes.forEach(recipe -> {
                        final SlimefunRecipe slimefunRecipe = SlimefunRecipe.deserialize(recipe.getAsJsonObject());
                        addSlimefunRecipe(slimefunRecipe);
                        //LOGGER.info("Loaded recipe: " + slimefunRecipe);
                    });
                });
            } catch (IOException e) {
                LOGGER.warn("Error loading slimefun recipes: " + e.getMessage());
            }
        }
        LOGGER.info("Loaded {} slimefun recipes", slimefunRecipes.size());
    }
}
