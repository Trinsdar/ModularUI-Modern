package brachy.modularui.integration.recipeviewer;

import brachy.modularui.api.widget.Interactable;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.integration.recipeviewer.entry.EntryList;
import brachy.modularui.integration.recipeviewer.entry.fluid.FluidStackList;
import brachy.modularui.integration.recipeviewer.entry.item.ItemStackList;
import brachy.modularui.integration.recipeviewer.handlers.RecipeViewerHandler;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.WidgetThemeEntry;
import brachy.modularui.widget.Widget;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public abstract class RecipeViewerSlotWidget<W extends RecipeViewerSlotWidget<W>> extends Widget<W> implements Interactable {

    @Accessors(fluent = true)
    @Getter @Setter protected float chance = 1f;

    @Accessors(fluent = true)
    @Getter protected RecipeSlotRole recipeSlotRole = RecipeSlotRole.RENDER_ONLY;
    protected EntryList<?> entries;

    public W recipeSlotRole(RecipeSlotRole recipeSlotRole) {
        this.recipeSlotRole = recipeSlotRole;
        rebuildRealSlot();
        return getThis();
    }

    public <T> W value(EntryList<T> entryList) {
        this.entries = entryList;
        rebuildRealSlot();
        if (this.entries.getType() == FluidStack.class) {
            background(GuiTextures.SLOT_FLUID);
        } else {
            background(GuiTextures.SLOT_ITEM); // TODO other types
        }
        return getThis();
    }

    public W value(ItemStack stack) {
        return value(ItemStackList.of(stack));
    }

    public W value(FluidStack stack) {
        return value(FluidStackList.of(stack));
    }

    protected abstract void rebuildRealSlot();

    protected abstract void drawRealSlot(ModularGuiContext context);

    @Override
    public final void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        context.graphicsPose().pushPose();
        context.getGraphics().pose().translate(-this.getArea().x, -this.getArea().y, 0);
        drawRealSlot(context);
        context.graphicsPose().popPose();
    }

    public static RecipeViewerSlotWidget<?> create() {
        return RecipeViewerHandler.getCurrent().createRecipeViewerSlot();
    }
}
