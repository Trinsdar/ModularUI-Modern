package brachy.modularui.integration.emi;

import brachy.modularui.ModularUI;
import brachy.modularui.integration.emi.handler.EmiScreenHandler;
import brachy.modularui.integration.emi.recipe.ModularUIEmiRecipe;
import brachy.modularui.screen.ContainerScreenWrapper;
import brachy.modularui.screen.ScreenWrapper;

import brachy.modularui.test.TestRecipeViewerGuis;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiInitRegistry;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;

@EmiEntrypoint
public class ModularUIEmiPlugin implements EmiPlugin {

    /// This is only used to clear the stale recipe display "screen" cache.
    @Override
    public void initialize(EmiInitRegistry registry) {
        ModularUIEmiRecipe.clearScreenCache();
    }

    @Override
    public void register(EmiRegistry registry) {
        EmiScreenHandler.register(ScreenWrapper.class, registry);
        EmiScreenHandler.register(ContainerScreenWrapper.class, registry);

        if (ModularUI.isDev()) {
            TestRecipeViewerGuis.EMI.register(registry);
        }
    }
}
