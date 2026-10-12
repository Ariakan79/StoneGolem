package eu.ariakan.stonegolem.world;

import com.mojang.serialization.MapCodec;
import eu.ariakan.stonegolem.config.StoneGolemBalance;
import eu.ariakan.stonegolem.registry.ModEntities;
import eu.ariakan.stonegolem.registry.ModTags;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;

/**
 * Adds the Stone Golem to every biome in {@code #stonegolem:spawns_stone_golem}. Unlike the stock
 * {@code neoforge:add_spawns} modifier, the weight and group size come from {@link StoneGolemBalance}, so both loaders
 * share one source of truth. Referenced by {@code data/stonegolem/neoforge/biome_modifier/stone_golem_spawns.json}.
 */
public final class StoneGolemSpawnsBiomeModifier implements BiomeModifier {

    public static final StoneGolemSpawnsBiomeModifier INSTANCE = new StoneGolemSpawnsBiomeModifier();
    public static final MapCodec<StoneGolemSpawnsBiomeModifier> CODEC = MapCodec.unit(INSTANCE);

    private StoneGolemSpawnsBiomeModifier() {
    }

    @Override
    public void modify(RegistryAccess registries, Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase == Phase.ADD && biome.is(ModTags.Biomes.SPAWNS_STONE_GOLEM)) {
            builder.getMobSpawnSettings().addSpawn(ModEntities.STONE_GOLEM.get(), StoneGolemBalance.spawnWeight(),
                    StoneGolemBalance.SPAWN_MIN_GROUP, StoneGolemBalance.SPAWN_MAX_GROUP);
        }
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return CODEC;
    }
}
