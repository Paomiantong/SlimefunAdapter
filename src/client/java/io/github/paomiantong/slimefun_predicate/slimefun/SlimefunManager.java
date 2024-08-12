package io.github.paomiantong.slimefun_predicate.slimefun;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.paomiantong.slimefun_predicate.SlimefunPredicateClient;
import io.github.paomiantong.slimefun_predicate.utils.JsonUtils;
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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class SlimefunManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(SlimefunPredicateClient.class);
    private static final Gson gson = new Gson().newBuilder().setPrettyPrinting().create();
    private static final @Getter Map<String, SlimefunItemStack> slimefunItems = new LinkedHashMap<>();
    private static final @Getter Map<String, Set<SlimefunRecipe>> slimefunRecipes = new LinkedHashMap<>();
    private static final @Getter Map<String, SlimefunRecipeCategory> slimefunRecipeCategories = new LinkedHashMap<>();
    private static final @Getter Map<String, Set<SlimefunRecipe>> partialUnlockedRecipes = new LinkedHashMap<>();
    private static final @Getter Set<String> vanillaItems = new HashSet<>();
    private static final @Getter Set<String> completelyUnlockedCategory = new HashSet<>();
    private static @Getter boolean initialized = false;
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

    public static boolean hasSlimefunItem(String id) {
        return slimefunRecipes.containsKey(id) || vanillaItems.contains(id);
    }

    public static void addSlimefunItem(SlimefunItemStack item) {
        String id = item.getId();
        if (id.startsWith(":")) {
            vanillaItems.add(id);
            return;
        }
        slimefunItems.put(item.getId(), item);
    }

    public static void addSlimefunRecipe(SlimefunRecipe recipe) {
        String outputId = recipe.getOutput().getId();
        SlimefunItemStack type = recipe.getType();
        String id = type.isVanilla() ? type.getStack().getName().toString() : type.getId();
        if (!slimefunRecipeCategories.containsKey(id)) {
//            LOGGER.info("Adding recipe category: {} {}", name, type.getId());
            slimefunRecipeCategories.put(id, new SlimefunRecipeCategory(type, new HashSet<>()));
        }
        slimefunRecipeCategories.get(id).recipes().add(recipe);
        if (!slimefunRecipes.containsKey(outputId)) {
            slimefunRecipes.put(outputId, new HashSet<>());
        }
        slimefunRecipes.get(outputId).add(recipe);
    }

    public static void addPartialUnlockedRecipe(String unLockPath, SlimefunRecipe recipe) {
        if (!partialUnlockedRecipes.containsKey(unLockPath)) {
            partialUnlockedRecipes.put(unLockPath, new HashSet<>());
        }
        partialUnlockedRecipes.get(unLockPath).add(recipe);
    }

    public static void addCompletelyUnlockedCategory(String category) {
        completelyUnlockedCategory.add(category);
    }

    public static void unlockItem(String unLockPath, SlimefunItemStack item) {
        if (partialUnlockedRecipes.containsKey(unLockPath)) {
            Set<SlimefunRecipe> recipes = partialUnlockedRecipes.get(unLockPath);
            recipes.forEach(recipe -> {
                SlimefunItemStack[] inputs = recipe.getInputs();
                for (int i = 0; i < 9; i++) {
                    SlimefunItemStack input = inputs[i];
                    if (input.isLocked() && input.getUnlockPath().equals(unLockPath)) {
                        recipe.setInput(i, item);
                    }
                }
                LOGGER.info("Unlocked recipe: {}", recipe);
            });
            partialUnlockedRecipes.remove(unLockPath);
        }
    }

    public static SlimefunItemStack getSlimefunItem(String id) {
        SlimefunItemStack ret = slimefunItems.get(id);
        if (ret != null) {
            return ret;
        }

        if (id.startsWith("!")) {
            String[] tmp = id.split("!");
            return new SlimefunItemStack(new ItemStack(Registries.ITEM.get(Identifier.of(tmp[2]))), tmp[1]);
        }

        return new SlimefunItemStack(new ItemStack(Registries.ITEM.get(Identifier.of(id))));
    }

    public static boolean isCompletelyUnlocked(String category) {
        return completelyUnlockedCategory.contains(category);
    }

    public static void load() {
        clear();
        try {
            loadItems();
            loadRecipes();
            loadCompletelyUnlockedCategory();
            initialized = !slimefunItems.isEmpty();
        } catch (Exception e) {
            LOGGER.warn("Error loading slimefun data: " + e.getMessage());
        }
    }

    public static void clear() {
        slimefunItems.clear();
        slimefunRecipes.clear();
        slimefunRecipeCategories.clear();
        initialized = false;
    }

    public static void finalizeData() {
        var toDelete = slimefunRecipes.keySet().stream()
                .filter(id -> !slimefunItems.containsKey(id) && (!id.startsWith("minecraft")))
                .toList();
        toDelete.forEach(slimefunRecipes::remove);
        slimefunRecipeCategories.entrySet().forEach(entry -> {
            var category = entry.getValue();
            var type = category.type();
            if (!type.isVanilla() && slimefunItems.containsKey(type.getId())) {
                final var finalType = slimefunItems.get(type.getId());
                category.recipes().forEach(recipe -> {
                    recipe.setType(finalType);
                });
                entry.setValue(new SlimefunRecipeCategory(finalType, category.recipes()));
            }
        });
    }

    public static void save() {
        JsonObject itemJson = new JsonObject();
        slimefunItems.forEach((id, item) -> {
            itemJson.add(id, item.serialize());
            LOGGER.debug("Saving item: {} {}", item, id);
        });
        itemJson.add("vanilla", gson.toJsonTree(vanillaItems));
        writeStringToFile(CONFIG_PATH + "/slimefun_items.json", gson.toJson(itemJson));

        JsonObject recipeJson = new JsonObject();
        slimefunRecipes.forEach((id, recipe) -> {
            JsonArray recipes = new JsonArray();
            recipe.forEach(r -> recipes.add(r.serialize()));
            recipeJson.add(id, recipes);
        });
        writeStringToFile(CONFIG_PATH + "/slimefun_recipes.json", gson.toJson(recipeJson));

        JsonArray categories = new JsonArray();
        completelyUnlockedCategory.forEach(categories::add);
        writeStringToFile(CONFIG_PATH + "/completely_unlocked_category.json", gson.toJson(categories));
    }

    private static void loadItems() {
        final Path ITEM_JSON_PATH = Path.of(CONFIG_PATH + "/slimefun_items.json");
        if (Files.exists(ITEM_JSON_PATH)) {
            try {
                final JsonObject itemJson = gson.fromJson(Files.newBufferedReader(ITEM_JSON_PATH), JsonObject.class);
                itemJson.entrySet().forEach(entry -> {
                    if (entry.getKey().equals("vanilla")) {
                        JsonArray vanilla = entry.getValue().getAsJsonArray();
                        vanilla.forEach(id -> vanillaItems.add(id.getAsString()));
                        return;
                    }
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

    public static void loadCompletelyUnlockedCategory() {
        final Path CATEGORY_JSON_PATH = Path.of(CONFIG_PATH + "/completely_unlocked_category.json");
        if (Files.exists(CATEGORY_JSON_PATH)) {
            try {
                final JsonArray categories = gson.fromJson(Files.newBufferedReader(CATEGORY_JSON_PATH), JsonArray.class);
                categories.forEach(category -> completelyUnlockedCategory.add(category.getAsString()));
            } catch (IOException e) {
                LOGGER.warn("Error loading completely unlocked category: " + e.getMessage());
            }
        }
        LOGGER.info("Loaded {} completely unlocked category", completelyUnlockedCategory.size());
    }
}
