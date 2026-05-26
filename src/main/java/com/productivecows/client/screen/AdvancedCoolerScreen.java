package com.productivecows.client.screen;

import com.productivecows.ProductiveCows;
import com.productivecows.block.entity.AdvancedCoolerBlockEntity;
import com.productivecows.data.CowDefinition;
import com.productivecows.menu.AdvancedCoolerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

public class AdvancedCoolerScreen extends AbstractContainerScreen<AdvancedCoolerMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ProductiveCows.MODID, "textures/gui/advanced_cooler.png");

    public AdvancedCoolerScreen(AdvancedCoolerMenu menu, Inventory playerInventory, Component title) {
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

        // --- GAUGE 1: COOLABLE FLUID TANK (Left: X=8, Y=17, Width=42, Height=52) ---
        int fluidGaugeX = x + 8;
        int fluidGaugeY = y + 17;

        int fluidAmount = menu.getFluidAmount();
        int fluidMax = menu.getFluidCapacity();
        if (fluidAmount > 0 && fluidMax > 0 && menu.getBlockEntity() instanceof AdvancedCoolerBlockEntity cooler) {
            net.neoforged.neoforge.fluids.capability.templates.FluidTank tank = cooler.getFluidTank();
            if (!tank.getFluid().isEmpty()) {
                int color = getFluidColor(tank.getFluid().getFluid());
                int fillHeight = Math.min(52, (fluidAmount * 52) / fluidMax);
                int fluidTopY = fluidGaugeY + 52 - fillHeight;
                // Draw colored fluid column taking the entire space
                guiGraphics.fill(fluidGaugeX, fluidTopY, fluidGaugeX + 42, fluidGaugeY + 52, 0xFF000000 | color);
            }
        }

        // --- GAUGE 2: RF ENERGY STORAGE (Right: X=156, Y=17, Width=14, Height=52) ---
        int energyGaugeX = x + 156;
        int energyGaugeY = y + 17;

        int energyAmount = menu.getEnergyStored();
        int energyMax = menu.getMaxEnergyStored();
        if (energyAmount > 0 && energyMax > 0) {
            int fillHeight = Math.min(52, (energyAmount * 52) / energyMax);
            int energyTopY = energyGaugeY + 52 - fillHeight;
            // Draw energy bar overlay from the texture itself (located at 204, 1)
            guiGraphics.blit(TEXTURE, energyGaugeX, energyTopY, 204, 1 + (52 - fillHeight), 14, fillHeight, 220, 166);
        }

        // --- PROGRESS HORIZONTAL BAR (X=83, Y=42, Width=39, Height=2) ---
        int progressX = x + 83;
        int progressY = y + 42;

        int progress = menu.getCookTime();
        int total = menu.getCookTimeTotal();
        if (total > 0 && progress > 0) {
            int fillWidth = Math.min(39, (progress * 39) / total);
            // Draw progress bar filling
            guiGraphics.fillGradient(progressX, progressY, progressX + fillWidth, progressY + 2, 0xFF00F0FF, 0xFF00FF88);
        }
    }



    private int getFluidColor(net.minecraft.world.level.material.Fluid fluid) {
        ResourceLocation key = BuiltInRegistries.FLUID.getKey(fluid);
        for (CowDefinition def : ProductiveCows.getActiveDefinitions().values()) {
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
        int fluidX = x + 8;
        int fluidY = y + 17;
        if (mouseX >= fluidX && mouseX < fluidX + 42 && mouseY >= fluidY && mouseY < fluidY + 52) {
            List<Component> text = new ArrayList<>();
            int amount = menu.getFluidAmount();
            int max = menu.getFluidCapacity();
            if (amount > 0 && menu.getBlockEntity() instanceof AdvancedCoolerBlockEntity cooler) {
                String name = cooler.getFluidTank().getFluid().getHoverName().getString();
                text.add(Component.literal(name).withStyle(net.minecraft.ChatFormatting.AQUA));
                text.add(Component.literal(amount + " / " + max + " mB").withStyle(net.minecraft.ChatFormatting.GRAY));
            } else {
                text.add(Component.translatable("gui.productivecows.empty_tank").withStyle(net.minecraft.ChatFormatting.GRAY));
            }
            guiGraphics.renderComponentTooltip(this.font, text, mouseX, mouseY);
            return;
        }

        // Energy storage tooltip
        int energyX = x + 156;
        int energyY = y + 17;
        if (mouseX >= energyX && mouseX < energyX + 14 && mouseY >= energyY && mouseY < energyY + 52) {
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
