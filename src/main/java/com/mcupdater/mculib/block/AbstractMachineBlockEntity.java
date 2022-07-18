package com.mcupdater.mculib.block;

import com.mcupdater.mculib.capabilities.PoweredBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import static com.mcupdater.mculib.setup.Config.OVERDRIVE_ENABLED;

public abstract class AbstractMachineBlockEntity extends PoweredBlockEntity implements WorldlyContainer, Nameable, MenuProvider {
    private final int powerUse;
    protected float storedXP = 0;
    protected int workProgress;
    protected int workTotal;
    protected Component name;

    public AbstractMachineBlockEntity(BlockEntityType<?> tileEntity, BlockPos blockPos, BlockState blockState, int capacity, int maxTransfer, ReceiveMode receive, SendMode send, int powerUse) {
        super(tileEntity, blockPos, blockState, capacity, maxTransfer, receive, send);
        this.powerUse = powerUse;
    }

    @Override
    public void tick() {
        int cycles = 1;
        if (OVERDRIVE_ENABLED.get()) {
            int fillPct = ((int) ((double) this.energyStorage.getEnergyStored() / (double) this.energyStorage.getMaxEnergyStored()) * 100);
            if (fillPct >= 80) {
                cycles = 8;
            } else if (fillPct >= 50) {
                cycles = 4;
            } else if (fillPct >= 25) {
                cycles = 2;
            }
        }
        for (int i = 0; i < cycles; i++) {
            if (this.energyStorage.getEnergyStored() >= this.powerUse) {
                if (this.performWork()) {
                    this.energyStorage.extractEnergy(this.powerUse, false);
                } else {
                    super.tick();
                    return;
                }
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
        if (compound.contains("CustomName",8)) {
            this.name = Component.Serializer.fromJson(compound.getString("CustomName"));
        }
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        compound.putInt("workTotal", this.workTotal);
        compound.putInt("workProgress", this.workProgress);
        compound.putFloat("storedXP", this.storedXP);
        if (this.name != null) {
            compound.putString("CustomName", Component.Serializer.toJson(this.name));
        }
        super.saveAdditional(compound);
    }

    public int extractExperience() {
        int exp = Mth.ceil(this.storedXP);
        this.storedXP = 0;
        return exp;
    }

    @Override
    public Component getName() {
        return this.hasCustomName() ? this.getCustomName() : this.getDefaultName();
    }

    @Override
    public Component getDisplayName() {
        return this.getName();
    }

    protected abstract Component getDefaultName();

    public void setCustomName(Component hoverName) {
        this.name = hoverName;
    }

    @Nullable
    @Override
    public Component getCustomName() {
        return this.name;
    }
}
