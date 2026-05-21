package com.materialcows.data.provider;

import com.materialcows.Materialcows;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {

    public ModRecipeProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {

        // Breeding Cage: Iron bars around hay block
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Materialcows.BREEDING_CAGE_ITEM.get())
                .pattern("III")
                .pattern("IHI")
                .pattern("III")
                .define('I', Items.IRON_BARS)
                .define('H', Items.HAY_BLOCK)
                .unlockedBy("has_iron_bars", has(Items.IRON_BARS))
                .save(output);

        // Cow Cage: Iron bars around a bucket
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Materialcows.COW_CAGE_ITEM.get())
                .pattern("III")
                .pattern("IBI")
                .pattern("III")
                .define('I', Items.IRON_BARS)
                .define('B', Items.BUCKET)
                .unlockedBy("has_bucket", has(Items.BUCKET))
                .save(output);

        // Basic Liquid Cooler: Iron ingots around redstone and glass
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Materialcows.LIQUID_COOLER_ITEM.get())
                .pattern("IGI")
                .pattern("IRI")
                .pattern("III")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('G', Items.GLASS)
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .unlockedBy("has_iron", has(Tags.Items.INGOTS_IRON))
                .save(output);

        // Advanced Cooler: Basic Cooler surrounded by Gold and Diamond
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Materialcows.ADVANCED_COOLER_ITEM.get())
                .pattern("DGD")
                .pattern("GCG")
                .pattern("DGD")
                .define('D', Tags.Items.GEMS_DIAMOND)
                .define('G', Tags.Items.INGOTS_GOLD)
                .define('C', Materialcows.LIQUID_COOLER_ITEM.get())
                .unlockedBy("has_liquid_cooler", has(Materialcows.LIQUID_COOLER_ITEM.get()))
                .save(output);

        // Speed Upgrade: Sugar and Redstone around Iron
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Materialcows.SPEED_UPGRADE.get())
                .pattern("SRS")
                .pattern("RIR")
                .pattern("SRS")
                .define('S', Items.SUGAR)
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('I', Tags.Items.INGOTS_IRON)
                .unlockedBy("has_sugar", has(Items.SUGAR))
                .save(output);

        // Efficiency Upgrade: Redstone and Gold
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Materialcows.EFFICIENCY_UPGRADE.get())
                .pattern(" R ")
                .pattern("RGR")
                .pattern(" R ")
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('G', Tags.Items.INGOTS_GOLD)
                .unlockedBy("has_gold", has(Tags.Items.INGOTS_GOLD))
                .save(output);

        // Energy Upgrade: Redstone and Diamond
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Materialcows.ENERGY_UPGRADE.get())
                .pattern(" R ")
                .pattern("RDR")
                .pattern(" R ")
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('D', Tags.Items.GEMS_DIAMOND)
                .unlockedBy("has_diamond", has(Tags.Items.GEMS_DIAMOND))
                .save(output);

        // Cow Stick: Stick and Leather
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Materialcows.COW_STICK.get())
                .pattern("  L")
                .pattern(" S ")
                .pattern("S  ")
                .define('L', Items.LEATHER)
                .define('S', Tags.Items.RODS_WOODEN)
                .unlockedBy("has_leather", has(Items.LEATHER))
                .save(output);
    }
}
