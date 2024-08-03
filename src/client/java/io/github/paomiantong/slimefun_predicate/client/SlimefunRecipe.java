package io.github.paomiantong.slimefun_predicate.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.paomiantong.slimefun_predicate.client.utils.JsonUtils;

public record SlimefunRecipe(SlimefunItemStack output, SlimefunItemStack[] inputs, SlimefunItemStack type) {

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
        SlimefunItemStack output = ResourceLoader.getSlimefunItem(jsonRecipe.get("output").getAsString()).copy();
        output.setAmount(jsonRecipe.get("outputAmount").getAsInt());

        // 获取输入物品
        JsonArray jsonInputs = jsonRecipe.getAsJsonArray("inputs");
        SlimefunItemStack[] inputs = new SlimefunItemStack[jsonInputs.size()];
        for (int i = 0; i < jsonInputs.size(); i++) {
            JsonObject jsonInput = jsonInputs.get(i).getAsJsonObject();
            SlimefunItemStack input = ResourceLoader.getSlimefunItem(jsonInput.get("id").getAsString()).copy();
            input.setAmount(jsonInput.get("amount").getAsInt());
            inputs[i] = input;
        }

        return new SlimefunRecipe(output, inputs, type);
    }
}
