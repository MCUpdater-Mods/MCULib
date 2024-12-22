package com.mcupdater.mculib.block;

import net.minecraft.core.Direction;

public interface IConfigurableMenu {
    AbstractConfigurableBlockEntity getBlockEntity();

    String getSideName(Direction direction);
}
