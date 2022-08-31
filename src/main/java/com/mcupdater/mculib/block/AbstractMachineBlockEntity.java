package com.mcupdater.mculib.block;

import com.mcupdater.mculib.capabilities.EnergyResourceHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import static com.mcupdater.mculib.setup.Config.OVERDRIVE_ENABLED;

public abstract class AbstractMachineBlockEntity extends AbstractConfigurableBlockEntity {
    private final int powerUse;
    protected float storedXP = 0;
    protected int workProgress;
    protected int workTotal;
    protected int multiplier;

    public AbstractMachineBlockEntity(BlockEntityType<?> tileEntity, BlockPos blockPos, BlockState blockState, int energyCapacity, int maxTransfer, int powerUse, int multiplier) {
        super(tileEntity, blockPos, blockState);
        this.powerUse = powerUse;
        this.multiplier = multiplier;
        this.configMap.put("power", new EnergyResourceHandler(this.level, energyCapacity, maxTransfer, true));
    }

    public void tick(Level pLevel, BlockPos pPos, BlockState pBlockState) {
        if (level.isClientSide()) {
            return; // Don't tick on the client
        }
        EnergyResourceHandler energyStorage = (EnergyResourceHandler) this.configMap.get("power");
        int cycles = multiplier;
        if (OVERDRIVE_ENABLED.get()) {
            double fill = (energyStorage.getStoredEnergy() * 1.0d) / energyStorage.getCapacity();
            if (fill >= 0.8d) {
                cycles = multiplier * 8;
            } else if (fill >= 0.5d) {
                cycles = multiplier * 4;
            } else if (fill >= 0.25d) {
                cycles = multiplier * 2;
            }
        }
        for (int i = 0; i < cycles; i++) {
            if (energyStorage.getStoredEnergy() >= this.powerUse) {
                if (this.performWork()) {
                    energyStorage.getInternalHandler().extractEnergy(this.powerUse, false);
                    boolean currentState = pBlockState.getValue((AbstractMachineBlock.ACTIVE));
                    if (!currentState) {
                        pBlockState = pBlockState.setValue(AbstractMachineBlock.ACTIVE, true);
                        pLevel.setBlock(pPos, pBlockState, 3);
                    }
                } else {
                    boolean currentState = pBlockState.getValue((AbstractMachineBlock.ACTIVE));
                    if (currentState) {
                        pBlockState = pBlockState.setValue(AbstractMachineBlock.ACTIVE, false);
                        pLevel.setBlock(pPos, pBlockState, 3);
                    }
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

    public void setCurrentRecipe(ResourceLocation recipeId) {
        notifyClients();
    }

}
