package eu.ariakan.stonegolem.registry;

import eu.ariakan.stonegolem.Constants;
import eu.ariakan.stonegolem.platform.Services;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;

import java.util.function.Supplier;

/**
 * Own sound events, so the vanilla placeholder sounds referenced in {@code sounds.json} can be swapped later without
 * touching code.
 */
public final class ModSounds {

    public static final Supplier<SoundEvent> STONE_GOLEM_AMBIENT = register("entity.stone_golem.ambient");
    public static final Supplier<SoundEvent> STONE_GOLEM_HURT = register("entity.stone_golem.hurt");
    public static final Supplier<SoundEvent> STONE_GOLEM_DEATH = register("entity.stone_golem.death");
    public static final Supplier<SoundEvent> STONE_GOLEM_STEP = register("entity.stone_golem.step");
    public static final Supplier<SoundEvent> STONE_GOLEM_AWAKEN = register("entity.stone_golem.awaken");
    public static final Supplier<SoundEvent> STONE_GOLEM_SETTLE = register("entity.stone_golem.settle");
    public static final Supplier<SoundEvent> STONE_GOLEM_STOMP_WINDUP = register("entity.stone_golem.stomp_windup");
    public static final Supplier<SoundEvent> STONE_GOLEM_STOMP = register("entity.stone_golem.stomp");
    public static final Supplier<SoundEvent> STONE_GOLEM_SLAM = register("entity.stone_golem.slam");
    public static final Supplier<SoundEvent> STONE_GOLEM_SUMMON = register("entity.stone_golem.summon");
    public static final Supplier<SoundEvent> ROCK_ERUPT = register("entity.rising_rock.erupt");

    private ModSounds() {
    }

    private static Supplier<SoundEvent> register(String name) {
        return Services.REGISTRAR.register(Registries.SOUND_EVENT, name, () -> SoundEvent.createVariableRangeEvent(Constants.id(name)));
    }

    /** Forces class loading so the static registrations above run. */
    public static void init() {
    }
}
