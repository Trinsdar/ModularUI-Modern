package brachy.modularui.integration.emi;

import brachy.modularui.drawable.ClientTooltipComponentIcon;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.integration.recipeviewer.entry.EntryList;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.WidgetThemeEntry;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.TankWidget;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.UnaryOperator;

@ApiStatus.Experimental
public class EmiRecipeViewerSlot extends RecipeViewerSlotWidget<EmiRecipeViewerSlot> {

    @ApiStatus.Internal
    @Getter
    private SlotWidget slotWidget;

    @Accessors(fluent = true)
    @Getter
    private RecipeSlotRole recipeSlotRole = RecipeSlotRole.RENDER_ONLY;
    private EntryList<?> value;
    @Accessors(fluent = true)
    @Getter
    @Setter
    private float chance = 1f;

    public EmiRecipeViewerSlot() {
        super();
        slotWidget = new SlotWidget(EmiIngredient.of(Ingredient.EMPTY), 0, 0);

        size(18, 18);

        tooltipAutoUpdate(true);
        tooltipDynamic(tooltip -> {
            for (ClientTooltipComponent ctc : this.slotWidget.getTooltip(getContext().getAbsMouseX(), getContext().getAbsMouseY())) {
                tooltip.addDrawableLine(new ClientTooltipComponentIcon(ctc));
            }
        });
    }

    @Override
    public EmiRecipeViewerSlot recipeSlotRole(RecipeSlotRole recipeSlotRole) {
        this.recipeSlotRole = recipeSlotRole;
        rebuildEmiSlot();
        return getThis();
    }

    @Override
    public <T> EmiRecipeViewerSlot value(EntryList<T> entryList) {
        this.value = entryList;
        rebuildEmiSlot();
        if (this.value.getType() == FluidStack.class) {
            background(GuiTextures.SLOT_FLUID);
        } else {
            background(GuiTextures.SLOT_ITEM); // TODO other types
        }
        return getThis();
    }

    @SuppressWarnings("unchecked")
    private void rebuildEmiSlot() {
        if (this.value.getType() == FluidStack.class) {
            // special case fluid slots
            EmiIngredient ingredient = EmiStackConverter.FLUID.convertTo((EntryList<FluidStack>) this.value, chance, UnaryOperator.identity());
            slotWidget = new TankWidget(ingredient, 0, 0, 18, 18, 1);
        } else {
            slotWidget = new SlotWidget(EmiStackConverter.convertToEmiEntry(this.value, chance), 0, 0);
        }
        slotWidget.drawBack(false);
        slotWidget.catalyst(recipeSlotRole == RecipeSlotRole.CATALYST);
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        context.graphicsPose().pushPose();
        context.getGraphics().pose().translate(-this.getArea().x, -this.getArea().y, 0);
        this.slotWidget.render(context.getGraphics(), context.getMouseX(), context.getMouseY(), context.getRenderPartialTicks());
        context.graphicsPose().popPose();
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
