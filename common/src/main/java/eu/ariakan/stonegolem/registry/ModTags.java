package eu.ariakan.stonegolem.registry;

import eu.ariakan.stonegolem.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

/**
 * All tags are data-driven; their contents live in {@code data/stonegolem/tags/...}.
 */
public final class ModTags {

    public static final class Items {
        /** Weapons that deal extra damage to the golem (pickaxes by default). */
        public static final TagKey<Item> EFFECTIVE_AGAINST_STONE_GOLEM = TagKey.create(Registries.ITEM, Constants.id("effective_against_stone_golem"));
        /** Weapons that deal reduced damage to the golem (swords by default). */
        public static final TagKey<Item> INEFFECTIVE_AGAINST_STONE_GOLEM = TagKey.create(Registries.ITEM, Constants.id("ineffective_against_stone_golem"));

        private Items() {
        }
    }

    public static final class DamageTypes {
        /** Damage types that deal reduced damage to the golem (projectiles by default). */
        public static final TagKey<DamageType> INEFFECTIVE_AGAINST_STONE_GOLEM = TagKey.create(Registries.DAMAGE_TYPE, Constants.id("ineffective_against_stone_golem"));

        private DamageTypes() {
        }
    }

    public static final class Blocks {
        /** Breaking one of these near a dormant golem wakes it up. */
        public static final TagKey<Block> WAKES_STONE_GOLEM = TagKey.create(Registries.BLOCK, Constants.id("wakes_stone_golem"));
        /** Natural spawns require one of these blocks below the golem. */
        public static final TagKey<Block> STONE_GOLEM_SPAWNABLE_ON = TagKey.create(Registries.BLOCK, Constants.id("stone_golem_spawnable_on"));
        /** Body and arms of a built golem (stone by default). */
        public static final TagKey<Block> STONE_GOLEM_BODY = TagKey.create(Registries.BLOCK, Constants.id("stone_golem_body"));
        /** Head of a built golem, placed last (block of diamond by default). */
        public static final TagKey<Block> STONE_GOLEM_HEAD = TagKey.create(Registries.BLOCK, Constants.id("stone_golem_head"));

        private Blocks() {
        }
    }

    public static final class Biomes {
        /** Biomes whose spawn list receives the golem (applied by the loader-specific biome modifiers). */
        public static final TagKey<Biome> SPAWNS_STONE_GOLEM = TagKey.create(Registries.BIOME, Constants.id("spawns_stone_golem"));
        /** Biomes where the golem may also spawn above ground; everywhere else it is restricted to caves. */
        public static final TagKey<Biome> STONE_GOLEM_SURFACE_SPAWNS = TagKey.create(Registries.BIOME, Constants.id("stone_golem_surface_spawns"));

        private Biomes() {
        }
    }

    private ModTags() {
    }
}
