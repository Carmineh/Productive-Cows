package com.productivecows.client;

import com.productivecows.ProductiveCows;
import com.productivecows.client.screen.CowCageScreen;
import com.productivecows.client.screen.LiquidCoolerScreen;
import com.productivecows.data.CowDefinition;
import com.productivecows.fluid.ModFluids;
import com.productivecows.network.ClientCowData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = ProductiveCows.MODID, value = Dist.CLIENT)
public class ClientModBusEvents {
    
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ProductiveCows.MATERIAL_COW.get(), MaterialCowRenderer::new);
        event.registerBlockEntityRenderer(ProductiveCows.COW_CAGE_BE.get(), com.productivecows.client.renderer.CowCageBlockEntityRenderer::new);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(com.productivecows.ProductiveCows.COW_CAGE_MENU.get(), com.productivecows.client.screen.CowCageScreen::new);
        event.register(com.productivecows.ProductiveCows.LIQUID_COOLER_MENU.get(), com.productivecows.client.screen.LiquidCoolerScreen::new);
        event.register(com.productivecows.ProductiveCows.BREEDING_CAGE_MENU.get(), com.productivecows.client.screen.BreedingCageScreen::new);
        event.register(com.productivecows.ProductiveCows.MILK_GENERATOR_MENU.get(), com.productivecows.client.screen.MilkGeneratorScreen::new);
        event.register(com.productivecows.ProductiveCows.ADVANCED_COOLER_MENU.get(), com.productivecows.client.screen.AdvancedCoolerScreen::new);
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        for (com.productivecows.fluid.ModFluids.RegisteredFluid fluid : com.productivecows.fluid.ModFluids.ALL_FLUIDS) {
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
        }, ProductiveCows.MATERIAL_COW_SPAWN_EGG.get());

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
        }, ProductiveCows.CAPTURED_COW.get());
    }
}
