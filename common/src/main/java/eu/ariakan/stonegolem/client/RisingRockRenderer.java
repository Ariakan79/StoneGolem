package eu.ariakan.stonegolem.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import eu.ariakan.stonegolem.Constants;
import eu.ariakan.stonegolem.entity.RisingRock;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/** Renders the rock sliding out of and back into the ground; the terrain hides the buried part. */
public class RisingRockRenderer extends EntityRenderer<RisingRock, RisingRockRenderState> {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Constants.id("rising_rock"), "main");
    private static final Identifier TEXTURE = Constants.id("textures/entity/rising_rock.png");
    private static final float MODEL_SCALE = 1.5F;

    private final RisingRockModel model;

    public RisingRockRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new RisingRockModel(context.bakeLayer(LAYER));
    }

    @Override
    public void submit(RisingRockRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.emergence > 0.0F) {
            poseStack.pushPose();
            poseStack.translate(0.0F, -(1.0F - state.emergence) * RisingRock.BASE_HEIGHT * state.scale, 0.0F);
            poseStack.rotateDegrees(Axis.YP, 180.0F - state.yRot);
            float scale = MODEL_SCALE * state.scale;
            poseStack.scale(-scale, -scale, scale);
            poseStack.translate(0.0F, -1.501F, 0.0F);
            collector.submitModel(this.model, state, poseStack, TEXTURE, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
            poseStack.popPose();
        }
        super.submit(state, poseStack, collector, camera);
    }

    @Override
    public RisingRockRenderState createRenderState() {
        return new RisingRockRenderState();
    }

    @Override
    public void extractRenderState(RisingRock entity, RisingRockRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.yRot = entity.getYRot();
        state.scale = entity.getScale();
        state.emergence = entity.getEmergence(partialTicks);
    }
}
