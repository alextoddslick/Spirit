package me.codexadrian.spirit.client;

import me.codexadrian.spirit.Spirit;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

public class CrudeSoulEntityModel extends EntityModel<LivingEntityRenderState> {
    // This layer location should be baked with EntityRendererProvider.Context in
    // the entity renderer and passed into this model's constructor
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(Spirit.MODID, "crudesoulentitymodel"), "main");

    public CrudeSoulEntityModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(4, 0)
                        .addBox(-3.0F, -6.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                        .texOffs(28, 0).addBox(-3.0F, -12.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 17.0F, 0.0F));

        partdefinition.addOrReplaceChild("right_arm",
                CubeListBuilder.create().texOffs(22, 0).addBox(-3.0F, -0.5F, -0.75F, 3.0F, 1.0F, 2.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.5F, 17.5F, 0.0F, 0.1332F, 0.2261F, -1.0321F));

        partdefinition.addOrReplaceChild("left_arm",
                CubeListBuilder.create().texOffs(46, 0).addBox(0.0F, -0.5F, -0.75F, 3.0F, 1.0F, 2.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.5F, 17.5F, 0.0F, 0.1564F, -0.2635F, 1.0264F));

        partdefinition.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(52, 3)
                        .addBox(-1.5F, 0.0F, -1.0F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-1.5F, -4.0F, -1.0F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 20.5F, 1.0F, 0.2182F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }
}
