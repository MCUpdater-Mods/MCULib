package com.mcupdater.mculib.capabilities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class PoweredBlockEntity extends BlockEntity {
    public enum ReceiveMode {ACCEPTS,NOT_SHARED,NO_RECEIVE}
    public enum SendMode {SHARE,SEND_ALL,NO_SEND}

    protected ConfigurableEnergyStorage energyStorage;
    protected ReceiveMode receiveMode;
    protected SendMode sendMode;

    public PoweredBlockEntity(BlockEntityType<?> tileEntity, BlockPos blockPos, BlockState blockState, int capacity, int maxTransfer) {
        this(tileEntity,blockPos,blockState,capacity,maxTransfer,ReceiveMode.ACCEPTS,SendMode.SHARE);
    }

    public PoweredBlockEntity(BlockEntityType<?> tileEntity, BlockPos blockPos, BlockState blockState, int capacity, int maxTransfer, ReceiveMode receive, SendMode send) {
        super(tileEntity, blockPos, blockState);
        this.energyStorage = new ConfigurableEnergyStorage(this.level, capacity, maxTransfer);
        this.receiveMode = receive;
        this.sendMode = send;
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        this.energyStorage.setEnergy(compound.getInt("energy"));
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        compound.putInt("energy", this.energyStorage.getStoredEnergy());
        //return compound;
    }

    public void tick() {
        if (!this.energyStorage.hasLevel() && this.level != null) {
            this.energyStorage.setLevel(this.level);
        }
        if (this.level != null && !this.level.isClientSide) {
            // Distribute energy
            switch (this.sendMode) {
                case SHARE:
                    if (this.energyStorage.getStoredEnergy() > (this.energyStorage.getCapacity() / 2)) {
                        int splitEnergy = this.energyStorage.getStoredEnergy() - (this.energyStorage.getCapacity() / 2);
                        int validReceivers = 0;
                        List<IEnergyStorage> receivers = new ArrayList<>();
                        for (Direction side : Direction.values()) {
                            BlockEntity tile = this.getLevel().getBlockEntity(worldPosition.relative(side));
                            if (tile != null && tile.getCapability(CapabilityEnergy.ENERGY, side.getOpposite()).isPresent()) {
                                IEnergyStorage externalStorage = tile.getCapability(CapabilityEnergy.ENERGY, side.getOpposite()).orElse(new EnergyStorage(0));
                                if (externalStorage.canReceive() && externalStorage.getEnergyStored() < externalStorage.getMaxEnergyStored()) {
                                    if (tile instanceof PoweredBlockEntity entityPowered) {
                                        if (entityPowered.receiveMode.equals(ReceiveMode.ACCEPTS)) {
                                            validReceivers++;
                                            receivers.add(externalStorage);
                                        }
                                    } else {
                                        validReceivers++;
                                        receivers.add(externalStorage);
                                    }
                                }
                            }
                        }
                        if (validReceivers > 0) {
                            int shared = Math.floorDiv(splitEnergy, validReceivers);
                            for (IEnergyStorage receiver : receivers) {
                                int removed = this.energyStorage.getInternalHandler().extractEnergy(receiver.receiveEnergy(shared, false), false);
                            }
                            this.setChanged();
                        }
                    }
                    break;
                case SEND_ALL:
                    if (this.energyStorage.getStoredEnergy() > 0) {
                        int splitEnergy = this.energyStorage.getStoredEnergy();
                        int validReceivers = 0;
                        List<IEnergyStorage> receivers = new ArrayList<>();
                        for (Direction side : Direction.values()) {
                            BlockEntity tile = this.getLevel().getBlockEntity(worldPosition.relative(side));
                            if (tile != null && tile.getCapability(CapabilityEnergy.ENERGY, side.getOpposite()).isPresent()) {
                                IEnergyStorage externalStorage = tile.getCapability(CapabilityEnergy.ENERGY, side.getOpposite()).orElse(new EnergyStorage(0));
                                if (externalStorage.canReceive() && externalStorage.getEnergyStored() < externalStorage.getMaxEnergyStored()) {
                                    if (tile instanceof PoweredBlockEntity entityPowered) {
                                        if (entityPowered.receiveMode.equals(ReceiveMode.ACCEPTS) || entityPowered.receiveMode.equals(ReceiveMode.NOT_SHARED)) {
                                            validReceivers++;
                                            receivers.add(externalStorage);
                                        }
                                    } else {
                                        validReceivers++;
                                        receivers.add(externalStorage);
                                    }
                                }
                            }
                        }
                        if (validReceivers > 0) {
                            int shared = Math.min(this.energyStorage.getMaxExtract(), Math.floorDiv(splitEnergy, validReceivers));
                            for (IEnergyStorage receiver : receivers) {
                                int removed = this.energyStorage.getInternalHandler().extractEnergy(receiver.receiveEnergy(shared, false), false);

                            }
                            this.setChanged();
                        }
                    }
                    break;
                default:
                    break;
            }
            this.setChanged();
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap.equals(CapabilityEnergy.ENERGY)) {
            if (side != null) {
                return energyStorage.getEnergyHandler(side).cast();
            } else {
                return LazyOptional.of(() -> energyStorage.getInternalHandler()).cast();
            }
        }
        return super.getCapability(cap, side);
    }

    public IEnergyStorage getEnergyHandler() {
        return this.energyStorage.getInternalHandler();
    }
}
