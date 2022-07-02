package com.mcupdater.mculib.inventory;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class BucketSlot extends SlotItemHandler {
    public BucketSlot(IItemHandler itemHandler, int slot, int posX, int posY) {
        super(itemHandler, slot, posX, posY);
    }

    @Override
    public boolean mayPlace(ItemStack itemStack) {
        return true;
    }
}