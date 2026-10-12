package eu.ariakan.stonegolem.client;

import eu.ariakan.stonegolem.Constants;
import eu.ariakan.stonegolem.registry.ModEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@Mod(value = Constants.MOD_ID, dist = Dist.CLIENT)
public class StoneGolemNeoForgeClient {

    public StoneGolemNeoForgeClient(IEventBus modBus) {
        modBus.addListener(StoneGolemNeoForgeClient::onRegisterLayers);
        modBus.addListener(StoneGolemNeoForgeClient::onRegisterRenderers);
    }

    private static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(StoneGolemRenderer.LAYER, StoneGolemModel::createBodyLayer);
    }

    private static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.STONE_GOLEM.get(), StoneGolemRenderer::new);
    }
}
