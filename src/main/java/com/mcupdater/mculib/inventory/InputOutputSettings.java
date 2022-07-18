package com.mcupdater.mculib.inventory;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;

public class InputOutputSettings {
    private SideSetting inputSetting;
    private SideSetting outputSetting;
    private Direction inputAutomatedSide;
    private Direction outputAutomatedSide;

    public InputOutputSettings(SideSetting inputSetting, Direction inputAutomatedSide, SideSetting outputSetting, Direction outputAutomatedSide) {
        this.inputSetting = inputSetting;
        this.inputAutomatedSide = inputAutomatedSide;
        this.outputSetting = outputSetting;
        this.outputAutomatedSide = outputAutomatedSide;
    }

    public SideSetting getInputSetting() {
        return inputSetting;
    }

    public void setInputSetting(SideSetting inputSetting) {
        this.inputSetting = inputSetting;
    }

    public SideSetting getOutputSetting() {
        return outputSetting;
    }

    public void setOutputSetting(SideSetting outputSetting) {
        this.outputSetting = outputSetting;
    }

    public Direction getInputAutomatedSide() {
        return inputAutomatedSide;
    }

    public void setInputAutomatedSide(Direction inputAutomatedSide) {
        this.inputAutomatedSide = inputAutomatedSide;
    }

    public Direction getOutputAutomatedSide() {
        return outputAutomatedSide;
    }

    public void setOutputAutomatedSide(Direction outputAutomatedSide) {
        this.outputAutomatedSide = outputAutomatedSide;
    }

    public static InputOutputSettings loadFromNBT(CompoundTag compound) {
        if (compound.contains("SideSetting")) {
            CompoundTag struct = compound.getCompound("SideSetting");
            return new InputOutputSettings(SideSetting.values()[struct.getByte("inputSetting")],Direction.values()[struct.getByte("inputAutoSide")],SideSetting.values()[struct.getByte("outputSetting")],Direction.values()[struct.getByte("outputAutoSide")]);
        } else {
            return null;
        }
    }

    public static Map<Direction, InputOutputSettings> loadMapFromNBT(CompoundTag compound) {
        Map<Direction, InputOutputSettings> output = new HashMap<>(6);
        for (Direction side : Direction.values()) {
            if (compound.contains(side.getName())) {
                CompoundTag sideObject = compound.getCompound(side.getName());
                InputOutputSettings sideSettings = loadFromNBT(sideObject);
                output.put(side, sideSettings);
            }
        }
        return output;
    }

    public static void saveToNBT(CompoundTag compound, InputOutputSettings settings) {
        CompoundTag struct = new CompoundTag();
        struct.putByte("inputSetting", (byte) settings.getInputSetting().ordinal());
        struct.putByte("inputAutoSide", (byte) settings.getInputAutomatedSide().ordinal());
        struct.putByte("outputSetting", (byte) settings.getOutputSetting().ordinal());
        struct.putByte("outputAutoSide", (byte) settings.getOutputAutomatedSide().ordinal());
        compound.put("SideSetting", struct);
    }

    public static void saveMapToNBT(CompoundTag compound, Map<Direction, InputOutputSettings> map) {
        for (Map.Entry<Direction, InputOutputSettings> entry : map.entrySet()) {
            CompoundTag sideSetting = new CompoundTag();
            saveToNBT(sideSetting, entry.getValue());
            compound.put(entry.getKey().getName(), sideSetting);
        }
    }

    public static Map<Direction, InputOutputSettings> getDefaultMap() {
        Map<Direction, InputOutputSettings> output = new HashMap<>(6);
        for (Direction side : Direction.values()) {
            output.put(side,new InputOutputSettings(SideSetting.PASSIVE,side.getOpposite(),SideSetting.PASSIVE,side.getOpposite()));
        }
        return output;
    }
}
