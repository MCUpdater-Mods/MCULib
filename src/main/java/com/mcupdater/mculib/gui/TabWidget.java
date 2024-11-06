package com.mcupdater.mculib.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

public class TabWidget extends AbstractWidget {
    private final int COLOR_SHADOW = 0x7f373737;
    private final int COLOR_HIGHLIGHT = 0x7fffffff;
    private int baseColor;
    private int selectedColor;
    private ResourceLocation icon;
    private ClickAction<?> clickAction;
    public boolean active = true;
    public boolean visible = true;
    protected boolean isHovered;
    protected boolean selected;
    private boolean focused;
    protected AbstractWidget child;

    public TabWidget(int x, int y, int width, int height, int baseColor, int selectedColor, ResourceLocation icon, Component pMessage, ClickAction<?> clickAction) {
        super(x, y, width, height, pMessage);
        this.baseColor = baseColor;
        this.selectedColor = selectedColor;
        this.icon = icon;
        this.clickAction = clickAction;
    }

    @Override
    public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        if (this.visible) {
            this.isHovered = pMouseX >= this.getX() && pMouseY >= this.getY() && pMouseX < this.getX() + this.width && pMouseY < this.getY() + this.height;
            // Render the tab
            pGuiGraphics.fill(getX(), getY(), getX() + width, getY() + height, this.selected ? selectedColor : baseColor);
            pGuiGraphics.hLine(getX(), getX() + width - 1, getY(), COLOR_HIGHLIGHT);
            pGuiGraphics.vLine(getX(), getY(), getY() + height - 1, COLOR_HIGHLIGHT);
            pGuiGraphics.hLine(getX(), getX() + width - 1, getY() + height - 1, COLOR_SHADOW);
            pGuiGraphics.vLine(getX() + width - 1, getY(), getY() + height - 1, COLOR_SHADOW);

            // Render the icon
            pGuiGraphics.blit(this.icon, this.getX() + 3, this.getY() + 3, 0, 0f, 0f, 16, 16, 16, 16);

            // Render child
            if (child != null) {
                child.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
            }
        }
    }

    public void setChild(AbstractWidget child) {
        this.child = child;
    }

    public boolean isHoveredOrFocused() {
        return this.isHovered || this.focused;
    }

	@Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (this.active && this.visible) {
            if (pButton == 0) { // Left click
                if (this.isMouseOver(pMouseX, pMouseY)) {
                    this.playDownSound(Minecraft.getInstance().getSoundManager());
                    this.onClick(pMouseX, pMouseY);
                    return true;
                }
            }
            if (this.selected && child instanceof GuiEventListener childEvent) {
                return childEvent.mouseClicked(pMouseX, pMouseY, pButton);
            }
        }
        return false;
    }

    public void onClick(double pMouseX, double pMouseY) {
        this.selected = !this.selected;
        this.clickAction.click(pMouseX, pMouseY);
    }

    public void playDownSound(SoundManager soundManager) {
        soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    public NarrationPriority narrationPriority() {
        if (this.focused) {
            return NarrationPriority.FOCUSED;
        } else {
            return this.isHovered ? NarrationPriority.HOVERED : NarrationPriority.NONE;
        }
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
        this.defaultButtonNarrationText(pNarrationElementOutput);
    }

    @FunctionalInterface
    public interface ClickAction<T> {
        void click(double mouseX, double mouseY);
    }
}
