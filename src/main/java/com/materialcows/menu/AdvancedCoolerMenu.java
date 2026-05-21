package com.materialcows.menu;

import com.materialcows.Materialcows;
import com.materialcows.block.entity.AdvancedCoolerBlockEntity;
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

public class AdvancedCoolerMenu extends AbstractContainerMenu {

    private final BlockEntity blockEntity;
    private final ContainerData data;

    // Client Constructor
    public AdvancedCoolerMenu(int windowId, Inventory inv, RegistryFriendlyByteBuf buf) {
        this(windowId, inv, inv.player.level().getBlockEntity(buf.readBlockPos()), new SimpleContainerData(6));
    }

    // Common/Server Constructor
    public AdvancedCoolerMenu(int windowId, Inventory inv, BlockEntity be, ContainerData data) {
        super(Materialcows.ADVANCED_COOLER_MENU.get(), windowId);
        checkContainerDataCount(data, 6);
        this.blockEntity = be;
        this.data = data;

        if (be instanceof AdvancedCoolerBlockEntity cooler) {
            // Slot 0: Bucket emptying input. X=36, Y=17
            this.addSlot(new SlotItemHandler(cooler.getInventory(), 0, 36, 17) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return getFluidFromBucket(stack) != null;
                }
            });
            // Slot 1: Output empty bucket. X=36, Y=53
            this.addSlot(new SlotItemHandler(cooler.getInventory(), 1, 36, 53) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            });
            // Slot 2: Speed upgrade. X=154, Y=17
            this.addSlot(new SlotItemHandler(cooler.getInventory(), 2, 154, 17) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.is(Materialcows.SPEED_UPGRADE.get());
                }
            });
            // Slot 3: Efficiency upgrade. X=154, Y=35
            this.addSlot(new SlotItemHandler(cooler.getInventory(), 3, 154, 35) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.is(Materialcows.EFFICIENCY_UPGRADE.get());
                }
            });
            // Slot 4: Output resource. X=104, Y=35
            this.addSlot(new SlotItemHandler(cooler.getInventory(), 4, 104, 35) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            });
            // Slot 5: Energy upgrade. X=154, Y=53
            this.addSlot(new SlotItemHandler(cooler.getInventory(), 5, 154, 53) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.is(Materialcows.ENERGY_UPGRADE.get());
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

    public int getEnergyStored() {
        return data.get(0);
    }

    public int getMaxEnergyStored() {
        return data.get(1);
    }

    public int getFluidAmount() {
        return data.get(2);
    }

    public int getFluidCapacity() {
        return data.get(3);
    }

    public int getCookTime() {
        return data.get(4);
    }

    public int getCookTimeTotal() {
        return data.get(5);
    }

    private ItemStack getFluidFromBucket(ItemStack stack) {
        if (stack.is(Items.WATER_BUCKET) || stack.is(Items.LAVA_BUCKET)) {
            return stack;
        }
        for (com.materialcows.fluid.ModFluids.RegisteredFluid rf : com.materialcows.fluid.ModFluids.ALL_FLUIDS) {
            if (stack.is(rf.bucket().get())) {
                return stack;
            }
        }
        return null;
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
            if (index < 6) {
                // BE slots -> Player inventory
                if (!this.moveItemStackTo(itemstack1, 6, 42, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Player inventory -> BE slots
                if (itemstack1.is(Materialcows.SPEED_UPGRADE.get())) {
                    if (!this.moveItemStackTo(itemstack1, 2, 3, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (itemstack1.is(Materialcows.EFFICIENCY_UPGRADE.get())) {
                    if (!this.moveItemStackTo(itemstack1, 3, 4, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (itemstack1.is(Materialcows.ENERGY_UPGRADE.get())) {
                    if (!this.moveItemStackTo(itemstack1, 5, 6, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (getFluidFromBucket(itemstack1) != null) {
                    if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    // Hotbar <-> main inventory transfer
                    if (index < 33) {
                        if (!this.moveItemStackTo(itemstack1, 33, 42, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!this.moveItemStackTo(itemstack1, 6, 33, false)) {
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
