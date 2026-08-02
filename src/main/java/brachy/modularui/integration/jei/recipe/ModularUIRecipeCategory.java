package brachy.modularui.integration.jei.recipe;

import brachy.modularui.api.drawable.IRichTextBuilder;
import brachy.modularui.api.widget.ITooltip;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.core.mixins.jei.RecipeLayoutAccessor;
import brachy.modularui.core.mixins.jei.RecipeLayoutBuilderAccessor;
import brachy.modularui.drawable.text.RichText;
import brachy.modularui.integration.jei.JeiRecipeViewerSlot;
import brachy.modularui.integration.jei.ModularUIJeiPlugin;
import brachy.modularui.integration.recipeviewer.util.RecipeDebugDecoratorUtil;
import brachy.modularui.screen.EmbedHandler;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.ModularScreen;
import brachy.modularui.screen.RichTooltip;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.resources.ResourceLocation;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.inputs.IJeiGuiEventListener;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

@ApiStatus.Experimental
public abstract class ModularUIRecipeCategory<T> implements IRecipeCategory<T> {

    public static final String SCREEN_NAME_PREFIX = "jei_recipe_";

    private final LoadingCache<T, ModularScreen> modularScreenCache;

    private final Function<T, IWidget> recipeUI;
    private final Function<T, ResourceLocation> recipeIdGetter;

    private boolean sizeCalculated = false;
    private int displayWidth, displayHeight;

    protected ModularUIRecipeCategory(Function<T, IWidget> recipeUI, Function<T, ResourceLocation> recipeIdGetter) {
        this.recipeUI = recipeUI;
        this.recipeIdGetter = recipeIdGetter;

        this.modularScreenCache = CacheBuilder.newBuilder()
                .expireAfterAccess(Duration.ofSeconds(1))
                .maximumSize(20)
                .build(new CacheLoader<>() {

                    @Override
                    public ModularScreen load(T recipe) {
                        return ModularUIRecipeCategory.this.createScreen(recipe);
                    }
                });
    }

    private ModularScreen getModularScreen(T recipe) {
        return this.modularScreenCache.getUnchecked(recipe);
    }

    /**
     * Calculates the size of the recipe if not already done.<br>
     * This should be called in subclasses' {@link #createRecipeDisplay(IRecipeLayoutBuilder, Object, IFocusGroup) createRecipeDisplay} methods.
     * Otherwise, the size of ALL the recipes in the same category are calculated at once, which can make the game lag for a few seconds.
     */
    protected void calculateSize(T recipe) {
        if (this.sizeCalculated) return;
        this.sizeCalculated = true;
        IWidget ui = this.recipeUI.apply(recipe);
        ResourceLocation id = this.recipeIdGetter.apply(recipe);
        int w = ui.resizer().getFixedPixelWidth(), h = ui.resizer().getFixedPixelHeight();
        if (w < 0 || h < 0) {
            ModularScreen screen = createScreen(ui, id.getNamespace(), SCREEN_NAME_PREFIX + id.getPath());
            w = EmbedHandler.getEmbedWidth(screen);
            h = EmbedHandler.getEmbedHeight(screen);
        }
        this.displayWidth = w;
        this.displayHeight = h;
    }

    /**
     * Return the maximum expected display width here.<br>
     * You should also return a per-category display width that's at most this value in {@link #getWidth(Object)} if the default value
     * doesn't suit your needs.
     *
     * @return The maximum expected display width
     */
    public abstract int getMaxWidth();

    /**
     * Return the maximum expected display height here.<br>
     * You should also return a per-category display height that's at most this value in {@link #getHeight(Object)} if the default value
     * doesn't suit your needs.
     *
     * @return The maximum expected display height
     */
    public abstract int getMaxHeight();

    /**
     * Customize your recipe displays here.
     * <p>
     * This is used by JEI for lookups to figure out what ingredients are inputs and outputs for a recipe.
     */
    public abstract void createRecipeDisplay(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses);

    private ModularScreen createScreen(T recipe) {
        ResourceLocation id = this.recipeIdGetter.apply(recipe);
        return createScreen(this.recipeUI.apply(recipe), id.getNamespace(), SCREEN_NAME_PREFIX + id.getPath());
    }

    public ModularScreen createScreen(IWidget recipeUI, String owner, String name) {
        ModularPanel<?> panel;
        if (recipeUI instanceof ModularPanel<?> panel1) {
            panel = panel1;
        } else {
            panel = new ModularPanel<>(name);
            panel.coverChildren(60, 40)
                    .invisible()
                    .child(recipeUI);
        }
        ModularScreen screen = ModularScreen.createEmbed(owner, panel);
        screen.getContext().getUISettings().drawTooltipExternally(true);

        RecipeDebugDecoratorUtil.addRecipeDebugOverlays(screen);
        return screen;
    }

