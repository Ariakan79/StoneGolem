package eu.ariakan.stonegolem.registry;

import eu.ariakan.stonegolem.Constants;
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
                    .sized(1.4F, 1.9F)
                    .eyeHeight(1.45F)
                    .clientTrackingRange(10)
                    .build(STONE_GOLEM_KEY));

    private ModEntities() {
    }

    /** Forces class loading so the static registrations above run. */
    public static void init() {
    }
}
