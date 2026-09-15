package com.mcupdater.mculib.gui;

import com.mcupdater.mculib.MCULib;
import com.mcupdater.mculib.block.AbstractConfigurableBlockEntity;
import com.mcupdater.mculib.block.IConfigurableMenu;
import com.mcupdater.mculib.inventory.InputOutputSettings;
import com.mcupdater.mculib.inventory.SideSetting;
import com.mcupdater.mculib.network.SideConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RedstonePanel extends AbstractParentWidget {

    private static final int COLOR_BACKGROUND = 0xffc6c6c6;
    private final Font font;
    private final IConfigurableMenu menu;

    // Icon definitions
    protected final ResourceLocation CLOSED = ResourceLocation.fromNamespaceAndPath(MCULib.MODID, "textures/gui/icon/prohibition.png");
    protected final ResourceLocation ALLOWED = ResourceLocation.fromNamespaceAndPath(MCULib.MODID, "textures/gui/icon/arrow.png");
    protected final ResourceLocation AUTOMATED = ResourceLocation.fromNamespaceAndPath(MCULib.MODID, "textures/gui/icon/gear-arrow.png");
    protected final ResourceLocation ITEMS = ResourceLocation.fromNamespaceAndPath(MCULib.MODID, "textures/gui/icon/box.png");
    protected final ResourceLocation ENERGY = ResourceLocation.fromNamespaceAndPath(MCULib.MODID, "textures/gui/icon/lightning.png");
    protected final ResourceLocation FLUIDS = ResourceLocation.fromNamespaceAndPath(MCULib.MODID, "textures/gui/icon/flask.png");

    public RedstonePanel(IConfigurableMenu srcMenu, int leftPos, int topPos, int width, int height) {
        super(leftPos, topPos, width, height, CommonComponents.EMPTY, COLOR_BACKGROUND);
        MCULib.LOGGER.trace("leftPos: %d, topPos: %d, width: %d, height: %d",leftPos,topPos,width,height);
        this.font = Minecraft.getInstance().font;
        this.menu = srcMenu;
        AbstractConfigurableBlockEntity self = this.menu.getBlockEntity();
    }

    @Override
    public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        MCULib.LOGGER.trace("RedstonePanel - x: %d, y: %d, width: %d, height: %d",this.getX(), this.getY(), this.width, this.height);

        if (this.isVisible()) {
            AbstractConfigurableBlockEntity blockEntity = this.menu.getBlockEntity();
            this.renderTooltips(pGuiGraphics, pMouseX, pMouseY);
        }
    }

    private void renderTooltips(GuiGraphics pPoseStack, int pMouseX, int pMouseY) {
    }

    private boolean testForCapability(BlockCapability capability, Level level, BlockPos blockPos) {
        return level.getCapability(capability, blockPos) != null;
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

}
