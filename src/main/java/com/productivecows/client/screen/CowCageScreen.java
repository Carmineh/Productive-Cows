package com.productivecows.client.screen;

import com.productivecows.ProductiveCows;
import com.productivecows.block.entity.CowCageBlockEntity;
import com.productivecows.data.CowDefinition;
import com.productivecows.menu.CowCageMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import java.util.ArrayList;
import java.util.List;

public class CowCageScreen extends AbstractContainerScreen<CowCageMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ProductiveCows.MODID, "textures/gui/cow_cage.png");

    public CowCageScreen(CowCageMenu menu, Inventory playerInventory, Component title) {
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

        // --- GAUGE 1: FLUID TANK (Left: X=28, Y=16, Width=42, Height=52) ---
        int gaugeX = x + 28;
        int gaugeY = y + 16;

        // Render fluid contents if available
        if (menu.getBlockEntity() instanceof CowCageBlockEntity cage) {
            FluidTank tank = cage.getFluidTank();
            int amount = tank.getFluidAmount();
            if (amount > 0 && !tank.getFluid().isEmpty()) {
                int color = getFluidColor(tank.getFluid().getFluid());
                int fillHeight = Math.min(52, (amount * 52) / 8000);
                
                int fluidTopY = gaugeY + 52 - fillHeight;
                guiGraphics.fill(gaugeX, fluidTopY, gaugeX + 42, gaugeY + 52, 0xFF000000 | color);
            }
        }

        // Draw Milking Cooldown Indicator overlay on the egg slot
        if (menu.getBlockEntity() instanceof CowCageBlockEntity cage) {
            int timer = menu.getMilkingTimer();
            if (timer > 0 && timer < 400) {
                int progress = ((400 - timer) * 16) / 400; // 16 pixels max
                guiGraphics.fill(x + 129, y + 52, x + 145, y + 54, 0xFF373737);
                guiGraphics.fill(x + 129, y + 52, x + 129 + progress, y + 54, 0xFF5DADE2);
            }
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
        int gaugeX = x + 28;
        int gaugeY = y + 16;

        if (mouseX >= gaugeX && mouseX < gaugeX + 42 && mouseY >= gaugeY && mouseY < gaugeY + 52) {
            if (menu.getBlockEntity() instanceof CowCageBlockEntity cage) {
                FluidTank tank = cage.getFluidTank();
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
