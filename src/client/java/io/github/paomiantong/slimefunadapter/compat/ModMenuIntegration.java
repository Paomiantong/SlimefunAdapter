package io.github.paomiantong.slimefunadapter.compat;


import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.paomiantong.slimefunadapter.config.ConfigScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import static io.github.paomiantong.slimefunadapter.utils.CompatUtils.isClothConfigLoaded;

@Environment(EnvType.CLIENT)
public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {

        return parent -> isClothConfigLoaded() ? ConfigScreen.build(parent) : parent;
    }
}