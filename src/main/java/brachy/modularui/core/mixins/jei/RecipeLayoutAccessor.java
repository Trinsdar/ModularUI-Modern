package brachy.modularui.core.mixins.jei;

import mezz.jei.library.gui.ingredients.CycleTicker;
import mezz.jei.library.gui.recipes.RecipeLayout;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RecipeLayout.class)
public interface RecipeLayoutAccessor {

    @Accessor("cycleTicker")
    CycleTicker modularui$getCycleTicker();

}
