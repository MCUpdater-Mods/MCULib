package com.mcupdater.mculib.inventory;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class PlayerPrioritySlotItemHandler extends SlotItemHandler {
    private final int index;

    public PlayerPrioritySlotItemHandler(IItemHandler itemHandler, int parentIndex, int xPosition, int yPosition) {
        super(itemHandler, parentIndex, xPosition, yPosition);
        this.index = parentIndex;
    }

    @Override
    public boolean mayPickup(Player player) {
        boolean result = !this.getItemHandler().getStackInSlot(this.index).isEmpty();
        return result;
    }
}
