package com.mcupdater.mculib.inventory;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class BucketSlot extends SlotItemHandler {
    public BucketSlot(IItemHandler itemHandler, int slot, int posX, int posY) {
        super(itemHandler, slot, posX, posY);
    }

    @Override
    public boolean mayPlace(ItemStack itemStack) {
        return true;
    }
}