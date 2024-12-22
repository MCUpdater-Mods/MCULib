package com.mcupdater.mculib.capabilities;

import com.mcupdater.mculib.inventory.FluidStackValidator;
import com.mcupdater.mculib.inventory.InputOutputSettings;
import com.mcupdater.mculib.inventory.SideSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FluidResourceHandler extends AbstractResourceHandler {

    private Level level;
    private List<FluidTank> tanks = new ArrayList<>();
    private List<Integer> exposedTanks = new ArrayList<Integer>();
    private List<Integer> inputTanks = new ArrayList<Integer>();
    private List<Integer> outputTanks = new ArrayList<Integer>();
    private FluidStackValidator insertFunction = (tank, fluid) -> inputTanks.contains(tank);
    private FluidStackValidator extractFunction = (tank, fluid) -> outputTanks.contains(tank);
    private boolean isDirty;
    private Function<Player,Boolean> playerValidator;
    protected Map<Direction, ConfigurableFluidHandler> sideConfigs;
    private ConfigurableFluidHandler internalHandler;
    private Map<Direction, BlockCapabilityCache<IFluidHandler, Direction>> inboundCache;
    private Map<Direction, BlockCapabilityCache<IFluidHandler, Direction>> outboundCache;

    public FluidResourceHandler(Level pLevel, Function<Player,Boolean> playerValidator) {
        super();
        this.level = pLevel;
        this.playerValidator = playerValidator;
        this.sideConfigs = new HashMap<>();
        this.inboundCache = new HashMap<>();
        this.outboundCache = new HashMap<>();
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

    public ConfigurableFluidHandler getInternalHandler() {
        return this.internalHandler;
    }

    public IFluidHandler getFluidHandler(Direction side) {
        return this.sideConfigs.get(side);
    }

    @Override
    public boolean tickHandler(Level pLevel, BlockPos pBlockPos) {
        // Do push and pull
        if (!pLevel.isClientSide()) {
            List<Direction> directions = getSortedDirections(this.sideIOMap);
            for (Direction side : directions) {
                InputOutputSettings ioSettings = this.sideIOMap.get(side);
                if (ioSettings != null && ioSettings.getInputSetting().equals(SideSetting.AUTOMATED)) {
                    IFluidHandler externalHandler = inboundCache.computeIfAbsent(side, k -> this.lookupExternalHandler((ServerLevel) pLevel, pBlockPos.relative(side), this.getIOSettings(side).getInputAutomatedSide())).getCapability();
                    if (externalHandler != null) {
                        for (int remoteTank = 0; remoteTank < externalHandler.getTanks(); remoteTank++) {
                            for (int inputTank : inputTanks) {
                                if (!externalHandler.getFluidInTank(remoteTank).isEmpty() && this.internalHandler.isFluidValid(inputTank, externalHandler.getFluidInTank(remoteTank))) {
                                    FluidStack fluidStack = externalHandler.drain(externalHandler.getFluidInTank(remoteTank).getAmount(), IFluidHandler.FluidAction.SIMULATE);
                                    if (!fluidStack.isEmpty()) {
                                        int fillAmount = this.internalHandler.fill(inputTank, fluidStack, IFluidHandler.FluidAction.EXECUTE);
                                        fluidStack.setAmount(fillAmount);
                                        externalHandler.drain(fluidStack, IFluidHandler.FluidAction.EXECUTE);
                                    }
                                }
                            }
                        }
                    }
                }
                if (ioSettings != null && ioSettings.getOutputSetting().equals(SideSetting.AUTOMATED)) {
                    IFluidHandler externalHandler = outboundCache.computeIfAbsent(side, k -> this.lookupExternalHandler((ServerLevel) pLevel, pBlockPos.relative(side), this.getIOSettings(side).getOutputAutomatedSide())).getCapability();
                    if (externalHandler != null) {
                        for (int remoteTank = 0; remoteTank < externalHandler.getTanks(); remoteTank++) {
                            for (int outputTank : outputTanks) {
                                if (!this.internalHandler.getFluidInTank(outputTank).isEmpty() && externalHandler.isFluidValid(outputTank, this.internalHandler.getFluidInTank(outputTank))) {
                                    FluidStack fluidStack = this.internalHandler.drain(outputTank, this.internalHandler.getFluidInTank(outputTank).getAmount(), IFluidHandler.FluidAction.SIMULATE);
                                    if (!fluidStack.isEmpty()) {
                                        int fillAmount = externalHandler.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
                                        fluidStack.setAmount(fillAmount);
                                        this.internalHandler.drain(outputTank, fluidStack, IFluidHandler.FluidAction.EXECUTE);
                                    }
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
        }
        return false;
    }

    private BlockCapabilityCache<IFluidHandler, Direction> lookupExternalHandler(ServerLevel level, BlockPos blockPos, Direction direction) {
        return BlockCapabilityCache.create(
                Capabilities.FluidHandler.BLOCK,
                level,
                blockPos,
                direction
        );
    }

    @Override
    public void loadAdditional(CompoundTag compound, HolderLookup.Provider pRegistries) {
        super.loadAdditional(compound, pRegistries);
        this.tanks.clear();
        if (compound.contains("tanks", Tag.TAG_LIST)) {
            ListTag listTag = compound.getList("tanks", Tag.TAG_COMPOUND);
            for (int index = 0; index < listTag.size(); index++) {
                CompoundTag tankTag = (CompoundTag) listTag.get(index);
                FluidTank tank = new FluidTank(tankTag.getInt("Capacity")).readFromNBT(pRegistries, tankTag);
                this.tanks.add(tank);
            }
        }
        this.exposedTanks = Arrays.stream(compound.getIntArray("exposed")).boxed().collect(Collectors.toList());
        this.inputTanks = Arrays.stream(compound.getIntArray("input")).boxed().collect(Collectors.toList());
        this.outputTanks = Arrays.stream(compound.getIntArray("output")).boxed().collect(Collectors.toList());
    }

    @Override
    public void saveAdditional(CompoundTag compound, HolderLookup.Provider pRegistries) {
        ListTag listTag = new ListTag();
        for (FluidTank tank : this.tanks) {
            CompoundTag tankTag = new CompoundTag();
            tank.writeToNBT(pRegistries, tankTag);
            tankTag.putInt("Capacity", tank.getCapacity());
            listTag.add(tankTag);
        }
        compound.put("tanks", listTag);
        compound.putIntArray("exposed", this.exposedTanks);
        compound.putIntArray("input", this.inputTanks);
        compound.putIntArray("output", this.outputTanks);
        super.saveAdditional(compound, pRegistries);
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
            return FluidResourceHandler.this.insertFunction.isStackValid(tank, stack) && FluidResourceHandler.this.tanks.get(tank).isFluidValid(stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (insertAllowed) {
                for (int tankIndex : FluidResourceHandler.this.inputTanks) {
                    if (FluidResourceHandler.this.insertFunction.isStackValid(tankIndex, resource)) {
                        int fillAmount = FluidResourceHandler.this.tanks.get(tankIndex).fill(resource, action);
                        markDirty();
                        return fillAmount;
                    }
                }
            }
            return 0;
        }

        public int fill(int tankId, FluidStack resource, FluidAction action) {
            if (insertAllowed) {
                if (FluidResourceHandler.this.insertFunction.isStackValid(tankId, resource)) {
                    int fillAmount = FluidResourceHandler.this.tanks.get(tankId).fill(resource, action);
                    markDirty();
                    return fillAmount;
                }
            }
            return 0;
        }

        public int forceFill(int tankId, FluidStack resource, FluidAction action) {
            int fillAmount = FluidResourceHandler.this.tanks.get(tankId).fill(resource, action);
            markDirty();
            return fillAmount;
        }

        @NotNull
        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (extractAllowed) {
                for (int tankIndex : FluidResourceHandler.this.outputTanks) {
                    if (FluidResourceHandler.this.extractFunction.isStackValid(tankIndex, resource)) {
                        FluidStack extracted = FluidResourceHandler.this.tanks.get(tankIndex).drain(resource, action);
                        markDirty();
                        return extracted;
                    }
                }
            }
            return FluidStack.EMPTY;
        }

        public FluidStack drain(int tankId, FluidStack resource, FluidAction action) {
            if (extractAllowed) {
                if (FluidResourceHandler.this.extractFunction.isStackValid(tankId, resource)) {
                    FluidStack extracted = FluidResourceHandler.this.tanks.get(tankId).drain(resource, action);
                    markDirty();
                    return extracted;
                }
            }
            return FluidStack.EMPTY;
        }

        @NotNull
        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            FluidStack outputStack = FluidStack.EMPTY;
            if (extractAllowed) {
                if (FluidResourceHandler.this.tanks.isEmpty()) return outputStack;
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
                if (FluidResourceHandler.this.tanks.isEmpty()) return outputStack;
                    outputStack = FluidResourceHandler.this.tanks.get(tankId).drain(maxDrain, action);
                    if (!outputStack.isEmpty()) markDirty();
            }
            return outputStack;
        }

        public FluidStack forceDrain(int tankId, int maxDrain, FluidAction action) {
            FluidStack outputStack = FluidStack.EMPTY;
            if (FluidResourceHandler.this.tanks.isEmpty()) return outputStack;
            outputStack = FluidResourceHandler.this.tanks.get(tankId).drain(maxDrain, action);
            if (!outputStack.isEmpty()) markDirty();
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
