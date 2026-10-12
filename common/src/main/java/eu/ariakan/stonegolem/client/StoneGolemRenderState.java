package eu.ariakan.stonegolem.client;

import eu.ariakan.stonegolem.entity.GolemState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

public class StoneGolemRenderState extends LivingEntityRenderState {
    public GolemState golemState = GolemState.DORMANT;
    public final AnimationState dormantAnimationState = new AnimationState();
    public final AnimationState awakenAnimationState = new AnimationState();
    public final AnimationState settleAnimationState = new AnimationState();
    public final AnimationState stompAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
}
