package com.materialcows.client.screen;

import com.materialcows.menu.BreedingCageMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class BreedingCageScreen extends AbstractContainerScreen<BreedingCageMenu> {

    public BreedingCageScreen(BreedingCageMenu menu, Inventory playerInventory, Component title) {
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
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // Draw vanilla gray 3D background panel
        drawVanillaPanel(guiGraphics, x, y, this.imageWidth, this.imageHeight);

        // Draw machine slots
        drawVanillaSlot(guiGraphics, x + 44, y + 17); // Parent 1
        drawVanillaSlot(guiGraphics, x + 44, y + 53); // Parent 2
        drawVanillaSlot(guiGraphics, x + 80, y + 53); // Wheat
        drawVanillaSlot(guiGraphics, x + 116, y + 35); // Output

        // Draw Player Inventory slots
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                drawVanillaSlot(guiGraphics, x + 8 + j * 18, y + 84 + i * 18);
            }
        }
        for (int k = 0; k < 9; ++k) {
            drawVanillaSlot(guiGraphics, x + 8 + k * 18, y + 142);
        }

        // Draw progress arrow frame (X=75, Y=35)
        int progressX = x + 75;
        int progressY = y + 35;
        drawVanillaSlotFrame(guiGraphics, progressX - 1, progressY + 1, progressX + 33, progressY + 7);

        int progress = menu.getProgress();
        int total = menu.getMaxProgress();
        if (total > 0 && progress > 0) {
            int fillWidth = Math.min(32, (progress * 32) / total);
            // Draw progress bar with a gradient (pinkish for breeding)
            guiGraphics.fillGradient(progressX, progressY + 2, progressX + fillWidth, progressY + 6, 0xFFFF69B4, 0xFFFF1493);
            
            // Draw a glowing indicator if progress is advanced
            if (fillWidth > 26) {
                guiGraphics.fill(progressX + 28, progressY, progressX + 29, progressY + 8, 0xFFFF1493);
                guiGraphics.fill(progressX + 30, progressY + 1, progressX + 31, progressY + 7, 0xFFFF1493);
            }
        }
    }

    private void drawVanillaPanel(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        guiGraphics.fill(x, y, x + width, y + height, 0xFFC6C6C6);
        guiGraphics.fill(x, y, x + width, y + 1, 0xFFFFFFFF);
        guiGraphics.fill(x, y, x + 1, y + height, 0xFFFFFFFF);
        guiGraphics.fill(x, y + height - 1, x + width, y + height, 0xFF555555);
        guiGraphics.fill(x + width - 1, y, x + width, y + height, 0xFF555555);
    }

    private void drawVanillaSlot(GuiGraphics guiGraphics, int slotX, int slotY) {
        drawVanillaSlotFrame(guiGraphics, slotX - 1, slotY - 1, slotX + 17, slotY + 17);
    }

    private void drawVanillaSlotFrame(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2) {
        guiGraphics.fill(x1, y1, x2, y2, 0xFF8B8B8B);
        guiGraphics.fill(x1, y1, x2, y1 + 1, 0xFF373737);
        guiGraphics.fill(x1, y1, x1 + 1, y2, 0xFF373737);
        guiGraphics.fill(x1, y2 - 1, x2, y2, 0xFFFFFFFF);
        guiGraphics.fill(x2 - 1, y1, x2, y2, 0xFFFFFFFF);
    }
}
