package com.mcupdater.mculib.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.energy.EnergyStorage;

public class SerializedEnergyStorage extends EnergyStorage {

    public SerializedEnergyStorage(int capacity, int maxTransfer) {
        this(capacity, maxTransfer, maxTransfer);
    }

    public SerializedEnergyStorage(int capacity, int maxReceive, int maxExtract) {
        super(capacity, maxReceive, maxExtract);
    }

    public void setEnergy(int newEnergy) {
        this.energy = newEnergy;
    }

}
