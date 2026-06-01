package com.mcupdater.mculib.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class PhantomSlot extends Slot {
    private ItemStackValidator validator = (unused, itemStack) -> true;

    public PhantomSlot(Container pContainer, int index, int xPosition, int yPosition) {
        super(pContainer, index, xPosition, yPosition);
    }

    public PhantomSlot(Container pContainer, int index, int xPosition, int yPosition, ItemStackValidator validator) {
        super(pContainer,index, xPosition, yPosition);
        this.validator = validator;
    }

    @Override
    public boolean mayPlace(ItemStack pStack) {
        return validator.isStackValid(0, pStack);
    }

    @Override
    public boolean mayPickup(Player pPlayer) {
        return true;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public ItemStack safeInsert(ItemStack pStack, int pCount) {
        if (!pStack.isEmpty() && mayPlace(pStack)) {
            ItemStack newStack = pStack.copy();
            newStack.setCount(1);
            this.setByPlayer(newStack);
        }
        return pStack;
    }

    @Override
    public Optional<ItemStack> tryRemove(int p_150642_, int p_150643_, Player p_150644_) {
        this.set(ItemStack.EMPTY);
        return Optional.of(ItemStack.EMPTY);
    }

    @Override
    public boolean isFake() {
        return true;
    }
}
