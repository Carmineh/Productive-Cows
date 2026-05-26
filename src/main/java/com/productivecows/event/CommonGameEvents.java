package com.productivecows.event;

import com.productivecows.data.CowDefinition;
import com.productivecows.data.MaterialCowDataLoader;
import com.productivecows.network.SyncCowDefinitionsPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;

@EventBusSubscriber(modid = "productivecows")
public class CommonGameEvents {

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        // Register our custom JSON reload listener to the server's resource reload listener list
        event.addListener(new MaterialCowDataLoader());
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            // When a player logs in, send the loaded cow definitions from Server to Client
            var definitions = new ArrayList<>(MaterialCowDataLoader.getDefinitions().values());
            PacketDistributor.sendToPlayer(serverPlayer, new SyncCowDefinitionsPayload(definitions));
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteract event) {
        net.minecraft.world.entity.player.Player player = event.getEntity();
        net.minecraft.world.InteractionHand hand = event.getHand();
        net.minecraft.world.item.ItemStack itemstack = player.getItemInHand(hand);
        net.minecraft.world.entity.Entity target = event.getTarget();

        if (target instanceof net.minecraft.world.entity.animal.Cow vanillaCow && !(vanillaCow instanceof com.productivecows.entity.MaterialCowEntity)) {
            String targetDef = null;
            boolean isBucket = false;

            if (itemstack.is(net.minecraft.world.item.Items.WATER_BUCKET)) {
                targetDef = "water";
                isBucket = true;
            } else if (itemstack.is(net.minecraft.world.item.Items.LAVA_BUCKET)) {
                targetDef = "lava";
                isBucket = true;
            } else if (itemstack.is(net.minecraft.world.item.Items.CLAY) || itemstack.is(net.minecraft.world.item.Items.CLAY_BALL)) {
                targetDef = "clay";
            } else if (itemstack.is(net.minecraft.world.item.Items.STONE) || itemstack.is(net.minecraft.world.item.Items.COBBLESTONE)) {
                targetDef = "stone";
            }

            if (targetDef != null) {
                net.minecraft.world.level.Level level = event.getLevel();
                if (!level.isClientSide() && level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                    com.productivecows.entity.MaterialCowEntity materialCow = com.productivecows.ProductiveCows.MATERIAL_COW.get().create(serverLevel);
                    if (materialCow != null) {
                        materialCow.moveTo(vanillaCow.getX(), vanillaCow.getY(), vanillaCow.getZ(), vanillaCow.getYRot(), vanillaCow.getXRot());
                        materialCow.setAge(vanillaCow.getAge());
                        materialCow.setCowDefinitionId(ResourceLocation.fromNamespaceAndPath(com.productivecows.ProductiveCows.MODID, targetDef));
                        
                        if (vanillaCow.hasCustomName()) {
                            materialCow.setCustomName(vanillaCow.getCustomName());
                            materialCow.setCustomNameVisible(vanillaCow.isCustomNameVisible());
                        }

                        // Consume item
                        if (!player.isCreative()) {
                            if (isBucket) {
                                net.minecraft.world.item.ItemStack emptyBucket = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BUCKET);
                                net.minecraft.world.item.ItemStack remaining = net.minecraft.world.item.ItemUtils.createFilledResult(itemstack, player, emptyBucket);
                                player.setItemInHand(hand, remaining);
                            } else {
                                itemstack.shrink(1);
                            }
                        }

                        serverLevel.addFreshEntity(materialCow);
                        vanillaCow.discard();

                        // Particles & Sound
                        serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                                materialCow.getX(), materialCow.getY() + 0.5D, materialCow.getZ(), 20, 0.5D, 0.5D, 0.5D, 1.0D);
                        serverLevel.playSound(null, materialCow.getX(), materialCow.getY(), materialCow.getZ(),
                                net.minecraft.sounds.SoundEvents.ZOMBIE_VILLAGER_CONVERTED, net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, 1.0F);
                    }
                }
                event.setCancellationResult(net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide()));
                event.setCanceled(true);
            }
        }
    }
}
