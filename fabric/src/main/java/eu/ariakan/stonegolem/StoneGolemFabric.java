package eu.ariakan.stonegolem;

import eu.ariakan.stonegolem.config.StoneGolemBalance;
import eu.ariakan.stonegolem.entity.StoneGolem;
import eu.ariakan.stonegolem.entity.StoneGolemBuilding;
import eu.ariakan.stonegolem.event.StoneGolemEvents;
import eu.ariakan.stonegolem.registry.ModEntities;
import eu.ariakan.stonegolem.registry.ModItems;
import eu.ariakan.stonegolem.registry.ModTags;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.levelgen.Heightmap;

public class StoneGolemFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        StoneGolemMod.init();

        FabricDefaultAttributeRegistry.register(ModEntities.STONE_GOLEM.get(), StoneGolem.createAttributes());
        SpawnPlacements.register(ModEntities.STONE_GOLEM.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, StoneGolem::checkStoneGolemSpawnRules);
        BiomeModifications.addSpawn(BiomeSelectors.tag(ModTags.Biomes.SPAWNS_STONE_GOLEM), MobCategory.MONSTER,
                ModEntities.STONE_GOLEM.get(), StoneGolemBalance.spawnWeight(),
                StoneGolemBalance.SPAWN_MIN_GROUP, StoneGolemBalance.SPAWN_MAX_GROUP);

        PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) ->
                StoneGolemEvents.onBlockBroken(level, player, pos, state));
        // Fabric API has no "block placed" event: when a golem head block is used, check the spot right after the
        // placement has happened (next server task).
        UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            if (level instanceof ServerLevel serverLevel && player.getItemInHand(hand).getItem() instanceof BlockItem blockItem
                    && blockItem.getBlock().defaultBlockState().is(ModTags.Blocks.STONE_GOLEM_HEAD)) {
                MinecraftServer server = serverLevel.getServer();
                server.schedule(new TickTask(server.getTickCount(), () -> {
                    StoneGolemBuilding.onBlockPlaced(serverLevel, hit.getBlockPos().relative(hit.getDirection()));
                    StoneGolemBuilding.onBlockPlaced(serverLevel, hit.getBlockPos());
                }));
            }
            return InteractionResult.PASS;
        });

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.SPAWN_EGGS).register(output -> output.accept(ModItems.STONE_GOLEM_SPAWN_EGG.get()));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(output -> output.accept(ModItems.GOLEM_CORE.get()));
    }
}
