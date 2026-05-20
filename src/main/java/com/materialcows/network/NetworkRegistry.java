package com.materialcows.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkRegistry {
    
    public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1.0.0"); // Protocol Version
        
        // Register the payload to be sent from Server to Client
        registrar.playToClient(
                SyncCowDefinitionsPayload.TYPE,
                SyncCowDefinitionsPayload.STREAM_CODEC,
                ClientPayloadHandler::handleSyncCows
        );
    }
}
