package io.github.paomiantong.slimefun_predicate.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.paomiantong.slimefun_predicate.client.utils.JsonUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Objects;

public record SlimefunRecipe(SlimefunItemStack output, SlimefunItemStack[] inputs, SlimefunItemStack type) {
    public void setInput(int index, SlimefunItemStack input) {
        this.inputs[index] = input;
    }

    public JsonObject serialize() {
        JsonObject jsonRecipe = new JsonObject();

        // 添加类型
        jsonRecipe.add("type", JsonUtils.serializeItem(type.getStack()));

        // 添加输出物品的 ID
        jsonRecipe.addProperty("output", output.getId());

        // 添加输出物品的数量
        jsonRecipe.addProperty("outputAmount", output.getStack().getCount());

        // 创建并添加输入数组
        JsonArray jsonInputs = new JsonArray();
        for (SlimefunItemStack input : inputs) {
            JsonObject jsonInput = new JsonObject();
            jsonInput.addProperty("id", input.getId());
            jsonInput.addProperty("amount", input.getStack().getCount());
            jsonInputs.add(jsonInput);
        }
        jsonRecipe.add("inputs", jsonInputs);

        return jsonRecipe;
    }

    public static SlimefunRecipe deserialize(JsonObject jsonRecipe) {
        // 获取类型
        SlimefunItemStack type = new SlimefunItemStack(
                JsonUtils.deserializeItem(jsonRecipe.getAsJsonObject("type"))
        );

        // 获取输出物品
        SlimefunItemStack output = SlimefunManager.getSlimefunItem(jsonRecipe.get("output").getAsString()).copy();
        output.setAmount(jsonRecipe.get("outputAmount").getAsInt());

        // 获取输入物品
        JsonArray jsonInputs = jsonRecipe.getAsJsonArray("inputs");
        SlimefunItemStack[] inputs = new SlimefunItemStack[9];
        LinkedList<String> unlockPaths = new LinkedList<>();
        for (int i = 0; i < 9; i++) {
            JsonObject jsonInput = jsonInputs.get(i).getAsJsonObject();
            SlimefunItemStack input = SlimefunManager.getSlimefunItem(jsonInput.get("id").getAsString()).copy();
            input.setAmount(jsonInput.get("amount").getAsInt());
            inputs[i] = input;
            if (input.isLocked()) {
                unlockPaths.add(input.getUnlockPath());
            }
        }
        var recipe = new SlimefunRecipe(output, inputs, type);
        unlockPaths.forEach(unlockPath -> SlimefunManager.addPartialUnlockedRecipe(unlockPath, recipe));
        return recipe;
    }

    @Override
    public String toString() {
        return "SlimefunRecipe{" +
                "output=" + output +
                ", inputs=" + Arrays.toString(inputs) +
                ", type=" + type +
                '}';
    }
}
