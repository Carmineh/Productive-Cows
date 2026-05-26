package com.productivecows.compat.jei;

import com.productivecows.ProductiveCows;
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

public class CowConversionCategory implements IRecipeCategory<CowConversionRecipe> {

    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(ProductiveCows.MODID, "conversion");
    public static final RecipeType<CowConversionRecipe> CONVERSION_TYPE = RecipeType.create(ProductiveCows.MODID, "conversion", CowConversionRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public CowConversionCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(130, 40);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Items.COW_SPAWN_EGG));
    }

    @Override
    public RecipeType<CowConversionRecipe> getRecipeType() {
        return CONVERSION_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("entity.minecraft.cow");
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
    public void setRecipe(IRecipeLayoutBuilder builder, CowConversionRecipe recipe, IFocusGroup focuses) {
        ItemStack vanillaCow = new ItemStack(Items.COW_SPAWN_EGG);
        ItemStack outputModel = createCowItem(recipe.resultCowId());
        ItemStack outputEgg = createEggItem(recipe.resultCowId());

        // Vanilla Cow Egg
        builder.addSlot(RecipeIngredientRole.INPUT, 5, 15)
                .addItemStack(vanillaCow);

        // Catalyst (Right click item)
        builder.addSlot(RecipeIngredientRole.CATALYST, 50, 15)
                .addItemStacks(recipe.catalysts());

        // Result Model
        builder.addSlot(RecipeIngredientRole.OUTPUT, 95, 15)
                .addItemStack(outputModel);

        // Invisible result egg so 'R' on eggs works
        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addItemStack(outputEgg);
    }

    @Override
    public void draw(CowConversionRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        // Draw + and ->
        guiGraphics.drawString(net.minecraft.client.Minecraft.getInstance().font, "+", 32, 19, 0x888888, false);
        guiGraphics.drawString(net.minecraft.client.Minecraft.getInstance().font, "->", 75, 19, 0x888888, false);
        
        // Draw "Right Click" text above catalyst
        Component rightClickText = Component.literal("Right Click");
        guiGraphics.drawString(net.minecraft.client.Minecraft.getInstance().font, rightClickText, 35, 4, 0x555555, false);
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
