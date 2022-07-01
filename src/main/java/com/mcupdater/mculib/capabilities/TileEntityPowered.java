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

public abstract class TileEntityPowered extends BlockEntity {
    public enum ReceiveMode {ACCEPTS,NOT_SHARED,NO_RECEIVE}
    public enum SendMode {SHARE,SEND_ALL,NO_SEND}

    protected SerializedEnergyStorage energyStorage;
    protected LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> energyStorage);

    protected ReceiveMode receiveMode;
    protected SendMode sendMode;

    public TileEntityPowered(BlockEntityType<?> tileEntity, BlockPos blockPos, BlockState blockState, int capacity, int maxTransfer) {
        this(tileEntity,blockPos,blockState,capacity,maxTransfer,ReceiveMode.ACCEPTS,SendMode.SHARE);
    }

    public TileEntityPowered(BlockEntityType<?> tileEntity, BlockPos blockPos, BlockState blockState, int capacity, int maxTransfer, ReceiveMode receive, SendMode send) {
        super(tileEntity, blockPos, blockState);
        this.energyStorage = new SerializedEnergyStorage(capacity, maxTransfer);
        this.receiveMode = receive;
        this.sendMode = send;
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        this.energyStorage.receiveEnergy(compound.getInt("energy"), false);
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        compound.putInt("energy", this.energyStorage.getEnergyStored());
        //return compound;
    }

    public void tick() {
        // Distribute energy
        switch (this.sendMode) {
            case SHARE:
                if (this.energyStorage.getEnergyStored() > (this.energyStorage.getMaxEnergyStored() / 2)) {
                    int splitEnergy = this.energyStorage.getEnergyStored() - (this.energyStorage.getMaxEnergyStored() / 2);
                    int validReceivers = 0;
                    List<IEnergyStorage> receivers = new ArrayList<>();
                    for (Direction side : Direction.values()) {
                        BlockEntity tile = this.getLevel().getBlockEntity(worldPosition.relative(side));
                        if (tile != null && tile.getCapability(CapabilityEnergy.ENERGY, side.getOpposite()).isPresent()) {
                            IEnergyStorage externalStorage = tile.getCapability(CapabilityEnergy.ENERGY, side.getOpposite()).orElse(new EnergyStorage(0));
                            if (externalStorage.canReceive() && externalStorage.getEnergyStored() < externalStorage.getMaxEnergyStored()) {
                                if (tile instanceof TileEntityPowered entityPowered) {
                                    if (entityPowered.receiveMode.equals(ReceiveMode.ACCEPTS)){
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
                            int removed = this.energyStorage.extractEnergy(receiver.receiveEnergy(shared, false), false);
                        }
                        this.setChanged();
                    }
                }
                break;
            case SEND_ALL:
                if (this.energyStorage.getEnergyStored() > 0) {
                    int splitEnergy = this.energyStorage.getEnergyStored();
                    int validReceivers = 0;
                    List<IEnergyStorage> receivers = new ArrayList<>();
                    for (Direction side : Direction.values()) {
                        BlockEntity tile = this.getLevel().getBlockEntity(worldPosition.relative(side));
                        if (tile != null && tile.getCapability(CapabilityEnergy.ENERGY, side.getOpposite()).isPresent()) {
                            IEnergyStorage externalStorage = tile.getCapability(CapabilityEnergy.ENERGY, side.getOpposite()).orElse(new EnergyStorage(0));
                            if (externalStorage.canReceive() && externalStorage.getEnergyStored() < externalStorage.getMaxEnergyStored()) {
                                if (tile instanceof TileEntityPowered entityPowered) {
                                    if (entityPowered.receiveMode.equals(ReceiveMode.ACCEPTS) || entityPowered.receiveMode.equals(ReceiveMode.NOT_SHARED)){
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
                            int removed = this.energyStorage.extractEnergy(receiver.receiveEnergy(shared, false), false);
                        }
                        this.setChanged();
                    }
                }
                break;
            default:
                break;
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap.equals(CapabilityEnergy.ENERGY)) {
            return energyHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    public IEnergyStorage getEnergyHandler() {
        return this.energyStorage;
    }
}
