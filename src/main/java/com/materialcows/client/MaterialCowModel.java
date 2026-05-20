package com.materialcows.client;

import com.materialcows.entity.MaterialCowEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.model.geom.ModelPart;

public class MaterialCowModel extends CowModel<MaterialCowEntity> {
    private int tintColor = 0xFFFFFF;

    public MaterialCowModel(ModelPart root) {
        super(root);
    }

    public void setTintColor(int color) {
        this.tintColor = color;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int packedColor) {
        // Multiply the incoming rendering tint (packedColor, e.g. hurt state overlays) with our custom hex resource color
        int finalColor = multiplyColors(this.tintColor, packedColor);
        super.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, finalColor);
    }

    private static int multiplyColors(int c1, int c2) {
        int a1 = (c1 >> 24) & 0xFF;
        int r1 = (c1 >> 16) & 0xFF;
        int g1 = (c1 >> 8) & 0xFF;
        int b1 = c1 & 0xFF;

        int a2 = (c2 >> 24) & 0xFF;
        int r2 = (c2 >> 16) & 0xFF;
        int g2 = (c2 >> 8) & 0xFF;
        int b2 = c2 & 0xFF;

        int a = (a1 * a2) / 255;
        int r = (r1 * r2) / 255;
        int g = (g1 * g2) / 255;
        int b = (b1 * b2) / 255;

        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
