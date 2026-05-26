package com.productivecows.client.screen;

import com.productivecows.block.entity.MilkGeneratorBlockEntity;
import com.productivecows.menu.MilkGeneratorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import com.productivecows.ProductiveCows;

import java.util.ArrayList;
import java.util.List;

public class MilkGeneratorScreen extends AbstractContainerScreen<MilkGeneratorMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ProductiveCows.MODID, "textures/gui/milk_generator.png");

    public MilkGeneratorScreen(MilkGeneratorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 202;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 72;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        this.renderGaugesTooltips(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // Draw main custom background texture
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, 220, 166);

        // --- GAUGE 1: MILK TANK (Left: X=43, Y=16, Width=42, Height=52) ---
        int fluidGaugeX = x + 43;
        int fluidGaugeY = y + 16;

        int amount = menu.getFluidAmount();
        int capacity = menu.getFluidCapacity();
        if (amount > 0 && capacity > 0) {
            int fillHeight = Math.min(52, (amount * 52) / capacity);
            int fluidTopY = fluidGaugeY + 52 - fillHeight;
            guiGraphics.fill(fluidGaugeX, fluidTopY, fluidGaugeX + 42, fluidGaugeY + 52, 0xFFFFFFFF); // White for milk
        }

        // --- GAUGE 2: RF ENERGY STORAGE (Right: X=120, Y=16, Width=14, Height=52) ---
        int energyGaugeX = x + 120;
        int energyGaugeY = y + 16;

        int energyAmount = menu.getEnergyStored();
        int energyMax = menu.getMaxEnergyStored();
        if (energyAmount > 0 && energyMax > 0) {
            int fillHeight = Math.min(52, (energyAmount * 52) / energyMax);
            int energyTopY = energyGaugeY + 52 - fillHeight;
            // Draw energy bar overlay from the texture itself (located at 204, 1)
            guiGraphics.blit(TEXTURE, energyGaugeX, energyTopY, 204, 1 + (52 - fillHeight), 14, fillHeight, 220, 166);
        }
    }

    private void renderGaugesTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        
        // Milk Tank gauge check
        int gaugeX = x + 43;
        int gaugeY = y + 16;
        if (mouseX >= gaugeX && mouseX < gaugeX + 42 && mouseY >= gaugeY && mouseY < gaugeY + 52) {
            List<Component> text = new ArrayList<>();
            int milkAmount = menu.getFluidAmount();
            int milkMax = menu.getFluidCapacity();
            if (milkAmount > 0) {
                text.add(Component.translatable("fluid.productivecows.milk_name").withStyle(net.minecraft.ChatFormatting.WHITE));
                text.add(Component.literal(milkAmount + " / " + milkMax + " mB").withStyle(net.minecraft.ChatFormatting.GRAY));
            } else {
                text.add(Component.translatable("gui.productivecows.empty_milk_tank").withStyle(net.minecraft.ChatFormatting.GRAY));
            }
            guiGraphics.renderComponentTooltip(this.font, text, mouseX, mouseY);
            return;
        }

        // Energy gauge check
        int energyX = x + 120;
        int energyY = y + 16;
        if (mouseX >= energyX && mouseX < energyX + 14 && mouseY >= energyY && mouseY < energyY + 52) {
            List<Component> text = new ArrayList<>();
            int energyAmount = menu.getEnergyStored();
            int energyMax = menu.getMaxEnergyStored();
            text.add(Component.literal("Energy (RF)").withStyle(net.minecraft.ChatFormatting.RED));
            text.add(Component.literal(energyAmount + " / " + energyMax + " RF").withStyle(net.minecraft.ChatFormatting.GRAY));
            
            // Show generation rate if active
            int milkAmount = menu.getFluidAmount();
            if (milkAmount > 0 && energyAmount < energyMax) {
                text.add(Component.literal("Producing: +40 RF/t").withStyle(net.minecraft.ChatFormatting.GREEN));
            }
            guiGraphics.renderComponentTooltip(this.font, text, mouseX, mouseY);
        }
    }
}
