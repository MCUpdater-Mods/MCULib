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

    protected EnergyStorage energyStorage;
    protected LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> energyStorage);

    public TileEntityPowered(BlockEntityType<?> tileEntity, BlockPos blockPos, BlockState blockState, int capacity, int maxTransfer) {
        super(tileEntity, blockPos, blockState);
        this.energyStorage = new EnergyStorage(capacity, maxTransfer);
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
        if (this.energyStorage.getEnergyStored() > (this.energyStorage.getMaxEnergyStored() / 2)) {
            int splitEnergy = this.energyStorage.getEnergyStored() - (this.energyStorage.getMaxEnergyStored() / 2);
            int validReceivers = 0;
            List<IEnergyStorage> receivers = new ArrayList<>();
            for (Direction side : Direction.values()) {
                BlockEntity tile = this.getLevel().getBlockEntity(worldPosition.relative(side));
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
                this.setChanged();
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
