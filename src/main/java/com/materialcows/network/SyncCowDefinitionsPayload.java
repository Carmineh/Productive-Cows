package com.materialcows.network;

import com.materialcows.data.CowDefinition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record SyncCowDefinitionsPayload(List<CowDefinition> definitions) implements CustomPacketPayload {
    
    public static final Type<SyncCowDefinitionsPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("materialcows", "sync_cows"));

    // Composite stream codec using the list decoder/encoder for CowDefinition
    public static final StreamCodec<FriendlyByteBuf, SyncCowDefinitionsPayload> STREAM_CODEC = StreamCodec.composite(
            CowDefinition.STREAM_CODEC.apply(ByteBufCodecs.list()), SyncCowDefinitionsPayload::definitions,
            SyncCowDefinitionsPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
