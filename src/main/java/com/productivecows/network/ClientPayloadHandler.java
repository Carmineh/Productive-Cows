package com.productivecows.network;

import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPayloadHandler {
    public static void handleSyncCows(final SyncCowDefinitionsPayload payload, final IPayloadContext context) {
        // Enqueues the task on the main Client Thread to prevent asynchronous threading race conditions
        context.enqueueWork(() -> {
            ClientCowData.setDefinitions(payload.definitions());
        });
    }
}
