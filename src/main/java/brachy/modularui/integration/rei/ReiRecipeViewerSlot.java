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
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.UnaryOperator;

@ApiStatus.Experimental
public class ReiRecipeViewerSlot extends RecipeViewerSlotWidget<ReiRecipeViewerSlot> {

    @ApiStatus.Internal
    @Getter
    private Slot slotWidget;
    private int x, y;

    @Accessors(fluent = true)
    @Getter
    private RecipeSlotRole recipeSlotRole;
    @Getter
    private EntryList<?> value;
    @Accessors(fluent = true)
    @Getter
    @Setter
    private float chance = 1f;

    public ReiRecipeViewerSlot() {
        super();
        slotWidget = Widgets.createSlot(new Point()).disableBackground();
        recipeSlotRole = RecipeSlotRole.RENDER_ONLY;

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
        this.value = entryList;
        rebuildReiSlot();
        if (this.value.getType() == FluidStack.class) {
            background(GuiTextures.SLOT_FLUID);
        } else {
            background(GuiTextures.SLOT_ITEM); // TODO other types
        }
        return getThis();
    }

    @SuppressWarnings("unchecked")
    private void rebuildReiSlot() {
        slotWidget = Widgets.createSlot(new Point()).disableBackground();
        slotWidget.entries(convertToReiEntry(this.value, chance));
        if (recipeSlotRole == RecipeSlotRole.INPUT || recipeSlotRole == RecipeSlotRole.CATALYST) {
            slotWidget.markInput();
        } else if (recipeSlotRole == RecipeSlotRole.OUTPUT) {
            slotWidget.markOutput();
        } else {
            slotWidget.unmarkInputOrOutput();
        }
    }
    private static <T> EntryIngredient convertToReiEntry(EntryList<T> entries, float chance) {
        REIStackConverter.Converter<T> converter = REIStackConverter.getForNullable(entries.getType());
        if (converter != null){
            return converter.convertTo(entries, chance, UnaryOperator.identity());
        }
        return EntryIngredient.empty();
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        context.getGraphics().pose().pushPose();
        context.getGraphics().pose().translate(-this.x, -this.y, 0);
        this.slotWidget.render(context.getGraphics(), context.getMouseX(), context.getMouseY(), context.getRenderPartialTicks());
        context.getGraphics().pose().popPose();
    }

    @Override
    public Result onMousePressed(int button) {
        this.slotWidget.mouseClicked(getContext().getMouseX(), getContext().getAbsMouseY(), button);
        return Result.SUCCESS;
    }

    @Override
    public Result onKeyPressed(int keyCode, int scanCode, int modifiers) {
        return this.slotWidget.keyPressed(keyCode, scanCode, modifiers) ? Result.SUCCESS : Result.ACCEPT;
    }
}
