package com.materialcows.fluid;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.function.Consumer;

public class TintedFluidType extends FluidType {
    
    private final int tintColor;
    private final ResourceLocation stillTexture;
    private final ResourceLocation flowingTexture;

    public TintedFluidType(Properties properties, int tintColor, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        super(properties);
        this.tintColor = tintColor;
        this.stillTexture = stillTexture;
        this.flowingTexture = flowingTexture;
    }

    @Override
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
        consumer.accept(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return stillTexture;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return flowingTexture;
            }

            @Override
            public int getTintColor() {
                // Ensure fully opaque ARGB by combining 0xFF000000 with the RGB color
                return 0xFF000000 | tintColor;
            }
        });
    }
}
