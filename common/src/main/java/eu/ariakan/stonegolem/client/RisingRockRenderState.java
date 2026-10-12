package eu.ariakan.stonegolem.client;

import net.minecraft.client.renderer.entity.state.EntityRenderState;

public class RisingRockRenderState extends EntityRenderState {
    public float yRot;
    public float scale = 1.0F;
    /** 0 = buried, 1 = fully out of the ground. */
    public float emergence;
}
