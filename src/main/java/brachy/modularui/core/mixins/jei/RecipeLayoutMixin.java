package brachy.modularui.core.mixins.jei;

import brachy.modularui.integration.jei.recipe.ModularUIRecipeCategory;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.library.gui.recipes.RecipeLayout;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = RecipeLayout.class, remap = false)
public abstract class RecipeLayoutMixin<R> implements IRecipeLayoutDrawable<R> {

    @ModifyExpressionValue(method = "<init>", at = @At(value = "INVOKE", target = "Lmezz/jei/api/recipe/category/IRecipeCategory;getWidth()I"), require = 0)
    private int modularui$makeWidthRecipeDependent(int original,
                                                   @Local(argsOnly = true) IRecipeCategory<R> recipeCategory,
                                                   @Local(argsOnly = true) R recipe) {
        if (recipeCategory instanceof ModularUIRecipeCategory<R> muiCategory) {
            return muiCategory.getWidth(recipe);
        }
        return original;
    }

    @ModifyExpressionValue(method = "<init>", at = @At(value = "INVOKE", target = "Lmezz/jei/api/recipe/category/IRecipeCategory;getHeight()I"), require = 0)
    private int modularui$makeHeightRecipeDependent(int original,
                                                    @Local(argsOnly = true) IRecipeCategory<R> recipeCategory,
                                                    @Local(argsOnly = true) R recipe) {
        if (recipeCategory instanceof ModularUIRecipeCategory<R> muiCategory) {
            return muiCategory.getHeight(recipe);
        }
        return original;
    }
}
