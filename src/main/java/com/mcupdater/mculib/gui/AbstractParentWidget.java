package com.mcupdater.mculib.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractParentWidget extends AbstractWidget implements Renderable, LayoutElement, GuiEventListener, NarratableEntry {
    private final int COLOR_SHADOW = 0x7f373737;
    private final int COLOR_HIGHLIGHT = 0x7fffffff;

    public int x;
    public int y;
    protected int width;
    protected int height;
    private int backgroundColor;
    private List<AbstractWidget> children = new ArrayList<>();
    private boolean visible = true;

    public AbstractParentWidget(int x, int y, int width, int height, Component pMessage, int backgroundColor) {
        super(x, y, width, height, pMessage);
        this.backgroundColor = backgroundColor;
    }

    @Override
    public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        if (this.visible) {
            pGuiGraphics.fill(x, y, x + width, y + height, backgroundColor);
            pGuiGraphics.hLine(x, x + width - 1, y, COLOR_HIGHLIGHT);
            pGuiGraphics.vLine(x, y, y + height - 1, COLOR_HIGHLIGHT);
            pGuiGraphics.hLine(x, x + width - 1, y + height - 1, COLOR_SHADOW);
            pGuiGraphics.vLine(x + width - 1, y, y + height - 1, COLOR_SHADOW);

            for (Renderable child : this.children) {
                child.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
            }
        }
    }
    
    public <T extends AbstractWidget> T addChild(T child) {
        this.children.add(child);
        return child;
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    public void setVisible(boolean newValue) {
        this.visible = newValue;
    }

    public boolean isVisible() {
        return this.visible;
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        for (Renderable child : this.children) {
            if (child instanceof GuiEventListener childEvent) {
                if (childEvent.mouseClicked(pMouseX,pMouseY,pButton)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void setFocused(boolean pFocused) {

    }

    @Override
    public boolean isFocused() {
        return false;
    }

    @Override
    public void setX(int pX) {

    }

    @Override
    public void setY(int pY) {

    }

    @Override
    public int getX() {
        return 0;
    }

    @Override
    public int getY() {
        return 0;
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
        for (AbstractWidget child : this.children) {
            child.updateNarration(pNarrationElementOutput);
        }
    }
}
