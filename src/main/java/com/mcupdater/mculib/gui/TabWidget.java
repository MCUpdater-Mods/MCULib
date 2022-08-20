package com.mcupdater.mculib.gui;

import com.mcupdater.mculib.MCULib;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

import java.util.Collections;

public class TabWidget extends GuiComponent implements Widget, GuiEventListener, NarratableEntry {
    private final int COLOR_SHADOW = 0x7f373737;
    private final int COLOR_HIGHLIGHT = 0x7fffffff;
    private int baseColor;
    private int selectedColor;
    private ResourceLocation icon;
    private ClickAction<?> clickAction;
    public int x;
    public int y;
    protected int width;
    protected int height;
    private Component message;
    public boolean active = true;
    public boolean visible = true;
    protected boolean isHovered;
    protected boolean selected;
    private boolean focused;
    protected Widget child;

    public TabWidget(int x, int y, int width, int height, int baseColor, int selectedColor, ResourceLocation icon, Component pMessage, ClickAction<?> clickAction) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.baseColor = baseColor;
        this.selectedColor = selectedColor;
        this.icon = icon;
        this.message = pMessage;
        this.clickAction = clickAction;
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        if (this.visible) {
            this.isHovered = pMouseX >= this.x && pMouseY >= this.y && pMouseX < this.x + this.width && pMouseY < this.y + this.height;
            // Render the tab
            fill(pPoseStack, x, y, x + width, y + height, this.selected ? selectedColor : baseColor);
            this.hLine(pPoseStack, x, x + width - 1, y, COLOR_HIGHLIGHT);
            this.vLine(pPoseStack, x, y, y + height - 1, COLOR_HIGHLIGHT);
            this.hLine(pPoseStack, x, x + width - 1, y + height - 1, COLOR_SHADOW);
            this.vLine(pPoseStack, x + width - 1, y, y + height - 1, COLOR_SHADOW);

            // Render the icon
            RenderSystem.setShaderTexture(0, this.icon);
            this.blit(pPoseStack, this.x + 3, this.y + 3, this.getBlitOffset(), 0f, 0f, 16, 16, 16, 16);

            // Render child
            if (child != null) {
                child.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
            }

            if (this.isHoveredOrFocused()) {
                renderTooltip(pPoseStack, pMouseX, pMouseY);
            }
        }
    }

    public void setChild(Widget child) {
        this.child = child;
    }

    public void renderTooltip(PoseStack pPoseStack, int pMouseX, int pMouseY) {
        if (Minecraft.getInstance().screen != null) {
            Minecraft.getInstance().screen.renderComponentTooltip(pPoseStack, Collections.singletonList(message), pMouseX, pMouseY);
        }
    }

    public boolean isHoveredOrFocused() {
        return this.isHovered || this.focused;
    }

    public int getHeight() {
        return this.height;
    }

    public int getWidth() {
        return this.width;
    }

    @Override
    public boolean changeFocus(boolean pFocus) {
        if (this.active && this.visible) {
            this.focused = !this.focused;
            this.onFocusChanged(this.focused);
            return this.focused;
        } else {
            return false;
        }
    }

    protected void onFocusChanged(boolean pFocused) {
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
                if (childEvent.mouseClicked(pMouseX,pMouseY,pButton)) {
                    return true;
                }
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
    public boolean isMouseOver(double pMouseX, double pMouseY) {
        return this.active && this.visible && pMouseX >= (double)this.x && pMouseY >= (double)this.y && pMouseX < (double)(this.x + this.width) && pMouseY < (double)(this.y + this.height);
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
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {
        if (this.isHoveredOrFocused()) {
            pNarrationElementOutput.add(NarratedElementType.HINT, this.message);
        }
    }

    @FunctionalInterface
    public interface ClickAction<T> {
        void click(double mouseX, double mouseY);
    }
}
