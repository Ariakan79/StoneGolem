package eu.ariakan.stonegolem;

import eu.ariakan.stonegolem.platform.Services;

/**
 * Loader-independent entry point. Each loader calls {@link #init()} once during mod construction.
 */
public final class StoneGolemMod {

    private StoneGolemMod() {
    }

    public static void init() {
        Constants.LOG.info("Initializing {} on {}", Constants.MOD_NAME, Services.PLATFORM.getPlatformName());
    }
}
