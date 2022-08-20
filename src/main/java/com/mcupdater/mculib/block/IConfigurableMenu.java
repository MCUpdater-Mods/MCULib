package com.mcupdater.mculib.block;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface IConfigurableMenu {
    public BlockEntity getBlockEntity();

    public Component getSideName(Direction direction);
}
