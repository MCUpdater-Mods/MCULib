package com.mcupdater.mculib.capabilities;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class TileEntityPowered extends TileEntity implements ITickableTileEntity {

    protected SerializedEnergyStorage energyStorage;
    protected LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> energyStorage);

    public TileEntityPowered(TileEntityType<?> tileEntity, int capacity, int maxTransfer) {
        super(tileEntity);
        this.energyStorage = new SerializedEnergyStorage(capacity, maxTransfer);
    }

    @Override
    public void read(BlockState blockState, CompoundNBT compound) {
        super.read(blockState, compound);
        this.energyStorage.receiveEnergy(compound.getInt("energy"), false);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        compound.putInt("energy", this.energyStorage.getEnergyStored());
        return compound;
    }

    @Override
    public void tick() {
        // Distribute energy
        if (this.energyStorage.getEnergyStored() > (this.energyStorage.getMaxEnergyStored() / 2)) {
            int splitEnergy = this.energyStorage.getEnergyStored() - (this.energyStorage.getMaxEnergyStored() / 2);
            int validReceivers = 0;
            List<IEnergyStorage> receivers = new ArrayList<>();
            for (Direction side : Direction.values()) {
                TileEntity tile = this.getWorld().getTileEntity(pos.offset(side));
                if (tile != null && tile.getCapability(CapabilityEnergy.ENERGY, side.getOpposite()).isPresent()) {
                    IEnergyStorage externalStorage = tile.getCapability(CapabilityEnergy.ENERGY, side.getOpposite()).orElse(new EnergyStorage(0));
                    if (externalStorage.canReceive() && externalStorage.getEnergyStored() < externalStorage.getMaxEnergyStored()) {
                        validReceivers++;
                        receivers.add(externalStorage);
                    }
                }
            }
            if (validReceivers > 0) {
                int shared = Math.floorDiv(splitEnergy, validReceivers);
                for (IEnergyStorage receiver : receivers) {
                    int removed = this.energyStorage.extractEnergy(receiver.receiveEnergy(shared, false), false);
                }
                this.markDirty();
            }
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
