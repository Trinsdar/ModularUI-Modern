package brachy.modularui.integration.jei;

import brachy.modularui.core.mixins.jei.RecipeSlotAccessor;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.resources.ResourceLocation;

import brachy.modularui.screen.viewport.ModularGuiContext;

import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.Getter;
import lombok.Setter;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.gui.widgets.ISlottedRecipeWidget;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.library.gui.ingredients.ICycler;
import mezz.jei.library.gui.recipes.OutputSlotTooltipCallback;
import mezz.jei.library.ingredients.DisplayIngredientAcceptor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static brachy.modularui.integration.jei.ModularUIJeiPlugin.mapToJeiRole;

@ApiStatus.Internal
public class JeiRecipeViewerSlot<I, R> extends RecipeViewerSlotWidget<I, JeiRecipeViewerSlot<I, R>> implements ISlottedRecipeWidget {

    @Getter private @UnknownNullability IRecipeSlotDrawable slotWidget;
    @Setter private IFocusGroup focuses;
    @Setter private ICycler cycler;

    @Setter private IRecipeCategory<R> recipeCategory;
    @Setter private R recipe;

    @Setter private IIngredientManager ingredientManager;

    public JeiRecipeViewerSlot(Class<I> ingredientClass) {
        super(ingredientClass);

        size(18, 18);
    }

    public void setSlotWidget(IRecipeSlotDrawable slotWidget) {
        this.slotWidget = slotWidget;
        rebuildRealSlot();
    }

    /**
     * Instead of creating a new slot, the JEI implementation of {@code RecipeViewerSlotWidget} (this class) overwrites most of the {@linkplain mezz.jei.library.gui.ingredients.RecipeSlot JEI recipe slot's} values with new ones.<br>
     * It does this because JEI keeps track of all recipe slots & it's easier to replace existing slots' data than it is to replace the slots themselves.
     */
    @Override
    protected void rebuildRealSlot() {
        // only assign all of these values once the slot has been created from JEI's side.
        if (!(slotWidget instanceof RecipeSlotAccessor recipeSlot)) {
            // null check and cast in one!
            return;
        }

        // only update the slot's role if it's out of date
        RecipeIngredientRole jeiRole = mapToJeiRole(this.recipeSlotRole);
        if (slotWidget.getRole() != jeiRole) {
            // add/remove the output slot tooltip callback depending on if this is now an output slot or not
            if (this.recipeSlotRole == RecipeSlotRole.OUTPUT) {
                addOutputSlotTooltipCallback(recipeSlot);
            } else {
                recipeSlot.modularui$getTooltipCallbacks().removeIf(callback -> callback instanceof OutputSlotTooltipCallback);
            }
            recipeSlot.modularui$setRole(jeiRole);
        }
        slotWidget.setPosition(this.getArea().x, this.getArea().y);

        replaceSlotIngredients(recipeSlot);
        recipeSlot.modularui$setCycler(this.cycler);
    }

    @ApiStatus.Internal
    public void configureJeiSlotBuilder(IRecipeSlotBuilder builder) {
        if (this.entries != null && !this.entries.isEmpty()) {
            builder.addIngredientsUnsafe(this.entries.getStacks().stream()
                    .map(this.renderMappingFunction)
                    .toList());
        }
    }

    // Mostly copied from RecipeSlotBuilder#build
    private void replaceSlotIngredients(RecipeSlotAccessor recipeSlot) {
        if (this.entries == null || this.entries.isEmpty()) {
            recipeSlot.modularui$setAllIngredients(List.of());
            return;
        }

        final DisplayIngredientAcceptor ingredients = new DisplayIngredientAcceptor(this.ingredientManager);
        ingredients.addIngredientsUnsafe(this.entries.getStacks().stream()
                .map(this.renderMappingFunction)
                .toList());

        List<Optional<ITypedIngredient<?>>> allIngredients = ingredients.getAllIngredients();

        IntSet focusMatches = ingredients.getMatches(this.focuses, mapToJeiRole(this.recipeSlotRole));
        List<Optional<ITypedIngredient<?>>> focusedIngredients = null;

        if (!focusMatches.isEmpty()) {
            focusedIngredients = new ArrayList<>();
            for (Integer i : focusMatches) {
                if (i < allIngredients.size()) {
                    Optional<ITypedIngredient<?>> ingredient = allIngredients.get(i);
                    focusedIngredients.add(ingredient);
                }
            }
        }

        recipeSlot.modularui$setAllIngredients(allIngredients);
        recipeSlot.modularui$setDisplayIngredients(focusedIngredients);
    }

    @Override
    public void drawRealSlot(ModularGuiContext context) {
        if (slotWidget != null) {
            slotWidget.draw(context.getGraphics());
        }
    }
/*
    @Override
    public Result onMousePressed(int button) {
        return this.slotWidget.mouseClicked(getContext().getMouseX(), getContext().getMouseY(), button) ? Result.SUCCESS : Result.ACCEPT;
    }

    @Override
    public Result onKeyPressed(int keyCode, int scanCode, int modifiers) {
        return this.slotWidget.keyPressed(keyCode, scanCode, modifiers) ? Result.SUCCESS : Result.ACCEPT;
    }
*/
    @Override
    public Optional<RecipeSlotUnderMouse> getSlotUnderMouse(double mouseX, double mouseY) {
        if (isHovering() && slotWidget != null) {
            return Optional.of(new RecipeSlotUnderMouse(slotWidget, this.getPosition()));
        }
        return Optional.empty();
    }

    @Override
    public ScreenPosition getPosition() {
        return new ScreenPosition(getArea().x, getArea().y);
    }

    // disable JEI draw functionality
    @Override
    public void drawWidget(GuiGraphics guiGraphics, double mouseX, double mouseY) {}

    // copied from RecipeLayoutBuilder#addOutputSlotTooltipCallback
    private void addOutputSlotTooltipCallback(RecipeSlotAccessor slot) {
        ResourceLocation recipeName = recipeCategory.getRegistryName(recipe);
        if (recipeName != null) {
            RecipeType<R> recipeType = recipeCategory.getRecipeType();
            OutputSlotTooltipCallback callback = new OutputSlotTooltipCallback(recipeName, recipeType);
            slot.modularui$getTooltipCallbacks().add(callback);
        }
    }
}
