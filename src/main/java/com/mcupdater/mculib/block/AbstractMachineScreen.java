package com.mcupdater.mculib.block;

import com.mcupdater.mculib.gui.TabConfig;
import com.mcupdater.mculib.gui.WidgetPower;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public abstract class AbstractMachineScreen<MACHINE extends AbstractMachineBlockEntity, MENU extends AbstractMachineMenu<MACHINE>> extends AbstractContainerScreen<MENU> {

    public AbstractMachineScreen(MENU pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(new WidgetPower(this.leftPos + 153, this.topPos + 5, 18, 71, menu.getEnergyHandler(), WidgetPower.Orientation.VERTICAL));
        this.addRenderableWidget(new TabConfig<MENU>(this.leftPos - 22, this.topPos + 2,22,22, (mouseX, mouseY) -> {
            this.getMenu().showConfigUI();
        }));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(poseStack, mouseX, mouseY);
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


    protected abstract ResourceLocation getGUIResourceLocation();
}
