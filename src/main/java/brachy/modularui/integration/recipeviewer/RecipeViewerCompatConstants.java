package brachy.modularui.integration.recipeviewer;

import org.jetbrains.annotations.ApiStatus;

import java.time.Duration;

/**
 * Constants for the recipe viewer compats' caches
 */
@ApiStatus.NonExtendable
public interface RecipeViewerCompatConstants {

    int EXPECTED_MAX_CATEGORY_RECIPES = 128;
    int EXPECTED_CATEGORIES_COUNT = 32;

    int EXPECTED_GOOD_CACHE_SIZE = EXPECTED_MAX_CATEGORY_RECIPES * EXPECTED_CATEGORIES_COUNT;
    int GOOD_CACHE_INITIAL_SIZE = EXPECTED_MAX_CATEGORY_RECIPES / 6;
    Duration CACHE_EXPIRY_TIME = Duration.ofSeconds(60);
}
