package com.productivecows.entity;

import com.productivecows.ProductiveCows;
import com.productivecows.data.CowDefinition;
import com.productivecows.data.MaterialCowDataLoader;
import com.productivecows.network.ClientCowData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class MaterialCowEntity extends Cow {

    private static final EntityDataAccessor<String> COW_DEFINITION_ID = SynchedEntityData.defineId(MaterialCowEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> MILKING_COOLDOWN = SynchedEntityData.defineId(MaterialCowEntity.class, EntityDataSerializers.INT);

    public MaterialCowEntity(EntityType<? extends Cow> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Cow.createAttributes();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(COW_DEFINITION_ID, "");
        builder.define(MILKING_COOLDOWN, 0);
    }

    public void setCowDefinitionId(ResourceLocation id) {
        this.entityData.set(COW_DEFINITION_ID, id == null ? "" : id.toString());
    }

    @Nullable
    public ResourceLocation getCowDefinitionId() {
        String idStr = this.entityData.get(COW_DEFINITION_ID);
        if (idStr.isEmpty()) {
            return null;
        }
        return ResourceLocation.tryParse(idStr);
    }

    @Nullable
    public CowDefinition getCowDefinition() {
        ResourceLocation id = getCowDefinitionId();
        if (id == null) {
            return null;
        }
        return this.level().isClientSide ? ClientCowData.getDefinition(id) : MaterialCowDataLoader.getDefinition(id);
    }

    public int getMilkingCooldown() {
        return this.entityData.get(MILKING_COOLDOWN);
    }

    public void setMilkingCooldown(int ticks) {
        this.entityData.set(MILKING_COOLDOWN, ticks);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            int cooldown = this.getMilkingCooldown();
            if (cooldown > 0) {
                this.setMilkingCooldown(cooldown - 1);
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        ResourceLocation id = getCowDefinitionId();
        tag.putString("CowDefinitionId", id == null ? "" : id.toString());
        tag.putInt("MilkingCooldown", this.getMilkingCooldown());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("CowDefinitionId")) {
            String idStr = tag.getString("CowDefinitionId");
            if (!idStr.isEmpty()) {
                setCowDefinitionId(ResourceLocation.tryParse(idStr));
            }
        }
        if (tag.contains("MilkingCooldown")) {
            setMilkingCooldown(tag.getInt("MilkingCooldown"));
        }
    }

    @Override
    public Component getName() {
        CowDefinition definition = getCowDefinition();
        if (definition != null) {
            return Component.literal(definition.name());
        }
        return super.getName();
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
        
        // Only assign a random definition if none has been set yet (e.g. from NBT summon commands or breeding)
        if (this.getCowDefinitionId() == null) {
            var definitions = MaterialCowDataLoader.getDefinitions().values();
            if (!definitions.isEmpty()) {
                var list = new ArrayList<>(definitions);
                var randomDef = list.get(this.random.nextInt(list.size()));
                this.setCowDefinitionId(randomDef.id());
            }
        }

        // Set milking cooldown to 24,000 ticks (20 minutes) if spawned from a spawn egg to prevent infinite milking exploits
        if (spawnType == MobSpawnType.SPAWN_EGG) {
            this.setMilkingCooldown(24000);
        }
        
        return data;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        // Milking logic with empty bucket
        if (itemstack.is(Items.BUCKET) && !this.isBaby()) {
            if (!this.level().isClientSide) {
                CowDefinition definition = getCowDefinition();
                if (definition != null) {
                    if (this.getMilkingCooldown() <= 0) {
                        ResourceLocation fluidId = definition.fluidId();
                        Fluid fluid = BuiltInRegistries.FLUID.get(fluidId);
                        
                        if (fluid != null && fluid != Fluids.EMPTY) {
                            Item bucketItem = fluid.getBucket();
                            if (bucketItem != Items.AIR) {
                                // Play milking sound
                                player.playSound(SoundEvents.COW_MILK, 1.0F, 1.0F);

                                // Replace empty bucket with filled fluid bucket
                                ItemStack filledBucket = new ItemStack(bucketItem);
                                ItemStack remaining = ItemUtils.createFilledResult(itemstack, player, filledBucket);
                                player.setItemInHand(hand, remaining);

                                // Set milking cooldown to 24,000 ticks (20 minutes)
                                this.setMilkingCooldown(24000);
                                return InteractionResult.SUCCESS;
                            } else {
                                player.sendSystemMessage(Component.literal("The fluid '" + fluidId + "' does not have an associated bucket registered!"));
                            }
                        } else {
                            player.sendSystemMessage(Component.literal("The fluid '" + fluidId + "' is not registered or does not exist!"));
                        }
                    } else {
                        int remainingSeconds = this.getMilkingCooldown() / 20;
                        player.sendSystemMessage(Component.literal("This cow is not ready to be milked! Cooldown remaining: " + remainingSeconds + " seconds."));
                    }
                } else {
                    player.sendSystemMessage(Component.literal("This cow has no dynamic material definition assigned!"));
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public MaterialCowEntity getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        MaterialCowEntity baby = ProductiveCows.MATERIAL_COW.get().create(level);
        if (baby != null && otherParent instanceof MaterialCowEntity otherMC) {
            ResourceLocation parentA = this.getCowDefinitionId();
            ResourceLocation parentB = otherMC.getCowDefinitionId();

            if (parentA != null && parentB != null) {
                ResourceLocation babyId = null;

                // 1. Scan for custom cross-breeding recipe defined in the dynamic JSONs (parent1 & parent2)
                CowDefinition mutationDef = null;
                for (CowDefinition def : MaterialCowDataLoader.getDefinitions().values()) {
                    if (def.parent1() != null && def.parent2() != null) {
                        if ((def.parent1().equals(parentA) && def.parent2().equals(parentB)) ||
                            (def.parent1().equals(parentB) && def.parent2().equals(parentA))) {
                            mutationDef = def;
                            break;
                        }
                    }
                }

                if (mutationDef != null) {
                    // Mutation recipe exists! Roll based on its configured breed chance.
                    double chance = mutationDef.breedChance();
                    if (this.random.nextDouble() < chance) {
                        babyId = mutationDef.id();
                    }
                }

                // 2. If no mutation recipe exists or the mutation roll failed,
                // fall back to a 50/50 genetic inheritance from the parents.
                if (babyId == null) {
                    babyId = this.random.nextBoolean() ? parentA : parentB;
                }

                baby.setCowDefinitionId(babyId);
            }
        }
        return baby;
    }
}
