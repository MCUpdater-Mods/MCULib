package com.mcupdater.mculib.block;

import com.mcupdater.mculib.capabilities.PoweredBlockEntity;
import com.mcupdater.mculib.inventory.InputOutputSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import static com.mcupdater.mculib.setup.Config.OVERDRIVE_ENABLED;

public abstract class AbstractMachineBlockEntity extends PoweredBlockEntity implements WorldlyContainer, Nameable, MenuProvider {
    private final int powerUse;
    protected float storedXP = 0;
    protected int workProgress;
    protected int workTotal;
    protected Component name;
    protected Map<String, Map<Direction, InputOutputSettings>> sideConfigs = new HashMap<>();

    public AbstractMachineBlockEntity(BlockEntityType<?> tileEntity, BlockPos blockPos, BlockState blockState, int capacity, int maxTransfer, ReceiveMode receive, SendMode send, int powerUse) {
        super(tileEntity, blockPos, blockState, capacity, maxTransfer, receive, send);
        this.powerUse = powerUse;
        this.sideConfigs.put("power",InputOutputSettings.getDefaultMap());
    }

    public void tick(Level pLevel, BlockPos pPos, BlockState pBlockState) {
        if (level.isClientSide()) {
            return; // Don't tick on the client
        }

        int cycles = 1;
        if (OVERDRIVE_ENABLED.get()) {
            double fill = (this.energyStorage.getStoredEnergy() * 1.0d) / this.energyStorage.getCapacity();
            if (fill >= 0.8d) {
                cycles = 8;
            } else if (fill >= 0.5d) {
                cycles = 4;
            } else if (fill >= 0.25d) {
                cycles = 2;
            }
        }
        for (int i = 0; i < cycles; i++) {
            if (this.energyStorage.getStoredEnergy() >= this.powerUse) {
                if (this.performWork()) {
                    this.energyStorage.getInternalHandler().extractEnergy(this.powerUse, false);
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
        if (compound.contains("CustomName",8)) {
            this.name = Component.Serializer.fromJson(compound.getString("CustomName"));
        }
        if (compound.contains("SideConfigs")) {
            CompoundTag configs = compound.getCompound("SideConfigs");
            if (configs.contains("power")) {
                CompoundTag power = configs.getCompound("power");
                Map<Direction, InputOutputSettings> powerMap = InputOutputSettings.loadMapFromNBT(power);
                sideConfigs.put("power",powerMap);
            }
            if (configs.contains("items")) {
                CompoundTag items = configs.getCompound("items");
                Map<Direction, InputOutputSettings> powerMap = InputOutputSettings.loadMapFromNBT(items);
                sideConfigs.put("itens",powerMap);
            }
            if (configs.contains("fluids")) {
                CompoundTag power = configs.getCompound("fluids");
                Map<Direction, InputOutputSettings> powerMap = InputOutputSettings.loadMapFromNBT(power);
                sideConfigs.put("fluids",powerMap);
            }
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
        CompoundTag configs = new CompoundTag();
        for (Map.Entry<String,Map<Direction,InputOutputSettings>> entry : sideConfigs.entrySet()) {
            String type = entry.getKey();
            Map<Direction, InputOutputSettings> internalMap = entry.getValue();
            CompoundTag configType = new CompoundTag();
            InputOutputSettings.saveMapToNBT(configType, internalMap);
            configs.put(type,configType);
        }
        compound.put("SideConfigs", configs);
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
