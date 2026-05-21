package com.materialcows.compat.jade;

import com.materialcows.Materialcows;
import com.materialcows.block.entity.AdvancedCoolerBlockEntity;
import com.materialcows.block.entity.LiquidCoolerBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class CoolerComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    public static final CoolerComponentProvider INSTANCE = new CoolerComponentProvider();
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(Materialcows.MODID, "cooler_info");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        
        if (data.contains("Energy")) {
            int energy = data.getInt("Energy");
            int maxEnergy = data.getInt("MaxEnergy");
            tooltip.add(Component.translatable("gui.materialcows.energy_stored", energy, maxEnergy).withStyle(net.minecraft.ChatFormatting.YELLOW));
        }

        if (data.contains("FluidAmount")) {
            int fluidAmount = data.getInt("FluidAmount");
            int fluidCapacity = data.getInt("FluidCapacity");
            String fluidName = data.getString("FluidName");
            if (fluidAmount > 0) {
                tooltip.add(Component.literal(fluidName + ": " + fluidAmount + " / " + fluidCapacity + " mB").withStyle(net.minecraft.ChatFormatting.AQUA));
            } else {
                tooltip.add(Component.translatable("gui.materialcows.empty_tank").withStyle(net.minecraft.ChatFormatting.GRAY));
            }
        }

        if (data.contains("CookTime") && data.contains("CookTimeTotal")) {
            int cookTime = data.getInt("CookTime");
            int cookTimeTotal = data.getInt("CookTimeTotal");
            if (cookTime > 0 && cookTimeTotal > 0) {
                int percent = (cookTime * 100) / cookTimeTotal;
                tooltip.add(Component.literal("Cooling: " + percent + "%").withStyle(net.minecraft.ChatFormatting.GREEN));
            }
        }
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof LiquidCoolerBlockEntity cooler) {
            data.putInt("Energy", cooler.getEnergyStorage().getEnergyStored());
            data.putInt("MaxEnergy", cooler.getEnergyStorage().getMaxEnergyStored());
            data.putInt("FluidAmount", cooler.getFluidTank().getFluidAmount());
            data.putInt("FluidCapacity", cooler.getFluidTank().getCapacity());
            if (!cooler.getFluidTank().getFluid().isEmpty()) {
                data.putString("FluidName", cooler.getFluidTank().getFluid().getHoverName().getString());
            }
            
            data.putInt("CookTime", cooler.getCookTime());
            data.putInt("CookTimeTotal", cooler.getCookTimeTotal());
            
        } else if (accessor.getBlockEntity() instanceof AdvancedCoolerBlockEntity advancedCooler) {
            data.putInt("Energy", advancedCooler.getEnergyStorage().getEnergyStored());
            data.putInt("MaxEnergy", advancedCooler.getEnergyStorage().getMaxEnergyStored());
            data.putInt("FluidAmount", advancedCooler.getFluidTank().getFluidAmount());
            data.putInt("FluidCapacity", advancedCooler.getFluidTank().getCapacity());
            if (!advancedCooler.getFluidTank().getFluid().isEmpty()) {
                data.putString("FluidName", advancedCooler.getFluidTank().getFluid().getHoverName().getString());
            }
            
            data.putInt("CookTime", advancedCooler.getCookTime());
            data.putInt("CookTimeTotal", advancedCooler.getCookTimeTotal());
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
