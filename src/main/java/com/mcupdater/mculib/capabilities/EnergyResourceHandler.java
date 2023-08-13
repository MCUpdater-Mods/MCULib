package com.mcupdater.mculib.capabilities;

import com.mcupdater.mculib.inventory.InputOutputSettings;
import com.mcupdater.mculib.inventory.SideSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnergyResourceHandler extends AbstractResourceHandler {
    private boolean reservePower;
    private Level level;
    protected int energy;
    protected int capacity;
    protected int maxReceive;
    protected int maxExtract;
    protected Map<Direction, ConfigurableEnergyHandler> sideConfigs;
    private ConfigurableEnergyHandler internalHandler;

    public EnergyResourceHandler(Level pLevel, int capacity, int maxTransfer, boolean reservePower) {
        this(pLevel, capacity, maxTransfer, maxTransfer, reservePower);
        for (Direction side : Direction.values()) {
            InputOutputSettings sideIO = this.sideIOMap.get(side);
            sideIO.setInputSetting(SideSetting.AUTOMATED);
            sideIO.setOutputSetting(SideSetting.AUTOMATED);
            this.updateIOSettings(side, sideIO);
        }
    }

    public EnergyResourceHandler(Level pLevel, int capacity, int maxReceive, int maxExtract, boolean reservePower) {
        super();
        this.level = pLevel;
        this.capacity = capacity;
        this.maxReceive = maxReceive;
        this.maxExtract = maxExtract;
        this.energy = 0;
        this.reservePower = reservePower;
        this.sideConfigs = new HashMap<>();
        initHandlers();
    }

    public void initHandlers() {
        for (Direction side : Direction.values()) {
            sideConfigs.put(side, new ConfigurableEnergyHandler());
        }
        this.internalHandler = new ConfigurableEnergyHandler();
    }

    @Override
    public void updateIOSettings(Direction side, InputOutputSettings settings) {
        super.updateIOSettings(side, settings);
        ConfigurableEnergyHandler handler = this.sideConfigs.get(side);
        handler.setExtract(settings.getOutputSetting().equals(SideSetting.AUTOMATED) || settings.getOutputSetting().equals(SideSetting.PASSIVE));
        handler.setReceive(settings.getInputSetting().equals(SideSetting.AUTOMATED) || settings.getInputSetting().equals(SideSetting.PASSIVE));
        this.sideConfigs.put(side, handler);
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

    public LazyOptional<IEnergyStorage> getEnergyHandler(Direction side) {
        return LazyOptional.of(() -> this.sideConfigs.get(side));
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap.equals(ForgeCapabilities.ENERGY)) {
            if (side != null) {
                return this.getEnergyHandler(side).cast();
            } else {
                return LazyOptional.of(this::getInternalHandler).cast();
            }
        }
        return LazyOptional.empty();
    }

    @Override
    public boolean tickHandler(Level pLevel, BlockPos pBlockPos) {
        if (!pLevel.isClientSide()) {
            int splitEnergy = 0;
            if (this.reservePower) {
                if (this.getStoredEnergy() > this.getCapacity() / 2) {
                    splitEnergy = this.getStoredEnergy() - (this.getCapacity() / 2);
                }
            } else {
                splitEnergy = this.getStoredEnergy();
            }
            int validReceivers = 0;
            List<IEnergyStorage> receivers = new ArrayList<>();
            for (Direction side : Direction.values()) {
                if (this.getIOSettings(side) != null && this.getIOSettings(side).getOutputSetting().equals(SideSetting.AUTOMATED)) {
                    BlockEntity tile = pLevel.getBlockEntity(pBlockPos.relative(side));
                    if (tile != null && tile.getCapability(ForgeCapabilities.ENERGY, this.getIOSettings(side).getOutputAutomatedSide()).isPresent()) {
                        IEnergyStorage externalStorage = tile.getCapability(ForgeCapabilities.ENERGY, this.getIOSettings(side).getOutputAutomatedSide()).orElse(new EnergyStorage(0));
                        if (externalStorage.canReceive() && externalStorage.getEnergyStored() < externalStorage.getMaxEnergyStored()) {
                            validReceivers++;
                            receivers.add(externalStorage);
                        }
                    }
                }
            }
            if (validReceivers > 0) {
                int shared = Math.floorDiv(splitEnergy, validReceivers);
                int removed = 0;
                for (IEnergyStorage receiver : receivers) {
                    removed += this.getInternalHandler().extractEnergy(receiver.receiveEnergy(shared, false), false);
                }
                return removed > 0;
            }
        }
        return false;
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        this.setEnergy(compound.getInt("energy"));
    }

    @Override
    public void save(CompoundTag compound) {
        compound.putInt("energy", this.getStoredEnergy());
        super.save(compound);
    }

    public class ConfigurableEnergyHandler implements IEnergyStorage {
        private boolean enabled;
        private boolean receive;
        private boolean extract;
        private long lastReceiveTick;

        public ConfigurableEnergyHandler() {
            this.receive = EnergyResourceHandler.this.maxReceive > 0;
            this.extract = EnergyResourceHandler.this.maxExtract > 0;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            EnergyResourceHandler storage = EnergyResourceHandler.this;
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
            EnergyResourceHandler storage = EnergyResourceHandler.this;
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
            return EnergyResourceHandler.this.energy;
        }

        @Override
        public int getMaxEnergyStored() {
            return EnergyResourceHandler.this.capacity;
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
