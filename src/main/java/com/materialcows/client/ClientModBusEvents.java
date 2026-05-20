package com.materialcows.client;

import com.materialcows.Materialcows;
import com.materialcows.client.screen.CowCageScreen;
import com.materialcows.client.screen.LiquidCoolerScreen;
import com.materialcows.data.CowDefinition;
import com.materialcows.fluid.ModFluids;
import com.materialcows.network.ClientCowData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = Materialcows.MODID, value = Dist.CLIENT)
public class ClientModBusEvents {
    
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(Materialcows.MATERIAL_COW.get(), MaterialCowRenderer::new);
        event.registerBlockEntityRenderer(Materialcows.COW_CAGE_BE.get(), com.materialcows.client.renderer.CowCageBlockEntityRenderer::new);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(com.materialcows.Materialcows.COW_CAGE_MENU.get(), com.materialcows.client.screen.CowCageScreen::new);
        event.register(com.materialcows.Materialcows.LIQUID_COOLER_MENU.get(), com.materialcows.client.screen.LiquidCoolerScreen::new);
        event.register(com.materialcows.Materialcows.BREEDING_CAGE_MENU.get(), com.materialcows.client.screen.BreedingCageScreen::new);
        event.register(com.materialcows.Materialcows.MILK_GENERATOR_MENU.get(), com.materialcows.client.screen.MilkGeneratorScreen::new);
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        for (com.materialcows.fluid.ModFluids.RegisteredFluid fluid : com.materialcows.fluid.ModFluids.ALL_FLUIDS) {
            event.register(new net.neoforged.neoforge.client.model.DynamicFluidContainerModel.Colors(), fluid.bucket().get());
        }
        
        event.register((stack, tintIndex) -> {
            net.minecraft.world.item.component.CustomData customData = stack.get(net.minecraft.core.component.DataComponents.ENTITY_DATA);
            if (customData != null) {
                CompoundTag tag = customData.copyTag();
                if (tag.contains("CowDefinitionId")) {
                    String idStr = tag.getString("CowDefinitionId");
                    ResourceLocation defId = ResourceLocation.tryParse(idStr);
                    if (defId != null) {
                        CowDefinition def = ClientCowData.getDefinition(defId);
                        if (def != null) {
                            int color = def.hexColor();
                            if (tintIndex == 0) {
                                // Primary color (the egg base) -> return the material's color (fully opaque)!
                                return color | 0xFF000000;
                            } else if (tintIndex == 1) {
                                // Secondary color (the egg spots) -> let's make it a nice clean opaque white
                                return 0xFFFFFFFF;
                            }
                        }
                    }
                }
            }
            // Fallback to default registered spawn egg colors (fully opaque)
            return tintIndex == 0 ? 0xFFC1C1C1 : 0xFF3F3F3F;
        }, Materialcows.MATERIAL_COW_SPAWN_EGG.get());

        event.register((stack, tintIndex) -> {
            net.minecraft.world.item.component.CustomData customData = stack.get(net.minecraft.core.component.DataComponents.ENTITY_DATA);
            if (customData != null) {
                CompoundTag tag = customData.copyTag();
                if (tag.contains("CowDefinitionId")) {
                    String idStr = tag.getString("CowDefinitionId");
                    ResourceLocation defId = ResourceLocation.tryParse(idStr);
                    if (defId != null) {
                        CowDefinition def = ClientCowData.getDefinition(defId);
                        if (def != null) {
                            int color = def.hexColor();
                            if (tintIndex == 0) {
                                return color | 0xFF000000;
                            }
                        }
                    }
                }
            }
            return 0xFFFFFFFF;
        }, Materialcows.CAPTURED_COW.get());
    }
}
