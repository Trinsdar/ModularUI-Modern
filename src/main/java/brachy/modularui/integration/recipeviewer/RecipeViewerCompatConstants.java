package brachy.modularui.integration.recipeviewer;

import org.jetbrains.annotations.ApiStatus;

import java.time.Duration;

/**
 * Constants for the recipe viewer compats' caches
 */
@ApiStatus.NonExtendable
public interface RecipeViewerCompatConstants {

    int EXPECTED_GOOD_CACHE_SIZE = 64;
    int GOOD_CACHE_INITIAL_SIZE = EXPECTED_GOOD_CACHE_SIZE / 8;
    Duration CACHE_EXPIRY_TIME = Duration.ofSeconds(60);
}
