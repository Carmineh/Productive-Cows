package com.materialcows.block.entity;

import com.materialcows.Materialcows;
import com.materialcows.data.CowDefinition;
import com.materialcows.menu.LiquidCoolerMenu;
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

public class LiquidCoolerBlockEntity extends BlockEntity implements MenuProvider {

    private final ItemStackHandler itemHandler = new ItemStackHandler(5) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == 0) {
                return getFluidFromBucket(stack) != null;
            }
            if (slot == 2) {
                return stack.is(Materialcows.SPEED_UPGRADE.get());
            }
            if (slot == 3) {
                return stack.is(Materialcows.EFFICIENCY_UPGRADE.get());
            }
            return false; // Slots 1 and 4 are output only
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot == 1 || slot == 4) return stack;
            return super.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot == 0 || slot == 2 || slot == 3) return ItemStack.EMPTY;
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

    private final FluidTank fluidTank = new FluidTank(8000) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    private int cookTime = 0;

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> cookTime;
                case 1 -> {
                    int speedUpgrades = Math.max(0, Math.min(3, itemHandler.getStackInSlot(2).getCount()));
                    yield getCookTimeTotalForSpeed(speedUpgrades);
                }
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                cookTime = value;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public LiquidCoolerBlockEntity(BlockPos pos, BlockState state) {
        super(Materialcows.LIQUID_COOLER_BE.get(), pos, state);
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

    public int getCookTime() {
        return cookTime;
    }

    @Nullable
    public static FluidStack getFluidFromBucket(ItemStack stack) {
        if (stack.is(Items.WATER_BUCKET)) {
            return new FluidStack(net.minecraft.world.level.material.Fluids.WATER, 1000);
        }
        if (stack.is(Items.LAVA_BUCKET)) {
            return new FluidStack(net.minecraft.world.level.material.Fluids.LAVA, 1000);
        }
        for (com.materialcows.fluid.ModFluids.RegisteredFluid rf : com.materialcows.fluid.ModFluids.ALL_FLUIDS) {
            if (stack.is(rf.bucket().get())) {
                return new FluidStack(rf.source().get(), 1000);
            }
        }
        return null;
    }

    public void tick(Level lvl, BlockPos pos, BlockState state) {
        tickBucketEmptying();

        FluidStack tankFluid = fluidTank.getFluid();
        if (!tankFluid.isEmpty()) {
            ResourceLocation fluidKey = BuiltInRegistries.FLUID.getKey(tankFluid.getFluid());
            CowDefinition matchingDef = null;
            for (CowDefinition def : Materialcows.getActiveDefinitions().values()) {
                if (def.fluidId().equals(fluidKey)) {
                    matchingDef = def;
                    break;
                }
            }

            if (matchingDef != null && matchingDef.coolingResult() != null) {
                Item resultItem = BuiltInRegistries.ITEM.get(matchingDef.coolingResult());
                if (resultItem != Items.AIR) {
                    int speedUpgrades = Math.max(0, Math.min(3, itemHandler.getStackInSlot(2).getCount()));
                    int efficiencyUpgrades = Math.max(0, Math.min(3, itemHandler.getStackInSlot(3).getCount()));

                    int cookTimeTotal = getCookTimeTotalForSpeed(speedUpgrades);
                    int fluidCost = getFluidCostForEfficiency(efficiencyUpgrades);

                    if (fluidTank.getFluidAmount() >= fluidCost) {
                        ItemStack outputSlot = itemHandler.getStackInSlot(4);
                        if (outputSlot.isEmpty() || (outputSlot.is(resultItem) && outputSlot.getCount() < outputSlot.getMaxStackSize())) {
                            cookTime++;
                            if (cookTime >= cookTimeTotal) {
                                fluidTank.drain(fluidCost, IFluidHandler.FluidAction.EXECUTE);
                                if (outputSlot.isEmpty()) {
                                    itemHandler.setStackInSlot(4, new ItemStack(resultItem));
                                } else {
                                    outputSlot.grow(1);
                                }
                                cookTime = 0;
                                setChanged();
                                lvl.sendBlockUpdated(pos, state, state, 3);
                            }
                            setChanged();
                        } else {
                            if (cookTime != 0) {
                                cookTime = 0;
                                setChanged();
                            }
                        }
                    } else {
                        if (cookTime != 0) {
                            cookTime = 0;
                            setChanged();
                        }
                    }
                    return;
                }
            }
        }

        if (cookTime != 0) {
            cookTime = 0;
            setChanged();
        }
    }

    private int getCookTimeTotalForSpeed(int level) {
        return switch (level) {
            case 1 -> 60;
            case 2 -> 40;
            case 3 -> 20;
            default -> 100;
        };
    }

    private int getFluidCostForEfficiency(int level) {
        return switch (level) {
            case 1 -> 800;
            case 2 -> 650;
            case 3 -> 500;
            default -> 1000;
        };
    }

    private void tickBucketEmptying() {
        ItemStack inputBucket = itemHandler.getStackInSlot(0);
        if (!inputBucket.isEmpty()) {
            FluidStack bucketFluid = getFluidFromBucket(inputBucket);
            if (bucketFluid != null) {
                int filled = fluidTank.fill(bucketFluid, IFluidHandler.FluidAction.SIMULATE);
                if (filled >= 1000) {
                    ItemStack outputSlot = itemHandler.getStackInSlot(1);
                    if (outputSlot.isEmpty() || (outputSlot.is(Items.BUCKET) && outputSlot.getCount() < outputSlot.getMaxStackSize())) {
                        fluidTank.fill(bucketFluid, IFluidHandler.FluidAction.EXECUTE);
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
        this.cookTime = tag.getInt("CookTime");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", this.itemHandler.serializeNBT(registries));
        CompoundTag tankTag = new CompoundTag();
        this.fluidTank.writeToNBT(registries, tankTag);
        tag.put("FluidTank", tankTag);
        tag.putInt("CookTime", this.cookTime);
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
        return Component.translatable("container.materialcows.liquid_cooler");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new LiquidCoolerMenu(id, playerInventory, this, this.dataAccess);
    }
}
