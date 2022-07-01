package com.mcupdater.mculib.gui;

import com.mcupdater.mculib.MCULib;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import java.util.Collections;

@OnlyIn(Dist.CLIENT)
public class WidgetPower extends AbstractWidget {
    private final Orientation orientation;
    private final Minecraft minecraft;
    private ResourceLocation ENERGY = new ResourceLocation(MCULib.MODID, "textures/gui/energy.png");
    private IEnergyStorage energyHandler;
    private int COLOR_BACKGROUND = 0xff8b8b8b;
    private int COLOR_TOPLEFT = 0x7f373737;
    private int COLOR_BOTTOMRIGHT = 0x7fffffff;

    public WidgetPower(int x, int y, int width, int height, IEnergyStorage handler, Orientation orientation) {
        super(x, y, width, height, TextComponent.EMPTY);
        this.minecraft = Minecraft.getInstance();
        this.energyHandler = handler;
        this.orientation = orientation;
    }

    @Override
    public void renderButton(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        //draw Box
        this.fillGradient(poseStack,x + 1, y + 1, x + width - 1, y + height - 1, COLOR_BACKGROUND, COLOR_BACKGROUND); // interior
        this.hLine(poseStack, x, x+width-1, y, COLOR_TOPLEFT); // top
        this.vLine(poseStack, x, y, y + height-1, COLOR_TOPLEFT); // left
        this.hLine(poseStack, x, x+width-1, y + height-1, COLOR_BOTTOMRIGHT); // bottom
        this.vLine(poseStack, x + width-1, y, y + height-1, COLOR_BOTTOMRIGHT); // right

        //fill Gauge
        Minecraft minecraft = Minecraft.getInstance();
        RenderSystem.setShaderTexture(0, ENERGY);
        int energyOffset;
        int transform = (int) minecraft.level.getLevelData().getGameTime() % 256;
        switch(orientation) {
            case VERTICAL:
                energyOffset = getEnergyScaled(this.height - 2);
                this.blit(poseStack, this.x + 1, this.y + (this.height - energyOffset) - 1, transform, transform, this.width-2, energyOffset);
            break;
            case HORIZONAL:
                energyOffset = getEnergyScaled(this.width - 2);
                this.blit(poseStack, this.x + 1, this.y + 1, transform, transform, energyOffset, this.height-2);
        }

        if (this.isHoveredOrFocused()) {
            renderToolTip(poseStack, mouseX, mouseY);
        }
    }

    private int getEnergyScaled(int height) {
        return this.energyHandler.getMaxEnergyStored() != 0 ? (int) (height * ((this.energyHandler.getEnergyStored() * 1.0d) / (this.energyHandler.getMaxEnergyStored() * 1.0d))) : height;
    }

    @Override
    public void renderToolTip(@Nonnull PoseStack poseStack, int x, int y){
        super.renderToolTip(poseStack, x, y);
        String msg = energyHandler.getEnergyStored() + " / " + energyHandler.getMaxEnergyStored() + " FE";
        if (this.minecraft.screen != null) {
            this.minecraft.screen.renderComponentTooltip(poseStack, Collections.singletonList(new TextComponent(msg)), x, y);
        }
    }

    @Override
    public void updateNarration(NarrationElementOutput p_169152_) {

    }

    public enum Orientation {
        HORIZONAL,VERTICAL;
    }
}
