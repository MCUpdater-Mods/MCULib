package com.mcupdater.mculib.gui;

import com.mcupdater.mculib.MCULib;
import com.mcupdater.mculib.block.AbstractMachineMenu;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public class TabConfig extends TabWidget {

    private static final int COLOR_BACKGROUND = 0xff2b8b2b;
    private static final int COLOR_SELECTED = 0xff5bbb5b;
    private static ResourceLocation WRENCH = new ResourceLocation(MCULib.MODID, "textures/gui/icon/wrench.png");

    public TabConfig(int x, int y, int width, int height, ClickAction<AbstractContainerScreen<AbstractMachineMenu<?>>> clickAction) {
        super(x, y, width, height, COLOR_BACKGROUND, COLOR_SELECTED, WRENCH, new TranslatableComponent("gui.processenhancement.config"), clickAction);
    }

    /*
    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {

    }

    @Override
    public void renderButton(@Nonnull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        fill(poseStack, x, y, x + width, y + height, selected ? COLOR_SELECTED : COLOR_BACKGROUND);
        this.hLine(poseStack, x, x + width - 1, y, COLOR_HIGHLIGHT);
        this.vLine(poseStack, x, y, y + height - 1, COLOR_HIGHLIGHT);
        this.hLine(poseStack, x, x + width - 1, y + height - 1, COLOR_SHADOW);
        this.vLine(poseStack, x + width -1, y, y + height - 1, COLOR_SHADOW);

        RenderSystem.setShaderTexture(0, WRENCH);
        this.blit(poseStack, this.x + 3, this.y + 3, this.getBlitOffset(), (float) 0, (float) 0, 16, 16, 16, 16);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.selected = !this.selected;
        this.clickAction.click(mouseX, mouseY);
    }

    @FunctionalInterface
    public interface ClickAction<T extends AbstractContainerScreen<AbstractMachineMenu>> {
        void click(double mouseX, double mouseY);
    }

     */
}
