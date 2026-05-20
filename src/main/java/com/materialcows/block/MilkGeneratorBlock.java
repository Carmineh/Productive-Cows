package com.materialcows.block;

import com.materialcows.Materialcows;
import com.materialcows.block.entity.MilkGeneratorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

public class MilkGeneratorBlock extends Block implements EntityBlock {

    public MilkGeneratorBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MilkGeneratorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return (lvl, pos, st, be) -> {
            if (be instanceof MilkGeneratorBlockEntity generator) {
                generator.tick(lvl, pos, st);
            }
        };
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        boolean isVanillaMilk = stack.is(Items.MILK_BUCKET);
        boolean isCustomMilk = stack.is(com.materialcows.fluid.ModFluids.LIQUID_MILK.bucket().get());
        if (isVanillaMilk || isCustomMilk) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof MilkGeneratorBlockEntity generator) {
                FluidTank tank = generator.getFluidTank();
                Fluid milkFluid = com.materialcows.fluid.ModFluids.LIQUID_MILK.source().get();
                
                FluidStack milkStack = new FluidStack(milkFluid, 1000);
                int accepted = tank.fill(milkStack, IFluidHandler.FluidAction.SIMULATE);
                if (accepted >= 1000) {
                    if (!level.isClientSide) {
                        tank.fill(milkStack, IFluidHandler.FluidAction.EXECUTE);
                        level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                        
                        ItemStack emptyBucket = new ItemStack(Items.BUCKET);
                        if (!player.getAbilities().instabuild) {
                            stack.shrink(1);
                        }
                        if (stack.isEmpty()) {
                            player.setItemInHand(hand, emptyBucket);
                        } else {
                            if (!player.getInventory().add(emptyBucket)) {
                                player.drop(emptyBucket, false);
                            }
                        }
                    }
                    return ItemInteractionResult.sidedSuccess(level.isClientSide);
                }
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof MilkGeneratorBlockEntity generator) {
                player.openMenu(generator, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof MilkGeneratorBlockEntity generator) {
                net.minecraft.world.SimpleContainer container = new net.minecraft.world.SimpleContainer(generator.getInventory().getSlots());
                for (int i = 0; i < generator.getInventory().getSlots(); i++) {
                    container.setItem(i, generator.getInventory().getStackInSlot(i));
                }
                Containers.dropContents(level, pos, container);
                level.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}
