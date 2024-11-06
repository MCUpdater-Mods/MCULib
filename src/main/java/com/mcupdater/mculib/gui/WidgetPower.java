package com.mcupdater.mculib.gui;

import com.mcupdater.mculib.MCULib;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import java.util.List;

public class WidgetPower extends AbstractWidget {
    private final Orientation orientation;
    private final Minecraft minecraft;
    private ResourceLocation ENERGY = ResourceLocation.fromNamespaceAndPath(MCULib.MODID, "textures/gui/energy.png");
    private IEnergyStorage energyHandler;
    private int COLOR_BACKGROUND = 0xff8b8b8b;
    private int COLOR_TOPLEFT = 0x7f373737;
    private int COLOR_BOTTOMRIGHT = 0x7fffffff;

    public WidgetPower(int x, int y, int width, int height, IEnergyStorage handler, Orientation orientation) {
        super(x, y, width, height, Component.empty());
        this.minecraft = Minecraft.getInstance();
        this.energyHandler = handler;
        this.orientation = orientation;
    }

    @Override
    protected boolean isValidClickButton(int button) {
        return false;
    }
    @Override
    public void renderWidget(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        //draw Box
        guiGraphics.fillGradient(getX() + 1, getY() + 1, getX() + width - 1, getY() + height - 1, COLOR_BACKGROUND, COLOR_BACKGROUND); // interior
        guiGraphics.hLine(getX(), getX()+width-1, getY(), COLOR_TOPLEFT); // top
        guiGraphics.vLine(getX(), getY(), getY() + height-1, COLOR_TOPLEFT); // left
        guiGraphics.hLine(getX(), getX()+width-1, getY() + height-1, COLOR_BOTTOMRIGHT); // bottom
        guiGraphics.vLine(getX() + width-1, getY(), getY() + height-1, COLOR_BOTTOMRIGHT); // right

        //fill Gauge
        Minecraft minecraft = Minecraft.getInstance();
        int energyOffset;
        int transform = (int) minecraft.level.getLevelData().getGameTime() % 256;
        switch (orientation) {
            case VERTICAL -> {
                energyOffset = getEnergyScaled(this.height - 2);
                guiGraphics.blit(ENERGY, this.getX() + 1, this.getY() + (this.height - energyOffset) - 1, transform, transform, this.width - 2, energyOffset);
            }
            case HORIZONAL -> {
                energyOffset = getEnergyScaled(this.width - 2);
                guiGraphics.blit(ENERGY, this.getX() + 1, this.getY() + 1, transform, transform, energyOffset, this.height - 2);
            }
        }

        if (this.isHoveredOrFocused()) {
            renderEnergyTooltip();
        }
    }

    private int getEnergyScaled(int height) {
        return this.energyHandler.getMaxEnergyStored() != 0 ? (int) (height * ((this.energyHandler.getEnergyStored() * 1.0d) / (this.energyHandler.getMaxEnergyStored() * 1.0d))) : height;
    }

    public void renderEnergyTooltip(){
        String msg = energyHandler.getEnergyStored() + " / " + energyHandler.getMaxEnergyStored() + " FE";
        if (this.minecraft.screen != null) {
            this.minecraft.screen.setTooltipForNextRenderPass(List.of(FormattedCharSequence.forward(msg, Style.EMPTY)));
        }
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput p_169152_) {

    }

    public enum Orientation {
        HORIZONAL,VERTICAL;
    }
}
