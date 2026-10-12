package eu.ariakan.stonegolem.client;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;
import org.joml.Vector3f;

import static eu.ariakan.stonegolem.client.StoneGolemModel.BODY;
import static eu.ariakan.stonegolem.client.StoneGolemModel.HEAD;
import static eu.ariakan.stonegolem.client.StoneGolemModel.LEFT_ARM;
import static eu.ariakan.stonegolem.client.StoneGolemModel.LEFT_LEG;
import static eu.ariakan.stonegolem.client.StoneGolemModel.RIGHT_ARM;
import static eu.ariakan.stonegolem.client.StoneGolemModel.RIGHT_LEG;

/**
 * Keyframe animations in the vanilla format that Blockbench exports for Java entity models. Values are offsets from
 * the model's rest pose: rotations in degrees, positions in pixels (positive Y is up, as in Blockbench).
 * <p>
 * Timings mirror {@link eu.ariakan.stonegolem.config.StoneGolemBalance}: awaken 1.25 s, settle 1.0 s, stomp wind-up
 * 0.75 s + 0.3 s recovery, slam impact at 0.4 s, summon wind-up 0.9 s + 0.4 s recovery.
 */
public final class StoneGolemAnimations {

    // The crouched boulder pose, shared by the dormant, awaken and settle animations.
    private static final float[] DORMANT_BODY_POS = {0.0F, -8.0F, 0.0F};
    private static final float[] DORMANT_BODY_ROT = {15.0F, 0.0F, 0.0F};
    private static final float[] DORMANT_HEAD_POS = {0.0F, -1.0F, 1.0F};
    private static final float[] DORMANT_HEAD_ROT = {30.0F, 0.0F, 0.0F};
    private static final float[] DORMANT_RIGHT_ARM_ROT = {-55.0F, 0.0F, -10.0F};
    private static final float[] DORMANT_LEFT_ARM_ROT = {-55.0F, 0.0F, 10.0F};

    public static final AnimationDefinition WALK = AnimationDefinition.Builder.withLength(1.5F).looping()
            .addAnimation(RIGHT_LEG, rotation(
                    key(0.0F, 22.5F, 0.0F, 0.0F), key(0.75F, -22.5F, 0.0F, 0.0F), key(1.5F, 22.5F, 0.0F, 0.0F)))
            .addAnimation(LEFT_LEG, rotation(
                    key(0.0F, -22.5F, 0.0F, 0.0F), key(0.75F, 22.5F, 0.0F, 0.0F), key(1.5F, -22.5F, 0.0F, 0.0F)))
            .addAnimation(RIGHT_ARM, rotation(
                    key(0.0F, -17.5F, 0.0F, 0.0F), key(0.75F, 17.5F, 0.0F, 0.0F), key(1.5F, -17.5F, 0.0F, 0.0F)))
            .addAnimation(LEFT_ARM, rotation(
                    key(0.0F, 17.5F, 0.0F, 0.0F), key(0.75F, -17.5F, 0.0F, 0.0F), key(1.5F, 17.5F, 0.0F, 0.0F)))
            .addAnimation(BODY, rotation(
                    key(0.0F, 0.0F, 0.0F, 3.0F), key(0.75F, 0.0F, 0.0F, -3.0F), key(1.5F, 0.0F, 0.0F, 3.0F)))
            .build();

    /** Held while dormant: a hunched, mossy boulder. */
    public static final AnimationDefinition DORMANT = AnimationDefinition.Builder.withLength(1.0F).looping()
            .addAnimation(BODY, position(hold(DORMANT_BODY_POS, 0.0F), hold(DORMANT_BODY_POS, 1.0F)))
            .addAnimation(BODY, rotation(hold(DORMANT_BODY_ROT, 0.0F), hold(DORMANT_BODY_ROT, 1.0F)))
            .addAnimation(HEAD, position(hold(DORMANT_HEAD_POS, 0.0F), hold(DORMANT_HEAD_POS, 1.0F)))
            .addAnimation(HEAD, rotation(hold(DORMANT_HEAD_ROT, 0.0F), hold(DORMANT_HEAD_ROT, 1.0F)))
            .addAnimation(RIGHT_ARM, rotation(hold(DORMANT_RIGHT_ARM_ROT, 0.0F), hold(DORMANT_RIGHT_ARM_ROT, 1.0F)))
            .addAnimation(LEFT_ARM, rotation(hold(DORMANT_LEFT_ARM_ROT, 0.0F), hold(DORMANT_LEFT_ARM_ROT, 1.0F)))
            .build();

