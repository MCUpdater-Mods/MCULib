package com.mcupdater.mculib.capabilities;

import com.mcupdater.mculib.MCULib;
import com.mcupdater.mculib.inventory.InputOutputSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractResourceHandler {
    public AbstractResourceHandler() {
        this.sideIOMap = InputOutputSettings.getDefaultMap();
    }
    protected Map<Direction, InputOutputSettings> sideIOMap = new HashMap<>();

    public InputOutputSettings getIOSettings(Direction side) {
        return sideIOMap.get(side);
    }

    public void updateIOSettings(Direction side, InputOutputSettings settings) {
        sideIOMap.put(side, settings);
    }

    public abstract <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side);

    public abstract boolean tickHandler(Level pLevel, BlockPos pBlockPos); // return true if contents have changed

    public void load(CompoundTag compound) {
        this.sideIOMap = InputOutputSettings.loadMapFromNBT(compound);
    }

    public void save(CompoundTag compound) {
        InputOutputSettings.saveMapToNBT(compound, this.sideIOMap);
    }
}
