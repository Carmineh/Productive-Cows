package com.materialcows.client.screen;

import com.materialcows.Materialcows;
import com.materialcows.block.entity.CowCageBlockEntity;
import com.materialcows.data.CowDefinition;
import com.materialcows.menu.CowCageMenu;
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

    public CowCageScreen(CowCageMenu menu, Inventory playerInventory, Component title) {
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
        this.renderFluidTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // Draw vanilla gray 3D background panel
        drawVanillaPanel(guiGraphics, x, y, this.imageWidth, this.imageHeight);

        // Draw machine slots
        drawVanillaSlot(guiGraphics, x + 80, y + 20); // Egg slot
        drawVanillaSlot(guiGraphics, x + 35, y + 51); // Empty bucket slot
        drawVanillaSlot(guiGraphics, x + 125, y + 51); // Output filled slot

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

        // Draw Fluid Tank Gauge Frame (X=10, Y=17, Width=18, Height=52)
        int gaugeX = x + 10;
        int gaugeY = y + 17;
        drawVanillaSlotFrame(guiGraphics, gaugeX - 1, gaugeY - 1, gaugeX + 19, gaugeY + 53);

        // Render fluid contents if available
        if (menu.getBlockEntity() instanceof CowCageBlockEntity cage) {
            FluidTank tank = cage.getFluidTank();
            int amount = tank.getFluidAmount();
            if (amount > 0 && !tank.getFluid().isEmpty()) {
                int color = getFluidColor(tank.getFluid().getFluid());
                int fillHeight = Math.min(50, (amount * 50) / 8000);
                
                // Draw fluid column
                int fluidTopY = gaugeY + 51 - fillHeight;
                guiGraphics.fill(gaugeX + 1, fluidTopY, gaugeX + 17, gaugeY + 51, 0xFF000000 | color);
                
                // Draw dynamic shine overlay
                guiGraphics.fill(gaugeX + 1, fluidTopY, gaugeX + 4, gaugeY + 51, 0x40FFFFFF);
            }
        }

        // Draw Milking Cooldown Indicator overlay on the egg slot
        if (menu.getBlockEntity() instanceof CowCageBlockEntity cage) {
            int timer = menu.getMilkingTimer();
            if (timer > 0 && timer < 400) {
                int progress = ((400 - timer) * 16) / 400; // 16 pixels max
                // Draw a small custom loading bar below the egg slot
                guiGraphics.fill(x + 80, y + 38, x + 96, y + 41, 0xFF373737);
                guiGraphics.fill(x + 80, y + 38, x + 80 + progress, y + 40, 0xFF5DADE2); // Elegant blue loading indicator
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

    private void renderFluidTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        int gaugeX = x + 10;
        int gaugeY = y + 17;

        if (mouseX >= gaugeX && mouseX < gaugeX + 18 && mouseY >= gaugeY && mouseY < gaugeY + 52) {
            if (menu.getBlockEntity() instanceof CowCageBlockEntity cage) {
                FluidTank tank = cage.getFluidTank();
                List<Component> text = new ArrayList<>();
                if (tank.getFluidAmount() > 0) {
                    String name = tank.getFluid().getHoverName().getString();
                    text.add(Component.literal(name).withStyle(net.minecraft.ChatFormatting.AQUA));
                    text.add(Component.literal(tank.getFluidAmount() + " / 8000 mB").withStyle(net.minecraft.ChatFormatting.GRAY));
                } else {
                    text.add(Component.translatable("gui.materialcows.empty_tank").withStyle(net.minecraft.ChatFormatting.GRAY));
                }
                guiGraphics.renderComponentTooltip(this.font, text, mouseX, mouseY);
            }
        }
    }
}
