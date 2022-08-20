package com.mcupdater.mculib.capabilities;

import net.minecraft.world.level.Level;
import net.minecraftforge.energy.EnergyStorage;

import javax.annotation.Nonnull;

public class SerializedEnergyStorage extends EnergyStorage {
    private Level level;
    protected long lastReceiveTick;

    public SerializedEnergyStorage(@Nonnull Level level, int capacity, int maxTransfer) {
        this(capacity, maxTransfer, maxTransfer);
        this.level = level;
    }

    public SerializedEnergyStorage(int capacity, int maxReceive, int maxExtract) {
        super(capacity, maxReceive, maxExtract);
    }

    public void setEnergy(int newEnergy) {
        this.energy = newEnergy;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (level != null && !simulate && level.getGameTime() > lastReceiveTick) {
            lastReceiveTick = level.getGameTime();
        }
        return super.receiveEnergy(maxReceive, simulate);
    }

    public int getMaxOutput() {
        return this.maxExtract;
    }

    public int getMaxInput() {
        return this.maxReceive;
    }

    public boolean hasLevel() {
        return this.level != null;
    }

    public void setLevel(Level level) {
        this.level = level;
    }
}
