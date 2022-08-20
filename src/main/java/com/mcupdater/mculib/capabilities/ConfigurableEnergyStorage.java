package com.mcupdater.mculib.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.HashMap;
import java.util.Map;

public class ConfigurableEnergyStorage {
    private Level level;
    protected int energy;
    protected int capacity;
    protected int maxReceive;
    protected int maxExtract;
    protected Map<Direction, ConfigurableEnergyHandler> sideConfigs;
    private ConfigurableEnergyHandler internalHandler;

    public ConfigurableEnergyStorage(Level pLevel, int capacity, int maxTransfer) {
        this(pLevel, capacity, maxTransfer, maxTransfer);
    }

    public ConfigurableEnergyStorage(Level pLevel, int capacity, int maxReceive, int maxExtract) {
        this.level = pLevel;
        this.capacity = capacity;
        this.maxReceive = maxReceive;
        this.maxExtract = maxExtract;
        this.energy = 0;
        sideConfigs = new HashMap<>();
        initHandlers();
    }

    public void initHandlers() {
        for (Direction side : Direction.values()) {
            sideConfigs.put(side, new ConfigurableEnergyHandler());
        }
        this.internalHandler = new ConfigurableEnergyHandler();
    }

    public void setEnergy(int energy) {
        this.energy = Math.max(0, Math.min(capacity, energy));
    }

    public int getStoredEnergy() {
        return energy;
    }

    public int getCapacity() {
        return this.capacity;
    }

    public boolean hasLevel() {
        return this.level != null;
    }

    public ConfigurableEnergyHandler getInternalHandler() {
        return internalHandler;
    }

    public int getMaxExtract() {
        return this.maxExtract;
    }

    public int getMaxReceive() {
        return this.maxReceive;
    }

    public LazyOptional<Object> getEnergyHandler(Direction side) {
        return LazyOptional.of(() -> this.sideConfigs.get(side));
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public class ConfigurableEnergyHandler implements IEnergyStorage {
        private boolean enabled;
        private boolean receive;
        private boolean extract;
        private long lastReceiveTick;

        public ConfigurableEnergyHandler() {
            this.receive = ConfigurableEnergyStorage.this.maxReceive > 0;
            this.extract = ConfigurableEnergyStorage.this.maxExtract > 0;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            ConfigurableEnergyStorage storage = ConfigurableEnergyStorage.this;
            if (this.canReceive() && (storage.level == null || storage.level.getGameTime() > lastReceiveTick)) {
                int energyReceived = Math.min(storage.capacity - storage.energy, Math.min(storage.maxReceive, maxReceive));
                if (!simulate) {
                    storage.energy += energyReceived;
                    if (storage.level != null) {
                        lastReceiveTick = storage.level.getGameTime();
                    }
                }
                return energyReceived;
            }
            return 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            ConfigurableEnergyStorage storage = ConfigurableEnergyStorage.this;
            if (canExtract()) {
                int energyExtracted = Math.min(storage.energy, Math.min(storage.maxExtract, maxExtract));
                if (!simulate)
                    energy -= energyExtracted;
                return energyExtracted;
            }
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return ConfigurableEnergyStorage.this.energy;
        }

        @Override
        public int getMaxEnergyStored() {
            return ConfigurableEnergyStorage.this.capacity;
        }

        @Override
        public boolean canExtract() {
            return extract;
        }

        @Override
        public boolean canReceive() {
            return receive;
        }

        public void setEnabled(boolean newState) {
            this.enabled = newState;
        }

        public boolean isEnabled() {
            return this.enabled;
        }

        public void setReceive(boolean newState) {
            this.receive = newState;
        }

        public void setExtract(boolean newState) {
            this.extract = newState;
        }
    }
}
