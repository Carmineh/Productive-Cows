package com.materialcows.block.entity;

import com.materialcows.Materialcows;
import com.materialcows.data.CowDefinition;
import com.materialcows.menu.BreedingCageMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.Map;
import java.util.Random;

public class BreedingCageBlockEntity extends BlockEntity implements MenuProvider {

    private final ItemStackHandler itemHandler = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == 0 || slot == 1) {
                return stack.is(Materialcows.CAPTURED_COW.get());
            } else if (slot == 2) {
                return stack.is(Items.WHEAT);
            }
            return false; // slot 3 is output
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot == 3) return stack; // Cannot insert into output
            return super.insertItem(slot, stack, simulate);
        }
    };

    public net.neoforged.neoforge.items.IItemHandler getItemHandlerCapability() {
        return itemHandler;
    }

    private int progress = 0;
    private int maxProgress = 200; // 10 seconds

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> BreedingCageBlockEntity.this.progress;
                case 1 -> BreedingCageBlockEntity.this.maxProgress;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> BreedingCageBlockEntity.this.progress = value;
                case 1 -> BreedingCageBlockEntity.this.maxProgress = value;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    private final Random random = new Random();

    public BreedingCageBlockEntity(BlockPos pos, BlockState blockState) {
        super(Materialcows.BREEDING_CAGE_BE.get(), pos, blockState);
    }

    public void serverTick(Level level, BlockPos pos, BlockState state, BreedingCageBlockEntity blockEntity) {
        if (level.isClientSide) return;

        boolean hasRecipe = hasRecipe();
        if (hasRecipe) {
            this.progress++;
            setChanged(level, pos, state);
            if (this.progress >= this.maxProgress) {
                craftItem();
            }
        } else {
            if (this.progress > 0) {
                this.progress--; // slowly decay if recipe becomes invalid
                setChanged(level, pos, state);
            }
        }
    }

    private boolean hasRecipe() {
        ItemStack parent1Stack = itemHandler.getStackInSlot(0);
        ItemStack parent2Stack = itemHandler.getStackInSlot(1);
        ItemStack wheatStack = itemHandler.getStackInSlot(2);
        
        if (parent1Stack.isEmpty() || parent2Stack.isEmpty() || wheatStack.isEmpty()) {
            return false;
        }

        ResourceLocation def1 = getCowDef(parent1Stack);
        ResourceLocation def2 = getCowDef(parent2Stack);

        if (def1 == null || def2 == null) {
            return false;
        }

        if (hasBreedingCooldown(parent1Stack) || hasBreedingCooldown(parent2Stack)) {
            return false;
        }

        ItemStack result = getBreedingResult(def1, def2);
        if (result.isEmpty()) {
            return false;
        }

        return canInsertIntoOutput(result);
    }

    private void craftItem() {
        ItemStack parent1Stack = itemHandler.getStackInSlot(0);
        ItemStack parent2Stack = itemHandler.getStackInSlot(1);
        ResourceLocation def1 = getCowDef(parent1Stack);
        ResourceLocation def2 = getCowDef(parent2Stack);

        ItemStack result = getBreedingResult(def1, def2);

        itemHandler.extractItem(2, 1, false); // Consume 1 wheat

        ItemStack outputSlot = itemHandler.getStackInSlot(3);
        if (outputSlot.isEmpty()) {
            itemHandler.setStackInSlot(3, result);
        } else {
            outputSlot.grow(result.getCount());
        }

        // Apply breeding cooldown (6000 ticks = 5 minutes)
        applyBreedingCooldown(parent1Stack);
        applyBreedingCooldown(parent2Stack);

        this.progress = 0;
    }

    private ResourceLocation getCowDef(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.ENTITY_DATA);
        if (customData != null) {
            CompoundTag tag = customData.copyTag();
            if (tag.contains("CowDefinitionId")) {
                return ResourceLocation.tryParse(tag.getString("CowDefinitionId"));
            }
        }
        return null;
    }

    private boolean hasBreedingCooldown(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.ENTITY_DATA);
        if (customData != null) {
            CompoundTag tag = customData.copyTag();
            return tag.getInt("BreedingCooldown") > 0;
        }
        return false;
    }

    private void applyBreedingCooldown(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.ENTITY_DATA);
        CompoundTag tag = customData != null ? customData.copyTag() : new CompoundTag();
        tag.putInt("BreedingCooldown", 6000);
        stack.set(DataComponents.ENTITY_DATA, CustomData.of(tag));
    }

    private ItemStack getBreedingResult(ResourceLocation p1, ResourceLocation p2) {
        if (p1.equals(p2)) {
            return createCowItem(p1);
        }

        Map<ResourceLocation, CowDefinition> defs = Materialcows.getActiveDefinitions();
        for (CowDefinition def : defs.values()) {
            ResourceLocation defP1 = def.parent1();
            ResourceLocation defP2 = def.parent2();
            if (defP1 != null && defP2 != null) {
                if ((defP1.equals(p1) && defP2.equals(p2)) || (defP1.equals(p2) && defP2.equals(p1))) {
                    if (random.nextDouble() < def.breedChance()) {
                        return createCowItem(def.id());
                    } else {
                        // Failed mutation, return one of the parents randomly
                        return createCowItem(random.nextBoolean() ? p1 : p2);
                    }
                }
            }
        }
        return ItemStack.EMPTY; // No valid recipe
    }

    private ItemStack createCowItem(ResourceLocation defId) {
        ItemStack stack = new ItemStack(Materialcows.CAPTURED_COW.get());
        CompoundTag entityData = new CompoundTag();
        entityData.putString("id", "materialcows:material_cow");
        entityData.putString("CowDefinitionId", defId.toString());
        stack.set(DataComponents.ENTITY_DATA, CustomData.of(entityData));
        return stack;
    }

    private boolean canInsertIntoOutput(ItemStack stack) {
        ItemStack output = itemHandler.getStackInSlot(3);
        if (output.isEmpty()) {
            return true;
        }
        if (!ItemStack.isSameItemSameComponents(output, stack)) {
            return false;
        }
        return output.getCount() + stack.getCount() <= output.getMaxStackSize();
    }

    public ItemStackHandler getInventory() {
        return itemHandler;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", itemHandler.serializeNBT(registries));
        tag.putInt("progress", progress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        itemHandler.deserializeNBT(registries, tag.getCompound("inventory"));
        progress = tag.getInt("progress");
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.materialcows.breeding_cage");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new BreedingCageMenu(id, playerInventory, this, this.data);
    }
}
