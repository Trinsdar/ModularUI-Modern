package brachy.modularui.integration.recipeviewer.util;

import brachy.modularui.ModularUI;
import brachy.modularui.integration.emi.ModularUIEmiPlugin;
import brachy.modularui.integration.jei.ModularUIJeiPlugin;
import brachy.modularui.integration.rei.ModularUIREIPlugin;

public class RecipeViewerUtil {
    public static boolean isSearchFocused(){
        if (ModularUI.Mods.EMI.isLoaded()) {
            return ModularUIEmiPlugin.isSearchFocused();
        }
        if (ModularUI.Mods.REI.isLoaded()){
            return ModularUIREIPlugin.isSearchFocused();
        }
        if (ModularUI.Mods.JEI.isLoaded()){
            return ModularUIJeiPlugin.isSearchFocused();
        }
        return false;
    }
}
