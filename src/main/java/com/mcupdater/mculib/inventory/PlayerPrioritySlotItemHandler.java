package com.mcupdater.mculib.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class PlayerPrioritySlotItemHandler extends SlotItemHandler {
    private final int index;

    public PlayerPrioritySlotItemHandler(IItemHandler itemHandler, int parentIndex, int xPosition, int yPosition) {
        super(itemHandler, parentIndex, xPosition, yPosition);
        this.index = parentIndex;
    }

    @Override
    public boolean canTakeStack(PlayerEntity playerEntity) {
        boolean result = !this.getItemHandler().getStackInSlot(this.index).isEmpty();
        return result;
    }
}
