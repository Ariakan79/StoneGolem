package eu.ariakan.stonegolem;

import eu.ariakan.stonegolem.platform.Services;
import eu.ariakan.stonegolem.registry.ModEntities;
import eu.ariakan.stonegolem.registry.ModItems;
import eu.ariakan.stonegolem.registry.ModSounds;

/**
 * Loader-independent entry point. Each loader calls {@link #init()} once during mod construction.
 */
public final class StoneGolemMod {

    private StoneGolemMod() {
    }

    public static void init() {
        Constants.LOG.info("Initializing {} on {}", Constants.MOD_NAME, Services.PLATFORM.getPlatformName());
        // Order matters on loaders that register immediately: the spawn egg needs the entity type.
        ModSounds.init();
        ModEntities.init();
        ModItems.init();
    }
}
