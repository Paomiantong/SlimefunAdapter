package io.github.paomiantong.slimefun_predicate.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.JsonOps;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class JsonUtils {
    private static final Gson gson = new Gson().newBuilder().setPrettyPrinting().create();

    public static JsonElement serializeText(Text text) {
        return TextCodecs.CODEC.encodeStart(JsonOps.INSTANCE, text).getOrThrow();
    }

    public static Text deserializeText(JsonElement jsonElement) {
        return TextCodecs.CODEC
                .decode(JsonOps.INSTANCE, jsonElement)
                .getOrThrow()
                .getFirst();
    }

    public static ProfileComponent deserializeProfile(JsonObject json) {
        return ProfileComponent.CODEC.decode(JsonOps.INSTANCE, json).getOrThrow().getFirst();
    }

    public static JsonElement serializeProfile(ProfileComponent profile) {
        return ProfileComponent.CODEC.encodeStart(JsonOps.INSTANCE, profile).getOrThrow();
    }

    public static JsonObject serializeItem(ItemStack itemStack) {
        final JsonObject json = new JsonObject();
        @Nullable final var nbt = itemStack.get(DataComponentTypes.CUSTOM_DATA);
        @Nullable final var lore = itemStack.get(DataComponentTypes.LORE);
        @Nullable final var name = itemStack.get(DataComponentTypes.CUSTOM_NAME);
        json.addProperty("item", Registries.ITEM.getId(itemStack.getItem()).toString());
        json.addProperty("amount", itemStack.getCount());
        if (nbt != null) {
            json.addProperty("nbt", nbt.copyNbt().toString());
        }
        if (lore != null) {
            JsonArray loreJson = new JsonArray();
            lore.lines().stream()
                    .map(JsonUtils::serializeText)
                    .forEach(loreJson::add);
            json.add("lore", loreJson);
        }
        if (name != null) {
            json.add("name", serializeText(name));
        }
        if (itemStack.getItem().equals(Registries.ITEM.get(Identifier.of("player_head")))) {
            @Nullable final ProfileComponent profile = itemStack.get(DataComponentTypes.PROFILE);
            if (profile != null)
                json.add("profile", serializeProfile(profile));
        }
        return json;
    }

    public static ItemStack deserializeItem(String string) {
        return deserializeItem(gson.fromJson(string, JsonObject.class));
    }

    public static ItemStack deserializeItem(JsonObject json) {
        if (json == null || json.isEmpty() || !json.has("item")) {
            return ItemStack.EMPTY;
        }

        final ItemStack itemStack = new ItemStack(Registries.ITEM.get(Identifier.of(json.get("item").getAsString())));
        itemStack.setCount(JsonHelper.getInt(json, "amount", 1));
        if (JsonHelper.hasString(json, "nbt")) {
            itemStack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(parseNbt(json)));
        }
        if (json.has("lore")) {
            JsonArray loreJson = json.getAsJsonArray("lore");
            List<Text> lore = new ArrayList<>();
            for (JsonElement element : loreJson) {
                lore.add(deserializeText(element));
            }
            itemStack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        }
        if (json.has("name")) {
            itemStack.set(DataComponentTypes.CUSTOM_NAME, deserializeText(json.get("name")));
        }
        if (itemStack.getItem()
                .equals(Registries.ITEM.get(Identifier.of("player_head")))
                && json.has("profile")) {
            itemStack.set(DataComponentTypes.PROFILE, deserializeProfile(json.getAsJsonObject("profile")));
        }

        return itemStack;
    }

    public static NbtCompound parseNbt(JsonObject json) {
        return parseNbt(JsonHelper.getString(json, "nbt"));
    }

    public static NbtCompound parseNbt(String nbt) {
        try {
            return StringNbtReader.parse(nbt);
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
