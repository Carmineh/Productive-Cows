package com.productivecows.event;

import com.productivecows.ProductiveCows;
import com.productivecows.entity.MaterialCowEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

public class ModBusEvents {

    @SubscribeEvent
    public static void registerAttributes(final EntityAttributeCreationEvent event) {
        // Register attributes (health, movement, speed) for our material cow entity
        event.put(ProductiveCows.MATERIAL_COW.get(), MaterialCowEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void registerCapabilities(net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,
                ProductiveCows.COW_CAGE_BE.get(),
                (be, side) -> be.getItemHandlerCapability()
        );
        event.registerBlockEntity(
                net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK,
                ProductiveCows.COW_CAGE_BE.get(),
                (be, side) -> be.getFluidHandlerCapability()
        );

        event.registerBlockEntity(
                net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,
                ProductiveCows.BREEDING_CAGE_BE.get(),
                (be, side) -> be.getItemHandlerCapability()
        );

        event.registerBlockEntity(
                net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,
                ProductiveCows.LIQUID_COOLER_BE.get(),
                (be, side) -> be.getItemHandlerCapability()
        );
        event.registerBlockEntity(
                net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK,
                ProductiveCows.LIQUID_COOLER_BE.get(),
                (be, side) -> be.getFluidHandlerCapability()
        );

        event.registerBlockEntity(
                net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,
                ProductiveCows.MILK_GENERATOR_BE.get(),
                (be, side) -> be.getItemHandlerCapability()
        );
        event.registerBlockEntity(
                net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK,
                ProductiveCows.MILK_GENERATOR_BE.get(),
                (be, side) -> be.getFluidHandlerCapability()
        );
        event.registerBlockEntity(
                net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK,
                ProductiveCows.MILK_GENERATOR_BE.get(),
                (be, side) -> be.getEnergyStorageCapability()
        );

        event.registerBlockEntity(
                net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,
                ProductiveCows.ADVANCED_COOLER_BE.get(),
                (be, side) -> be.getItemHandlerCapability()
        );
        event.registerBlockEntity(
                net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK,
                ProductiveCows.ADVANCED_COOLER_BE.get(),
                (be, side) -> be.getFluidHandlerCapability()
        );
        event.registerBlockEntity(
                net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK,
                ProductiveCows.ADVANCED_COOLER_BE.get(),
                (be, side) -> be.getEnergyStorageCapability()
        );
    }
}
