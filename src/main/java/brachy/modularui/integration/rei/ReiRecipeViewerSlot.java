package brachy.modularui.integration.rei;

import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.screen.viewport.ModularGuiContext;

import brachy.modularui.theme.WidgetThemeEntry;

import lombok.Getter;
import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class ReiRecipeViewerSlot<I> extends RecipeViewerSlotWidget<I, ReiRecipeViewerSlot<I>> {

    @Getter private Slot slotWidget;

    public ReiRecipeViewerSlot(Class<I> ingredientClass) {
        super(ingredientClass);
        this.slotWidget = Widgets.createSlot(new Point()).disableBackground();

        size(18, 18);
    }

    @Override
    protected void rebuildRealSlot() {
        slotWidget = Widgets.createSlot(new Point()).disableBackground();
        slotWidget.entries(REIStackConverter.convertToReiEntry(this.entries, this.chance, this.renderMappingFunction));

        if (recipeSlotRole == RecipeSlotRole.INPUT || recipeSlotRole == RecipeSlotRole.CATALYST) {
            slotWidget.markInput();
        } else if (recipeSlotRole == RecipeSlotRole.OUTPUT) {
            slotWidget.markOutput();
        } else {
            slotWidget.unmarkInputOrOutput();
        }
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        this.slotWidget.render(context.getGraphics(), context.getMouseX(), context.getMouseY(), context.getRenderPartialTicks());
    }

    @Override
    public Result onMousePressed(int button) {
        return this.slotWidget.mouseClicked(getContext().getMouseX(), getContext().getMouseY(), button) ? Result.SUCCESS : Result.ACCEPT;
    }

    @Override
    public Result onKeyPressed(int keyCode, int scanCode, int modifiers) {
        return this.slotWidget.keyPressed(keyCode, scanCode, modifiers) ? Result.SUCCESS : Result.ACCEPT;
    }
}
