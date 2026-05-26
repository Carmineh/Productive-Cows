package com.productivecows.compat.jade;

import com.productivecows.ProductiveCows;
import com.productivecows.entity.MaterialCowEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum CowComponentProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        if (accessor.getServerData().contains("MilkingCooldown")) {
            int milking = accessor.getServerData().getInt("MilkingCooldown");
            if (milking > 0) {
                int secs = milking / 20;
                tooltip.add(Component.translatable("tooltip.productivecows.jade.milking_cooldown", secs / 60, String.format("%02d", secs % 60)));
            } else {
                tooltip.add(Component.translatable("tooltip.productivecows.jade.milking_ready"));
            }
        }
        
        if (accessor.getServerData().contains("BreedingCooldown")) {
            int breeding = accessor.getServerData().getInt("BreedingCooldown");
            if (breeding > 0) {
                int secs = breeding / 20;
                tooltip.add(Component.translatable("tooltip.productivecows.jade.breeding_cooldown", secs / 60, String.format("%02d", secs % 60)));
            } else {
                tooltip.add(Component.translatable("tooltip.productivecows.jade.breeding_ready"));
            }
        }
    }

    @Override
    public void appendServerData(CompoundTag data, EntityAccessor accessor) {
        if (accessor.getEntity() instanceof MaterialCowEntity cow) {
            data.putInt("MilkingCooldown", cow.getMilkingCooldown());
            data.putInt("BreedingCooldown", Math.max(0, cow.getAge()));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return ResourceLocation.fromNamespaceAndPath(ProductiveCows.MODID, "cow_cooldowns");
    }
}
