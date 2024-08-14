package io.github.paomiantong.slimefun_predicate.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

public class ConfigScreen {
    public static Screen build(Screen parent) {
        final ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("config.slimefun_predicate.title"));
        final ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        final ConfigCategory sync = builder.getOrCreateCategory(Text.translatable("config.slimefun_predicate.sync"));
        final ConfigCategory emi = builder.getOrCreateCategory(Text.translatable("config.slimefun_predicate.emi"));

        sync.addEntry(entryBuilder.startIntField(Text.translatable("config.slimefun_predicate.sync.ticks_per_action"), Config.TICKS_PER_ACTION)
                .setSaveConsumer(Config::setTICKS_PER_ACTION)
                .setDefaultValue(Config.TICKS_PER_ACTION)
                .build());

        sync.addEntry(entryBuilder.startStrList(Text.translatable("config.slimefun_predicate.sync.exclude"), Config.EXCLUDE.stream().toList())
                .setSaveConsumer(toSet(Config::setEXCLUDE))
                .setDefaultValue(Config.EXCLUDE.stream().toList())
                .build());

        sync.addEntry(entryBuilder.startStrList(Text.translatable("config.slimefun_predicate.sync.special"), Config.SPECIAL.stream().toList())
                .setSaveConsumer(toSet(Config::setSPECIAL))
                .setDefaultValue(Config.SPECIAL.stream().toList())
                .build());

        sync.addEntry(entryBuilder.startStrList(Text.translatable("config.slimefun_predicate.sync.exclude_workstation"), Config.EXCLUDE_WORKSTATION.stream().toList())
                .setSaveConsumer(toSet(Config::setEXCLUDE_WORKSTATION))
                .setDefaultValue(Config.EXCLUDE_WORKSTATION.stream().toList())
                .build());

        sync.addEntry(entryBuilder.startStrList(Text.translatable("config.slimefun_predicate.sync.menu_title"), Config.MENU_TITLE.stream().toList())
                .setSaveConsumer(toSet(Config::setMENU_TITLE))
                .setDefaultValue(Config.MENU_TITLE.stream().toList())
                .build());

        emi.addEntry(entryBuilder.startStrList(Text.translatable("config.slimefun_predicate.emi.support_recipe_category"), Config.SUPPORT_RECIPE_CATEGORY.stream().toList())
                .setSaveConsumer(toSet(Config::setSUPPORT_RECIPE_CATEGORY))
                .setDefaultValue(Config.SUPPORT_RECIPE_CATEGORY.stream().toList())
                .build());

        builder.setSavingRunnable(Config::saveConfig);
        return builder.build();
    }

    public static Consumer<List<String>> toSet(Consumer<HashSet<String>> setConsumer) {
        return strings -> setConsumer.accept(new HashSet<>(strings));
    }
}
