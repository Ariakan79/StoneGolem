package eu.ariakan.stonegolem.registry;

import eu.ariakan.stonegolem.Constants;
import eu.ariakan.stonegolem.entity.RisingRock;
import eu.ariakan.stonegolem.entity.StoneGolem;
import eu.ariakan.stonegolem.platform.Services;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.function.Supplier;

public final class ModEntities {

    public static final ResourceKey<EntityType<?>> STONE_GOLEM_KEY = ResourceKey.create(Registries.ENTITY_TYPE, Constants.id("stone_golem"));

    public static final Supplier<EntityType<StoneGolem>> STONE_GOLEM = Services.REGISTRAR.register(
            Registries.ENTITY_TYPE, STONE_GOLEM_KEY.identifier().getPath(),
            () -> EntityType.Builder.of(StoneGolem::new, MobCategory.MONSTER)
                    // Standing size: twice iron golem height. While dormant StoneGolem shrinks to boulder size.
                    .sized(1.8F, 5.4F)
                    .eyeHeight(4.6F)
                    .clientTrackingRange(10)
                    .build(STONE_GOLEM_KEY));

    public static final ResourceKey<EntityType<?>> RISING_ROCK_KEY = ResourceKey.create(Registries.ENTITY_TYPE, Constants.id("rising_rock"));

    public static final Supplier<EntityType<RisingRock>> RISING_ROCK = Services.REGISTRAR.register(
            Registries.ENTITY_TYPE, RISING_ROCK_KEY.identifier().getPath(),
            () -> EntityType.Builder.<RisingRock>of(RisingRock::new, MobCategory.MISC)
                    .sized(1.5F, 3.5F)
                    .noLootTable()
                    .noSave()
                    .fireImmune()
                    .clientTrackingRange(10)
                    .updateInterval(20)
                    .build(RISING_ROCK_KEY));

    private ModEntities() {
    }

    /** Forces class loading so the static registrations above run. */
    public static void init() {
    }
}
