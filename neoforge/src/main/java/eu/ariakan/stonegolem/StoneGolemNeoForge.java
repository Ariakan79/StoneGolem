package eu.ariakan.stonegolem;

import eu.ariakan.stonegolem.entity.StoneGolem;
import eu.ariakan.stonegolem.event.StoneGolemEvents;
import eu.ariakan.stonegolem.gametest.StoneGolemNeoForgeGameTests;
import eu.ariakan.stonegolem.platform.NeoForgeRegistrar;
import eu.ariakan.stonegolem.platform.Services;
import eu.ariakan.stonegolem.registry.ModEntities;
import eu.ariakan.stonegolem.registry.ModItems;
import eu.ariakan.stonegolem.world.StoneGolemSpawnsBiomeModifier;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@Mod(Constants.MOD_ID)
public class StoneGolemNeoForge {

    public StoneGolemNeoForge(IEventBus modBus) {
        NeoForgeRegistrar.setModBus(modBus);
        StoneGolemMod.init();
        Services.REGISTRAR.register(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, "stone_golem_spawns",
                () -> StoneGolemSpawnsBiomeModifier.CODEC);
        StoneGolemNeoForgeGameTests.register(modBus);

        modBus.addListener(StoneGolemNeoForge::onAttributeCreation);
        modBus.addListener(StoneGolemNeoForge::onRegisterSpawnPlacements);
        modBus.addListener(StoneGolemNeoForge::onBuildCreativeTabs);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, StoneGolemNeoForge::onBlockBreak);
    }

    private static void onAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(ModEntities.STONE_GOLEM.get(), StoneGolem.createAttributes().build());
    }

    private static void onRegisterSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(ModEntities.STONE_GOLEM.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                StoneGolem::checkStoneGolemSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    private static void onBuildCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ModItems.STONE_GOLEM_SPAWN_EGG.get());
        } else if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.GOLEM_CORE.get());
        }
    }

    private static void onBlockBreak(BreakBlockEvent event) {
        if (!event.isCanceled() && event.getLevel() instanceof Level level) {
            StoneGolemEvents.onBlockBroken(level, event.getPlayer(), event.getPos(), event.getState());
        }
    }
}
