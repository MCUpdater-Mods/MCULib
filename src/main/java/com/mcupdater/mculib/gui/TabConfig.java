package com.mcupdater.mculib.gui;

import com.mcupdater.mculib.MCULib;
import com.mcupdater.mculib.block.AbstractMachineMenu;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class TabConfig extends TabWidget {

    private static final int COLOR_BACKGROUND = 0xff2b8b2b;
    private static final int COLOR_SELECTED = 0xff5bbb5b;
    private static ResourceLocation WRENCH = ResourceLocation.fromNamespaceAndPath(MCULib.MODID, "textures/gui/icon/wrench.png");

    public TabConfig(int x, int y, int width, int height, ClickAction<AbstractContainerScreen<AbstractMachineMenu<?>>> clickAction) {
        super(x, y, width, height, COLOR_BACKGROUND, COLOR_SELECTED, WRENCH, Component.translatable("gui.processenhancement.config"), clickAction);
    }
}
