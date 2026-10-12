package eu.ariakan.stonegolem.client;

import eu.ariakan.stonegolem.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.renderer.entity.EntityRenderers;

public class StoneGolemFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ModelLayerRegistry.registerModelLayer(StoneGolemRenderer.LAYER, StoneGolemModel::createBodyLayer);
        EntityRenderers.register(ModEntities.STONE_GOLEM.get(), StoneGolemRenderer::new);
    }
}
