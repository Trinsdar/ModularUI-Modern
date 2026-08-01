package brachy.modularui.integration.rei.recipe;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

@ApiStatus.Experimental
public abstract class ModularUIREIDisplayCategory<D extends ModularUIREIDisplay> implements DisplayCategory<D> {

    @Override
    public List<Widget> setupDisplay(D display, Rectangle bounds) {
        return display.createWidgets(bounds);
    }

    /**
     * Returns the display-dependent width.
     *
     * @param display the display
     * @return the display width
     */
    @Override
    public int getDisplayWidth(D display) {
        return display.getDisplayWidth();
    }

    /**
     * Return the maximum expected display height here.<br>
     * You should also return a per-category display height that's at most this value in {@link #getDisplayHeight(ModularUIREIDisplay)}.
     * @return The maximum expected display height
     */
    @ApiStatus.OverrideOnly
    @Override
    public abstract int getDisplayHeight();

    /**
     * Returns the display-dependent height.
     *
     * @param display the display
     * @return the display height
     */
    public int getDisplayHeight(D display) {
        return display.getDisplayHeight();
    }
}
