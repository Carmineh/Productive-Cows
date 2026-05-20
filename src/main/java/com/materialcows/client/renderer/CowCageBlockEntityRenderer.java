package com.materialcows.client.renderer;

import com.materialcows.Materialcows;
import com.materialcows.block.entity.CowCageBlockEntity;
import com.materialcows.data.CowDefinition;
import com.materialcows.entity.MaterialCowEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class CowCageBlockEntityRenderer implements BlockEntityRenderer<CowCageBlockEntity> {
    private MaterialCowEntity cachedCow;

    public CowCageBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CowCageBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        CowDefinition def = blockEntity.getCowDefinitionFromEgg();
        if (def == null) {
            return;
        }

        if (this.cachedCow == null || this.cachedCow.level() != blockEntity.getLevel()) {
            this.cachedCow = new MaterialCowEntity(Materialcows.MATERIAL_COW.get(), blockEntity.getLevel());
        }

        this.cachedCow.setCowDefinitionId(def.id());

        // Don't show name tag
        this.cachedCow.setCustomName(null);
        this.cachedCow.setCustomNameVisible(false);

        poseStack.pushPose();

        // Translate to the center of the block and slightly up
        poseStack.translate(0.5D, 0.25D, 0.5D);

        // Dynamic rotation based on time
        float gameTime = (blockEntity.getLevel().getGameTime() + partialTick) * 1.5F;
        poseStack.mulPose(Axis.YP.rotationDegrees(gameTime));

        // Scale down the cow so it fits nicely inside the cage block
        float scale = 0.4F;
        poseStack.scale(scale, scale, scale);

        // Render the entity
        Minecraft.getInstance().getEntityRenderDispatcher().render(
            this.cachedCow,
            0.0D, 0.0D, 0.0D,
            0.0F,
            partialTick,
            poseStack,
            buffer,
            packedLight
        );

        poseStack.popPose();
    }
}
