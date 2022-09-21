package com.mcupdater.mculib.gui;

import com.mcupdater.mculib.MCULib;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;

public class UpdatableImageButton extends Button {
    private final int textureWidth;
    private final int textureHeight;
    private ResourceLocation resourceLocation;
    private Component tooltip;

    public UpdatableImageButton(int pX, int pY, int pWidth, int pHeight, int pTexWidth, int pTexHeight, Component pMessage, OnPress pOnPress) {
        super(pX, pY, pWidth, pHeight, pMessage, pOnPress);
        this.textureWidth = pTexWidth;
        this.textureHeight = pTexHeight;
    }

    public void setResourceLocation(ResourceLocation resLoc) {
        this.resourceLocation = resLoc;
    }

    @Override
    public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        drawButton(pPoseStack);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, this.resourceLocation);
        RenderSystem.enableDepthTest();
        blit(pPoseStack, this.x + 2, this.y + 2, this.width - 4, this.height - 4, 0f, 0f, 16, 16, this.textureWidth, this.textureHeight);
    }

    private void drawButton(PoseStack pPoseStack) {
        fill(pPoseStack, x, y, x + width, y + height, 0xff969696);
        this.hLine(pPoseStack, x, x + width - 1, y, 0x7fffffff);
        this.vLine(pPoseStack, x, y, y + height - 1, 0x7fffffff);
        this.hLine(pPoseStack, x, x + width - 1, y + height - 1, 0x7f373737);
        this.vLine(pPoseStack, x + width - 1, y, y + height - 1, 0x7f373737);
    }

    @Override
    public void renderToolTip(PoseStack pPoseStack, int pMouseX, int pMouseY) {
        if (Minecraft.getInstance().screen != null) {
            Minecraft.getInstance().screen.renderComponentTooltip(pPoseStack, Collections.singletonList(this.tooltip), pMouseX, pMouseY);
        }
    }

    public void setTooltip(Component tooltip) {
        this.tooltip = tooltip;
    }
}
