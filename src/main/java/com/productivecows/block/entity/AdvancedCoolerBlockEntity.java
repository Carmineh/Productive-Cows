package com.productivecows.block.entity;

import com.productivecows.ProductiveCows;
import com.productivecows.data.CowDefinition;
import com.productivecows.menu.AdvancedCoolerMenu;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class AdvancedCoolerBlockEntity extends BlockEntity implements MenuProvider {

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

        public void onEnergyChanged() {}

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int received = super.receiveEnergy(maxReceive, simulate);
            if (received > 0 && !simulate) onEnergyChanged();
            return received;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int extracted = super.extractEnergy(maxExtract, simulate);
            if (extracted > 0 && !simulate) onEnergyChanged();
            return extracted;
        }
    }

    private final ItemStackHandler itemHandler = new ItemStackHandler(6) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == 0) {
                return getFluidFromBucket(stack) != null;
            }
            if (slot == 2) {
                return stack.is(ProductiveCows.SPEED_UPGRADE.get());
            }
            if (slot == 3) {
                return stack.is(ProductiveCows.EFFICIENCY_UPGRADE.get());
            }
            if (slot == 5) {
                return stack.is(ProductiveCows.ENERGY_UPGRADE.get());
            }
            return false; // Slots 1 and 4 are output only
        }

        @Override
        public int getSlotLimit(int slot) {
            if (slot == 2 || slot == 3 || slot == 5) {
                return 3;
            }
            return super.getSlotLimit(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot == 1 || slot == 4) return stack;
            return super.insertItem(slot, stack, simulate);
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
        public boolean isFluidValid(FluidStack stack) {
            Fluid f = stack.getFluid();
            ResourceLocation fluidKey = BuiltInRegistries.FLUID.getKey(f);
            for (CowDefinition def : ProductiveCows.getActiveDefinitions().values()) {
                if (def.fluidId().equals(fluidKey)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        protected void onContentsChanged() {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    private final CustomEnergyStorage energyStorage = new CustomEnergyStorage(Math.max(1000, com.productivecows.Config.advancedCoolerCapacity), 2000, 2000) {
        @Override
        public void onEnergyChanged() {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };
    private int cookTime = 0;
    private ResourceLocation lastActiveFluid = null;

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> energyStorage.getEnergyStored();
                case 1 -> energyStorage.getMaxEnergyStored();
                case 2 -> fluidTank.getFluidAmount();
                case 3 -> fluidTank.getCapacity();
                case 4 -> cookTime;
                case 5 -> {
                    int speedUpgrades = Math.max(0, Math.min(3, itemHandler.getStackInSlot(2).getCount()));
                    yield getCookTimeTotalForSpeed(speedUpgrades);
                }
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> energyStorage.setEnergy(value);
                case 2 -> {
                    if (fluidTank.getFluid().isEmpty()) {
                        fluidTank.setFluid(FluidStack.EMPTY);
                    } else {
                        fluidTank.setFluid(new FluidStack(fluidTank.getFluid().getFluid(), value));
                    }
                }
                case 4 -> cookTime = value;
            }
        }

        @Override
        public int getCount() {
            return 6;
        }
    };

    public AdvancedCoolerBlockEntity(BlockPos pos, BlockState state) {
        super(ProductiveCows.ADVANCED_COOLER_BE.get(), pos, state);
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

    @Nullable
    public static FluidStack getFluidFromBucket(ItemStack stack) {
        if (stack.is(Items.WATER_BUCKET)) {
            return new FluidStack(net.minecraft.world.level.material.Fluids.WATER, 1000);
        }
        if (stack.is(Items.LAVA_BUCKET)) {
            return new FluidStack(net.minecraft.world.level.material.Fluids.LAVA, 1000);
        }
        for (com.productivecows.fluid.ModFluids.RegisteredFluid rf : com.productivecows.fluid.ModFluids.ALL_FLUIDS) {
            if (stack.is(rf.bucket().get())) {
                return new FluidStack(rf.source().get(), 1000);
            }
        }
        return null;
    }

    public void tick(Level lvl, BlockPos pos, BlockState state) {
        tickAutoInput(lvl, pos);
        tickBucketEmptying();

        boolean processedThisTick = false;
        FluidStack tankFluid = fluidTank.getFluid();
        if (tankFluid.isEmpty()) {
            if (cookTime != 0) {
                cookTime = 0;
                setChanged();
            }
            lastActiveFluid = null;
        } else {
            ResourceLocation currentFluid = BuiltInRegistries.FLUID.getKey(tankFluid.getFluid());
            if (lastActiveFluid != null && !lastActiveFluid.equals(currentFluid)) {
                cookTime = 0;
                setChanged();
            }
            lastActiveFluid = currentFluid;
        }

        if (!tankFluid.isEmpty()) {
            ResourceLocation fluidKey = BuiltInRegistries.FLUID.getKey(tankFluid.getFluid());
            CowDefinition matchingDef = null;
            for (CowDefinition def : ProductiveCows.getActiveDefinitions().values()) {
                if (def.fluidId().equals(fluidKey)) {
                    matchingDef = def;
                    break;
                }
            }

            if (matchingDef != null) {
                Item resultItem = Items.AIR;
                if (matchingDef.coolingResult() != null) {
                    resultItem = BuiltInRegistries.ITEM.get(matchingDef.coolingResult());
                } else if (matchingDef.coolingResultTag() != null) {
                    net.minecraft.tags.TagKey<Item> tagKey = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, ResourceLocation.tryParse(matchingDef.coolingResultTag()));
                    var registry = lvl.registryAccess().registry(net.minecraft.core.registries.Registries.ITEM).orElse(null);
                    if (registry != null) {
                        java.util.Optional<net.minecraft.core.HolderSet.Named<Item>> tagItem = registry.getTag(tagKey);
                        if (tagItem.isPresent() && tagItem.get().size() > 0) {
                            resultItem = tagItem.get().get(0).value();
                        }
                    }
                }

                if (resultItem != Items.AIR) {
                    int speedUpgrades = Math.max(0, Math.min(3, itemHandler.getStackInSlot(2).getCount()));
                    int efficiencyUpgrades = Math.max(0, Math.min(3, itemHandler.getStackInSlot(3).getCount()));
                    int energyUpgrades = Math.max(0, Math.min(3, itemHandler.getStackInSlot(5).getCount()));

                    int cookTimeTotal = getCookTimeTotalForSpeed(speedUpgrades);
                    int fluidCost = getFluidCostForEfficiency(efficiencyUpgrades);
                    
                    int baseEnergyPerTick = getBaseEnergyPerTick(speedUpgrades);
                    float efficiencyMultiplier = getEfficiencyEnergyMultiplier(efficiencyUpgrades);
                    float energyMultiplier = getEnergyUpgradeMultiplier(energyUpgrades);
                    
                    int energyCostPerTick = (int) (baseEnergyPerTick * efficiencyMultiplier * energyMultiplier);

                    if (fluidTank.getFluidAmount() >= fluidCost && energyStorage.getEnergyStored() >= energyCostPerTick) {
                        ItemStack outputSlot = itemHandler.getStackInSlot(4);
                        if (outputSlot.isEmpty() || (outputSlot.is(resultItem) && outputSlot.getCount() < outputSlot.getMaxStackSize())) {
                            
                            energyStorage.consumeEnergy(energyCostPerTick);
                            processedThisTick = true;
                            
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
                        }
                    }
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
        }

        tickAutoOutput(lvl, pos);
    }

    private int getCookTimeTotalForSpeed(int upgrades) {
        int baseTime = Math.max(1, com.productivecows.Config.advancedCoolerCookTime);
        if (upgrades == 1) return (int)(baseTime * 0.75f);
        if (upgrades == 2) return (int)(baseTime * 0.50f);
        if (upgrades == 3) return (int)(baseTime * 0.25f);
        return baseTime;
    }

    private int getFluidCostForEfficiency(int upgrades) {
        int baseCost = Math.max(1, com.productivecows.Config.advancedCoolerFluidCost);
        if (upgrades == 1) return (int)(baseCost * 0.90f);
        if (upgrades == 2) return (int)(baseCost * 0.75f);
        if (upgrades == 3) return (int)(baseCost * 0.50f);
        return baseCost;
    }

    private int getBaseEnergyPerTick(int speedUpgrades) {
        int baseCost = Math.max(0, com.productivecows.Config.advancedCoolerEnergyCost);
        if (speedUpgrades == 1) return (int)(baseCost * 1.5f);
        if (speedUpgrades == 2) return (int)(baseCost * 2.0f);
        if (speedUpgrades == 3) return (int)(baseCost * 3.0f);
        return baseCost;
    }

    private float getEfficiencyEnergyMultiplier(int efficiencyUpgrades) {
        return switch (efficiencyUpgrades) {
            case 1 -> 1.2f;
            case 2 -> 1.5f;
            case 3 -> 2.0f;
            default -> 1.0f;
        };
    }

    private float getEnergyUpgradeMultiplier(int energyUpgrades) {
        return switch (energyUpgrades) {
            case 1 -> 0.7f;
            case 2 -> 0.5f;
            case 3 -> 0.3f;
            default -> 1.0f;
        };
    }

    private void tickAutoInput(Level lvl, BlockPos pos) {
        // Query neighbors in all 6 directions to pull fluid from
        for (Direction dir : Direction.values()) {
            // Check if our fluid tank has room to pull fluid (at least 100 mB room)
            int spaceLeft = fluidTank.getCapacity() - fluidTank.getFluidAmount();
            if (spaceLeft <= 0) {
                break;
            }

            BlockPos targetPos = pos.relative(dir);
            BlockEntity neighborBE = lvl.getBlockEntity(targetPos);
            if (neighborBE instanceof AdvancedCoolerBlockEntity || neighborBE instanceof LiquidCoolerBlockEntity) {
                continue;
            }
            IFluidHandler neighborFluidHandler = lvl.getCapability(Capabilities.FluidHandler.BLOCK, targetPos, dir.getOpposite());
            if (neighborFluidHandler != null) {
                // If our local tank has fluid, we can only pull matching fluid. If empty, we can pull any coolable fluid.
                FluidStack localFluid = fluidTank.getFluid();
                int amountToDrain = Math.min(spaceLeft, 1000); // Pull up to 1000 mB per tick

                if (localFluid.isEmpty()) {
                    // Try to simulate draining up to 1000 mB from the neighbor to check what fluid they have
                    FluidStack drainedSimulated = neighborFluidHandler.drain(amountToDrain, IFluidHandler.FluidAction.SIMULATE);
                    if (!drainedSimulated.isEmpty()) {
                        // Check if this fluid is coolable
                        ResourceLocation fluidKey = BuiltInRegistries.FLUID.getKey(drainedSimulated.getFluid());
                        boolean isCoolable = false;
                        for (CowDefinition def : ProductiveCows.getActiveDefinitions().values()) {
                            if (def.fluidId().equals(fluidKey)) {
                                isCoolable = true;
                                break;
                            }
                        }
                        if (isCoolable) {
                            // Pull the fluid actively
                            FluidStack drainedActive = neighborFluidHandler.drain(amountToDrain, IFluidHandler.FluidAction.EXECUTE);
                            if (!drainedActive.isEmpty()) {
                                fluidTank.fill(drainedActive, IFluidHandler.FluidAction.EXECUTE);
                                setChanged();
                            }
                        }
                    }
                } else {
                    // Only try to pull matching fluid
                    FluidStack targetToPullSimulate = new FluidStack(localFluid.getFluid(), amountToDrain);
                    FluidStack drainedActive = neighborFluidHandler.drain(targetToPullSimulate, IFluidHandler.FluidAction.EXECUTE);
                    if (!drainedActive.isEmpty()) {
                        fluidTank.fill(drainedActive, IFluidHandler.FluidAction.EXECUTE);
                        setChanged();
                    }
                }
            }
        }
    }

    private void tickAutoOutput(Level lvl, BlockPos pos) {
        ItemStack outputStack = itemHandler.getStackInSlot(4);
        if (!outputStack.isEmpty()) {
            BlockPos targetPos = pos.above();
            IItemHandler neighborItemHandler = lvl.getCapability(Capabilities.ItemHandler.BLOCK, targetPos, Direction.DOWN);
            if (neighborItemHandler != null) {
                ItemStack remainder = ItemHandlerHelper.insertItem(neighborItemHandler, outputStack.copy(), false);
                if (remainder.getCount() != outputStack.getCount()) {
                    itemHandler.setStackInSlot(4, remainder);
                    setChanged();
                }
            }
        }
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
        if (this.itemHandler.getSlots() < 6) {
            this.itemHandler.setSize(6);
        }
        this.fluidTank.readFromNBT(registries, tag.getCompound("FluidTank"));
        this.energyStorage.setEnergy(tag.getInt("Energy"));
        this.cookTime = tag.getInt("CookTime");
        if (tag.contains("LastActiveFluid")) {
            this.lastActiveFluid = ResourceLocation.tryParse(tag.getString("LastActiveFluid"));
        } else {
            this.lastActiveFluid = null;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", this.itemHandler.serializeNBT(registries));
        CompoundTag tankTag = new CompoundTag();
        this.fluidTank.writeToNBT(registries, tankTag);
        tag.put("FluidTank", tankTag);
        tag.putInt("Energy", this.energyStorage.getEnergyStored());
        tag.putInt("CookTime", this.cookTime);
        if (this.lastActiveFluid != null) {
            tag.putString("LastActiveFluid", this.lastActiveFluid.toString());
        }
    }

    // Getters for Jade/WAILA
    public int getCookTime() {
        return this.cookTime;
    }
    
    public int getCookTimeTotal() {
        int speedUpgrades = Math.max(0, Math.min(3, itemHandler.getStackInSlot(2).getCount()));
        return getCookTimeTotalForSpeed(speedUpgrades);
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
        return Component.translatable("container.productivecows.advanced_cooler");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new AdvancedCoolerMenu(id, playerInventory, this, this.dataAccess);
    }
}
