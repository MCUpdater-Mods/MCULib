package com.mcupdater.mculib.block;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

public interface IConfigurableMenu {
    AbstractConfigurableBlockEntity getBlockEntity();

    Component getSideName(Direction direction);
}
