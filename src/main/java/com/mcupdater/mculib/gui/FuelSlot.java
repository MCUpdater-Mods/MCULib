package com.mcupdater.mculib.gui;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class FuelSlot extends SlotItemHandler {

    public FuelSlot(IItemHandler itemHandler, int slot, int xPos, int yPos) {
        super(itemHandler, slot, xPos, yPos);
    }

    @Override
    public boolean mayPlace(ItemStack itemStack) {
        return (ForgeHooks.getBurnTime(itemStack, RecipeType.SMELTING) > 0);
    }

    @Override
    public boolean mayPickup(Player player) {
        return !this.getItem().isEmpty();
    }
}