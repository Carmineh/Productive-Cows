package com.materialcows.client.screen;

import com.materialcows.Materialcows;
import com.materialcows.block.entity.AdvancedCoolerBlockEntity;
import com.materialcows.data.CowDefinition;
import com.materialcows.menu.AdvancedCoolerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

public class AdvancedCoolerScreen extends AbstractContainerScreen<AdvancedCoolerMenu> {

    public AdvancedCoolerScreen(AdvancedCoolerMenu menu, Inventory playerInventory, Component title) {
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
        drawVanillaSlot(guiGraphics, x + 36, y + 17);  // Bucket emptying input
        drawVanillaSlot(guiGraphics, x + 36, y + 53);  // Output empty bucket
        drawVanillaSlot(guiGraphics, x + 154, y + 17); // Speed upgrade slot
        drawVanillaSlot(guiGraphics, x + 154, y + 35); // Efficiency upgrade slot
        drawVanillaSlot(guiGraphics, x + 154, y + 53); // Energy upgrade
        drawVanillaSlot(guiGraphics, x + 104, y + 35); // Output cooled resource

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

        // --- GAUGE 1: COOLABLE FLUID TANK (Left: X=10, Y=17, Width=18, Height=52) ---
        int fluidGaugeX = x + 10;
        int fluidGaugeY = y + 17;
        drawVanillaSlotFrame(guiGraphics, fluidGaugeX - 1, fluidGaugeY - 1, fluidGaugeX + 19, fluidGaugeY + 53);

        int fluidAmount = menu.getFluidAmount();
        int fluidMax = menu.getFluidCapacity();
        if (fluidAmount > 0 && fluidMax > 0 && menu.getBlockEntity() instanceof AdvancedCoolerBlockEntity cooler) {
            net.neoforged.neoforge.fluids.capability.templates.FluidTank tank = cooler.getFluidTank();
            if (!tank.getFluid().isEmpty()) {
                int color = getFluidColor(tank.getFluid().getFluid());
                int fillHeight = Math.min(50, (fluidAmount * 50) / fluidMax);
                int fluidTopY = fluidGaugeY + 51 - fillHeight;
                // Draw colored fluid column
                guiGraphics.fill(fluidGaugeX + 1, fluidTopY, fluidGaugeX + 17, fluidGaugeY + 51, 0xFF000000 | color);
                // Draw shine overlay
                guiGraphics.fill(fluidGaugeX + 1, fluidTopY, fluidGaugeX + 4, fluidGaugeY + 51, 0x40FFFFFF);
            }
        }

        // --- GAUGE 2: RF ENERGY STORAGE (Right: X=132, Y=17, Width=18, Height=52) ---
        int energyGaugeX = x + 132;
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

        // --- PROGRESS HORIZONTAL BAR (Center-Left to Center-Right: X=62, Y=38) ---
        int progressX = x + 62;
        int progressY = y + 38;
        drawVanillaSlotFrame(guiGraphics, progressX - 1, progressY + 1, progressX + 33, progressY + 7);

        int progress = menu.getCookTime();
        int total = menu.getCookTimeTotal();
        if (total > 0 && progress > 0) {
            int fillWidth = Math.min(32, (progress * 32) / total);
            // Draw progress bar with a gradient (light cyan/bright teal: 0xFF00F0FF to 0xFF00FF88)
            guiGraphics.fillGradient(progressX, progressY + 2, progressX + fillWidth, progressY + 6, 0xFF00F0FF, 0xFF00FF88);
            
            // Draw a glowing indicator if progress is advanced
            if (fillWidth > 26) {
                guiGraphics.fill(progressX + 28, progressY, progressX + 29, progressY + 8, 0xFF00FF88);
                guiGraphics.fill(progressX + 30, progressY + 1, progressX + 31, progressY + 7, 0xFF00FF88);
            }
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
        // Bottom/Right light sporgenti reflections
        guiGraphics.fill(x1, y2 - 1, x2, y2, 0xFFFFFFFF);
        guiGraphics.fill(x2 - 1, y1, x2, y2, 0xFFFFFFFF);
    }

    private int getFluidColor(net.minecraft.world.level.material.Fluid fluid) {
        ResourceLocation key = BuiltInRegistries.FLUID.getKey(fluid);
        for (CowDefinition def : Materialcows.getActiveDefinitions().values()) {
            if (def.fluidId().equals(key)) {
                return def.hexColor();
            }
        }
        if (fluid == net.minecraft.world.level.material.Fluids.WATER) return 0x3F76E4;
        if (fluid == net.minecraft.world.level.material.Fluids.LAVA) return 0xFF3C00;
        return 0x948A7A;
    }

    private void renderGaugesTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // Fluid tank tooltip
        int fluidX = x + 10;
        int fluidY = y + 17;
        if (mouseX >= fluidX && mouseX < fluidX + 18 && mouseY >= fluidY && mouseY < fluidY + 52) {
            List<Component> text = new ArrayList<>();
            int amount = menu.getFluidAmount();
            int max = menu.getFluidCapacity();
            if (amount > 0 && menu.getBlockEntity() instanceof AdvancedCoolerBlockEntity cooler) {
                String name = cooler.getFluidTank().getFluid().getHoverName().getString();
                text.add(Component.literal(name).withStyle(net.minecraft.ChatFormatting.AQUA));
                text.add(Component.literal(amount + " / " + max + " mB").withStyle(net.minecraft.ChatFormatting.GRAY));
            } else {
                text.add(Component.translatable("gui.materialcows.empty_tank").withStyle(net.minecraft.ChatFormatting.GRAY));
            }
            guiGraphics.renderComponentTooltip(this.font, text, mouseX, mouseY);
            return;
        }

        // Energy storage tooltip
        int energyX = x + 132;
        int energyY = y + 17;
        if (mouseX >= energyX && mouseX < energyX + 18 && mouseY >= energyY && mouseY < energyY + 52) {
            List<Component> text = new ArrayList<>();
            int energyAmount = menu.getEnergyStored();
            int energyMax = menu.getMaxEnergyStored();
            text.add(Component.literal("Energy (RF)").withStyle(net.minecraft.ChatFormatting.RED));
            text.add(Component.literal(energyAmount + " / " + energyMax + " RF").withStyle(net.minecraft.ChatFormatting.GRAY));
            
            // If currently cooling, show the dynamic RF/t consumption rate
            int progress = menu.getCookTime();
            if (progress > 0) {
                int speedUpgrades = Math.max(0, Math.min(3, menu.getSlot(2).getItem().getCount()));
                int efficiencyUpgrades = Math.max(0, Math.min(3, menu.getSlot(3).getItem().getCount()));
                int energyUpgrades = Math.max(0, Math.min(3, menu.getSlot(5).getItem().getCount()));
                
                int baseEnergyPerTick = switch(speedUpgrades) { case 1 -> 200; case 2 -> 500; case 3 -> 2000; default -> 100; };
                float efficiencyMultiplier = switch(efficiencyUpgrades) { case 1 -> 1.2f; case 2 -> 1.5f; case 3 -> 2.0f; default -> 1.0f; };
                float energyMultiplier = switch(energyUpgrades) { case 1 -> 0.7f; case 2 -> 0.5f; case 3 -> 0.3f; default -> 1.0f; };
                int currentRfCost = (int)(baseEnergyPerTick * efficiencyMultiplier * energyMultiplier);

                text.add(Component.literal("Consuming: " + currentRfCost + " RF/t").withStyle(net.minecraft.ChatFormatting.YELLOW));
            } else {
                text.add(Component.literal("Consuming: 0 RF/t (Idle)").withStyle(net.minecraft.ChatFormatting.GREEN));
            }
            guiGraphics.renderComponentTooltip(this.font, text, mouseX, mouseY);
        }
    }
}
