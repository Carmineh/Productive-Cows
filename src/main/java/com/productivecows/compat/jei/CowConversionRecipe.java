package com.productivecows.compat.jei;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import java.util.List;

public record CowConversionRecipe(List<ItemStack> catalysts, ResourceLocation resultCowId) {}
