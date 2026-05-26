package com.productivecows.block.entity;

import com.productivecows.ProductiveCows;
import com.productivecows.menu.MilkGeneratorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class MilkGeneratorBlockEntity extends BlockEntity implements MenuProvider {

    // Custom energy storage subclass to allow manual energy manipulation
    public static class CustomEnergyStorage extends EnergyStorage {
        public CustomEnergyStorage(int capacity, int maxReceive, int maxExtract) {
            super(capacity, maxReceive, maxExtract);
        }

        public void setEnergy(int energy) {
            this.energy = Math.max(0, Math.min(capacity, energy));
        }

        public void addEnergy(int amount) {
            this.energy = Math.max(0, Math.min(capacity, this.energy + amount));
        }

        public void consumeEnergy(int amount) {
            this.energy = Math.max(0, this.energy - amount);
        }
    }

    private final ItemStackHandler itemHandler = new ItemStackHandler(2) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == 0) {
                return stack.is(Items.MILK_BUCKET) || stack.is(com.productivecows.fluid.ModFluids.LIQUID_MILK.bucket().get());
            }
            return false; // Slot 1 is output only
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot == 1) return stack;
            return super.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot == 0) return ItemStack.EMPTY;
            return super.extractItem(slot, amount, simulate);
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    private final FluidTank fluidTank = new FluidTank(4000) {
        @Override
        public boolean isFluidValid(FluidStack stack) {
            Fluid f = stack.getFluid();
            if (f == com.productivecows.fluid.ModFluids.LIQUID_MILK.source().get()) {
                return true;
            }
            String name = BuiltInRegistries.FLUID.getKey(f).toString();
            return name.equals("neoforge:milk") || name.equals("minecraft:milk") || name.endsWith(":milk");
        }

        @Override
        protected void onContentsChanged() {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    private final CustomEnergyStorage energyStorage = new CustomEnergyStorage(50000, 0, 200);
    private double fractionalMilk = 0.0;

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> energyStorage.getEnergyStored();
                case 1 -> energyStorage.getMaxEnergyStored();
                case 2 -> fluidTank.getFluidAmount();
                case 3 -> fluidTank.getCapacity();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> energyStorage.setEnergy(value);
                case 2 -> {
                    Fluid milkFluid = com.productivecows.fluid.ModFluids.LIQUID_MILK.source().get();
                    fluidTank.setFluid(new FluidStack(milkFluid, value));
                }
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public MilkGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ProductiveCows.MILK_GENERATOR_BE.get(), pos, state);
    }

    public ItemStackHandler getInventory() {
        return itemHandler;
    }

    public FluidTank getFluidTank() {
        return fluidTank;
    }

    public CustomEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public IItemHandler getItemHandlerCapability() {
        return itemHandler;
    }

    public IFluidHandler getFluidHandlerCapability() {
        return fluidTank;
    }

    public IEnergyStorage getEnergyStorageCapability() {
        return energyStorage;
    }

    public void tick(Level lvl, BlockPos pos, BlockState state) {
        tickBucketEmptying();

        // 1. Generate power if we have milk fluid and energy storage has room
        int space = energyStorage.getMaxEnergyStored() - energyStorage.getEnergyStored();
        if (space > 0 && fluidTank.getFluidAmount() > 0) {
            // Maximum RF generated this tick is 40 RF
            int toGenerate = Math.min(40, space);
            
            // Milk cost: 1 mB of milk = 100 RF. So cost = toGenerate / 100.0 mB
            double milkCost = toGenerate / 100.0;
            
            // We must ensure we actually have enough milk.
            // Available milk in mB = fluidTank.getFluidAmount() - fractionalMilk
            double availableMilk = fluidTank.getFluidAmount() - fractionalMilk;
            if (availableMilk <= 0.0) {
                // Not even a tiny bit of milk available, let's align fractionalMilk to 0
                fractionalMilk = 0.0;
            } else {
                if (milkCost > availableMilk) {
                    milkCost = availableMilk;
                    toGenerate = (int) (milkCost * 100.0);
                }

                if (toGenerate > 0) {
                    energyStorage.addEnergy(toGenerate);
                    fractionalMilk += milkCost;

                    // Drain whole mB from the fluid tank
                    if (fractionalMilk >= 1.0) {
                        int wholeMilkToDrain = (int) Math.floor(fractionalMilk);
                        fluidTank.drain(wholeMilkToDrain, IFluidHandler.FluidAction.EXECUTE);
                        fractionalMilk -= wholeMilkToDrain;
                    }
                    setChanged();
                }
            }
        }

        // 2. Auto-push generated energy to neighboring blocks (max 200 RF/t output)
        int energyStored = energyStorage.getEnergyStored();
        if (energyStored > 0) {
            int maxSend = Math.min(200, energyStored);
            int sent = 0;

            for (Direction dir : Direction.values()) {
                if (sent >= maxSend) break;

                BlockPos targetPos = pos.relative(dir);
                IEnergyStorage targetEnergy = lvl.getCapability(Capabilities.EnergyStorage.BLOCK, targetPos, dir.getOpposite());
                if (targetEnergy != null && targetEnergy.canReceive()) {
                    int toSend = maxSend - sent;
                    int accepted = targetEnergy.receiveEnergy(toSend, false);
                    if (accepted > 0) {
                        sent += accepted;
                        energyStorage.consumeEnergy(accepted);
                        setChanged();
                    }
                }
            }
        }
    }

    private void tickBucketEmptying() {
        ItemStack inputBucket = itemHandler.getStackInSlot(0);
        if (!inputBucket.isEmpty()) {
            boolean isVanillaMilk = inputBucket.is(Items.MILK_BUCKET);
            boolean isCustomMilk = inputBucket.is(com.productivecows.fluid.ModFluids.LIQUID_MILK.bucket().get());
            
            if (isVanillaMilk || isCustomMilk) {
                Fluid milkFluid = com.productivecows.fluid.ModFluids.LIQUID_MILK.source().get();
                FluidStack milkStack = new FluidStack(milkFluid, 1000);
                int filled = fluidTank.fill(milkStack, IFluidHandler.FluidAction.SIMULATE);
                if (filled >= 1000) {
                    ItemStack outputSlot = itemHandler.getStackInSlot(1);
                    if (outputSlot.isEmpty() || (outputSlot.is(Items.BUCKET) && outputSlot.getCount() < outputSlot.getMaxStackSize())) {
                        fluidTank.fill(milkStack, IFluidHandler.FluidAction.EXECUTE);
                        inputBucket.shrink(1);
                        if (outputSlot.isEmpty()) {
                            itemHandler.setStackInSlot(1, new ItemStack(Items.BUCKET));
                        } else {
                            outputSlot.grow(1);
                        }
                        setChanged();
                    }
                }
            }
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.itemHandler.deserializeNBT(registries, tag.getCompound("Inventory"));
        this.fluidTank.readFromNBT(registries, tag.getCompound("FluidTank"));
        this.energyStorage.setEnergy(tag.getInt("Energy"));
        this.fractionalMilk = tag.getDouble("FractionalMilk");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", this.itemHandler.serializeNBT(registries));
        CompoundTag tankTag = new CompoundTag();
        this.fluidTank.writeToNBT(registries, tankTag);
        tag.put("FluidTank", tankTag);
        tag.putInt("Energy", this.energyStorage.getEnergyStored());
        tag.putDouble("FractionalMilk", this.fractionalMilk);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.productivecows.milk_generator");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new MilkGeneratorMenu(id, playerInventory, this, this.dataAccess);
    }
}
