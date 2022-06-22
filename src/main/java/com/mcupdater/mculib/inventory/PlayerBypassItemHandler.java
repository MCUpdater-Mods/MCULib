package com.mcupdater.mculib.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;

public class PlayerBypassItemHandler implements IItemHandlerModifiable {
    private IItemHandlerModifiable normalHandler;
    private Container sourceInventory;

    public PlayerBypassItemHandler(IItemHandler h, Container sourceInventory) {
        if (!(h instanceof IItemHandlerModifiable)) {
            this.normalHandler = (IItemHandlerModifiable) h;
        } else {
            this.normalHandler = new IItemHandlerModifiable(){
                @Override
                public int getSlots() {
                    return h.getSlots();
                }

                @Nonnull
                @Override
                public ItemStack getStackInSlot(int slot) {
                    return h.getStackInSlot(slot);
                }

                @Nonnull
                @Override
                public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                    return h.insertItem(slot, stack, simulate);
                }

                @Nonnull
                @Override
                public ItemStack extractItem(int slot, int amount, boolean simulate) {
                    return h.extractItem(slot, amount, simulate);
                }

                @Override
                public int getSlotLimit(int slot) {
                    return h.getSlotLimit(slot);
                }

                @Override
                public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                    return h.isItemValid(slot, stack);
                }

                @Override
                public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
                    sourceInventory.setItem(slot, stack);
                }
            };
        }
        this.sourceInventory = sourceInventory;
    }

    @Override
    public int getSlots() {
        return normalHandler.getSlots();
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return normalHandler.getStackInSlot(slot);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        return normalHandler.insertItem(slot, stack, simulate);
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0)
            return ItemStack.EMPTY;

        ItemStack stackInSlot = normalHandler.getStackInSlot(slot);

        if (stackInSlot.isEmpty())
            return ItemStack.EMPTY;

        if (simulate)
        {
            if (stackInSlot.getCount() < amount)
            {
                return stackInSlot.copy();
            }
            else
            {
                ItemStack copy = stackInSlot.copy();
                copy.setCount(amount);
                return copy;
            }
        }
        else
        {
            int m = Math.min(stackInSlot.getCount(), amount);
            ItemStack ret = sourceInventory.removeItem(slot, m);
            sourceInventory.setChanged();
            return ret;
        }    }

    @Override
    public int getSlotLimit(int slot) {
        return normalHandler.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return normalHandler.isItemValid(slot, stack);
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        normalHandler.setStackInSlot(slot, stack);
    }
}
