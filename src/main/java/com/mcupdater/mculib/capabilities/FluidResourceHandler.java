package com.mcupdater.mculib.capabilities;

import com.mcupdater.mculib.inventory.FluidStackValidator;
import com.mcupdater.mculib.inventory.InputOutputSettings;
import com.mcupdater.mculib.inventory.SideSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FluidResourceHandler extends AbstractResourceHandler {

    private Level level;
    private List<FluidTank> tanks = new ArrayList<>();
    private List<Integer> exposedTanks = new ArrayList<Integer>();
    private List<Integer> inputTanks = new ArrayList<Integer>();
    private List<Integer> outputTanks = new ArrayList<Integer>();
    private FluidStackValidator insertFunction = (tank, fluid) -> true;
    private FluidStackValidator extractFunction = (tank, fluid) -> true;
    private boolean isDirty;
    private Function<Player,Boolean> playerValidator;
    protected Map<Direction, ConfigurableFluidHandler> sideConfigs;
    private ConfigurableFluidHandler internalHandler;

    public FluidResourceHandler(Level pLevel, Function<Player,Boolean> playerValidator) {
        super();
        this.level = pLevel;
        this.playerValidator = playerValidator;
        this.sideConfigs = new HashMap<>();
        initHandlers();
    }

    private void initHandlers() {
        for (Direction side : Direction.values()) {
            sideConfigs.put(side, new ConfigurableFluidHandler(true, true));
        }
        this.internalHandler = new ConfigurableFluidHandler(true, true);
    }

    public int addTank(FluidTank tank, boolean insert, boolean extract) {
        int tankIndex = this.tanks.size();
        this.tanks.add(tank);
        if (insert || extract)
            this.exposedTanks.add(tankIndex);
        if (insert)
            this.inputTanks.add(tankIndex);
        if (extract)
            this.outputTanks.add(tankIndex);
        return tankIndex;
    }

    public void setInsertFunction(FluidStackValidator function) {
        this.insertFunction = function;
    }

    public void setExtractFunction(FluidStackValidator function) {
        this.extractFunction = function;
    }

    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap.equals(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)) {
            if (side != null) {
                return this.getFluidHandler(side).cast();
            } else {
                return LazyOptional.of(this::getInternalHandler).cast();
            }
        }
        return LazyOptional.empty();
    }

    public ConfigurableFluidHandler getInternalHandler() {
        return this.internalHandler;
    }

    private LazyOptional<IFluidHandler> getFluidHandler(Direction side) {
        return LazyOptional.of(() -> this.sideConfigs.get(side));
    }

    @Override
    public boolean tickHandler(Level pLevel, BlockPos pBlockPos) {
        // Do push and pull
        for (Direction side : Direction.values()) {
            InputOutputSettings ioSettings = this.sideIOMap.get(side);
            if (ioSettings != null && ioSettings.getInputSetting().equals(SideSetting.AUTOMATED)) {
                BlockEntity remoteBlock = pLevel.getBlockEntity(pBlockPos.relative(side));
                if (remoteBlock != null && remoteBlock.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, ioSettings.getInputAutomatedSide()).isPresent()) {
                    IFluidHandler remoteHandler = remoteBlock.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, ioSettings.getInputAutomatedSide()).orElse(EmptyFluidHandler.INSTANCE);
                    for (int remoteTank = 0; remoteTank < remoteHandler.getTanks(); remoteTank++) {
                        for (int inputTank : inputTanks) {
                            if (!remoteHandler.getFluidInTank(remoteTank).isEmpty() && this.internalHandler.isFluidValid(inputTank, remoteHandler.getFluidInTank(remoteTank))) {
                                FluidStack fluidStack = remoteHandler.drain(remoteHandler.getFluidInTank(remoteTank).getAmount(), IFluidHandler.FluidAction.SIMULATE);
                                int fillAmount = this.internalHandler.fill(inputTank, fluidStack, IFluidHandler.FluidAction.EXECUTE);
                                fluidStack.setAmount(fillAmount);
                                remoteHandler.drain(fluidStack, IFluidHandler.FluidAction.EXECUTE);
                            }
                        }
                    }
                }
            }
            if (ioSettings != null && ioSettings.getOutputSetting().equals(SideSetting.AUTOMATED)) {
                BlockEntity remoteBlock = pLevel.getBlockEntity(pBlockPos.relative(side));
                if (remoteBlock != null && remoteBlock.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, ioSettings.getInputAutomatedSide()).isPresent()) {
                    IFluidHandler remoteHandler = remoteBlock.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, ioSettings.getOutputAutomatedSide()).orElse(EmptyFluidHandler.INSTANCE);
                    for (int remoteTank = 0; remoteTank < remoteHandler.getTanks(); remoteTank++) {
                        for (int outputTank : outputTanks) {
                            if (!this.internalHandler.getFluidInTank(outputTank).isEmpty() && remoteHandler.isFluidValid(outputTank, this.internalHandler.getFluidInTank(outputTank))) {
                                FluidStack fluidStack = this.internalHandler.drain(outputTank, this.internalHandler.getFluidInTank(outputTank).getAmount(), IFluidHandler.FluidAction.SIMULATE);
                                int fillAmount = remoteHandler.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
                                fluidStack.setAmount(fillAmount);
                                this.internalHandler.drain(outputTank, fluidStack, IFluidHandler.FluidAction.EXECUTE);
                            }
                        }
                    }
                }
            }
        }
        //
        if (this.isDirty) {
            this.isDirty = false;
            return true;
        }
        return false;
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        this.tanks.clear();
        if (compound.contains("tanks", Tag.TAG_LIST)) {
            ListTag listTag = compound.getList("tanks", Tag.TAG_COMPOUND);
            for (int index = 0; index < listTag.size(); index++) {
                CompoundTag tankTag = (CompoundTag) listTag.get(index);
                FluidTank tank = new FluidTank(tankTag.getInt("Capacity")).readFromNBT(tankTag);
                this.tanks.add(tank);
            }
        }
        this.exposedTanks = Arrays.stream(compound.getIntArray("exposed")).boxed().collect(Collectors.toList());
        this.inputTanks = Arrays.stream(compound.getIntArray("input")).boxed().collect(Collectors.toList());
        this.outputTanks = Arrays.stream(compound.getIntArray("output")).boxed().collect(Collectors.toList());
    }

    @Override
    public void save(CompoundTag compound) {
        ListTag listTag = new ListTag();
        for (FluidTank tank : this.tanks) {
            CompoundTag tankTag = new CompoundTag();
            tank.writeToNBT(tankTag);
            tankTag.putInt("Capacity", tank.getCapacity());
            listTag.add(tankTag);
        }
        compound.put("tanks", listTag);
        compound.putIntArray("exposed", this.exposedTanks);
        compound.putIntArray("input", this.inputTanks);
        compound.putIntArray("output", this.outputTanks);
        super.save(compound);
    }

    @Override
    public void updateIOSettings(Direction side, InputOutputSettings settings) {
        super.updateIOSettings(side, settings);
        ConfigurableFluidHandler handler = this.sideConfigs.get(side);
        handler.setExtractAllowed(settings.getOutputSetting().equals(SideSetting.AUTOMATED) || settings.getOutputSetting().equals(SideSetting.PASSIVE));
        handler.setInsertAllowed(settings.getInputSetting().equals(SideSetting.AUTOMATED) || settings.getInputSetting().equals(SideSetting.PASSIVE));
        this.sideConfigs.put(side, handler);
    }

    public void markDirty() {
        isDirty = true;
    }

    public class ConfigurableFluidHandler implements IFluidHandler {
        private boolean insertAllowed;
        private boolean extractAllowed;

        public ConfigurableFluidHandler(boolean insertAllowed, boolean extractAllowed) {
            this.insertAllowed = insertAllowed;
            this.extractAllowed = extractAllowed;
        }

        @Override
        public int getTanks() {
            return FluidResourceHandler.this.tanks.size();
        }

        @NotNull
        @Override
        public FluidStack getFluidInTank(int tank) {
            return FluidResourceHandler.this.tanks.get(tank).getFluid();
        }

        @Override
        public int getTankCapacity(int tank) {
            return FluidResourceHandler.this.tanks.get(tank).getCapacity();
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return FluidResourceHandler.this.insertFunction.isStackValid(tank, stack) || FluidResourceHandler.this.extractFunction.isStackValid(tank, stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (insertAllowed) {
                for (int tankIndex : FluidResourceHandler.this.inputTanks) {
                    if (FluidResourceHandler.this.insertFunction.isStackValid(tankIndex, resource)) {
                        markDirty();
                        return FluidResourceHandler.this.tanks.get(tankIndex).fill(resource, action);
                    }
                }
            }
            return 0;
        }

        public int fill(int tankId, FluidStack resource, FluidAction action) {
            if (insertAllowed) {
                if (FluidResourceHandler.this.insertFunction.isStackValid(tankId, resource)) {
                    markDirty();
                    return FluidResourceHandler.this.tanks.get(tankId).fill(resource, action);
                }
            }
            return 0;
        }

        @NotNull
        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (extractAllowed) {
                for (int tankIndex : FluidResourceHandler.this.outputTanks) {
                    if (FluidResourceHandler.this.extractFunction.isStackValid(tankIndex, resource)) {
                        markDirty();
                        return FluidResourceHandler.this.tanks.get(tankIndex).drain(resource, action);
                    }
                }
            }
            return FluidStack.EMPTY;
        }

        public FluidStack drain(int tankId, FluidStack resource, FluidAction action) {
            if (extractAllowed) {
                if (FluidResourceHandler.this.extractFunction.isStackValid(tankId, resource)) {
                    markDirty();
                    return FluidResourceHandler.this.tanks.get(tankId).drain(resource, action);
                }
            }
            return FluidStack.EMPTY;
        }

        @NotNull
        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            FluidStack outputStack = FluidStack.EMPTY;
            if (extractAllowed) {
                if (FluidResourceHandler.this.tanks.size() == 0) return outputStack;
                int tankIndex = 0;
                while (outputStack.equals(FluidStack.EMPTY) && tankIndex < FluidResourceHandler.this.tanks.size()) {
                    outputStack = FluidResourceHandler.this.tanks.get(tankIndex).drain(maxDrain, action);
                    if (!outputStack.isEmpty()) markDirty();
                    tankIndex++;
                }
            }
            return outputStack;
        }

        public FluidStack drain(int tankId, int maxDrain, FluidAction action) {
            FluidStack outputStack = FluidStack.EMPTY;
            if (extractAllowed) {
                if (FluidResourceHandler.this.tanks.size() == 0) return outputStack;
                    outputStack = FluidResourceHandler.this.tanks.get(tankId).drain(maxDrain, action);
                    if (!outputStack.isEmpty()) markDirty();
            }
            return outputStack;
        }

        public void setInsertAllowed(boolean insertAllowed) {
            this.insertAllowed = insertAllowed;
        }

        public void setExtractAllowed(boolean extractAllowed) {
            this.extractAllowed = extractAllowed;
        }
    }
}
