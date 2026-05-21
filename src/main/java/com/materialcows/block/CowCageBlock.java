package com.materialcows.block;

import com.materialcows.Materialcows;
import com.materialcows.block.entity.CowCageBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

public class CowCageBlock extends Block implements EntityBlock {

    public CowCageBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CowCageBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return (lvl, pos, st, be) -> {
            if (be instanceof CowCageBlockEntity cage) {
                cage.tick(lvl, pos, st);
            }
        };
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(Items.BUCKET)) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof CowCageBlockEntity cage) {
                FluidTank tank = cage.getFluidTank();
                if (tank.getFluidAmount() >= 1000) {
                    Fluid fluid = tank.getFluid().getFluid();
                    Item filledBucketItem = fluid.getBucket();
                    if (fluid == com.materialcows.fluid.ModFluids.LIQUID_MILK.source().get()) {
                        filledBucketItem = Items.MILK_BUCKET;
                    }
                    if (filledBucketItem != Items.AIR) {
                        if (!level.isClientSide) {
                            tank.drain(1000, IFluidHandler.FluidAction.EXECUTE);
                            level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                            
                            ItemStack filledBucket = new ItemStack(filledBucketItem);
                            if (!player.getAbilities().instabuild) {
                                stack.shrink(1);
                            }
                            if (stack.isEmpty()) {
                                player.setItemInHand(hand, filledBucket);
                            } else {
                                if (!player.getInventory().add(filledBucket)) {
                                    player.drop(filledBucket, false);
                                }
                            }
                        }
                        return ItemInteractionResult.sidedSuccess(level.isClientSide);
                    }
                }
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof CowCageBlockEntity cage) {
                player.openMenu(cage, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof CowCageBlockEntity cage) {
                net.minecraft.world.SimpleContainer container = new net.minecraft.world.SimpleContainer(cage.getInventory().getSlots());
                for (int i = 0; i < cage.getInventory().getSlots(); i++) {
                    container.setItem(i, cage.getInventory().getStackInSlot(i));
                }
                Containers.dropContents(level, pos, container);
                level.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}
