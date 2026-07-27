package brachy.modularui.integration.jei;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.integration.recipeviewer.entry.EntryList;

import brachy.modularui.widget.Widget;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public class JeiRecipeViewerSlot extends RecipeViewerSlotWidget<JeiRecipeViewerSlot> {
    @Accessors(fluent = true)
    @Getter
    @Setter
    private RecipeSlotRole recipeSlotRole;
    @Getter
    private EntryList<?> value;
    @Accessors(fluent = true)
    @Getter
    @Setter
    private float chance = 1f;

    public JeiRecipeViewerSlot() {
        recipeSlotRole = RecipeSlotRole.RENDER_ONLY;

        size(18, 18);
    }

    @Override
    public <T> JeiRecipeViewerSlot value(EntryList<T> entryList) {
        this.value = entryList;
        return getThis();
    }
}
