package com.materialcows.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.joml.Matrix4f;
import com.materialcows.block.entity.AdvancedCoolerBlockEntity;
import com.materialcows.block.entity.LiquidCoolerBlockEntity;

public class CoolerBlockEntityRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {

    public CoolerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(T blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        FluidTank tank = null;
        if (blockEntity instanceof AdvancedCoolerBlockEntity adv) {
            tank = adv.getFluidTank();
        } else if (blockEntity instanceof LiquidCoolerBlockEntity liq) {
            tank = liq.getFluidTank();
        }

        if (tank == null || tank.isEmpty()) return;

        FluidStack fluidStack = tank.getFluid();
        int amount = fluidStack.getAmount();
        int capacity = tank.getCapacity();
        if (amount <= 0) return;

        IClientFluidTypeExtensions fluidExt = IClientFluidTypeExtensions.of(fluidStack.getFluid());
        ResourceLocation stillTexture = fluidExt.getStillTexture(fluidStack);
        if (stillTexture == null) return;

        TextureAtlasSprite sprite = net.minecraft.client.Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(stillTexture);
        
        int color = fluidExt.getTintColor(fluidStack);
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        float a = ((color >> 24) & 0xFF) / 255f;
        if (a == 0f) a = 1f;

        float minX, minZ, maxX, maxZ, minY, maxH;
        if (blockEntity instanceof AdvancedCoolerBlockEntity) {
            minX = 4f / 16f;
            minZ = 4f / 16f;
            maxX = 12f / 16f;
            maxZ = 12f / 16f;
            minY = 1f / 16f;
            maxH = 12f / 16f;
        } else {
            // Liquid Cooler: Pos: 1 2 1, Size: 14 11 14
            minX = 1f / 16f;
            minZ = 1f / 16f;
            maxX = 15f / 16f;
            maxZ = 15f / 16f;
            minY = 2f / 16f;
            maxH = 11f / 16f;
        }
        
        float fillRatio = (float) amount / capacity;
        float maxY = minY + (maxH * fillRatio);

        VertexConsumer builder = bufferSource.getBuffer(RenderType.translucent());
        poseStack.pushPose();

        PoseStack.Pose pose = poseStack.last();

        buildQuad(builder, pose, minX, minY, minZ, maxX, maxY, maxZ, sprite, r, g, b, a, packedLight, Direction.UP);
        buildQuad(builder, pose, minX, minY, minZ, maxX, maxY, maxZ, sprite, r, g, b, a, packedLight, Direction.DOWN);
        buildQuad(builder, pose, minX, minY, minZ, maxX, maxY, maxZ, sprite, r, g, b, a, packedLight, Direction.NORTH);
        buildQuad(builder, pose, minX, minY, minZ, maxX, maxY, maxZ, sprite, r, g, b, a, packedLight, Direction.SOUTH);
        buildQuad(builder, pose, minX, minY, minZ, maxX, maxY, maxZ, sprite, r, g, b, a, packedLight, Direction.WEST);
        buildQuad(builder, pose, minX, minY, minZ, maxX, maxY, maxZ, sprite, r, g, b, a, packedLight, Direction.EAST);

        poseStack.popPose();
    }

