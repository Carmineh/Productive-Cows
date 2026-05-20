package com.materialcows.client.screen;

import com.materialcows.block.entity.MilkGeneratorBlockEntity;
import com.materialcows.menu.MilkGeneratorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

public class MilkGeneratorScreen extends AbstractContainerScreen<MilkGeneratorMenu> {

    public MilkGeneratorScreen(MilkGeneratorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
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

        // Draw vanilla gray 3D background panel
        drawVanillaPanel(guiGraphics, x, y, this.imageWidth, this.imageHeight);

        // Draw machine slots
        drawVanillaSlot(guiGraphics, x + 56, y + 17); // Bucket input slot
        drawVanillaSlot(guiGraphics, x + 56, y + 53); // Output empty bucket slot

        // Draw Player Inventory slots (3x9)
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                drawVanillaSlot(guiGraphics, x + 8 + j * 18, y + 84 + i * 18);
            }
        }
        // Player Hotbar slots (1x9)
        for (int k = 0; k < 9; ++k) {
            drawVanillaSlot(guiGraphics, x + 8 + k * 18, y + 142);
        }

        // --- GAUGE 1: MILK FLUID TANK (Left: X=20, Y=17, Width=18, Height=52) ---
        int milkGaugeX = x + 20;
        int milkGaugeY = y + 17;
        drawVanillaSlotFrame(guiGraphics, milkGaugeX - 1, milkGaugeY - 1, milkGaugeX + 19, milkGaugeY + 53);

        int milkAmount = menu.getFluidAmount();
        int milkMax = menu.getFluidCapacity();
        if (milkAmount > 0 && milkMax > 0) {
            int fillHeight = Math.min(50, (milkAmount * 50) / milkMax);
            int fluidTopY = milkGaugeY + 51 - fillHeight;
            // Draw milk (pure cream white: 0xFFFDFDFD)
            guiGraphics.fill(milkGaugeX + 1, fluidTopY, milkGaugeX + 17, milkGaugeY + 51, 0xFFFDFDFD);
            // Draw shine overlay
            guiGraphics.fill(milkGaugeX + 1, fluidTopY, milkGaugeX + 4, milkGaugeY + 51, 0x40FFFFFF);
        }

        // --- GAUGE 2: RF ENERGY STORAGE (Right: X=138, Y=17, Width=18, Height=52) ---
        int energyGaugeX = x + 138;
        int energyGaugeY = y + 17;
        drawVanillaSlotFrame(guiGraphics, energyGaugeX - 1, energyGaugeY - 1, energyGaugeX + 19, energyGaugeY + 53);

        int energyAmount = menu.getEnergyStored();
        int energyMax = menu.getMaxEnergyStored();
        if (energyAmount > 0 && energyMax > 0) {
            int fillHeight = Math.min(50, (energyAmount * 50) / energyMax);
            int energyTopY = energyGaugeY + 51 - fillHeight;
            // Draw energy bar (vibrant Red/Orange gradient: 0xFFFF2200 to 0xFFFF8800)
            guiGraphics.fillGradient(energyGaugeX + 1, energyTopY, energyGaugeX + 17, energyGaugeY + 51, 0xFFFF2200, 0xFFFF8800);
            // Draw shine overlay
            guiGraphics.fill(energyGaugeX + 1, energyTopY, energyGaugeX + 4, energyGaugeY + 51, 0x40FFFFFF);
        }

        // --- ANIMATED ACTIVE GENERATOR BOLT (Center: X=98, Y=35) ---
        int boltX = x + 98;
        int boltY = y + 35;
        // Hollow slot frame for lightning/bolt status indicator
        drawVanillaSlotFrame(guiGraphics, boltX - 1, boltY - 1, boltX + 17, boltY + 17);

        // Animate bolt if generator is active (has milk fluid AND energy is generating)
        boolean isActive = milkAmount > 0 && energyAmount < energyMax;
        if (isActive) {
            long sysTime = System.currentTimeMillis();
            // Pulse opacity/glow between 0.3 and 1.0 based on system time
            float anim = (float) (Math.sin(sysTime * 0.01) * 0.35 + 0.65);
            int alpha = (int) (anim * 255);
            int pulseColor = (alpha << 24) | 0xFFE020; // Glowing Yellow
            
            // Draw a simplified lightning bolt shape in the center of the slot
            // Top segment
            guiGraphics.fill(boltX + 6, boltY + 2, boltX + 12, boltY + 4, pulseColor);
            guiGraphics.fill(boltX + 5, boltY + 4, boltX + 10, boltY + 7, pulseColor);
            // Middle break
            guiGraphics.fill(boltX + 7, boltY + 7, boltX + 13, boltY + 9, pulseColor);
            // Bottom segment
            guiGraphics.fill(boltX + 6, boltY + 9, boltX + 11, boltY + 12, pulseColor);
            guiGraphics.fill(boltX + 7, boltY + 12, boltX + 9, boltY + 14, pulseColor);
        } else {
            // Draw a dark/inactive bolt silhouette
            int darkColor = 0xFF404040;
            guiGraphics.fill(boltX + 6, boltY + 2, boltX + 12, boltY + 4, darkColor);
            guiGraphics.fill(boltX + 5, boltY + 4, boltX + 10, boltY + 7, darkColor);
            guiGraphics.fill(boltX + 7, boltY + 7, boltX + 13, boltY + 9, darkColor);
            guiGraphics.fill(boltX + 6, boltY + 9, boltX + 11, boltY + 12, darkColor);
            guiGraphics.fill(boltX + 7, boltY + 12, boltX + 9, boltY + 14, darkColor);
        }
    }

    private void drawVanillaPanel(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        // Main panel body
        guiGraphics.fill(x, y, x + width, y + height, 0xFFC6C6C6);
        // Highlight borders (top and left)
        guiGraphics.fill(x, y, x + width, y + 1, 0xFFFFFFFF);
        guiGraphics.fill(x, y, x + 1, y + height, 0xFFFFFFFF);
        // Shadow borders (bottom and right)
        guiGraphics.fill(x, y + height - 1, x + width, y + height, 0xFF555555);
        guiGraphics.fill(x + width - 1, y, x + width, y + height, 0xFF555555);
    }

    private void drawVanillaSlot(GuiGraphics guiGraphics, int slotX, int slotY) {
        drawVanillaSlotFrame(guiGraphics, slotX - 1, slotY - 1, slotX + 17, slotY + 17);
    }

    private void drawVanillaSlotFrame(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2) {
        // Inner background of the slot
        guiGraphics.fill(x1, y1, x2, y2, 0xFF8B8B8B);
        // Top/Left dark inset shadows
        guiGraphics.fill(x1, y1, x2, y1 + 1, 0xFF373737);
        guiGraphics.fill(x1, y1, x1 + 1, y2, 0xFF373737);
        // Bottom/Right light reflections
        guiGraphics.fill(x1, y2 - 1, x2, y2, 0xFFFFFFFF);
        guiGraphics.fill(x2 - 1, y1, x2, y2, 0xFFFFFFFF);
    }

    private void renderGaugesTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        
        // Milk Tank gauge check
        int milkX = x + 20;
        int milkY = y + 17;
        if (mouseX >= milkX && mouseX < milkX + 18 && mouseY >= milkY && mouseY < milkY + 52) {
            List<Component> text = new ArrayList<>();
            int milkAmount = menu.getFluidAmount();
            int milkMax = menu.getFluidCapacity();
            if (milkAmount > 0) {
                text.add(Component.translatable("fluid.materialcows.milk_name").withStyle(net.minecraft.ChatFormatting.WHITE));
                text.add(Component.literal(milkAmount + " / " + milkMax + " mB").withStyle(net.minecraft.ChatFormatting.GRAY));
            } else {
                text.add(Component.translatable("gui.materialcows.empty_milk_tank").withStyle(net.minecraft.ChatFormatting.GRAY));
            }
            guiGraphics.renderComponentTooltip(this.font, text, mouseX, mouseY);
            return;
        }

        // Energy gauge check
        int energyX = x + 138;
        int energyY = y + 17;
        if (mouseX >= energyX && mouseX < energyX + 18 && mouseY >= energyY && mouseY < energyY + 52) {
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
