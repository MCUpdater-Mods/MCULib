package com.mcupdater.mculib.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class TabRedstone extends TabWidget {
	private static final int COLOR_BACKGROUND = 0xff2b0000;
	private static final int COLOR_SELECTED = 0xff5b0000;
	private static ResourceLocation REDSTONE = ResourceLocation.withDefaultNamespace("textures/item/redstone.png");

	public TabRedstone(int x, int y, int width, int height, ClickAction<?> clickAction) {
		super(x, y, width, height, COLOR_BACKGROUND, COLOR_SELECTED, REDSTONE, Component.translatable("gui.mculib.redstone"), clickAction);
	}
}
