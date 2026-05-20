package com.materialcows.item;

import com.materialcows.Materialcows;
import com.materialcows.entity.MaterialCowEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public class CowStickItem extends Item {
    
    public CowStickItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (target instanceof MaterialCowEntity cow) {
            if (!player.level().isClientSide) {
                ResourceLocation defId = cow.getCowDefinitionId();
                if (defId != null) {
                    ItemStack cowStack = new ItemStack(Materialcows.CAPTURED_COW.get());
                    
                    CompoundTag entityData = new CompoundTag();
                    entityData.putString("id", "materialcows:material_cow");
                    entityData.putString("CowDefinitionId", defId.toString());
                    entityData.putInt("MilkingCooldown", cow.getMilkingCooldown());
                    
                    if (cow.hasCustomName()) {
                        String nameStr = cow.getCustomName().getString();
                        if (!nameStr.endsWith(" Spawn egg") && !nameStr.endsWith(" Spawn Egg")) {
                            cowStack.set(DataComponents.CUSTOM_NAME, cow.getCustomName());
                        }
                    }
                    
                    cowStack.set(DataComponents.ENTITY_DATA, CustomData.of(entityData));
                    
                    ItemEntity itemEntity = new ItemEntity(
                            player.level(), cow.getX(), cow.getY(), cow.getZ(), cowStack
                    );
                    itemEntity.setDefaultPickUpDelay();
                    player.level().addFreshEntity(itemEntity);
                    
                    cow.discard();
                    
                    stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
                    
                    if (player.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.POOF,
                                cow.getX(), cow.getY() + 0.5D, cow.getZ(), 20, 0.3D, 0.3D, 0.3D, 0.1D);
                        serverLevel.playSound(null, cow.getX(), cow.getY(), cow.getZ(),
                                SoundEvents.CHICKEN_EGG, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                    }
                }
            }
            return InteractionResult.sidedSuccess(player.level().isClientSide);
        }
        return super.interactLivingEntity(stack, player, target, hand);
    }
}
