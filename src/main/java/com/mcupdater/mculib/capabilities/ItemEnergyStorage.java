package com.mcupdater.mculib.capabilities;

import com.mcupdater.mculib.setup.MCULibRegistration;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.energy.EnergyStorage;

public class ItemEnergyStorage extends EnergyStorage {
    protected final ItemStack stack;

    public ItemEnergyStorage(ItemStack itemStack, int capacity, int maxReceive, int maxExtract) {
        super(capacity, maxReceive, maxExtract);
        this.stack = itemStack;
        this.stack.set(MCULibRegistration.MAX_ENERGY, capacity);
        this.stack.set(MCULibRegistration.MAX_RECEIVE, maxReceive);
        this.stack.set(MCULibRegistration.MAX_EXTRACT, maxExtract);
        this.energy = itemStack.getOrDefault(MCULibRegistration.STORED_ENERGY,0);
    }

    public void setStoredEnergy(int amount) {
        this.energy = Math.min(amount, this.capacity);
        stack.set(MCULibRegistration.STORED_ENERGY, this.energy);
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int energyReceived = super.receiveEnergy(maxReceive, simulate);
        if (!simulate) {
            stack.set(MCULibRegistration.STORED_ENERGY, this.energy);
        }
        return energyReceived;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int energyExtracted = super.extractEnergy(maxExtract, simulate);
        if (!simulate) {
            stack.set(MCULibRegistration.STORED_ENERGY, this.energy);
        }
        return energyExtracted;
    }
}
