package com.mcupdater.mculib.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

import java.util.Collections;

public class TextButton extends Button {

    public TextButton(int pX, int pY, int pWidth, int pHeight, Component pMessage, OnPress pOnPress, Component pTooltip) {
        super(pX, pY, pWidth, pHeight, pMessage, pOnPress, supplier->Component.empty());
        setTooltip(Tooltip.create(pTooltip));
    }

    @Override
    public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        drawButton(pGuiGraphics);
        pGuiGraphics.drawCenteredString(Minecraft.getInstance().font, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, 0xffffffff);
    }

    private void drawButton(GuiGraphics guiGraphics) {
        guiGraphics.fill(getX(), getY(), getX() + width, getY() + height, 0xff969696);
        guiGraphics.hLine(getX(), getX() + width - 1, getY(), 0x7fffffff);
        guiGraphics.vLine(getX(), getY(), getY() + height - 1, 0x7fffffff);
        guiGraphics.hLine(getX(), getX() + width - 1, getY() + height - 1, 0x7f373737);
        guiGraphics.vLine(getX() + width - 1, getY(), getY() + height - 1, 0x7f373737);
    }
}
