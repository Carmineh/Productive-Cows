package com.productivecows.client.screen;

import com.productivecows.menu.BreedingCageMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import com.productivecows.ProductiveCows;

public class BreedingCageScreen extends AbstractContainerScreen<BreedingCageMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ProductiveCows.MODID, "textures/gui/breeding_cage.png");

    public BreedingCageScreen(BreedingCageMenu menu, Inventory playerInventory, Component title) {
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
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // Draw main custom background texture
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, 220, 166);

        // --- PROGRESS HORIZONTAL BAR (X=67, Y=42, Width=39, Height=2) ---
        int progressX = x + 67;
        int progressY = y + 42;

        int progress = menu.getProgress();
        int total = menu.getMaxProgress();
        if (total > 0 && progress > 0) {
            int fillWidth = Math.min(39, (progress * 39) / total);
            guiGraphics.fillGradient(progressX, progressY, progressX + fillWidth, progressY + 2, 0xFFFF69B4, 0xFFFF1493);
        }
    }


}
