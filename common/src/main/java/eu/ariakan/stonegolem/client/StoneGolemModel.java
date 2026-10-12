package eu.ariakan.stonegolem.client;

import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * Vanilla-API model of the Stone Golem (64x64 texture). Part names match Blockbench conventions so the model can be
 * re-imported, edited and exported again: {@code body} carries {@code head}, {@code left_arm} and {@code right_arm};
 * the legs hang off the root.
 */
public class StoneGolemModel extends EntityModel<StoneGolemRenderState> {

    public static final String BODY = "body";
    public static final String HEAD = "head";
    public static final String LEFT_ARM = "left_arm";
    public static final String RIGHT_ARM = "right_arm";
    public static final String LEFT_LEG = "left_leg";
    public static final String RIGHT_LEG = "right_leg";

    private static final float MAX_HEAD_YAW = 35.0F;
    private static final float MAX_HEAD_PITCH = 25.0F;

    private final ModelPart head;
    private final KeyframeAnimation walkAnimation;
    private final KeyframeAnimation dormantAnimation;
    private final KeyframeAnimation awakenAnimation;
    private final KeyframeAnimation settleAnimation;
    private final KeyframeAnimation stompAnimation;
    private final KeyframeAnimation attackAnimation;

    public StoneGolemModel(ModelPart root) {
        super(root);
        this.head = root.getChild(BODY).getChild(HEAD);
        this.walkAnimation = StoneGolemAnimations.WALK.bake(root);
        this.dormantAnimation = StoneGolemAnimations.DORMANT.bake(root);
        this.awakenAnimation = StoneGolemAnimations.AWAKEN.bake(root);
        this.settleAnimation = StoneGolemAnimations.SETTLE.bake(root);
        this.stompAnimation = StoneGolemAnimations.STOMP.bake(root);
        this.attackAnimation = StoneGolemAnimations.ATTACK.bake(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition body = root.addOrReplaceChild(BODY, CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-10.0F, -13.0F, -5.5F, 20.0F, 13.0F, 11.0F)
                        // Moss cap on the shoulders.
                        .texOffs(0, 56).addBox(-6.0F, -15.0F, -2.0F, 12.0F, 2.0F, 6.0F),
                PartPose.offset(0.0F, 15.0F, 0.0F));

        body.addOrReplaceChild(HEAD, CubeListBuilder.create()
                        .texOffs(0, 24).addBox(-4.5F, -7.0F, -7.0F, 9.0F, 8.0F, 8.0F)
                        // Heavy brow ridge.
                        .texOffs(36, 56).addBox(-4.5F, -5.0F, -8.0F, 9.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, -11.0F, -5.0F));

        body.addOrReplaceChild(RIGHT_ARM, CubeListBuilder.create()
                        .texOffs(34, 24).addBox(-3.5F, -2.0F, -3.0F, 6.0F, 20.0F, 6.0F),
                PartPose.offset(-12.5F, -11.0F, 0.0F));
        body.addOrReplaceChild(LEFT_ARM, CubeListBuilder.create()
                        .texOffs(34, 24).mirror().addBox(-2.5F, -2.0F, -3.0F, 6.0F, 20.0F, 6.0F),
                PartPose.offset(12.5F, -11.0F, 0.0F));

        root.addOrReplaceChild(RIGHT_LEG, CubeListBuilder.create()
                        .texOffs(0, 40).addBox(-3.5F, 0.0F, -3.5F, 7.0F, 9.0F, 7.0F),
                PartPose.offset(-5.5F, 15.0F, 0.0F));
        root.addOrReplaceChild(LEFT_LEG, CubeListBuilder.create()
                        .texOffs(0, 40).mirror().addBox(-3.5F, 0.0F, -3.5F, 7.0F, 9.0F, 7.0F),
                PartPose.offset(5.5F, 15.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(StoneGolemRenderState state) {
        super.setupAnim(state);
        if (state.golemState.isAwake()) {
            this.head.yRot = Mth.clamp(state.yRot, -MAX_HEAD_YAW, MAX_HEAD_YAW) * Mth.DEG_TO_RAD;
            this.head.xRot = Mth.clamp(state.xRot, -MAX_HEAD_PITCH, MAX_HEAD_PITCH) * Mth.DEG_TO_RAD;
            this.walkAnimation.applyWalk(state.walkAnimationPos, state.walkAnimationSpeed, 2.3F, 2.5F);
        }
        this.dormantAnimation.apply(state.dormantAnimationState, state.ageInTicks);
        this.awakenAnimation.apply(state.awakenAnimationState, state.ageInTicks);
        this.settleAnimation.apply(state.settleAnimationState, state.ageInTicks);
        this.stompAnimation.apply(state.stompAnimationState, state.ageInTicks);
        this.attackAnimation.apply(state.attackAnimationState, state.ageInTicks);
    }
}
