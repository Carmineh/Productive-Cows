package com.materialcows.menu;

import com.materialcows.Materialcows;
import com.materialcows.block.entity.CowCageBlockEntity;
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

public class CowCageMenu extends AbstractContainerMenu {

    private final BlockEntity blockEntity;
    private final ContainerData data;

    // Client Constructor
    public CowCageMenu(int windowId, Inventory inv, RegistryFriendlyByteBuf buf) {
        this(windowId, inv, inv.player.level().getBlockEntity(buf.readBlockPos()), new SimpleContainerData(1));
    }

    // Common/Server Constructor
    public CowCageMenu(int windowId, Inventory inv, BlockEntity be, ContainerData data) {
        super(Materialcows.COW_CAGE_MENU.get(), windowId);
        checkContainerDataCount(data, 1);
        this.blockEntity = be;
        this.data = data;

        if (be instanceof CowCageBlockEntity cage) {
            this.addSlot(new SlotItemHandler(cage.getInventory(), 0, 80, 20) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.is(Materialcows.CAPTURED_COW.get()) || stack.is(Materialcows.MATERIAL_COW_SPAWN_EGG.get());
                }

                @Override
                public int getMaxStackSize() {
                    return 1;
                }

                @Override
                public int getMaxStackSize(ItemStack stack) {
                    return 1;
                }
            });
            // Slot 1: Empty bucket input
            this.addSlot(new SlotItemHandler(cage.getInventory(), 1, 35, 51) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.is(Items.BUCKET);
                }
            });
            // Slot 2: Output bucket
            this.addSlot(new SlotItemHandler(cage.getInventory(), 2, 125, 51) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            });
        }

        // Player Inventory (27 slots)
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(inv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        // Player Hotbar (9 slots)
        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(inv, k, 8 + k * 18, 142));
        }

        this.addDataSlots(data);
    }

    public BlockEntity getBlockEntity() {
        return blockEntity;
    }

    public int getMilkingTimer() {
        return data.get(0);
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
            if (index < 3) {
                // Block Entity slots -> Player inventory
                if (!this.moveItemStackTo(itemstack1, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Player inventory -> Block Entity slots
                if (itemstack1.is(Materialcows.CAPTURED_COW.get()) || itemstack1.is(Materialcows.MATERIAL_COW_SPAWN_EGG.get())) {
                    if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (itemstack1.is(Items.BUCKET)) {
                    if (!this.moveItemStackTo(itemstack1, 1, 2, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    // Hotbar <-> main inventory transfer
                    if (index < 30) {
                        if (!this.moveItemStackTo(itemstack1, 30, 39, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!this.moveItemStackTo(itemstack1, 3, 30, false)) {
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
