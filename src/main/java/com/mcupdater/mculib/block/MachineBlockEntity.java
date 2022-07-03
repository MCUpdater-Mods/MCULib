package com.mcupdater.mculib.block;

import com.mcupdater.mculib.capabilities.PoweredBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class MachineBlockEntity extends PoweredBlockEntity {
    private final int powerUse;
    protected float storedXP = 0;
    protected int workProgress;
    protected int workTotal;

    public MachineBlockEntity(BlockEntityType<?> tileEntity, BlockPos blockPos, BlockState blockState, int capacity, int maxTransfer, ReceiveMode receive, SendMode send, int powerUse) {
        super(tileEntity, blockPos, blockState, capacity, maxTransfer, receive, send);
        this.powerUse = powerUse;
    }

    @Override
    public void tick() {
        if (this.energyStorage.getEnergyStored() >= this.powerUse) {
            if (this.performWork()) {
                this.energyStorage.extractEnergy(this.powerUse, false);
            }
        }
        super.tick();
    }

    protected abstract boolean performWork();

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        this.workTotal = compound.getInt("workTotal");
        this.workProgress = compound.getInt("workProgress");
        this.storedXP = compound.getFloat("storedXP");
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        compound.putInt("workTotal", this.workTotal);
        compound.putInt("workProgress", this.workProgress);
        compound.putFloat("storedXP", this.storedXP);
        super.saveAdditional(compound);
    }

    public int extractExperience() {
        int exp = Mth.ceil(this.storedXP);
        this.storedXP = 0;
        return exp;
    }
}
