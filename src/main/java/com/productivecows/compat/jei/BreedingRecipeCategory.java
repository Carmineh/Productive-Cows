package com.productivecows.compat.jei;

import com.productivecows.ProductiveCows;
import com.productivecows.data.CowDefinition;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

public class BreedingRecipeCategory implements IRecipeCategory<CowDefinition> {

    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(ProductiveCows.MODID, "breeding");
    public static final RecipeType<CowDefinition> BREEDING_TYPE = RecipeType.create(ProductiveCows.MODID, "breeding", CowDefinition.class);

    private final IDrawable background;
    private final IDrawable icon;

    public BreedingRecipeCategory(IGuiHelper guiHelper) {
        // JEI uses a simple 176x166 texture for standard guis, but we can draw a custom one.
        // For simplicity, we create a blank drawable and draw slots manually, or use a gui helper to create a generic background.
        // Let's create a blank drawable of 130x50 and place slots.
        this.background = guiHelper.createBlankDrawable(130, 50);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ProductiveCows.BREEDING_CAGE_ITEM.get()));
    }

    @Override
    public RecipeType<CowDefinition> getRecipeType() {
        return BREEDING_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.productivecows.breeding_cage");
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
        ItemStack parent1 = createCowItem(recipe.parent1());
        ItemStack parent2 = createCowItem(recipe.parent2());
        ItemStack output = createCowItem(recipe.id());
        ItemStack wheat = new ItemStack(Items.WHEAT);

        ItemStack parent1Egg = createEggItem(recipe.parent1());
        ItemStack parent2Egg = createEggItem(recipe.parent2());
        ItemStack outputEgg = createEggItem(recipe.id());

        // Invisible items so 'R' on eggs works
        builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addItemStack(parent1Egg);
        builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addItemStack(parent2Egg);
        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addItemStack(outputEgg);

        // Parent 1 slot (Left Top)
        builder.addSlot(RecipeIngredientRole.INPUT, 5, 5)
                .addItemStack(parent1);

        // Parent 2 slot (Left Bottom)
        builder.addSlot(RecipeIngredientRole.INPUT, 5, 30)
                .addItemStack(parent2);

        // Wheat slot (Middle Bottom, under percentage)
        builder.addSlot(RecipeIngredientRole.INPUT, 55, 25)
                .addItemStack(wheat);

        // Output slot (Right Middle)
        builder.addSlot(RecipeIngredientRole.OUTPUT, 105, 17)
                .addItemStack(output);
    }

    @Override
    public void draw(CowDefinition recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        // Draw chance text
        if (recipe.breedChance() < 1.0) {
            int chance = (int) (recipe.breedChance() * 100);
            Component chanceText = Component.literal(chance + "%");
            guiGraphics.drawString(net.minecraft.client.Minecraft.getInstance().font, chanceText, 57, 10, 0xFF5555, false);
        }
    }

    private ItemStack createCowItem(ResourceLocation defId) {
        if (defId == null) return ItemStack.EMPTY;
        ItemStack stack = new ItemStack(ProductiveCows.CAPTURED_COW.get());
        CompoundTag entityData = new CompoundTag();
        entityData.putString("id", "productivecows:material_cow");
        entityData.putString("CowDefinitionId", defId.toString());
        stack.set(DataComponents.ENTITY_DATA, CustomData.of(entityData));
        return stack;
    }

    private ItemStack createEggItem(ResourceLocation defId) {
        if (defId == null) return ItemStack.EMPTY;
        ItemStack stack = new ItemStack(ProductiveCows.MATERIAL_COW_SPAWN_EGG.get());
        CompoundTag entityData = new CompoundTag();
        entityData.putString("id", "productivecows:material_cow");
        entityData.putString("CowDefinitionId", defId.toString());
        stack.set(DataComponents.ENTITY_DATA, CustomData.of(entityData));
        return stack;
    }
}