    private void buildQuad(VertexConsumer builder, PoseStack.Pose pose, float x1, float y1, float z1, float x2, float y2, float z2,
                           TextureAtlasSprite sprite, float r, float g, float b, float a,
                           int packedLight, Direction dir) {
        float u1, u2, v1, v2;
        float u0 = sprite.getU0();
        float uDiff = sprite.getU1() - u0;
        float v0 = sprite.getV0();
        float vDiff = sprite.getV1() - v0;

        if (dir == Direction.UP || dir == Direction.DOWN) {
            u1 = u0 + uDiff * x1; u2 = u0 + uDiff * x2;
            v1 = v0 + vDiff * z1; v2 = v0 + vDiff * z2;
        } else if (dir == Direction.NORTH || dir == Direction.SOUTH) {
            u1 = u0 + uDiff * x1; u2 = u0 + uDiff * x2;
            v1 = v0 + vDiff * (1f - y2); v2 = v0 + vDiff * (1f - y1);
        } else {
            u1 = u0 + uDiff * z1; u2 = u0 + uDiff * z2;
            v1 = v0 + vDiff * (1f - y2); v2 = v0 + vDiff * (1f - y1);
        }

        switch (dir) {
            case UP:
                addVertex(builder, pose, x1, y2, z2, u1, v2, r, g, b, a, packedLight, 0, 1, 0);
                addVertex(builder, pose, x2, y2, z2, u2, v2, r, g, b, a, packedLight, 0, 1, 0);
                addVertex(builder, pose, x2, y2, z1, u2, v1, r, g, b, a, packedLight, 0, 1, 0);
                addVertex(builder, pose, x1, y2, z1, u1, v1, r, g, b, a, packedLight, 0, 1, 0);
                break;
            case DOWN:
                addVertex(builder, pose, x1, y1, z1, u1, v1, r, g, b, a, packedLight, 0, -1, 0);
                addVertex(builder, pose, x2, y1, z1, u2, v1, r, g, b, a, packedLight, 0, -1, 0);
                addVertex(builder, pose, x2, y1, z2, u2, v2, r, g, b, a, packedLight, 0, -1, 0);
                addVertex(builder, pose, x1, y1, z2, u1, v2, r, g, b, a, packedLight, 0, -1, 0);
                break;
            case NORTH:
                addVertex(builder, pose, x1, y2, z1, u2, v1, r, g, b, a, packedLight, 0, 0, -1);
                addVertex(builder, pose, x2, y2, z1, u1, v1, r, g, b, a, packedLight, 0, 0, -1);
                addVertex(builder, pose, x2, y1, z1, u1, v2, r, g, b, a, packedLight, 0, 0, -1);
                addVertex(builder, pose, x1, y1, z1, u2, v2, r, g, b, a, packedLight, 0, 0, -1);
                break;
            case SOUTH:
                addVertex(builder, pose, x2, y2, z2, u2, v1, r, g, b, a, packedLight, 0, 0, 1);
                addVertex(builder, pose, x1, y2, z2, u1, v1, r, g, b, a, packedLight, 0, 0, 1);
                addVertex(builder, pose, x1, y1, z2, u1, v2, r, g, b, a, packedLight, 0, 0, 1);
                addVertex(builder, pose, x2, y1, z2, u2, v2, r, g, b, a, packedLight, 0, 0, 1);
                break;
            case WEST:
                addVertex(builder, pose, x1, y2, z2, u2, v1, r, g, b, a, packedLight, -1, 0, 0);
                addVertex(builder, pose, x1, y2, z1, u1, v1, r, g, b, a, packedLight, -1, 0, 0);
                addVertex(builder, pose, x1, y1, z1, u1, v2, r, g, b, a, packedLight, -1, 0, 0);
                addVertex(builder, pose, x1, y1, z2, u2, v2, r, g, b, a, packedLight, -1, 0, 0);
                break;
            case EAST:
                addVertex(builder, pose, x2, y2, z1, u2, v1, r, g, b, a, packedLight, 1, 0, 0);
                addVertex(builder, pose, x2, y2, z2, u1, v1, r, g, b, a, packedLight, 1, 0, 0);
                addVertex(builder, pose, x2, y1, z2, u1, v2, r, g, b, a, packedLight, 1, 0, 0);
                addVertex(builder, pose, x2, y1, z1, u2, v2, r, g, b, a, packedLight, 1, 0, 0);
                break;
        }
    }

    private void addVertex(VertexConsumer builder, PoseStack.Pose pose, float x, float y, float z, float u, float v, float r, float g, float b, float a, int packedLight, float nx, float ny, float nz) {
        builder.addVertex(pose, x, y, z).setColor(r, g, b, a).setUv(u, v).setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, nx, ny, nz);
    }
}