    @ApiStatus.OverrideOnly
    public IWidget transformWidget(IRecipeExtrasBuilder builder, IWidget widget) {
        if (!(widget instanceof JeiRecipeViewerSlot<?, ?> recipeViewerSlot)) return widget;

        if (builder instanceof RecipeLayoutAccessor accessor) {
            recipeViewerSlot.setCycler(accessor.modularui$getCycleTicker());
        }

        String name = recipeViewerSlot.getName();
        assert name != null; // the slots should always have a name assigned in createRecipeSlotForWidget()
        // the JEI slot should also always exist as it's created in the same method
        IRecipeSlotDrawable slot = builder.getRecipeSlots().findSlotByName(name).orElseThrow();

        recipeViewerSlot.setSlotWidget(slot);
        builder.addSlottedWidget(recipeViewerSlot, List.of(slot));

        return recipeViewerSlot;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @ApiStatus.OverrideOnly
    public IWidget createRecipeSlotForWidget(IRecipeLayoutBuilder builder, IWidget widget, T recipe, IFocusGroup focuses, MutableInt index) {
        // guard against JEMI issues
        if (!(builder instanceof RecipeLayoutBuilderAccessor recipeLayoutBuilder)) return widget;
        if (!(widget instanceof JeiRecipeViewerSlot recipeViewerSlot)) return widget;

        recipeViewerSlot.setIngredientManager(recipeLayoutBuilder.modularui$getIngredientManager());
        recipeViewerSlot.setRecipeCategory(this);
        recipeViewerSlot.setRecipe(recipe);
        recipeViewerSlot.setFocuses(focuses);
        if (recipeViewerSlot.getName() == null) {
            recipeViewerSlot.name("jei_slot_" + index.getAndIncrement());
        }

        IRecipeSlotBuilder slotBuilder = builder.addSlot(ModularUIJeiPlugin.mapToJeiRole(recipeViewerSlot.recipeSlotRole()))
                .setSlotName(recipeViewerSlot.getName());
        recipeViewerSlot.configureJeiSlotBuilder(slotBuilder);

        // always configure a fluid renderer (it's only used if a fluid is in the slot)
        slotBuilder.setFluidRenderer(1, false, 16, 16);

        return recipeViewerSlot;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses) {
        ModularScreen screen = getModularScreen(recipe);
        MutableInt index = new MutableInt(0);
        screen.getMainPanel().visitTransformAllChildren(widget -> createRecipeSlotForWidget(builder, widget, recipe, focuses, index));

        this.createRecipeDisplay(builder, recipe, focuses);
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, T recipe, IFocusGroup focuses) {
        ModularScreen screen = getModularScreen(recipe);

        screen.getMainPanel().visitTransformAllChildren(widget -> transformWidget(builder, widget));
        builder.addGuiEventListener(new ModularUIGuiEventListener(recipe));
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltipBuilder, T recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        ModularScreen screen = getModularScreen(recipe);
        if (!screen.getContext().getUISettings().drawTooltipExternally()) {
            IRecipeCategory.super.getTooltip(tooltipBuilder, recipe, recipeSlotsView, mouseX, mouseY);
            return;
        }

        IWidget hovered = screen.getContext().getTopHovered();
        if (hovered instanceof ITooltip<?> tooltip && tooltip.getTooltip() != null) {
            RichTooltip richTooltip = tooltip.getTooltip();
            if (richTooltip.autoUpdate()) richTooltip.markDirty();
            richTooltip.isEmpty(); // causes the tooltip to rebuild if necessary

            IRichTextBuilder<?> richTextBuilder = richTooltip.getRichText();
            if (richTextBuilder instanceof RichText richText) {
                for (var line : richText.getAsText()) {
                    // scuffed conversion, but it mostly works
                    line.ifLeft(tooltipBuilder::add).ifRight(tooltipBuilder::add);
                }
            }
        }
    }

    @Override
    public void draw(T recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        ModularScreen screen = getModularScreen(recipe);
        EmbedHandler.drawEmbed(screen, graphics, Minecraft.getInstance().getPartialTick());
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return null;
    }

    @Override
    public final int getHeight() {
        return getMaxHeight();
    }

    public int getHeight(T recipe) {
        calculateSize(recipe);
        return this.displayHeight;
    }

    @Override
    public final int getWidth() {
        return getMaxWidth();
    }

    public int getWidth(T recipe) {
        calculateSize(recipe);
        return this.displayWidth;
    }

    public class ModularUIGuiEventListener implements IJeiGuiEventListener {

        private final T recipe;

        public ModularUIGuiEventListener(T recipe) {
            this.recipe = recipe;
        }

        public ScreenRectangle getArea() {
            return getModularScreen(this.recipe).getMainRectangle();
        }

        @Override
        public void mouseMoved(double mouseX, double mouseY) {
            //getModularScreen(this.recipe).mouseMoved(mouseX, mouseY);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return getModularScreen(this.recipe).mousePressed(button);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            return getModularScreen(this.recipe).mouseReleased(button);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            return getModularScreen(this.recipe).mouseDragged(button, dragX, dragY);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
            return getModularScreen(this.recipe).mouseScrolled(scrollDelta);
        }

        @Override
        public boolean keyPressed(double mouseX, double mouseY, int keyCode, int scanCode, int modifiers) {
            return getModularScreen(this.recipe).keyPressed(keyCode, scanCode, modifiers);
        }
    }

}
