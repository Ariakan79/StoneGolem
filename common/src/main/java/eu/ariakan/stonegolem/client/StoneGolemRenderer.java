package eu.ariakan.stonegolem.client;

import com.mojang.blaze3d.vertex.PoseStack;
import eu.ariakan.stonegolem.Constants;
import eu.ariakan.stonegolem.entity.StoneGolem;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public class StoneGolemRenderer extends MobRenderer<StoneGolem, StoneGolemRenderState, StoneGolemModel> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Constants.id("stone_golem"), "main");
    private static final Identifier TEXTURE = Constants.id("textures/entity/stone_golem/stone_golem.png");
    /**
     * The 64x64 model is authored small (27 px tall); scaling keeps the texture budget and brings it to iron golem
     * height (2.7 blocks), matching the hitbox.
     */
    private static final float MODEL_SCALE = 1.6F;

    public StoneGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new StoneGolemModel(context.bakeLayer(LAYER)), 1.4F);
    }

    @Override
    public Identifier getTextureLocation(StoneGolemRenderState state) {
        return TEXTURE;
    }

    @Override
    public StoneGolemRenderState createRenderState() {
        return new StoneGolemRenderState();
    }

    @Override
    public void extractRenderState(StoneGolem entity, StoneGolemRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.golemState = entity.getState();
        state.dormantAnimationState.copyFrom(entity.dormantAnimationState);
        state.awakenAnimationState.copyFrom(entity.awakenAnimationState);
        state.settleAnimationState.copyFrom(entity.settleAnimationState);
        state.stompAnimationState.copyFrom(entity.stompAnimationState);
        state.attackAnimationState.copyFrom(entity.attackAnimationState);
        state.summonAnimationState.copyFrom(entity.summonAnimationState);
    }

    @Override
    protected void scale(StoneGolemRenderState state, PoseStack poseStack) {
        poseStack.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
    }
}
