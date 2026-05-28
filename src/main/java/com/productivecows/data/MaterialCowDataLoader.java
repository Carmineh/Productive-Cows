package com.productivecows.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.core.registries.BuiltInRegistries;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MaterialCowDataLoader extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    
    // Directory we read from: data/<namespace>/cows/
    private static final String DIRECTORY = "cows";
    
    private static Map<ResourceLocation, CowDefinition> DEFINITIONS = Collections.emptyMap();

    public MaterialCowDataLoader() {
        super(GSON, DIRECTORY);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objectMap, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        Map<ResourceLocation, CowDefinition> newDefinitions = new HashMap<>();

        objectMap.forEach((location, jsonElement) -> {
            CowDefinition.CODEC.parse(JsonOps.INSTANCE, jsonElement)
                    .resultOrPartial(error -> LOGGER.error("Failed to parse material cow definition {}: {}", location, error))
                    .ifPresent(definition -> {
                        boolean isValid = true;
                        
                        if (!BuiltInRegistries.FLUID.containsKey(definition.fluidId())) {
                            isValid = false;
                        }
                        
                        if (definition.coolingResult() != null && !BuiltInRegistries.ITEM.containsKey(definition.coolingResult())) {
                            isValid = false;
                        }
                        
                        if (isValid) {
                            newDefinitions.put(location, definition.withId(location));
                        }
                    });
        });

        if (net.neoforged.fml.ModList.get().isLoaded("kubejs")) {
            com.productivecows.compat.kubejs.KubeJSCompat.fireRegisterCowsEvent(newDefinitions);
        }

        DEFINITIONS = Map.copyOf(newDefinitions);
        LOGGER.info("Successfully loaded {} material cow definitions.", DEFINITIONS.size());
    }

    /**
     * Gets all loaded cow definitions.
     */
    public static Map<ResourceLocation, CowDefinition> getDefinitions() {
        return DEFINITIONS;
    }
    
    /**
     * Gets a single cow definition by its ID.
     */
    public static CowDefinition getDefinition(ResourceLocation id) {
        return DEFINITIONS.get(id);
    }
}
