package com.mcupdater.mculib.gui;

import com.mcupdater.mculib.MCULib;
import com.mcupdater.mculib.block.AbstractMachineMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class TabConfig<CONTAINER extends AbstractMachineMenu> extends AbstractWidget {

    private final int COLOR_BACKGROUND = 0xff2b8b2b;
    private final int COLOR_SHADOW = 0x7f373737;
    private final int COLOR_HIGHLIGHT = 0x7fffffff;
    private ResourceLocation WRENCH = new ResourceLocation(MCULib.MODID, "textures/gui/wrench.png");
    private ClickAction<AbstractContainerScreen<AbstractMachineMenu>> clickAction;

    public TabConfig(int x, int y, int width, int height, ClickAction<AbstractContainerScreen<AbstractMachineMenu>> clickAction) {
        super(x, y, width, height, TextComponent.EMPTY);
        this.clickAction = clickAction;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {

    }

    @Override
    public void renderButton(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.fillGradient(poseStack, x, y, x + width, y + height, COLOR_BACKGROUND, COLOR_BACKGROUND);
        this.hLine(poseStack, x, x + width - 1, y, COLOR_HIGHLIGHT);
        this.vLine(poseStack, x, y, y + height - 1, COLOR_HIGHLIGHT);
        this.hLine(poseStack, x, x + width - 1, y + height - 1, COLOR_SHADOW);
        this.vLine(poseStack, x + width -1, y, y + height - 1, COLOR_SHADOW);

        RenderSystem.setShaderTexture(0, WRENCH);
        this.blit(poseStack, this.x + 3, this.y + 3, this.getBlitOffset(), (float) 0, (float) 0, 16, 16, 16, 16);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.clickAction.click(mouseX, mouseY);
    }

    @FunctionalInterface
    public interface ClickAction<T extends AbstractContainerScreen<AbstractMachineMenu>> {
        void click(double mouseX, double mouseY);
    }
}
