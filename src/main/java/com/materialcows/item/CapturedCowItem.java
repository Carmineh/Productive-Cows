package com.materialcows.item;

import com.materialcows.Materialcows;
import com.materialcows.data.CowDefinition;
import com.materialcows.entity.MaterialCowEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class CapturedCowItem extends Item {

    public CapturedCowItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockPos clickedPos = context.getClickedPos();
        BlockState blockState = level.getBlockState(clickedPos);
        BlockPos spawnPos = clickedPos;
        if (!blockState.getCollisionShape(level, clickedPos).isEmpty()) {
            spawnPos = clickedPos.relative(context.getClickedFace());
        }

        ItemStack stack = context.getItemInHand();
        CustomData customData = stack.get(DataComponents.ENTITY_DATA);
        if (customData != null) {
            CompoundTag tag = customData.copyTag();
            if (tag.contains("CowDefinitionId")) {
                String idStr = tag.getString("CowDefinitionId");
                ResourceLocation defId = ResourceLocation.tryParse(idStr);
                if (defId != null) {
                    MaterialCowEntity cow = new MaterialCowEntity(Materialcows.MATERIAL_COW.get(), level);
                    cow.setCowDefinitionId(defId);
                    
                    // Set milking cooldown: if none, default to 24000 ticks (20 min) to prevent exploits
                    if (tag.contains("MilkingCooldown")) {
                        cow.setMilkingCooldown(tag.getInt("MilkingCooldown"));
                    } else if (tag.contains("milkingCooldown")) {
                        cow.setMilkingCooldown(tag.getInt("milkingCooldown"));
                    } else {
                        cow.setMilkingCooldown(24000);
                    }

                    if (stack.has(DataComponents.CUSTOM_NAME)) {
                        cow.setCustomName(stack.get(DataComponents.CUSTOM_NAME));
                    }

                    cow.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, 0.0F, 0.0F);
                    level.addFreshEntity(cow);

                    level.playSound(null, spawnPos, SoundEvents.COW_AMBIENT, SoundSource.BLOCKS, 1.0F, 1.0F);

                    if (context.getPlayer() != null && !context.getPlayer().getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                    return InteractionResult.CONSUME;
                }
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        CustomData customData = stack.get(DataComponents.ENTITY_DATA);
        if (customData != null) {
            CompoundTag tag = customData.copyTag();
            if (tag.contains("CowDefinitionId")) {
                String idStr = tag.getString("CowDefinitionId");
                ResourceLocation defId = ResourceLocation.tryParse(idStr);
                if (defId != null) {
                    // Check definition (both on client and server depending on logical side, 
                    // ClientCowData handles client side definitions perfectly!)
                    CowDefinition def = com.materialcows.network.ClientCowData.getDefinition(defId);
                    if (def != null) {
                        tooltip.add(Component.translatable("tooltip.materialcows.captured_cow.tier", def.tier())
                                .withStyle(ChatFormatting.GOLD));
                    } else {
                        tooltip.add(Component.literal("Cow: " + defId.getPath()).withStyle(ChatFormatting.RED));
                    }
                    
                    int cooldown = 0;
                    if (tag.contains("MilkingCooldown")) {
                        cooldown = tag.getInt("MilkingCooldown");
                    } else if (tag.contains("milkingCooldown")) {
                        cooldown = tag.getInt("milkingCooldown");
                    }
                    
                    if (cooldown > 0) {
                        int minutes = cooldown / 1200;
                        int seconds = (cooldown % 1200) / 20;
                        tooltip.add(Component.translatable("tooltip.materialcows.captured_cow.cooldown", minutes, seconds)
                                .withStyle(ChatFormatting.RED));
                    } else {
                        tooltip.add(Component.translatable("tooltip.materialcows.captured_cow.ready")
                                .withStyle(ChatFormatting.GREEN));
                    }

                    int breedingCooldown = tag.getInt("BreedingCooldown");
                    if (breedingCooldown > 0) {
                        int minutes = breedingCooldown / 1200;
                        int seconds = (breedingCooldown % 1200) / 20;
                        tooltip.add(Component.translatable("tooltip.materialcows.captured_cow.breeding_cooldown", minutes, seconds)
                                .withStyle(ChatFormatting.LIGHT_PURPLE));
                    }
                    return;
                }
            }
        }
        tooltip.add(Component.translatable("tooltip.materialcows.captured_cow.empty").withStyle(ChatFormatting.DARK_RED));
    }

    @Override
    public Component getName(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.ENTITY_DATA);
        if (customData != null) {
            CompoundTag tag = customData.copyTag();
            if (tag.contains("CowDefinitionId")) {
                String idStr = tag.getString("CowDefinitionId");
                ResourceLocation defId = ResourceLocation.tryParse(idStr);
                if (defId != null) {
                    CowDefinition def = com.materialcows.data.MaterialCowDataLoader.getDefinition(defId);
                    if (def == null) {
                        def = com.materialcows.network.ClientCowData.getDefinition(defId);
                    }
                    if (def != null) {
                        return Component.literal(def.name());
                    }
                }
            }
        }
        return super.getName(stack);
    }

    @Override
    public void initializeClient(java.util.function.Consumer<net.neoforged.neoforge.client.extensions.common.IClientItemExtensions> consumer) {
        consumer.accept(new net.neoforged.neoforge.client.extensions.common.IClientItemExtensions() {
            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return com.materialcows.client.renderer.CapturedCowItemRenderer.getInstance();
            }
        });
    }
}
