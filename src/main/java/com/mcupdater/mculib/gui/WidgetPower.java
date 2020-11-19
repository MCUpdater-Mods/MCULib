package com.mcupdater.mculib.gui;

import com.mcupdater.mculib.MCULib;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import java.util.Collections;

@OnlyIn(Dist.CLIENT)
public class WidgetPower extends Widget {
    private final Orientation orientation;
    private final Minecraft minecraft;
    private ResourceLocation ENERGY = new ResourceLocation(MCULib.MODID, "textures/gui/energy.png");
    private IEnergyStorage energyHandler;
    private int COLOR_BACKGROUND = 0xff8b8b8b;
    private int COLOR_TOPLEFT = 0x7f373737;
    private int COLOR_BOTTOMRIGHT = 0x7fffffff;

    public WidgetPower(int x, int y, int width, int height, IEnergyStorage handler, Orientation orientation) {
        super(x, y, width, height, StringTextComponent.EMPTY);
        this.minecraft = Minecraft.getInstance();
        this.energyHandler = handler;
        this.orientation = orientation;
    }

    @Override
    public void renderButton(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        //draw Box
        this.fillGradient(matrixStack,x + 1, y + 1, x + width - 1, y + height - 1, COLOR_BACKGROUND, COLOR_BACKGROUND); // interior
        this.hLine(matrixStack, x, x+width-1, y, COLOR_TOPLEFT); // top
        this.vLine(matrixStack, x, y, y + height-1, COLOR_TOPLEFT); // left
        this.hLine(matrixStack, x, x+width-1, y + height-1, COLOR_BOTTOMRIGHT); // bottom
        this.vLine(matrixStack, x + width-1, y, y + height-1, COLOR_BOTTOMRIGHT); // right

        //fill Gauge
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getTextureManager().bindTexture(ENERGY);
        int energyOffset;
        int transform = (int) minecraft.world.getWorldInfo().getGameTime() % 256;
        switch(orientation) {
            case VERTICAL:
                energyOffset = getEnergyScaled(this.height - 2);
                this.blit(matrixStack, this.x + 1, this.y + (this.height - energyOffset) - 1, transform, transform, this.width-2, energyOffset);
            break;
            case HORIZONAL:
                energyOffset = getEnergyScaled(this.width - 2);
                this.blit(matrixStack, this.x + 1, this.y + 1, transform, transform, energyOffset, this.height-2);
        }

        if (this.isHovered()) {
            renderToolTip(matrixStack, mouseX, mouseY);
        }
    }

    private int getEnergyScaled(int height) {
        return this.energyHandler.getMaxEnergyStored() != 0 ? (int) (height * ((this.energyHandler.getEnergyStored() * 1.0d) / (this.energyHandler.getMaxEnergyStored() * 1.0d))) : height;
    }

    @Override
    public void renderToolTip(@Nonnull MatrixStack matrixStack, int x, int y){
        super.renderToolTip(matrixStack, x, y);
        String msg = energyHandler.getEnergyStored() + " / " + energyHandler.getMaxEnergyStored() + " FE";
        if (this.minecraft.currentScreen != null) {
            this.minecraft.currentScreen.renderTooltip(matrixStack, Collections.singletonList(new StringTextComponent(msg)), x, y);
        }
    }

    public enum Orientation {
        HORIZONAL,VERTICAL;
    }
}
