package com.productivecows.client.screen;

import com.productivecows.ProductiveCows;
import com.productivecows.block.entity.LiquidCoolerBlockEntity;
import com.productivecows.data.CowDefinition;
import com.productivecows.menu.LiquidCoolerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import java.util.ArrayList;
import java.util.List;

public class LiquidCoolerScreen extends AbstractContainerScreen<LiquidCoolerMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ProductiveCows.MODID, "textures/gui/liquid_cooler.png");

    public LiquidCoolerScreen(LiquidCoolerMenu menu, Inventory playerInventory, Component title) {
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
        this.renderFluidTooltip(guiGraphics, mouseX, mouseY);
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

        if (menu.getBlockEntity() instanceof LiquidCoolerBlockEntity cooler) {
            FluidTank tank = cooler.getFluidTank();
            int amount = tank.getFluidAmount();
            if (amount > 0 && !tank.getFluid().isEmpty()) {
                int color = getFluidColor(tank.getFluid().getFluid());
                int fillHeight = Math.min(52, (amount * 52) / 8000);
                
                int fluidTopY = fluidGaugeY + 52 - fillHeight;
                guiGraphics.fill(fluidGaugeX, fluidTopY, fluidGaugeX + 42, fluidGaugeY + 52, 0xFF000000 | color);
            }
        }

        // --- PROGRESS HORIZONTAL BAR (X=83, Y=42, Width=39, Height=2) ---
        int progressX = x + 83;
        int progressY = y + 42;

        int progress = menu.getCookTime();
        int total = menu.getCookTimeTotal();
        if (total > 0 && progress > 0) {
            int fillWidth = Math.min(39, (progress * 39) / total);
            // Draw progress bar with gradient
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

    private void renderFluidTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        int gaugeX = x + 8;
        int gaugeY = y + 17;

        if (mouseX >= gaugeX && mouseX < gaugeX + 42 && mouseY >= gaugeY && mouseY < gaugeY + 52) {
            if (menu.getBlockEntity() instanceof LiquidCoolerBlockEntity cooler) {
                FluidTank tank = cooler.getFluidTank();
                List<Component> text = new ArrayList<>();
                if (tank.getFluidAmount() > 0) {
                    String name = tank.getFluid().getHoverName().getString();
                    text.add(Component.literal(name).withStyle(net.minecraft.ChatFormatting.AQUA));
                    text.add(Component.literal(tank.getFluidAmount() + " / 8000 mB").withStyle(net.minecraft.ChatFormatting.GRAY));
                } else {
                    text.add(Component.translatable("gui.productivecows.empty_tank").withStyle(net.minecraft.ChatFormatting.GRAY));
                }
                guiGraphics.renderComponentTooltip(this.font, text, mouseX, mouseY);
            }
        }
    }
}
