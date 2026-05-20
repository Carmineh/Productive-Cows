package com.materialcows.client;

import com.materialcows.Materialcows;
import com.materialcows.data.CowDefinition;
import com.materialcows.entity.MaterialCowEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class MaterialCowRenderer extends MobRenderer<MaterialCowEntity, MaterialCowModel> {
    
    private static final ResourceLocation COW_TEXTURE = ResourceLocation.fromNamespaceAndPath(Materialcows.MODID, "textures/entity/material_cow.png");

    public MaterialCowRenderer(EntityRendererProvider.Context context) {
        super(context, new MaterialCowModel(context.bakeLayer(ModelLayers.COW)), 0.7F);
    }

    @Override
    public ResourceLocation getTextureLocation(MaterialCowEntity entity) {
        return COW_TEXTURE;
    }

    @Override
    public void render(MaterialCowEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // Retrieve dynamic definition from synchronized client-side map
        CowDefinition definition = entity.getCowDefinition();
        int color = 0xFFFFFF; // Default fallback to white
        
        if (definition != null) {
            color = definition.hexColor();
        }

        // Pack it as fully opaque ARGB (0xFF000000 | RGB)
        int argbColor = 0xFF000000 | color;

        // Apply tint color dynamically to our model
        this.model.setTintColor(argbColor);

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}
