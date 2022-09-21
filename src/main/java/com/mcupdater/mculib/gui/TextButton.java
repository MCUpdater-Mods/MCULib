package com.mcupdater.mculib.gui;

import com.mcupdater.mculib.MCULib;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.Collections;

public class TextButton extends Button {
    private final Component tooltip;

    public TextButton(int pX, int pY, int pWidth, int pHeight, Component pMessage, OnPress pOnPress, Component pTooltip) {
        super(pX, pY, pWidth, pHeight, pMessage, pOnPress);
        this.tooltip = pTooltip;
    }

    @Override
    public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        drawButton(pPoseStack);
        drawCenteredString(pPoseStack, Minecraft.getInstance().font, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, 0xffffffff);
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
}
