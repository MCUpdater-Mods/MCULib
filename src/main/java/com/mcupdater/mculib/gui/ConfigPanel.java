package com.mcupdater.mculib.gui;

import com.mcupdater.mculib.MCULib;
import com.mcupdater.mculib.block.IConfigurableMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConfigPanel extends AbstractContainerWidget {

    private static final int COLOR_BACKGROUND = 0xffc6c6c6;
    private final Font font;
    private final IConfigurableMenu menu;

    // Icon definitions
    protected final ResourceLocation CLOSED = new ResourceLocation(MCULib.MODID, "textures/gui/icon/prohibition.png");
    protected final ResourceLocation ALLOWED = new ResourceLocation(MCULib.MODID, "textures/gui/icon/arrow.png");
    protected final ResourceLocation AUTOMATED = new ResourceLocation(MCULib.MODID, "textures/gui/icon/gear-arrow.png");
    protected final ResourceLocation ITEMS = new ResourceLocation(MCULib.MODID, "textures/gui/icon/box.png");
    protected final ResourceLocation ENERGY = new ResourceLocation(MCULib.MODID, "textures/gui/icon/lightning.png");
    protected final ResourceLocation FLUIDS = new ResourceLocation(MCULib.MODID, "textures/gui/icon/flask.png");

    private List<TabWidget> tabs = new ArrayList<>();

    public ConfigPanel(IConfigurableMenu srcMenu, int leftPos, int topPos, int width, int height) {
        super(leftPos, topPos, width, height, COLOR_BACKGROUND);
        this.font = Minecraft.getInstance().font;
        this.menu = srcMenu;

        BlockEntity self = this.menu.getBlockEntity();
        int hOffset = 0;
        if (self.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent()) {
            MCULib.LOGGER.debug("Item Capability Present");
            this.addTab(new TabWidget(leftPos + hOffset, topPos - 22, 22, 22, 0xff969696, 0xffd6d6d6, ITEMS, new TranslatableComponent("gui.processenhancement.items"), (mouseX, mouseY) -> {}));
            hOffset += 23;
        } else {
            MCULib.LOGGER.debug("No items");
        }
        if (self.getCapability(CapabilityEnergy.ENERGY).isPresent()) {
            MCULib.LOGGER.debug("Energy Capability Present");
            this.addTab(new TabWidget(leftPos + hOffset, topPos - 22, 22, 22, 0xff969696, 0xffd6d6d6, ENERGY, new TranslatableComponent("gui.processenhancement.energy"), (mouseX, mouseY) -> {}));
            hOffset += 23;
        } else {
            MCULib.LOGGER.debug("No energy");
        }
        if (self.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).isPresent()) {
            MCULib.LOGGER.debug("Fluid Capability Present");
            this.addTab(new TabWidget(leftPos + hOffset, topPos - 22, 22, 22, 0xff969696, 0xffd6d6d6, FLUIDS, new TranslatableComponent("gui.processenhancement.fluids"), (mouseX, mouseY) -> {}));
            hOffset += 23;
        } else {
            MCULib.LOGGER.debug("No fluids");
        }
    }

    private void addTab(TabWidget newTab) {
        this.tabs.add(newTab);
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);

        if (this.isVisible()) {
            // Render tabs in reverse order to ensure tooltips render properly
            List<TabWidget> reverseTabs = new ArrayList<>(this.tabs);
            Collections.reverse(reverseTabs);
            for (TabWidget tab : reverseTabs) {
                tab.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
            }

            Level level = this.menu.getBlockEntity().getLevel();
            BlockPos blockPos = this.menu.getBlockEntity().getBlockPos();
            BlockEntity tempEntity = level.getBlockEntity(blockPos.above());
            int yOffset = 0;
            this.font.draw(pPoseStack, new TextComponent("U: ").append(menu.getSideName(Direction.UP)), this.x + 5, this.y + 5 + yOffset, 0xff000000);
            yOffset += 10;
            if (tempEntity != null && tempEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent()) {
                RenderSystem.setShaderTexture(0, ITEMS);
                this.blit(pPoseStack, this.x + 5, this.y + 5 + yOffset, this.getBlitOffset(), 0f, 0f, 16, 16, 16, 16);
                this.font.draw(pPoseStack, new TextComponent("Items"), this.x + 21, this.y + 5 + yOffset, 0xff000000);
            }
            tempEntity = level.getBlockEntity(blockPos.below());
            yOffset += 18;
            this.font.draw(pPoseStack, new TextComponent("D: ").append(menu.getSideName(Direction.DOWN)), this.x + 5, this.y + 5 + yOffset, 0xff000000);
            yOffset += 10;
            if (tempEntity != null && tempEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent()) {
                RenderSystem.setShaderTexture(0, ITEMS);
                this.blit(pPoseStack, this.x + 5, this.y + 5 + yOffset, this.getBlitOffset(), 0f, 0f, 16, 16, 16, 16);
                this.font.draw(pPoseStack, new TextComponent("Items"), this.x + 21, this.y + 5 + yOffset, 0xff000000);
            }
            tempEntity = level.getBlockEntity(blockPos.north());
            yOffset += 18;
            this.font.draw(pPoseStack, new TextComponent("N: ").append(menu.getSideName(Direction.NORTH)), this.x + 5, this.y + 5 + yOffset, 0xff000000);
            yOffset += 10;
            if (tempEntity != null && tempEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent()) {
                RenderSystem.setShaderTexture(0, ITEMS);
                this.blit(pPoseStack, this.x + 5, this.y + 5 + yOffset, this.getBlitOffset(), 0f, 0f, 16, 16, 16, 16);
                this.font.draw(pPoseStack, new TextComponent("Items"), this.x + 21, this.y + 5 + yOffset, 0xff000000);
            }
            tempEntity = level.getBlockEntity(blockPos.south());
            yOffset += 18;
            this.font.draw(pPoseStack, new TextComponent("S: ").append(menu.getSideName(Direction.SOUTH)), this.x + 5, this.y + 5 + yOffset, 0xff000000);
            yOffset += 10;
            if (tempEntity != null && tempEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent()) {
                RenderSystem.setShaderTexture(0, ITEMS);
                this.blit(pPoseStack, this.x + 5, this.y + 5 + yOffset, this.getBlitOffset(), 0f, 0f, 16, 16, 16, 16);
                this.font.draw(pPoseStack, new TextComponent("Items"), this.x + 21, this.y + 5 + yOffset, 0xff000000);
            }
            tempEntity = level.getBlockEntity(blockPos.east());
            yOffset += 18;
            this.font.draw(pPoseStack, new TextComponent("E: ").append(menu.getSideName(Direction.EAST)), this.x + 5, this.y + 5 + yOffset, 0xff000000);
            yOffset += 10;
            if (tempEntity != null && tempEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent()) {
                RenderSystem.setShaderTexture(0, ITEMS);
                this.blit(pPoseStack, this.x + 5, this.y + 5 + yOffset, this.getBlitOffset(), 0f, 0f, 16, 16, 16, 16);
                this.font.draw(pPoseStack, new TextComponent("Items"), this.x + 21, this.y + 5 + yOffset, 0xff000000);
            }
            tempEntity = level.getBlockEntity(blockPos.west());
            yOffset += 18;
            this.font.draw(pPoseStack, new TextComponent("W: ").append(menu.getSideName(Direction.WEST)), this.x + 5, this.y + 5 + yOffset, 0xff000000);
            yOffset += 10;
            if (tempEntity != null && tempEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent()) {
                RenderSystem.setShaderTexture(0, ITEMS);
                this.blit(pPoseStack, this.x + 5, this.y + 5 + yOffset, this.getBlitOffset(), 0f, 0f, 16, 16, 16, 16);
                this.font.draw(pPoseStack, new TextComponent("Items"), this.x + 21, this.y + 5 + yOffset, 0xff000000);
            }
        }
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        for (TabWidget child : this.tabs) {
            if (child.mouseClicked(pMouseX,pMouseY,pButton)) {
                return true;
            }
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }
}
