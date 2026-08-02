package brachy.modularui.integration.recipeviewer.util;

import net.minecraft.client.Minecraft;

import brachy.modularui.ModularUIConfig;
import brachy.modularui.overlay.DebugOverlay;
import brachy.modularui.overlay.OverlayStack;
import brachy.modularui.screen.ModularScreen;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@UtilityClass
public class RecipeDebugDecoratorUtil {

    public static void addRecipeDebugOverlays(ModularScreen screen) {
        if (!ModularUIConfig.Dev.debugUI()) {
            return;
        }

        MutableObject<ModularScreen> debugOverlay = new MutableObject<>();
        Runnable close = () -> {
            ModularScreen overlay = debugOverlay.getValue();
            if (overlay != null) {
                OverlayStack.close(overlay);
            }
        };

        screen.getMainPanel()
                .onMouseEnterArea((ctx) -> {
                    ModularScreen overlay = new DebugOverlay(screen.getScreenWrapper());
                    overlay.constructOverlay(Minecraft.getInstance().screen);
                    OverlayStack.open(overlay);

                    debugOverlay.setValue(overlay);
                })
                .onMouseLeaveArea((ctx, timeBelowMouse) -> close.run())
                .onCloseAction(close);
    }
}
