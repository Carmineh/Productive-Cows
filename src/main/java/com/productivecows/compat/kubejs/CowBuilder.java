package com.productivecows.compat.kubejs;

import com.mojang.logging.LogUtils;
import com.productivecows.data.CowDefinition;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class CowBuilder {
    private static final Logger LOGGER = LogUtils.getLogger();
    
    private final ResourceLocation id;
    private final RegisterCowsEventKJS event;

    private String name;
    private int tier = 1;
    private int hexColor = 0xFFFFFF;
    private ResourceLocation fluidId;
    private ResourceLocation parent1;
    private ResourceLocation parent2;
    private double breedChance = 1.0;
    private ResourceLocation coolingResult;
    private String coolingResultTag;

    public CowBuilder(ResourceLocation id, RegisterCowsEventKJS event) {
        this.id = id;
        this.event = event;
        String path = id.getPath();
        this.name = path.substring(0, 1).toUpperCase() + path.substring(1).replace("_", " ");
    }

    public CowBuilder name(String name) {
        this.name = name;
        return this;
    }

    public CowBuilder tier(int tier) {
        this.tier = tier;
        return this;
    }

    public CowBuilder color(String hex) {
        try {
            if (hex.startsWith("#")) {
                this.hexColor = Integer.parseInt(hex.substring(1), 16);
            } else {
                this.hexColor = Integer.parseInt(hex, 16);
            }
        } catch (NumberFormatException e) {
            LOGGER.error("Invalid hex color for cow {}: {}", id, hex);
        }
        return this;
    }

    public CowBuilder color(int hexColor) {
        this.hexColor = hexColor;
        return this;
    }

    public CowBuilder fluid(String fluid) {
        this.fluidId = ResourceLocation.parse(fluid);
        return this;
    }

    public CowBuilder parent1(String parent1) {
        this.parent1 = ResourceLocation.parse(parent1);
        return this;
    }

    public CowBuilder parent2(String parent2) {
        this.parent2 = ResourceLocation.parse(parent2);
        return this;
    }

    public CowBuilder parents(String parent1, String parent2) {
        this.parent1 = ResourceLocation.parse(parent1);
        this.parent2 = ResourceLocation.parse(parent2);
        return this;
    }

    public CowBuilder breedChance(double breedChance) {
        this.breedChance = breedChance;
        return this;
    }

    public CowBuilder coolingResult(String result) {
        if (result.startsWith("#")) {
            this.coolingResultTag = result.substring(1);
        } else if (result.startsWith("c:")) {
            this.coolingResultTag = result;
        } else {
            this.coolingResult = ResourceLocation.parse(result);
        }
        return this;
    }

    public void register() {
        if (fluidId == null) {
            LOGGER.error("Cow {} must have a fluid defined!", id);
            return;
        }
        
        if (!BuiltInRegistries.FLUID.containsKey(fluidId)) {
            LOGGER.error("Cow {} references unknown fluid: {}", id, fluidId);
            return;
        }
        
        if (coolingResult != null && !BuiltInRegistries.ITEM.containsKey(coolingResult)) {
            LOGGER.error("Cow {} references unknown item cooling result: {}", id, coolingResult);
            return;
        }

        CowDefinition def = new CowDefinition(id, name, tier, hexColor, fluidId, parent1, parent2, breedChance, coolingResult, coolingResultTag);
        event.register(id, def);
    }
}
