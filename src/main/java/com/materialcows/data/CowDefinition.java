package com.materialcows.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record CowDefinition(
        ResourceLocation id,
        String name,
        int tier,
        int hexColor,
        ResourceLocation fluidId,
        ResourceLocation parent1,
        ResourceLocation parent2,
        double breedChance,
        ResourceLocation coolingResult
) {
    // Codec used to parse fields from a JSON file.
    // The 'id' is omitted here as it will be mapped from the file location itself.
    public static final Codec<CowDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(CowDefinition::name),
            Codec.INT.fieldOf("tier").forGetter(CowDefinition::tier),
            Codec.STRING.fieldOf("hex_color").xmap(
                    hex -> {
                        try {
                            if (hex.startsWith("#")) {
                                return Integer.parseInt(hex.substring(1), 16);
                            }
                            return Integer.parseInt(hex, 16);
                        } catch (NumberFormatException e) {
                            return 0xFFFFFF; // Fallback to white on error
                        }
                    },
                    color -> String.format("#%06X", color & 0xFFFFFF)
            ).forGetter(CowDefinition::hexColor),
            ResourceLocation.CODEC.fieldOf("fluid").forGetter(CowDefinition::fluidId),
            ResourceLocation.CODEC.optionalFieldOf("parent1").forGetter(def -> Optional.ofNullable(def.parent1())),
            ResourceLocation.CODEC.optionalFieldOf("parent2").forGetter(def -> Optional.ofNullable(def.parent2())),
            Codec.DOUBLE.optionalFieldOf("breed_chance", 1.0).forGetter(CowDefinition::breedChance),
            ResourceLocation.CODEC.optionalFieldOf("cooling_result").forGetter(def -> Optional.ofNullable(def.coolingResult()))
    ).apply(instance, (name, tier, hexColor, fluidId, p1Opt, p2Opt, breedChance, coolingResultOpt) -> new CowDefinition(
            null, name, tier, hexColor, fluidId, p1Opt.orElse(null), p2Opt.orElse(null), breedChance, coolingResultOpt.orElse(null)
    )));

    // Convenience method to attach the ID after reading from a JSON pack
    public CowDefinition withId(ResourceLocation id) {
        return new CowDefinition(id, this.name, this.tier, this.hexColor, this.fluidId, this.parent1, this.parent2, this.breedChance, this.coolingResult);
    }

    // Modern NeoForge 1.21.1 StreamCodec for synchronization over the network, written manually to bypass the 9-argument limit of StreamCodec.composite
    public static final StreamCodec<FriendlyByteBuf, CowDefinition> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public CowDefinition decode(FriendlyByteBuf buffer) {
            ResourceLocation id = ResourceLocation.STREAM_CODEC.decode(buffer);
            String name = ByteBufCodecs.STRING_UTF8.decode(buffer);
            int tier = ByteBufCodecs.VAR_INT.decode(buffer);
            int hexColor = ByteBufCodecs.VAR_INT.decode(buffer);
            ResourceLocation fluidId = ResourceLocation.STREAM_CODEC.decode(buffer);
            ResourceLocation parent1 = buffer.readBoolean() ? ResourceLocation.STREAM_CODEC.decode(buffer) : null;
            ResourceLocation parent2 = buffer.readBoolean() ? ResourceLocation.STREAM_CODEC.decode(buffer) : null;
            double breedChance = buffer.readDouble();
            ResourceLocation coolingResult = buffer.readBoolean() ? ResourceLocation.STREAM_CODEC.decode(buffer) : null;
            return new CowDefinition(id, name, tier, hexColor, fluidId, parent1, parent2, breedChance, coolingResult);
        }

        @Override
        public void encode(FriendlyByteBuf buffer, CowDefinition val) {
            ResourceLocation.STREAM_CODEC.encode(buffer, val.id());
            ByteBufCodecs.STRING_UTF8.encode(buffer, val.name());
            ByteBufCodecs.VAR_INT.encode(buffer, val.tier());
            ByteBufCodecs.VAR_INT.encode(buffer, val.hexColor());
            ResourceLocation.STREAM_CODEC.encode(buffer, val.fluidId());
            
            if (val.parent1() != null) {
                buffer.writeBoolean(true);
                ResourceLocation.STREAM_CODEC.encode(buffer, val.parent1());
            } else {
                buffer.writeBoolean(false);
            }
            
            if (val.parent2() != null) {
                buffer.writeBoolean(true);
                ResourceLocation.STREAM_CODEC.encode(buffer, val.parent2());
            } else {
                buffer.writeBoolean(false);
            }

            buffer.writeDouble(val.breedChance());

            if (val.coolingResult() != null) {
                buffer.writeBoolean(true);
                ResourceLocation.STREAM_CODEC.encode(buffer, val.coolingResult());
            } else {
                buffer.writeBoolean(false);
            }
        }
    };
}
