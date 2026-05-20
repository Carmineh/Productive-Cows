package com.materialcows.compat.jei;

import com.materialcows.Materialcows;
import com.materialcows.data.CowDefinition;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class MaterialCowsJEIPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(Materialcows.MODID, "jei_plugin");
    }

    @Override
    public void registerItemSubtypes(mezz.jei.api.registration.ISubtypeRegistration registration) {
        mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter<net.minecraft.world.item.ItemStack> interpreter = (stack, context) -> {
            net.minecraft.world.item.component.CustomData customData = stack.get(net.minecraft.core.component.DataComponents.ENTITY_DATA);
            if (customData != null) {
                net.minecraft.nbt.CompoundTag tag = customData.copyTag();
                if (tag.contains("CowDefinitionId")) {
                    return tag.getString("CowDefinitionId");
                }
            }
            return mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter.NONE;
        };
        registration.registerSubtypeInterpreter(Materialcows.CAPTURED_COW.get(), interpreter);
        registration.registerSubtypeInterpreter(Materialcows.MATERIAL_COW_SPAWN_EGG.get(), interpreter);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new BreedingRecipeCategory(guiHelper));
        registration.addRecipeCategories(new CowConversionCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<CowDefinition> breedingRecipes = new ArrayList<>();

        for (CowDefinition def : Materialcows.getActiveDefinitions().values()) {
            if (def.parent1() != null && def.parent2() != null) {
                if (!def.parent1().equals(def.parent2())) {
                    breedingRecipes.add(def);
                }
            }
        }

        registration.addRecipes(BreedingRecipeCategory.BREEDING_TYPE, breedingRecipes);

        List<CowConversionRecipe> conversionRecipes = new ArrayList<>();
        conversionRecipes.add(new CowConversionRecipe(List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.WATER_BUCKET)), ResourceLocation.fromNamespaceAndPath(Materialcows.MODID, "water")));
        conversionRecipes.add(new CowConversionRecipe(List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.LAVA_BUCKET)), ResourceLocation.fromNamespaceAndPath(Materialcows.MODID, "lava")));
        conversionRecipes.add(new CowConversionRecipe(List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.CLAY), new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.CLAY_BALL)), ResourceLocation.fromNamespaceAndPath(Materialcows.MODID, "clay")));
        conversionRecipes.add(new CowConversionRecipe(List.of(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STONE), new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COBBLESTONE)), ResourceLocation.fromNamespaceAndPath(Materialcows.MODID, "stone")));
        registration.addRecipes(CowConversionCategory.CONVERSION_TYPE, conversionRecipes);
    }

    @Override
    public void registerRecipeCatalysts(mezz.jei.api.registration.IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new net.minecraft.world.item.ItemStack(Materialcows.BREEDING_CAGE_ITEM.get()), BreedingRecipeCategory.BREEDING_TYPE);
        registration.addRecipeCatalyst(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COW_SPAWN_EGG), CowConversionCategory.CONVERSION_TYPE);
    }

    @Override
    public void registerGuiHandlers(mezz.jei.api.registration.IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(com.materialcows.client.screen.BreedingCageScreen.class, 75, 35, 34, 8, BreedingRecipeCategory.BREEDING_TYPE);
    }
}