    /** Dormant pose to standing: a shudder, a heave, a slight overshoot. */
    public static final AnimationDefinition AWAKEN = AnimationDefinition.Builder.withLength(1.25F)
            .addAnimation(BODY, position(
                    hold(DORMANT_BODY_POS, 0.0F),
                    hold(DORMANT_BODY_POS, 0.4F),
                    key(0.8F, 0.0F, -3.0F, 0.0F),
                    key(1.05F, 0.0F, 1.0F, 0.0F),
                    key(1.25F, 0.0F, 0.0F, 0.0F)))
            .addAnimation(BODY, rotation(
                    hold(DORMANT_BODY_ROT, 0.0F),
                    key(0.15F, 15.0F, 0.0F, 3.0F),
                    key(0.3F, 15.0F, 0.0F, -3.0F),
                    key(0.4F, 15.0F, 0.0F, 0.0F),
                    key(1.05F, -5.0F, 0.0F, 0.0F),
                    key(1.25F, 0.0F, 0.0F, 0.0F)))
            .addAnimation(HEAD, position(
                    hold(DORMANT_HEAD_POS, 0.0F), hold(DORMANT_HEAD_POS, 0.5F), key(1.0F, 0.0F, 0.0F, 0.0F)))
            .addAnimation(HEAD, rotation(
                    hold(DORMANT_HEAD_ROT, 0.0F), hold(DORMANT_HEAD_ROT, 0.6F), key(1.05F, -10.0F, 0.0F, 0.0F), key(1.25F, 0.0F, 0.0F, 0.0F)))
            .addAnimation(RIGHT_ARM, rotation(
                    hold(DORMANT_RIGHT_ARM_ROT, 0.0F), hold(DORMANT_RIGHT_ARM_ROT, 0.4F), key(1.0F, -20.0F, 0.0F, 25.0F), key(1.25F, 0.0F, 0.0F, 0.0F)))
            .addAnimation(LEFT_ARM, rotation(
                    hold(DORMANT_LEFT_ARM_ROT, 0.0F), hold(DORMANT_LEFT_ARM_ROT, 0.4F), key(1.0F, -20.0F, 0.0F, -25.0F), key(1.25F, 0.0F, 0.0F, 0.0F)))
            .build();

    /** Standing to dormant pose. Ends exactly in the pose held by {@link #DORMANT}. */
    public static final AnimationDefinition SETTLE = AnimationDefinition.Builder.withLength(1.0F)
            .addAnimation(BODY, position(key(0.0F, 0.0F, 0.0F, 0.0F), key(0.6F, 0.0F, -5.0F, 0.0F), hold(DORMANT_BODY_POS, 1.0F)))
            .addAnimation(BODY, rotation(key(0.0F, 0.0F, 0.0F, 0.0F), key(0.6F, 20.0F, 0.0F, 0.0F), hold(DORMANT_BODY_ROT, 1.0F)))
            .addAnimation(HEAD, position(key(0.0F, 0.0F, 0.0F, 0.0F), hold(DORMANT_HEAD_POS, 1.0F)))
            .addAnimation(HEAD, rotation(key(0.0F, 0.0F, 0.0F, 0.0F), hold(DORMANT_HEAD_ROT, 1.0F)))
            .addAnimation(RIGHT_ARM, rotation(key(0.0F, 0.0F, 0.0F, 0.0F), hold(DORMANT_RIGHT_ARM_ROT, 1.0F)))
            .addAnimation(LEFT_ARM, rotation(key(0.0F, 0.0F, 0.0F, 0.0F), hold(DORMANT_LEFT_ARM_ROT, 1.0F)))
            .build();

    /** Ground stomp: lean back with both arms raised (telegraph), slam at 0.75 s, recover. */
    public static final AnimationDefinition STOMP = AnimationDefinition.Builder.withLength(1.05F)
            .addAnimation(BODY, position(
                    key(0.0F, 0.0F, 0.0F, 0.0F), key(0.6F, 0.0F, 2.0F, 0.0F), key(0.75F, 0.0F, -3.0F, 0.0F),
                    key(0.85F, 0.0F, -3.0F, 0.0F), key(1.05F, 0.0F, 0.0F, 0.0F)))
            .addAnimation(BODY, rotation(
                    key(0.0F, 0.0F, 0.0F, 0.0F), key(0.6F, -12.5F, 0.0F, 0.0F), key(0.75F, 25.0F, 0.0F, 0.0F),
                    key(0.85F, 25.0F, 0.0F, 0.0F), key(1.05F, 0.0F, 0.0F, 0.0F)))
            .addAnimation(RIGHT_ARM, rotation(
                    key(0.0F, 0.0F, 0.0F, 0.0F), key(0.6F, -165.0F, 0.0F, -10.0F), key(0.75F, -20.0F, 0.0F, 0.0F),
                    key(0.85F, -20.0F, 0.0F, 0.0F), key(1.05F, 0.0F, 0.0F, 0.0F)))
            .addAnimation(LEFT_ARM, rotation(
                    key(0.0F, 0.0F, 0.0F, 0.0F), key(0.6F, -165.0F, 0.0F, 10.0F), key(0.75F, -20.0F, 0.0F, 0.0F),
                    key(0.85F, -20.0F, 0.0F, 0.0F), key(1.05F, 0.0F, 0.0F, 0.0F)))
            .addAnimation(HEAD, rotation(
                    key(0.0F, 0.0F, 0.0F, 0.0F), key(0.6F, -15.0F, 0.0F, 0.0F), key(0.75F, 10.0F, 0.0F, 0.0F), key(1.05F, 0.0F, 0.0F, 0.0F)))
            .build();

