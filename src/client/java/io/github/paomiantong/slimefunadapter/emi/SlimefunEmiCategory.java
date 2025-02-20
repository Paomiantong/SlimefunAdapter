package io.github.paomiantong.slimefunadapter.emi;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Locale;

public class SlimefunEmiCategory extends EmiRecipeCategory {
    private final Text displayName;

    public SlimefunEmiCategory(EmiStack workstation) {
        super(createCategoryId(workstation), workstation);
        this.displayName = workstation.getItemStack().getName();
    }

    private static Identifier createCategoryId(EmiStack workstation) {
        // 从物品名称生成更可读的ID
        String name = workstation.getItemStack().getName().getString()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "_")  // 替换非法字符为下划线
                .replaceAll("_+", "_")         // 合并多个下划线
                .replaceAll("^_|_$", "");      // 移除首尾下划线
        return Identifier.of("slimefun", "category_" + name + workstation.hashCode());
    }

    @Override
    public Text getName() {
        return this.displayName;
    }
}
