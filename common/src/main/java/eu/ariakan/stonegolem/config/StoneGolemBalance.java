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
    /** Ground slam (the regular melee attack): 12 = 6 hearts on Normal difficulty, before armor. */
    public static final double ATTACK_DAMAGE = 12.0;
    /** Zombie is 0.23; the golem is deliberately much slower. */
    public static final double MOVEMENT_SPEED = 0.17;
    public static final double KNOCKBACK_RESISTANCE = 1.0;
    public static final double FOLLOW_RANGE = 24.0;
    /** Extra knockback dealt by the melee punch (vanilla attribute, range 0-5). */
    public static final double ATTACK_KNOCKBACK = 2.0;
    /** Additional upward push applied by the ground slam, before the target's knockback resistance. */
    public static final double MELEE_LIFT = 0.35;
    /** Time from raising both fists to the impact of the ground slam; the hit lands only at impact. */
    public static final int SLAM_WINDUP_TICKS = 8;
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
    public static final double STOMP_TRIGGER_RANGE = 4.0;
    public static final float STOMP_DAMAGE = 8.0F;
    public static final double STOMP_LAUNCH_VELOCITY = 0.55;
    public static final double STOMP_OUTWARD_VELOCITY = 0.35;

    // --- Rock summon (one-handed ranged attack) --------------------------------------------------------------------
    public static final int SUMMON_COOLDOWN_TICKS = 10 * 20;
    /** Telegraph time: the golem raises a hand and the ground under the target crumbles before the rocks erupt. */
    public static final int SUMMON_WINDUP_TICKS = 18;
    public static final int SUMMON_RECOVERY_TICKS = 8;
    /** The golem only summons rocks against targets at least this far away (closer ones get slammed). */
    public static final double SUMMON_MIN_RANGE = 4.0;
    public static final double SUMMON_MAX_RANGE = 16.0;
    /** One large rock under the target plus this many smaller ones around it. */
    public static final int SUMMON_EXTRA_ROCKS = 2;
    /** Initial upward velocity of launched mobs: 1.19 reaches about 8 blocks with vanilla gravity and drag. */
    public static final double ROCK_LAUNCH_VELOCITY = 1.19;
    /**
     * Players simulate their own movement and apply one tick of gravity and drag before the first move, so they need a
     * little more for the same 8 blocks (measured: 1.19 gave 6.87 blocks; 1.289 is the exact compensation).
     */
    public static final double ROCK_LAUNCH_VELOCITY_PLAYER = 1.289;
    /** Rock life cycle: erupt, stand, then slowly sink back into the ground. */
    public static final int ROCK_RISE_TICKS = 4;
    public static final int ROCK_HOLD_TICKS = 6;
    public static final int ROCK_SINK_TICKS = 30;

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
