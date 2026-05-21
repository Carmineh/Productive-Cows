package com.materialcows.event;

import com.materialcows.Materialcows;
import com.materialcows.entity.MaterialCowEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

public class ModBusEvents {

    @SubscribeEvent
    public static void registerAttributes(final EntityAttributeCreationEvent event) {
        // Register attributes (health, movement, speed) for our material cow entity
        event.put(Materialcows.MATERIAL_COW.get(), MaterialCowEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void registerCapabilities(net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,
                Materialcows.COW_CAGE_BE.get(),
                (be, side) -> be.getItemHandlerCapability()
        );
        event.registerBlockEntity(
                net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,
                Materialcows.BREEDING_CAGE_BE.get(),
                (be, side) -> be.getItemHandlerCapability()
        );
        event.registerBlockEntity(
                net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK,
                Materialcows.COW_CAGE_BE.get(),
                (be, side) -> be.getFluidHandlerCapability()
        );

        event.registerBlockEntity(
                net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,
                Materialcows.LIQUID_COOLER_BE.get(),
                (be, side) -> be.getItemHandlerCapability()
        );
        event.registerBlockEntity(
                net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK,
                Materialcows.LIQUID_COOLER_BE.get(),
                (be, side) -> be.getFluidHandlerCapability()
        );

        event.registerBlockEntity(
                net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,
                Materialcows.MILK_GENERATOR_BE.get(),
                (be, side) -> be.getItemHandlerCapability()
        );
        event.registerBlockEntity(
                net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK,
                Materialcows.MILK_GENERATOR_BE.get(),
                (be, side) -> be.getFluidHandlerCapability()
        );
        event.registerBlockEntity(
                net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK,
                Materialcows.MILK_GENERATOR_BE.get(),
                (be, side) -> be.getEnergyStorageCapability()
        );

        event.registerBlockEntity(
                net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,
                Materialcows.ADVANCED_COOLER_BE.get(),
                (be, side) -> be.getItemHandlerCapability()
        );
        event.registerBlockEntity(
                net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK,
                Materialcows.ADVANCED_COOLER_BE.get(),
                (be, side) -> be.getFluidHandlerCapability()
        );
        event.registerBlockEntity(
                net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK,
                Materialcows.ADVANCED_COOLER_BE.get(),
                (be, side) -> be.getEnergyStorageCapability()
        );
    }
}
