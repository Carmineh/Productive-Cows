package com.materialcows.compat.jei;

import com.materialcows.Materialcows;
import com.materialcows.data.CowDefinition;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

public class CoolerRecipeCategory implements IRecipeCategory<CowDefinition> {

    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(Materialcows.MODID, "cooler");
    public static final RecipeType<CowDefinition> COOLER_TYPE = RecipeType.create(Materialcows.MODID, "cooler", CowDefinition.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable tankOverlay;

    public CoolerRecipeCategory(IGuiHelper guiHelper) {
        // We make a 120x60 GUI
        this.background = guiHelper.createBlankDrawable(120, 60);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Materialcows.ADVANCED_COOLER_ITEM.get()));
        this.tankOverlay = guiHelper.drawableBuilder(ResourceLocation.fromNamespaceAndPath(Materialcows.MODID, "textures/gui/liquid_cooler.png"), 9, 16, 18, 52).build();
    }

    @Override
    public RecipeType<CowDefinition> getRecipeType() {
        return COOLER_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.materialcows.advanced_cooler");
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CowDefinition recipe, IFocusGroup focuses) {
        if (recipe.fluidId() != null) {
            Fluid fluid = BuiltInRegistries.FLUID.get(recipe.fluidId());
            if (fluid != null && fluid != net.minecraft.world.level.material.Fluids.EMPTY) {
                // Fluid input slot (1000 mB)
                builder.addSlot(RecipeIngredientRole.INPUT, 10, 4)
                        .addIngredient(NeoForgeTypes.FLUID_STACK, new FluidStack(fluid, 1000))
                        .setFluidRenderer(8000, false, 18, 52); // We will draw the tank frame in draw()
            }
        }

        if (recipe.coolingResult() != null) {
            net.minecraft.world.item.Item resultItem = BuiltInRegistries.ITEM.get(recipe.coolingResult());
            if (resultItem != net.minecraft.world.item.Items.AIR) {
                // Result output slot
                builder.addSlot(RecipeIngredientRole.OUTPUT, 85, 21)
                        .addItemStack(new ItemStack(resultItem));
            }
        }
    }

    @Override
    public void draw(CowDefinition recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        // Draw the tank overlay over the fluid
        this.tankOverlay.draw(guiGraphics, 9, 3);
        
        // Draw an arrow pointing from fluid to item
        guiGraphics.drawString(net.minecraft.client.Minecraft.getInstance().font, "->", 50, 25, 0x888888, false);

        // Draw energy/time stats
        Component energyText = Component.literal(com.materialcows.Config.basicCoolerEnergyCost + " RF/t");
        Component timeText = Component.literal((com.materialcows.Config.basicCoolerCookTime / 20) + "s");
        
        guiGraphics.drawString(net.minecraft.client.Minecraft.getInstance().font, energyText, 40, 45, 0x555555, false);
        guiGraphics.drawString(net.minecraft.client.Minecraft.getInstance().font, timeText, 40, 55, 0x555555, false);
    }
}
