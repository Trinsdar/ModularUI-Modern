package brachy.modularui.integration.rei;

import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.integration.recipeviewer.entry.EntryList;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.WidgetThemeEntry;

import net.minecraftforge.fluids.FluidStack;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public class ReiRecipeViewerSlot extends RecipeViewerSlotWidget<ReiRecipeViewerSlot> {

    @ApiStatus.Internal
    @Getter
    private Slot slotWidget;

    @Accessors(fluent = true)
    @Getter
    private RecipeSlotRole recipeSlotRole = RecipeSlotRole.RENDER_ONLY;
    @Getter
    private EntryList<?> entryList;
    @Accessors(fluent = true)
    @Getter
    @Setter
    private float chance = 1f;

    public ReiRecipeViewerSlot() {
        super();
        slotWidget = Widgets.createSlot(new Point()).disableBackground();

        size(18, 18);
    }

    @Override
    public ReiRecipeViewerSlot recipeSlotRole(RecipeSlotRole recipeSlotRole) {
        this.recipeSlotRole = recipeSlotRole;
        rebuildReiSlot();
        return getThis();
    }

    @Override
    public <T> ReiRecipeViewerSlot value(EntryList<T> entryList) {
        this.entryList = entryList;
        rebuildReiSlot();
        if (this.entryList.getType() == FluidStack.class) {
            background(GuiTextures.SLOT_FLUID);
        } else {
            background(GuiTextures.SLOT_ITEM); // TODO other types
        }
        return getThis();
    }

    private void rebuildReiSlot() {
        slotWidget = Widgets.createSlot(new Point()).disableBackground();
        slotWidget.entries(REIStackConverter.convertToReiEntry(this.entryList, chance));
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
        context.getGraphics().pose().pushPose();
        context.getGraphics().pose().translate(-this.getArea().x, -this.getArea().y, 0);
        this.slotWidget.render(context.getGraphics(), context.getMouseX(), context.getMouseY(), context.getRenderPartialTicks());
        context.getGraphics().pose().popPose();
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
