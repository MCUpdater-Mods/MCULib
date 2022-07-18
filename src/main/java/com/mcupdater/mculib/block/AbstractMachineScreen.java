package com.mcupdater.mculib.block;

import com.mcupdater.mculib.gui.ConfigPanel;
import com.mcupdater.mculib.gui.TabConfig;
import com.mcupdater.mculib.gui.WidgetPower;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public abstract class AbstractMachineScreen<MACHINE extends AbstractMachineBlockEntity, MENU extends AbstractMachineMenu<MACHINE>> extends AbstractContainerScreen<MENU> {

    private ConfigPanel configPanel;

    public AbstractMachineScreen(MENU pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(new WidgetPower(this.leftPos + 153, this.topPos + 5, 18, 71, menu.getEnergyHandler(), WidgetPower.Orientation.VERTICAL));
        this.configPanel = this.addRenderableWidget(new ConfigPanel(this.menu, this.leftPos, this.topPos, this.imageWidth, this.imageHeight));
        this.configPanel.visible = false;
        this.addRenderableWidget(new TabConfig<MENU>(this.leftPos - 22, this.topPos + 2,22,22, (mouseX, mouseY) -> {
            this.configPanel.visible = !this.configPanel.visible;
        }));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);
        if (!this.configPanel.visible) {
            super.render(poseStack, mouseX, mouseY, partialTicks);
        } else {
            renderNoSlots(poseStack, mouseX, mouseY, partialTicks);
        }
        this.renderTooltip(poseStack, mouseX, mouseY);
    }

    public void renderNoSlots(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        int i = this.leftPos;
        int j = this.topPos;
        this.renderBg(pPoseStack, pPartialTick, pMouseX, pMouseY);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.ContainerScreenEvent.DrawBackground(this, pPoseStack, pMouseX, pMouseY));
        RenderSystem.disableDepthTest();
        for(Widget widget : this.renderables) {
            widget.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        }
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.translate((double)i, (double)j, 0.0D);
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.renderLabels(pPoseStack, pMouseX, pMouseY);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.ContainerScreenEvent.DrawForeground(this, pPoseStack, pMouseX, pMouseY));
        posestack.popPose();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.enableDepthTest();
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, getGUIResourceLocation());
        int relX = this.leftPos;
        int relY = this.topPos;
        this.blit(poseStack, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
        if (this.menu.isWorking()) {
            int progress = this.menu.getWorkProgress();
            this.blit(poseStack, relX + 79, relY + 36, 176, 0, progress, 18);
        }
    }

    @Override
    protected void renderLabels(PoseStack pPoseStack, int pMouseX, int pMouseY) {
        if (!this.configPanel.visible) {
            super.renderLabels(pPoseStack, pMouseX, pMouseY);
        }
    }

    protected abstract ResourceLocation getGUIResourceLocation();
}
