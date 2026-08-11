package brachy.modularui.core.mixins.jei;

import brachy.modularui.integration.jei.recipe.ModularUIJeiCategory;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryDecorator;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.library.gui.recipes.RecipeLayout;

import mezz.jei.library.gui.recipes.RecipeLayoutInputHandler;

import net.minecraft.client.gui.GuiGraphics;

import net.minecraft.client.gui.navigation.ScreenPosition;

import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.List;

@Mixin(value = RecipeLayout.class, remap = false)
public abstract class RecipeLayoutMixin<R> implements IRecipeLayoutDrawable<R> {

    @Shadow
    @Final
    private IRecipeCategory<R> recipeCategory;

    @Shadow
    @Final
    private List<IRecipeWidget> allWidgets;

    @Shadow private ImmutableRect2i area;

    @Inject(method = "drawOverlays", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V", shift = At.Shift.AFTER), cancellable = true)
    private void modularui$disableTooltipRender(GuiGraphics guiGraphics, int mouseX, int mouseY, CallbackInfo ci){
        if (this.recipeCategory instanceof ModularUIJeiCategory<R>){
            int recipeMouseX = mouseX - this.area.getX();
            int recipeMouseY = mouseY - this.area.getY();
            JeiTooltip tooltip = new JeiTooltip();
            for(IRecipeWidget widget : this.allWidgets) {
                ScreenPosition position = widget.getPosition();
                widget.getTooltip(tooltip, recipeMouseX - position.x(), recipeMouseY - position.y());
            }
            tooltip.draw(guiGraphics, mouseX, mouseY);
            ci.cancel();
        }
    }

    @ModifyExpressionValue(method = "<init>", at = @At(value = "INVOKE", target = "Lmezz/jei/api/recipe/category/IRecipeCategory;getWidth()I"), require = 0)
    private int modularui$makeWidthRecipeDependent(int original,
                                                   @Local(argsOnly = true) IRecipeCategory<R> recipeCategory,
                                                   @Local(argsOnly = true) R recipe) {
        if (recipeCategory instanceof ModularUIJeiCategory<R> muiCategory) {
            return muiCategory.getWidth(recipe);
        }
        return original;
    }

    @ModifyExpressionValue(method = "<init>", at = @At(value = "INVOKE", target = "Lmezz/jei/api/recipe/category/IRecipeCategory;getHeight()I"), require = 0)
    private int modularui$makeHeightRecipeDependent(int original,
                                                    @Local(argsOnly = true) IRecipeCategory<R> recipeCategory,
                                                    @Local(argsOnly = true) R recipe) {
        if (recipeCategory instanceof ModularUIJeiCategory<R> muiCategory) {
            return muiCategory.getHeight(recipe);
        }
        return original;
    }
}
