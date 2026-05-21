package com.materialcows.client.renderer;

import com.materialcows.Materialcows;
import com.materialcows.data.CowDefinition;
import com.materialcows.entity.MaterialCowEntity;
import com.materialcows.network.ClientCowData;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import com.mojang.math.Axis;

public class CapturedCowItemRenderer extends BlockEntityWithoutLevelRenderer {
    
    private static CapturedCowItemRenderer instance;
    private MaterialCowEntity cachedCow;
    private net.minecraft.world.entity.animal.Cow cachedVanillaCow;

    public CapturedCowItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    public static CapturedCowItemRenderer getInstance() {
        if (instance == null) {
            instance = new CapturedCowItemRenderer();
        }
        return instance;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (Minecraft.getInstance().level == null) return;
        
        CustomData customData = stack.get(DataComponents.ENTITY_DATA);
        if (customData != null) {
            CompoundTag tag = customData.copyTag();
            if ("minecraft:cow".equals(tag.getString("id"))) {
                if (this.cachedVanillaCow == null || this.cachedVanillaCow.level() != Minecraft.getInstance().level) {
                    this.cachedVanillaCow = new net.minecraft.world.entity.animal.Cow(net.minecraft.world.entity.EntityType.COW, Minecraft.getInstance().level);
                }
                this.cachedVanillaCow.setCustomNameVisible(false);
                this.cachedVanillaCow.setCustomName(null);
                
                poseStack.pushPose();
                poseStack.translate(0.5D, 0.0D, 0.5D);
                
                float scale = 0.55F;
                if (displayContext == ItemDisplayContext.GUI) {
                    poseStack.translate(0.0D, 0.05D, 0.0D);
                    poseStack.mulPose(Axis.XP.rotationDegrees(15F));
                    poseStack.mulPose(Axis.YP.rotationDegrees(315F));
                } else if (displayContext == ItemDisplayContext.GROUND) {
                    poseStack.translate(0.0D, 0.2D, 0.0D);
                } else if (displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND || displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
                    poseStack.mulPose(Axis.YP.rotationDegrees(180F)); 
                }
                poseStack.scale(scale, scale, scale);

                Minecraft.getInstance().getEntityRenderDispatcher().render(
                    this.cachedVanillaCow,
                    0.0D, 0.0D, 0.0D,
                    0.0F,
                    Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true),
                    poseStack,
                    buffer,
                    packedLight
                );
                
                poseStack.popPose();
                return;
            }
            if (tag.contains("CowDefinitionId")) {
                String idStr = tag.getString("CowDefinitionId");
                ResourceLocation defId = ResourceLocation.tryParse(idStr);
                if (defId != null) {
                    CowDefinition def = ClientCowData.getDefinition(defId);
                    if (def != null) {
                        if (this.cachedCow == null || this.cachedCow.level() != Minecraft.getInstance().level) {
                            this.cachedCow = new MaterialCowEntity(Materialcows.MATERIAL_COW.get(), Minecraft.getInstance().level);
                        }
                        
                        this.cachedCow.setCowDefinitionId(defId);
                        this.cachedCow.setCustomNameVisible(false);
                        this.cachedCow.setCustomName(null);
                        
                        poseStack.pushPose();
                        
                        poseStack.translate(0.5D, 0.0D, 0.5D);
                        
                        float scale = 0.55F;
                        if (displayContext == ItemDisplayContext.GUI) {
                            poseStack.translate(0.0D, 0.05D, 0.0D);
                            poseStack.mulPose(Axis.XP.rotationDegrees(15F));
                            poseStack.mulPose(Axis.YP.rotationDegrees(315F));
                        } else if (displayContext == ItemDisplayContext.GROUND) {
                            poseStack.translate(0.0D, 0.2D, 0.0D);
                        } else if (displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND || displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
                            poseStack.mulPose(Axis.YP.rotationDegrees(180F)); 
                        }

                        poseStack.scale(scale, scale, scale);

                        Minecraft.getInstance().getEntityRenderDispatcher().render(
                            this.cachedCow,
                            0.0D, 0.0D, 0.0D,
                            0.0F,
                            Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true),
                            poseStack,
                            buffer,
                            packedLight
                        );
                        
                        poseStack.popPose();
                        return;
                    }
                }
            }
        }
    }
}
