package com.materialcows.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.registries.BuiltInRegistries;
import java.util.List;

public class UpgradeItem extends Item {
    private final String tooltipKey;

    public UpgradeItem(Properties properties, String tooltipKey) {
        super(properties);
        this.tooltipKey = tooltipKey;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (player == null) {
            return InteractionResult.PASS;
        }

        BlockState state = level.getBlockState(pos);
        if (BuiltInRegistries.BLOCK.getKey(state.getBlock()).getNamespace().equals("materialcows")) {
            IItemHandler itemHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, context.getClickedFace());
            if (itemHandler != null) {
                ItemStack remainder = ItemHandlerHelper.insertItem(itemHandler, stack.copy(), false);
                if (remainder.getCount() != stack.getCount()) {
                    if (!level.isClientSide) {
                        player.setItemInHand(context.getHand(), remainder);
                    }
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            }
        }

        return super.useOn(context);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable(this.tooltipKey).withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
