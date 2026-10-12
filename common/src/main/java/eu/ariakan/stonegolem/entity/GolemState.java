package eu.ariakan.stonegolem.entity;

/**
 * Behaviour state of a {@link StoneGolem}. The server owns the state; it is synchronized to clients through entity data
 * and drives all animations there.
 */
public enum GolemState {
    /** Crouched like a mossy boulder: no movement, no AI, no sounds. */
    DORMANT,
    /** Standing up. Invulnerable for the duration. */
    AWAKENING,
    /** Normal behaviour: neutral, but hunts whoever attacked it. */
    ACTIVE,
    /** Telegraphed ground stomp: wind-up, impact and a short recovery. */
    STOMPING,
    /** Crouching back down into the dormant pose. */
    SETTLING;

    private static final GolemState[] VALUES = values();

    public static GolemState byId(int id) {
        return id >= 0 && id < VALUES.length ? VALUES[id] : DORMANT;
    }

    /** Whether the golem's regular AI (moving, looking around, targeting) may run in this state. */
    public boolean isAwake() {
        return this == ACTIVE || this == STOMPING;
    }
}
