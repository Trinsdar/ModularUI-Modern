package brachy.modularui.integration.jei;

import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.integration.recipeviewer.entry.EntryList;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraftforge.fluids.FluidStack;

import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.WidgetThemeEntry;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.gui.widgets.ISlottedRecipeWidget;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.library.gui.ingredients.ICycler;
import mezz.jei.library.gui.recipes.layout.builder.RecipeSlotBuilder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Optional;

@ApiStatus.Experimental
public class JeiRecipeViewerSlot extends RecipeViewerSlotWidget<JeiRecipeViewerSlot> implements ISlottedRecipeWidget {

    @ApiStatus.Internal
    @Getter @Setter private @UnknownNullability IRecipeSlotDrawable slotWidget;
    @ApiStatus.Internal
    @Setter private IFocusGroup focuses;
    @ApiStatus.Internal
    @Setter private ICycler cycler;

    @Accessors(fluent = true)
    @Getter private RecipeSlotRole recipeSlotRole = RecipeSlotRole.RENDER_ONLY;
    @Getter private EntryList<?> value;
    @Accessors(fluent = true)
    @Getter @Setter private float chance = 1f;

    public JeiRecipeViewerSlot() {
        super();

        size(18, 18);
    }

    @Override
    public JeiRecipeViewerSlot recipeSlotRole(RecipeSlotRole recipeSlotRole) {
        this.recipeSlotRole = recipeSlotRole;
        rebuildJeiSlot();
        return getThis();
    }

    @Override
    public <T> JeiRecipeViewerSlot value(EntryList<T> entryList) {
        this.value = entryList;
        rebuildJeiSlot();
        if (this.value.getType() == FluidStack.class) {
            background(GuiTextures.SLOT_FLUID);
        } else {
            background(GuiTextures.SLOT_ITEM); // TODO other types
        }
        return getThis();
    }

    // TODO make sure this doesn't break everything (for example: search). It shouldn't but I'm not 100% on that.
    private void rebuildJeiSlot() {
        IIngredientManager ingredientManager = ModularUIJeiPlugin.getRuntime().getIngredientManager();

        // kinda meh solution. this isn't API, but I can't find a good way to do this within the API.
        RecipeSlotBuilder builder = new RecipeSlotBuilder(ingredientManager, 0, ModularUIJeiPlugin.mapToJeiRole(this.recipeSlotRole));
        JeiRecipeViewerSlot.addTypedIngredients(this.value, builder);
        builder.setPosition(this.getArea().x, this.getArea().y);

        if (this.value.getType() == FluidStack.class) {
            // special case fluid slots (this is why we can't have nice things.)
            builder.setFluidRenderer(1, false, 18, 18);
        }

        slotWidget = builder.build(this.focuses, this.cycler).second();
    }

    private static <T> void addTypedIngredients(EntryList<T> entries, RecipeSlotBuilder builder) {
        IIngredientManager ingredientManager = ModularUIJeiPlugin.getRuntime().getIngredientManager();
        var ingredientType = ingredientManager.getIngredientTypeChecked(entries.getType());
        if (ingredientType.isEmpty()) {
            return;
        }
        builder.addIngredients(ingredientType.get(), entries.getStacks());
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        context.graphicsPose().pushPose();
        context.getGraphics().pose().translate(-this.getArea().x, -this.getArea().y, 0);
        this.slotWidget.draw(context.getGraphics());
        context.graphicsPose().popPose();
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
        if (isHovering()) {
            return Optional.of(new RecipeSlotUnderMouse(this.slotWidget, this.getPosition()));
        }
        return Optional.empty();
    }

    @Override
    public ScreenPosition getPosition() {
        return new ScreenPosition(getArea().x, getArea().y);
    }

    @Override
    public void drawWidget(GuiGraphics guiGraphics, double mouseX, double mouseY) {}
}
