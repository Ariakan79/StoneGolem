package eu.ariakan.stonegolem.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/** A jagged stone pillar (64x64 texture), 38 px tall, rendered at 1.5x by {@link RisingRockRenderer}. */
public class RisingRockModel extends EntityModel<RisingRockRenderState> {

    public RisingRockModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("rock", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 32.0F, 16.0F)
                        .texOffs(0, 48).addBox(-5.0F, -14.0F, -4.0F, 10.0F, 6.0F, 10.0F)
                        .texOffs(40, 48).addBox(-3.0F, -20.0F, -1.0F, 4.0F, 6.0F, 4.0F),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }
}
