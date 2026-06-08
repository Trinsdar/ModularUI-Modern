package brachy.modularui.integration.recipeviewer.util;

import brachy.modularui.ModularUI;
import brachy.modularui.integration.emi.handler.EmiScreenHandler;
import brachy.modularui.integration.jei.handler.JeiScreenHandler;
import brachy.modularui.integration.rei.handler.REIScreenHandler;

public class RecipeViewerUtil {
    public static boolean isSearchFocused(){
        if (ModularUI.Mods.EMI.isLoaded()) {
            return EmiScreenHandler.isSearchFocused();
        }
        if (ModularUI.Mods.REI.isLoaded()){
            return REIScreenHandler.isSearchFocused();
        }
        if (ModularUI.Mods.JEI.isLoaded()){
            return JeiScreenHandler.isSearchFocused();
        }
        return false;
    }
}
