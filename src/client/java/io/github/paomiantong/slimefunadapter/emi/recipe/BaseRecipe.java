package io.github.paomiantong.slimefunadapter.emi.recipe;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import io.github.paomiantong.slimefunadapter.emi.SlimefunEmiCategory;
import io.github.paomiantong.slimefunadapter.slimefun.SlimefunRecipe;
import lombok.Getter;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static io.github.paomiantong.slimefunadapter.config.Config.SUPPORT_RECIPE_CATEGORY;

public class BaseRecipe implements EmiRecipe {
    private final Identifier id;
    private final List<EmiIngredient> input;
    private final List<EmiStack> output;
    private final EmiRecipeCategory category;

    @Getter
    private final String type;
    private final boolean supportRecipe;

    public BaseRecipe(SlimefunRecipe recipe, SlimefunEmiCategory category) {
        String path;
        if (recipe.getOutput().isVanilla()) {
            path = recipe.getOutput().getId().replace("minecraft:", "").toLowerCase(Locale.ROOT);
        } else {

            path = recipe.getOutput().getId().toLowerCase(Locale.ROOT);
        }
        path += "_" + recipe.hashCode() + "_" + category.getId().getPath();
        this.id = Identifier.of("slimefun", path);
        this.input = Arrays.stream(recipe.getInputs()).map(stack ->
                (EmiIngredient) EmiStack.of(stack.getStack())
        ).toList();
        this.output = List.of(EmiStack.of(recipe.getOutput().getStack()));
        this.category = category;
        this.type = recipe.getType().getId();
        this.supportRecipe = SUPPORT_RECIPE_CATEGORY.contains(recipe.getType().getId());
    }

    public boolean supportRecipe() {
        return this.supportRecipe;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return category;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return input;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return output;
    }

    @Override
    public int getDisplayWidth() {
        return 104;
    }

    @Override
    public int getDisplayHeight() {
        return 54;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        // Add an arrow texture to indicate processing
        widgets.addTexture(EmiTexture.EMPTY_ARROW, 36 + 21, 18);

        // Adds an input slot on the left
        widgets.addSlot(input.get(0), 0, 0);
        widgets.addSlot(input.get(1), 18, 0);
        widgets.addSlot(input.get(2), 36, 0);
        widgets.addSlot(input.get(3), 0, 18);
        widgets.addSlot(input.get(4), 18, 18);
        widgets.addSlot(input.get(5), 36, 18);
        widgets.addSlot(input.get(6), 0, 36);
        widgets.addSlot(input.get(7), 18, 36);
        widgets.addSlot(input.get(8), 36, 36);

        // Adds an output slot on the right
        // Note that output slots need to call `recipeContext` to inform EMI about their recipe context
        // This includes being able to resolve recipe trees, favorite stacks with recipe context, and more
        widgets.addSlot(output.get(0), 72 + 14, 18).recipeContext(this);
    }

}
