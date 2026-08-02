package brachy.modularui.test;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

import brachy.modularui.ModularUI;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.integration.emi.recipe.ModularUIEmiRecipe;
import brachy.modularui.integration.jei.recipe.ModularUIRecipeCategory;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.handlers.IngredientProvider;
import brachy.modularui.integration.rei.recipe.ModularUIREIDisplay;
import brachy.modularui.integration.rei.recipe.ModularUIREIDisplayCategory;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.value.DoubleValue;
import brachy.modularui.widgets.slot.ItemSlot;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import lombok.Getter;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class TestRecipeViewerGuis {

    private static final IItemHandler EMPTY_INFINITE_ITEM_HANDLER = new EmptyHandler() {
        @Override
        public int getSlots() {
            return Integer.MAX_VALUE; // pls don't iterate UwU
        }
    };
    public static final Component CATEGORY_TITLE = Component.translatable("recipe_category.modularui.machine");

    public static IWidget buildViewerUI(TestMachine.Recipe recipe) {
        var panel = new ModularPanel<>("recipe_viewer_recipe")
                .coverChildren(60, 40)
                .invisible();
        IWidget recipeUI = TestMachine.Recipes.buildMachineUI(panel, EMPTY_INFINITE_ITEM_HANDLER, EMPTY_INFINITE_ITEM_HANDLER, DoubleValue.simulateProgress(5000));
        recipeUI.visitTransformAllChildren(w -> {
            if (w instanceof ItemSlot slot) {
                List<ItemStack> l = slot.getRecipeRole() == RecipeSlotRole.INPUT ? recipe.in : recipe.out;
                int index = slot.getSlot().getSlotIndex();
                ItemStack item = index >= l.size() ? ItemStack.EMPTY : l.get(index);
                return slot.toRecipeViewerSlot()
                        .value(item);
            } else if (w instanceof IngredientProvider<?> slot) {
                return slot.toRecipeViewerSlot();
            }
            return w;
        });
        return panel.child(recipeUI);
    }

    public static class EMI {

        public static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(ModularUI.id("machine"), EmiStack.of(TestRegistration.TEST_MACHINE_BLOCK_ITEM.get()));

        public static void register(EmiRegistry registry) {
            registry.addCategory(CATEGORY);
            TestMachine.Recipes.list.stream()
                    .map(r -> new RecipeDisplay(() -> buildViewerUI(r), r))
                    .forEach(registry::addRecipe);
        }

        public static class RecipeDisplay extends ModularUIEmiRecipe {

            private final TestMachine.Recipe recipe;
            @Getter private final List<EmiIngredient> inputs = new ArrayList<>();
            @Getter private final List<EmiStack> outputs = new ArrayList<>();

            public RecipeDisplay(Supplier<IWidget> widgetSupplier, TestMachine.Recipe recipe) {
                super(recipe.id, widgetSupplier);
                this.recipe = recipe;
                recipe.in.stream().map(EmiStack::of).forEach(inputs::add);
                recipe.out.stream().map(EmiStack::of).forEach(outputs::add);

                calculateSize();
            }

            @Override
            public EmiRecipeCategory getCategory() {
                return CATEGORY;
            }
        }
    }

    public static class JEI {

        public static final RecipeType<TestMachine.Recipe> RECIPE_TYPE = new RecipeType<>(ModularUI.id("machine"), TestMachine.Recipe.class);

        public static void registerCategory(IRecipeCategoryRegistration registry) {
            registry.addRecipeCategories(new RecipeCategory(TestRecipeViewerGuis::buildViewerUI, r -> r.id));
        }

        public static void registerRecipes(IRecipeRegistration registry) {
            registry.addRecipes(RECIPE_TYPE, TestMachine.Recipes.list);
        }

        public static void registerRecipeCatalyst(IRecipeCatalystRegistration registry) {
            registry.addRecipeCatalyst(TestRegistration.TEST_MACHINE_BLOCK_ITEM.get(), RECIPE_TYPE);
        }

        @NotNullByDefault
        public static class RecipeCategory extends ModularUIRecipeCategory<TestMachine.Recipe> {

            protected RecipeCategory(Function<TestMachine.Recipe, IWidget> recipeUI,
                                     Function<TestMachine.Recipe, ResourceLocation> recipeIdGetter) {
                super(recipeUI, recipeIdGetter);
            }

            @Override
            public RecipeType<TestMachine.Recipe> getRecipeType() {
                return RECIPE_TYPE;
            }

            @Override
            public Component getTitle() {
                return CATEGORY_TITLE;
            }

            @Override
            public int getMaxWidth() {
                return 112;
            }

            @Override
            public int getMaxHeight() {
                return 108;
            }

            @Override
            public void createRecipeDisplay(IRecipeLayoutBuilder builder, TestMachine.Recipe recipe, IFocusGroup focuses) {
                calculateSize(recipe);
            }
        }
    }

    public static class REI {

        public static final CategoryIdentifier<RecipeDisplay> CATEGORY = CategoryIdentifier.of(ModularUI.id("machine"));

        public static void registerCategory(CategoryRegistry registry) {
            registry.add(new RecipeCategory());
            registry.addWorkstations(CATEGORY, EntryStacks.of(TestRegistration.TEST_MACHINE_BLOCK_ITEM.get()));
        }

        public static void registerDisplay(DisplayRegistry registry) {
            TestMachine.Recipes.list.stream()
                    .map(r -> new RecipeDisplay(() -> buildViewerUI(r), r))
                    .forEach(registry::add);
        }

        public static class RecipeDisplay extends ModularUIREIDisplay {

            private final TestMachine.Recipe recipe;
            @Getter private final List<EntryIngredient> inputEntries = new ArrayList<>();
            @Getter private final List<EntryIngredient> outputEntries = new ArrayList<>();

            public RecipeDisplay(Supplier<IWidget> recipeUI, TestMachine.Recipe recipe) {
                super(recipe.id, recipeUI, CATEGORY);
                this.recipe = recipe;
                recipe.in.stream().map(EntryIngredients::of).forEach(inputEntries::add);
                recipe.out.stream().map(EntryIngredients::of).forEach(outputEntries::add);

                calculateSize();
            }
        }

        @NotNullByDefault
        public static class RecipeCategory extends ModularUIREIDisplayCategory<RecipeDisplay> {

            protected RecipeCategory() {
                super();
            }

            @Override
            public CategoryIdentifier<RecipeDisplay> getCategoryIdentifier() {
                return CATEGORY;
            }

            @Override
            public Component getTitle() {
                return CATEGORY_TITLE;
            }

            @Override
            public Renderer getIcon() {
                return EntryStacks.of(TestRegistration.TEST_MACHINE_BLOCK_ITEM.get());
            }

            @Override
            public int getMaxDisplayHeight() {
                return 108;
            }
        }
    }
}
