package com.mcupdater.mculib.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractContainerWidget extends GuiComponent implements Widget, GuiEventListener, NarratableEntry {
    private final int COLOR_SHADOW = 0x7f373737;
    private final int COLOR_HIGHLIGHT = 0x7fffffff;

    public int x;
    public int y;
    protected int width;
    protected int height;
    private int backgroundColor;
    private List<Widget> children = new ArrayList<>();
    private boolean visible = true;
    private List<NarratableEntry> narratables = new ArrayList<>();

    public AbstractContainerWidget(int x, int y, int width, int height, int backgroundColor) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.backgroundColor = backgroundColor;
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        if (this.visible) {
            fill(pPoseStack, x, y, x + width, y + height, backgroundColor);
            this.hLine(pPoseStack, x, x + width - 1, y, COLOR_HIGHLIGHT);
            this.vLine(pPoseStack, x, y, y + height - 1, COLOR_HIGHLIGHT);
            this.hLine(pPoseStack, x, x + width - 1, y + height - 1, COLOR_SHADOW);
            this.vLine(pPoseStack, x + width - 1, y, y + height - 1, COLOR_SHADOW);

            for (Widget child : this.children) {
                child.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
            }
        }
    }
    
    public <T extends Widget> T addChild(T child) {
        this.children.add(child);
        if (child instanceof NarratableEntry) {
            this.narratables.add((NarratableEntry) child);
        }
        return child;
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {

    }

    public void setVisible(boolean newValue) {
        this.visible = newValue;
    }

    public boolean isVisible() {
        return this.visible;
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        for (Widget child : this.children) {
            if (child instanceof GuiEventListener childEvent) {
                if (childEvent.mouseClicked(pMouseX,pMouseY,pButton)) {
                    return true;
                }
            }
        }
        return false;
    }
}
