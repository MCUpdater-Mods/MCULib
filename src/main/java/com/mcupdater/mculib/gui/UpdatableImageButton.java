package com.mcupdater.mculib.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class UpdatableImageButton extends Button {
    private final int textureWidth;
    private final int textureHeight;
    private ResourceLocation resourceLocation;

    public UpdatableImageButton(int pX, int pY, int pWidth, int pHeight, int pTexWidth, int pTexHeight, Component pMessage, OnPress pOnPress) {
        super(pX, pY, pWidth, pHeight, pMessage, pOnPress, supplier->Component.empty());
        this.textureWidth = pTexWidth;
        this.textureHeight = pTexHeight;
    }

    public void setResourceLocation(ResourceLocation resLoc) {
        this.resourceLocation = resLoc;
    }

    @Override
    public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        drawButton(pGuiGraphics);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableDepthTest();
        pGuiGraphics.blit(this.resourceLocation,this.getX() + 2, this.getY() + 2, this.width - 4, this.height - 4, 0f, 0f, 16, 16, this.textureWidth, this.textureHeight);
    }

    private void drawButton(GuiGraphics guiGraphics) {
        guiGraphics.fill(getX(), getY(), getX() + width, getY() + height, 0xff969696);
        guiGraphics.hLine(getX(), getX() + width - 1, getY(), 0x7fffffff);
        guiGraphics.vLine(getX(), getY(), getY() + height - 1, 0x7fffffff);
        guiGraphics.hLine(getX(), getX() + width - 1, getY() + height - 1, 0x7f373737);
        guiGraphics.vLine(getX() + width - 1, getY(), getY() + height - 1, 0x7f373737);
    }

    public void setTooltip(Component tooltip) {
        setTooltip(Tooltip.create(tooltip));
    }
}
