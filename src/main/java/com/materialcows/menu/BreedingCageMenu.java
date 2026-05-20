package com.materialcows.menu;

import com.materialcows.Materialcows;
import com.materialcows.block.entity.BreedingCageBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

public class BreedingCageMenu extends AbstractContainerMenu {

    private final BlockEntity blockEntity;
    private final ContainerData data;

    public BreedingCageMenu(int windowId, Inventory inv, RegistryFriendlyByteBuf buf) {
        this(windowId, inv, inv.player.level().getBlockEntity(buf.readBlockPos()), new SimpleContainerData(2));
    }

    public BreedingCageMenu(int windowId, Inventory inv, BlockEntity be, ContainerData data) {
        super(Materialcows.BREEDING_CAGE_MENU.get(), windowId);
        checkContainerDataCount(data, 2);
        this.blockEntity = be;
        this.data = data;

        if (be instanceof BreedingCageBlockEntity cage) {
            // Slot 0: Parent 1 (X=44, Y=17)
            this.addSlot(new SlotItemHandler(cage.getInventory(), 0, 44, 17) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.is(Materialcows.CAPTURED_COW.get());
                }
            });
            // Slot 1: Parent 2 (X=44, Y=53)
            this.addSlot(new SlotItemHandler(cage.getInventory(), 1, 44, 53) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.is(Materialcows.CAPTURED_COW.get());
                }
            });
            // Slot 2: Wheat (X=80, Y=53)
            this.addSlot(new SlotItemHandler(cage.getInventory(), 2, 80, 53) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.is(Items.WHEAT);
                }
            });
            // Slot 3: Output (X=116, Y=35)
            this.addSlot(new SlotItemHandler(cage.getInventory(), 3, 116, 35) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            });
        }

        // Player Inventory
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(inv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(inv, k, 8 + k * 18, 142));
        }

        this.addDataSlots(data);
    }

    public int getProgress() {
        return data.get(0);
    }

    public int getMaxProgress() {
        return data.get(1);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(net.minecraft.world.inventory.ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()), player, blockEntity.getBlockState().getBlock());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < 4) { // BE slots
                if (!this.moveItemStackTo(itemstack1, 4, 40, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (itemstack1.is(Materialcows.CAPTURED_COW.get())) {
                    if (!this.moveItemStackTo(itemstack1, 0, 2, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (itemstack1.is(Items.WHEAT)) {
                    if (!this.moveItemStackTo(itemstack1, 2, 3, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (index < 31) {
                        if (!this.moveItemStackTo(itemstack1, 31, 40, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!this.moveItemStackTo(itemstack1, 4, 31, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);
        }

        return itemstack;
    }
}