    /** Ground slam (regular melee): both fists up, smash into the ground in front at 0.4 s (= impact tick), recover. */
    public static final AnimationDefinition SLAM = AnimationDefinition.Builder.withLength(0.8F)
            .addAnimation(RIGHT_ARM, rotation(
                    key(0.0F, 0.0F, 0.0F, 0.0F), key(0.25F, -170.0F, 0.0F, -10.0F), key(0.4F, -45.0F, 0.0F, 5.0F),
                    key(0.55F, -45.0F, 0.0F, 5.0F), key(0.8F, 0.0F, 0.0F, 0.0F)))
            .addAnimation(LEFT_ARM, rotation(
                    key(0.0F, 0.0F, 0.0F, 0.0F), key(0.25F, -170.0F, 0.0F, 10.0F), key(0.4F, -45.0F, 0.0F, -5.0F),
                    key(0.55F, -45.0F, 0.0F, -5.0F), key(0.8F, 0.0F, 0.0F, 0.0F)))
            .addAnimation(BODY, rotation(
                    key(0.0F, 0.0F, 0.0F, 0.0F), key(0.25F, -10.0F, 0.0F, 0.0F), key(0.4F, 30.0F, 0.0F, 0.0F),
                    key(0.55F, 30.0F, 0.0F, 0.0F), key(0.8F, 0.0F, 0.0F, 0.0F)))
            .addAnimation(BODY, position(
                    key(0.0F, 0.0F, 0.0F, 0.0F), key(0.25F, 0.0F, 1.0F, 0.0F), key(0.4F, 0.0F, -2.0F, 0.0F),
                    key(0.55F, 0.0F, -2.0F, 0.0F), key(0.8F, 0.0F, 0.0F, 0.0F)))
            .build();

    /**
     * One-handed rock summon: the right hand rises slowly (telegraph, 0.9 s), then thrusts down, palm to the ground,
     * as the rocks erupt; short recovery.
     */
    public static final AnimationDefinition SUMMON = AnimationDefinition.Builder.withLength(1.3F)
            .addAnimation(RIGHT_ARM, rotation(
                    key(0.0F, 0.0F, 0.0F, 0.0F), key(0.6F, -175.0F, 0.0F, -15.0F), key(0.75F, -170.0F, 0.0F, -20.0F),
                    key(0.9F, -60.0F, 0.0F, 10.0F), key(1.05F, -60.0F, 0.0F, 10.0F), key(1.3F, 0.0F, 0.0F, 0.0F)))
            .addAnimation(LEFT_ARM, rotation(
                    key(0.0F, 0.0F, 0.0F, 0.0F), key(0.6F, 10.0F, 0.0F, -15.0F), key(0.9F, 15.0F, 0.0F, -20.0F), key(1.3F, 0.0F, 0.0F, 0.0F)))
            .addAnimation(BODY, rotation(
                    key(0.0F, 0.0F, 0.0F, 0.0F), key(0.6F, -7.5F, -10.0F, 0.0F), key(0.9F, 15.0F, 10.0F, 0.0F),
                    key(1.05F, 15.0F, 10.0F, 0.0F), key(1.3F, 0.0F, 0.0F, 0.0F)))
            .addAnimation(HEAD, rotation(
                    key(0.0F, 0.0F, 0.0F, 0.0F), key(0.6F, -15.0F, 0.0F, 0.0F), key(0.9F, 15.0F, 0.0F, 0.0F), key(1.3F, 0.0F, 0.0F, 0.0F)))
            .build();

    private StoneGolemAnimations() {
    }

    private static AnimationChannel rotation(Keyframe... keyframes) {
        return new AnimationChannel(AnimationChannel.Targets.ROTATION, toDegrees(keyframes));
    }

    private static AnimationChannel position(Keyframe... keyframes) {
        return new AnimationChannel(AnimationChannel.Targets.POSITION, toPosition(keyframes));
    }

    /** Raw keyframe; converted to radians or model-space position by {@link #rotation} / {@link #position}. */
    private static Keyframe key(float time, float x, float y, float z) {
        return new Keyframe(time, new Vector3f(x, y, z), AnimationChannel.Interpolations.CATMULLROM);
    }

    private static Keyframe hold(float[] value, float time) {
        return key(time, value[0], value[1], value[2]);
    }

    private static Keyframe[] toDegrees(Keyframe[] raw) {
        Keyframe[] result = new Keyframe[raw.length];
        for (int i = 0; i < raw.length; i++) {
            Keyframe k = raw[i];
            result[i] = new Keyframe(k.timestamp(), KeyframeAnimations.degreeVec(k.postTarget().x(), k.postTarget().y(), k.postTarget().z()), k.interpolation());
        }
        return result;
    }

    private static Keyframe[] toPosition(Keyframe[] raw) {
        Keyframe[] result = new Keyframe[raw.length];
        for (int i = 0; i < raw.length; i++) {
            Keyframe k = raw[i];
            result[i] = new Keyframe(k.timestamp(), KeyframeAnimations.posVec(k.postTarget().x(), k.postTarget().y(), k.postTarget().z()), k.interpolation());
        }
        return result;
    }
}
