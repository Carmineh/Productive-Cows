package com.productivecows.compat.kubejs;

import com.productivecows.data.CowDefinition;
import dev.latvian.mods.kubejs.event.KubeEvent;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public class RegisterCowsEventKJS implements KubeEvent {
    private final Map<ResourceLocation, CowDefinition> registry;
    private final java.util.List<CowBuilder> builders = new java.util.ArrayList<>();

    public RegisterCowsEventKJS(Map<ResourceLocation, CowDefinition> registry) {
        this.registry = registry;
    }

    public CowBuilder create(String id) {
        ResourceLocation location = ResourceLocation.parse(id);
        if (!location.getNamespace().equals("minecraft") && !id.contains(":")) {
            location = ResourceLocation.fromNamespaceAndPath("kubejs", id);
        }
        CowBuilder builder = new CowBuilder(location, this);
        builders.add(builder);
        return builder;
    }

    public void register(ResourceLocation id, CowDefinition definition) {
        registry.put(id, definition);
    }
    
    public void registerAll() {
        for (CowBuilder builder : builders) {
            builder.register();
        }
    }
}
