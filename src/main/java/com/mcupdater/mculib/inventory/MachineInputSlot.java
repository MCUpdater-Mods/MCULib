package com.mcupdater.mculib.inventory;

import com.mcupdater.mculib.block.AbstractMachineBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class MachineInputSlot extends SlotItemHandler {
    private final int index;
    private final AbstractMachineBlockEntity blockEntity;

    public MachineInputSlot(AbstractMachineBlockEntity blockEntity, IItemHandler itemHandler, int parentIndex, int xPosition, int yPosition) {
        super(itemHandler, parentIndex, xPosition, yPosition);
        this.blockEntity = blockEntity;
        this.index = parentIndex;
    }

    @Override
    public boolean mayPickup(Player player) {
        return true;
    }
}
