package com.materialcows.block.entity;

import com.materialcows.Materialcows;
import com.materialcows.data.CowDefinition;
import com.materialcows.menu.CowCageMenu;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class CowCageBlockEntity extends BlockEntity implements MenuProvider {

    private final ItemStackHandler itemHandler = new ItemStackHandler(3) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == 0) {
                return stack.is(Materialcows.CAPTURED_COW.get()) || stack.is(Materialcows.MATERIAL_COW_SPAWN_EGG.get());
            }
            if (slot == 1) {
                return stack.is(Items.BUCKET);
            }
            return false; // Slot 2 is output only
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot == 2) return stack; // Cannot insert into output
            return super.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return super.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            if (slot == 0) {
                return 1;
            }
            return super.getSlotLimit(slot);
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    private final FluidTank fluidTank = new FluidTank(8000) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    private int milkingTimer = 400;

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return index == 0 ? milkingTimer : 0;
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                milkingTimer = value;
            }
        }

        @Override
        public int getCount() {
            return 1;
        }
    };

    public CowCageBlockEntity(BlockPos pos, BlockState state) {
        super(Materialcows.COW_CAGE_BE.get(), pos, state);
    }

    public ItemStackHandler getInventory() {
        return itemHandler;
    }

    public FluidTank getFluidTank() {
        return fluidTank;
    }

    public IItemHandler getItemHandlerCapability() {
        return itemHandler;
    }

    public IFluidHandler getFluidHandlerCapability() {
        return fluidTank;
    }

    public int getMilkingTimer() {
        return milkingTimer;
    }

    @Nullable
    public CowDefinition getCowDefinitionFromEgg() {
        ItemStack eggStack = itemHandler.getStackInSlot(0);
        if (eggStack.is(Materialcows.CAPTURED_COW.get()) || eggStack.is(Materialcows.MATERIAL_COW_SPAWN_EGG.get())) {
            net.minecraft.world.item.component.CustomData customData = eggStack.get(net.minecraft.core.component.DataComponents.ENTITY_DATA);
            if (customData != null) {
                CompoundTag tag = customData.copyTag();
                if (tag.contains("CowDefinitionId")) {
                    String idStr = tag.getString("CowDefinitionId");
                    ResourceLocation defId = ResourceLocation.tryParse(idStr);
                    if (defId != null) {
                        return level.isClientSide ? com.materialcows.network.ClientCowData.getDefinition(defId) : com.materialcows.data.MaterialCowDataLoader.getDefinition(defId);
                    }
                }
            }
        }
        return null;
    }

    public void tick(Level lvl, BlockPos pos, BlockState state) {
        tickBucketFilling();

        CowDefinition def = getCowDefinitionFromEgg();
        if (def != null) {
            Fluid fluid = BuiltInRegistries.FLUID.get(def.fluidId());
            if (fluid != null && fluid != net.minecraft.world.level.material.Fluids.EMPTY) {
                FluidStack fluidStack = new FluidStack(fluid, 1000);
                // FluidTank fill returns the amount filled, checking if we can accept 1000 mB of this fluid
                int filled = fluidTank.fill(fluidStack, IFluidHandler.FluidAction.SIMULATE);
                if (filled >= 1000) {
                    if (milkingTimer > 0) {
                        milkingTimer--;
                        setChanged();
                    } else {
                        fluidTank.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
                        milkingTimer = 400;
                        setChanged();
                        lvl.sendBlockUpdated(pos, state, state, 3);
                    }
                }
            }
        } else {
            if (milkingTimer != 400) {
                milkingTimer = 400;
                setChanged();
            }
        }
    }

    private void tickBucketFilling() {
        ItemStack emptyBucket = itemHandler.getStackInSlot(1);
        if (emptyBucket.is(Items.BUCKET) && fluidTank.getFluidAmount() >= 1000) {
            Fluid fluid = fluidTank.getFluid().getFluid();
            Item filledBucketItem = fluid.getBucket();
            if (filledBucketItem != Items.AIR) {
                ItemStack filledBucket = new ItemStack(filledBucketItem);
                ItemStack outputStack = itemHandler.getStackInSlot(2);
                if (outputStack.isEmpty() || (outputStack.is(filledBucketItem) && outputStack.getCount() < outputStack.getMaxStackSize())) {
                    fluidTank.drain(1000, IFluidHandler.FluidAction.EXECUTE);
                    itemHandler.getStackInSlot(1).shrink(1);
                    if (outputStack.isEmpty()) {
                        itemHandler.setStackInSlot(2, filledBucket);
                    } else {
                        outputStack.grow(1);
                    }
                    setChanged();
                }
            }
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.itemHandler.deserializeNBT(registries, tag.getCompound("Inventory"));
        this.fluidTank.readFromNBT(registries, tag.getCompound("FluidTank"));
        this.milkingTimer = tag.getInt("MilkingTimer");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", this.itemHandler.serializeNBT(registries));
        CompoundTag tankTag = new CompoundTag();
        this.fluidTank.writeToNBT(registries, tankTag);
        tag.put("FluidTank", tankTag);
        tag.putInt("MilkingTimer", this.milkingTimer);
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
        return Component.translatable("container.materialcows.cow_cage");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new CowCageMenu(id, playerInventory, this, this.dataAccess);
    }
}
