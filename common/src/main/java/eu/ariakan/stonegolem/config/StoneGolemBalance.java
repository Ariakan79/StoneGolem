package eu.ariakan.stonegolem.config;

/**
 * Every tunable gameplay number of the Stone Golem lives here, so balancing never requires searching the code base.
 * <p>
 * Durations are given in ticks (20 ticks = 1 second), distances in blocks.
 * Phase 2 will load these values from a config file; until then they are compile-time constants.
 */
public final class StoneGolemBalance {

    // --- Attributes -------------------------------------------------------------------------------------------------
    public static final double MAX_HEALTH = 80.0;
    public static final double ARMOR = 10.0;
    public static final double ATTACK_DAMAGE = 12.0;
    /** Zombie is 0.23; the golem is deliberately much slower. */
    public static final double MOVEMENT_SPEED = 0.17;
    public static final double KNOCKBACK_RESISTANCE = 1.0;
    public static final double FOLLOW_RANGE = 24.0;
    /** Extra knockback dealt by the melee punch (vanilla attribute, range 0-5). */
    public static final double ATTACK_KNOCKBACK = 2.0;
    /** Additional upward push applied by the melee punch, before the target's knockback resistance. */
    public static final double MELEE_LIFT = 0.35;
    public static final double STEP_HEIGHT = 1.0;
    public static final int EXPERIENCE = 10;

    // --- Dormancy ---------------------------------------------------------------------------------------------------
    /** Breaking a block tagged {@code stonegolem:wakes_stone_golem} within this radius wakes the golem. */
    public static final double WAKE_RADIUS = 4.0;
    /** Duration of the stand-up animation. The golem is invulnerable during it. */
    public static final int AWAKEN_TICKS = 25;
    /** Duration of the crouch-down animation when returning to dormancy. */
    public static final int SETTLE_TICKS = 20;
    /** Time without a target after which an active golem returns to dormancy. */
    public static final int RETURN_TO_DORMANT_TICKS = 30 * 20;

    // --- Ground stomp -----------------------------------------------------------------------------------------------
    public static final int STOMP_COOLDOWN_TICKS = 8 * 20;
    /** Telegraph time between the start of the wind-up and the impact. */
    public static final int STOMP_WINDUP_TICKS = 15;
    /** Recovery time after the impact before the golem acts again. */
    public static final int STOMP_RECOVERY_TICKS = 6;
    public static final double STOMP_RADIUS = 3.0;
    /** The golem only starts a stomp when its target is at most this far away. */
    public static final double STOMP_TRIGGER_RANGE = 3.5;
    public static final float STOMP_DAMAGE = 8.0F;
    public static final double STOMP_LAUNCH_VELOCITY = 0.55;
    public static final double STOMP_OUTWARD_VELOCITY = 0.35;

    // --- Weapon weakness (item and damage-type tags decide what counts as what) -------------------------------------
    /** Applies to weapons in {@code #stonegolem:effective_against_stone_golem} (pickaxes by default). */
    public static final float EFFECTIVE_WEAPON_MULTIPLIER = 2.0F;
    /** Applies to weapons in {@code #stonegolem:ineffective_against_stone_golem} (swords by default). */
    public static final float INEFFECTIVE_WEAPON_MULTIPLIER = 0.5F;
    /** Applies to damage types in {@code #stonegolem:ineffective_against_stone_golem} (projectiles by default). */
    public static final float INEFFECTIVE_DAMAGE_TYPE_MULTIPLIER = 0.5F;

    // --- Spawning ---------------------------------------------------------------------------------------------------
    /**
     * Weight within the biome's monster spawn list (zombies use 95). Can be overridden for testing with the
     * JVM argument {@code -Dstonegolem.spawnWeight=<n>}.
     */
    public static final int SPAWN_WEIGHT = 3;
    public static final int SPAWN_MIN_GROUP = 1;
    public static final int SPAWN_MAX_GROUP = 1;
    /** Below this Y level the golem may spawn in caves of any biome from {@code #stonegolem:spawns_stone_golem}. */
    public static final int UNDERGROUND_MAX_Y = 40;

    private StoneGolemBalance() {
    }

    public static int spawnWeight() {
        return Integer.getInteger("stonegolem.spawnWeight", SPAWN_WEIGHT);
    }
}
