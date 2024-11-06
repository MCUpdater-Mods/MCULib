package com.mcupdater.mculib.block;

import com.mcupdater.mculib.gui.ConfigPanel;
import com.mcupdater.mculib.gui.TabConfig;
import com.mcupdater.mculib.gui.WidgetPower;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMachineScreen<MACHINE extends AbstractMachineBlockEntity, MENU extends AbstractMachineMenu<MACHINE>> extends AbstractContainerScreen<MENU> {

    private ConfigPanel configPanel;
    private TabConfig configTab;
    private List<AbstractWidget> extraWidgets = new ArrayList<>();

    public AbstractMachineScreen(MENU pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(new WidgetPower(this.leftPos + 153, this.topPos + 5, 18, 71, menu.getEnergyHandler(), WidgetPower.Orientation.VERTICAL));
        this.registerWidgets();
        this.configPanel = new ConfigPanel(this.menu, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
        this.configPanel.setVisible(false);
        this.configTab = this.addRenderableWidget(new TabConfig(this.leftPos - 22, this.topPos + 2,22,22, (mouseX, mouseY) -> {
            this.configPanel.setVisible(!this.configPanel.isVisible());
            for (AbstractWidget widget : extraWidgets) {
                widget.visible = !widget.visible;
            }
        }));
        this.configTab.setChild(this.configPanel);
    }

    public void registerWidgets() {
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
        if (!this.configPanel.isVisible()) {
            super.render(guiGraphics, mouseX, mouseY, partialTicks);
            this.renderTooltip(guiGraphics, mouseX, mouseY);
        } else {
            renderNoSlots(guiGraphics, mouseX, mouseY, partialTicks);
        }
    }

    public void renderNoSlots(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        int i = this.leftPos;
        int j = this.topPos;
        this.renderBg(guiGraphics, pPartialTick, pMouseX, pMouseY);
        NeoForge.EVENT_BUS.post(new ContainerScreenEvent.Render.Background(this, guiGraphics, pMouseX, pMouseY));
        for(Renderable widget : this.renderables) {
            widget.render(guiGraphics, pMouseX, pMouseY, pPartialTick);
        }
        RenderSystem.disableDepthTest();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(i, j, 0.0D);
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.renderLabels(guiGraphics, pMouseX, pMouseY);
        NeoForge.EVENT_BUS.post(new ContainerScreenEvent.Render.Foreground(this, guiGraphics, pMouseX, pMouseY));
        guiGraphics.pose().popPose();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.enableDepthTest();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int relX = this.leftPos;
        int relY = this.topPos;
        guiGraphics.blit(getGUIResourceLocation(), relX, relY, 0, 0, this.imageWidth, this.imageHeight);
        if (this.menu.isWorking()) {
            int progress = this.menu.getWorkProgress();
            guiGraphics.blit(getGUIResourceLocation(), relX + 79, relY + 36, 176, 0, progress, 18);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int pMouseX, int pMouseY) {
        if (!this.configPanel.isVisible()) {
            super.renderLabels(guiGraphics, pMouseX, pMouseY);
        }
    }

    protected void addExtraWidget(AbstractWidget widget) {
        this.extraWidgets.add(widget);
    }

    protected abstract ResourceLocation getGUIResourceLocation();
}
