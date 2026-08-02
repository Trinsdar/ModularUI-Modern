package brachy.modularui.integration.rei;

import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.screen.viewport.ModularGuiContext;

import lombok.Getter;
import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class ReiRecipeViewerSlot extends RecipeViewerSlotWidget<ReiRecipeViewerSlot> {

    @Getter private Slot slotWidget;

    public ReiRecipeViewerSlot() {
        super();
        this.slotWidget = Widgets.createSlot(new Point()).disableBackground();

        size(18, 18);
    }

    @Override
    protected void rebuildRealSlot() {
        slotWidget = Widgets.createSlot(new Point()).disableBackground();
        slotWidget.entries(REIStackConverter.convertToReiEntry(this.entries, chance));
        if (recipeSlotRole == RecipeSlotRole.INPUT || recipeSlotRole == RecipeSlotRole.CATALYST) {
            slotWidget.markInput();
        } else if (recipeSlotRole == RecipeSlotRole.OUTPUT) {
            slotWidget.markOutput();
        } else {
            slotWidget.unmarkInputOrOutput();
        }
    }

    @Override
    public void drawRealSlot(ModularGuiContext context) {
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
