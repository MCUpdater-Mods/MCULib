package com.mcupdater.mculib.inventory;

import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface ItemStackValidator {
    boolean isStackValid(int slot, ItemStack itemStack);
}
