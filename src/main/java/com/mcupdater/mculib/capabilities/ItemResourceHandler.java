package com.mcupdater.mculib.capabilities;

import com.mcupdater.mculib.inventory.InputOutputSettings;
import com.mcupdater.mculib.inventory.ItemStackValidator;
import com.mcupdater.mculib.inventory.SideSetting;
import com.mcupdater.mculib.setup.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class ItemResourceHandler extends AbstractResourceHandler implements WorldlyContainer {
    private int maxStackSize = 64;
    private Level level;
    private NonNullList<ItemStack> itemStorage;
    private int[] exposedSlots;
    private int[] inputSlots;
    private int[] outputSlots;
    private ItemStackValidator insertFunction = (slot, stack) -> true;
    private ItemStackValidator extractFunction = (slot, stack) -> true;
    private boolean isDirty;
    private Function<Player,Boolean> playerValidator;
    private Direction currentSide;
    private int currentSlot;

    protected Map<Direction, ConfigurableItemHandler> sideConfigs;
    private ConfigurableItemHandler internalHandler;
    private Map<Direction, BlockCapabilityCache<IItemHandler, Direction>> inboundCache;
    private Map<Direction, BlockCapabilityCache<IItemHandler, Direction>> outboundCache;

    public ItemResourceHandler(Level pLevel, int size, int[] exposedSlots, int[] inputSlots, int[] outputSlots, Function<Player,Boolean> playerValidator) {
        super();
        this.level = pLevel;
        this.itemStorage = NonNullList.withSize(size, ItemStack.EMPTY);
        this.exposedSlots = exposedSlots;
        this.inputSlots = inputSlots;
        this.outputSlots = outputSlots;
        this.playerValidator = playerValidator;
        this.sideConfigs = new HashMap<>();
        this.inboundCache = new HashMap<>();
        this.outboundCache = new HashMap<>();
        initHandlers();
    }

    public ItemResourceHandler(Level pLevel, int size, int[] exposedSlots, int[] inputSlots, int[] outputSlots, Function<Player,Boolean> playerValidator, int maxStack) {
        this(pLevel, size, exposedSlots, inputSlots, outputSlots, playerValidator);
        this.maxStackSize = maxStack;
    }

    @Override
    public int getMaxStackSize() {
        return maxStackSize;
    }

    public void initHandlers() {
        for (Direction side : Direction.values()) {
            sideConfigs.put(side, new ConfigurableItemHandler(true,true, false));
        }
        this.internalHandler = new ConfigurableItemHandler(true, true, true);
    }

    public String debugHandlers() {
        List<String> entries = new ArrayList<>();
        for (Map.Entry<Direction, ConfigurableItemHandler> entry : sideConfigs.entrySet()) {
            entries.add(String.format("{%s: %s}", entry.getKey(), entry.getValue()));
        }
        return String.join(",", entries);
    }

    public void setInsertFunction(ItemStackValidator function) {
        this.insertFunction = function;
    }

    public void setExtractFunction(ItemStackValidator function) {
        this.extractFunction = function;
    }

    public IItemHandlerModifiable getItemHandler(Direction side) {
        return this.sideConfigs.get(side);
    }

    @Override
    public boolean tickHandler(Level pLevel, BlockPos pBlockPos) {
        int slotTickLimit = 0;
        List<Direction> directions = getSortedDirections(this.sideIOMap);
        if (currentSide == null) currentSide = directions.get(0);
        while (!directions.get(0).equals(currentSide)) Collections.rotate(directions,1);
        for (Direction side : directions) {
            currentSide = side;
            InputOutputSettings ioSettings = this.sideIOMap.get(side);
            if (slotTickLimit <= Config.SLOTS_PER_TICK.get()) {
                if (ioSettings != null && ioSettings.getInputSetting().equals(SideSetting.AUTOMATED)) {
                    IItemHandler externalHandler = inboundCache.computeIfAbsent(side, k -> this.lookupExternalHandler((ServerLevel) pLevel, pBlockPos.relative(side), this.getIOSettings(side).getInputAutomatedSide())).getCapability();
                    if (externalHandler != null) {
                        for (int remoteSlot = currentSlot; remoteSlot < externalHandler.getSlots(); remoteSlot++) {
                            for (int inputSlot : inputSlots) {
                                if (!externalHandler.getStackInSlot(remoteSlot).isEmpty() && this.canPlaceItem(inputSlot, externalHandler.getStackInSlot(remoteSlot))) {
                                    ItemStack stack = externalHandler.extractItem(remoteSlot, externalHandler.getStackInSlot(remoteSlot).getCount(), true);
                                    ItemStack remainder = this.internalHandler.insertItem(inputSlot, stack, false);
                                    int extractCount = stack.getCount() - remainder.getCount();
                                    externalHandler.extractItem(remoteSlot, extractCount, false);
                                }
                            }
                            currentSlot = remoteSlot;
                            slotTickLimit++;
                            if (slotTickLimit >= Config.SLOTS_PER_TICK.get()) {
                                break;
                            }
                        }
                    }
                    if (slotTickLimit < Config.SLOTS_PER_TICK.get()) currentSlot = 0;
                }
            }
        }
        for (Direction side : directions) {
            currentSide = side;
            InputOutputSettings ioSettings = this.sideIOMap.get(side);
            if (ioSettings != null && ioSettings.getOutputSetting().equals(SideSetting.AUTOMATED)) {
                IItemHandler externalHandler = outboundCache.computeIfAbsent(side, k -> this.lookupExternalHandler((ServerLevel) pLevel, pBlockPos.relative(side), this.getIOSettings(side).getOutputAutomatedSide())).getCapability();
                if (externalHandler != null) {
                    for (int remoteSlot = 0; remoteSlot < externalHandler.getSlots(); remoteSlot++) {
                        for (int outputSlot : outputSlots) {
                            if (!this.getItem(outputSlot).isEmpty() && this.canTakeItemThroughFace(outputSlot, this.getItem(outputSlot), side) && externalHandler.isItemValid(remoteSlot, this.internalHandler.getStackInSlot(outputSlot))) {
                                ItemStack stack = this.internalHandler.extractItem(outputSlot, this.internalHandler.getStackInSlot(outputSlot).getCount(), true);
                                ItemStack remainder = externalHandler.insertItem(remoteSlot, stack, false);
                                int extractCount = stack.getCount() - remainder.getCount();
                                this.internalHandler.extractItem(outputSlot, extractCount, false);
                            }
                        }
                    }
                }
            }
        }
        if (this.isDirty) {
            this.isDirty = false;
            return true;
        }
        return false;
    }

    private BlockCapabilityCache<IItemHandler, Direction> lookupExternalHandler(ServerLevel level, BlockPos blockPos, Direction direction) {
        return BlockCapabilityCache.create(
                Capabilities.ItemHandler.BLOCK,
                level,
                blockPos,
                direction
        );
    }

    @Override
    public void loadAdditional(CompoundTag compound, HolderLookup.Provider pRegistries) {
        super.loadAdditional(compound, pRegistries);
        this.itemStorage = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(compound, this.itemStorage, pRegistries);
    }

    @Override
    public void saveAdditional(CompoundTag compound, HolderLookup.Provider pRegistries) {
        ContainerHelper.saveAllItems(compound, this.itemStorage, pRegistries);
        super.saveAdditional(compound, pRegistries);
    }

    // WorldlyContainer methods
    @Override
    public int[] getSlotsForFace(Direction pSide) {
        return exposedSlots;
    }

    @Override
    public boolean canPlaceItemThroughFace(int pIndex, ItemStack pItemStack, @Nullable Direction pDirection) {
        InputOutputSettings ioSettings = this.sideIOMap.get(pDirection);
        SideSetting inputSetting = ioSettings.getInputSetting();
        if ((inputSetting.equals(SideSetting.PASSIVE) || inputSetting.equals(SideSetting.AUTOMATED)) && Arrays.stream(inputSlots).anyMatch(slot -> slot == pIndex)) {
            return this.canPlaceItem(pIndex, pItemStack);
        }
        return false;
    }

    @Override
    public boolean canPlaceItem(int pIndex, ItemStack pStack) {
        return WorldlyContainer.super.canPlaceItem(pIndex, pStack) && this.insertFunction.isStackValid(pIndex, pStack);
    }

    @Override
    public boolean canTakeItemThroughFace(int pIndex, ItemStack pStack, Direction pDirection) {
        InputOutputSettings ioSettings = this.sideIOMap.get(pDirection);
        SideSetting outputSetting = ioSettings.getOutputSetting();
        if ((outputSetting.equals(SideSetting.PASSIVE) || outputSetting.equals(SideSetting.AUTOMATED)) && Arrays.stream(outputSlots).anyMatch(slot -> slot == pIndex)) {
            return extractFunction.isStackValid(pIndex, pStack);
        }
        return false;
    }

    @Override
    public int getContainerSize() {
        return this.itemStorage.size();
    }

    @Override
    public boolean isEmpty() {
        for (int slot = 0; slot < this.itemStorage.size(); slot++) {
            int finalSlot = slot;
            if (Arrays.stream(exposedSlots).anyMatch(index -> index == finalSlot)) {
                if (!this.itemStorage.get(slot).isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int pSlot) {
        return this.itemStorage.get(pSlot);
    }

    @Override
    public ItemStack removeItem(int pSlot, int pAmount) {
        return ContainerHelper.removeItem(this.itemStorage, pSlot, pAmount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int pSlot) {
        return ContainerHelper.takeItem(this.itemStorage, pSlot);
    }

    @Override
    public void setItem(int pSlot, ItemStack pStack) {
        this.itemStorage.set(pSlot, pStack);
        if (pStack.getCount() > this.getMaxStackSize()) {
            pStack.setCount(this.getMaxStackSize());
        }
    }

    @Override
    public void setChanged() {
        this.isDirty = true;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return this.playerValidator.apply(pPlayer);
    }

    @Override
    public void clearContent() {
        this.itemStorage.clear();
    }

    public ConfigurableItemHandler getInternalHandler() {
        return this.internalHandler;
    }

    @Override
    public void updateIOSettings(Direction side, InputOutputSettings settings) {
        super.updateIOSettings(side, settings);
        ConfigurableItemHandler handler = this.sideConfigs.get(side);
        handler.setExtractAllowed(settings.getOutputSetting().equals(SideSetting.AUTOMATED) || settings.getOutputSetting().equals(SideSetting.PASSIVE));
        handler.setInsertAllowed(settings.getInputSetting().equals(SideSetting.AUTOMATED) || settings.getInputSetting().equals(SideSetting.PASSIVE));
        this.sideConfigs.put(side, handler);
    }

    public class ConfigurableItemHandler implements IItemHandlerModifiable {
        private final boolean bypass;
        private boolean insertAllowed;
        private boolean extractAllowed;

        public ConfigurableItemHandler(boolean insertAllowed, boolean extractAllowed, boolean bypass) {
            this.insertAllowed = insertAllowed;
            this.extractAllowed = extractAllowed;
            this.bypass = bypass;
        }

        @Override
        public void setStackInSlot(int slot, @NotNull ItemStack stack) {
            ItemResourceHandler.this.setItem(slot, stack);
        }

        @Override
        public int getSlots() {
            return ItemResourceHandler.this.getContainerSize();
        }

        @NotNull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return ItemResourceHandler.this.getItem(slot);
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (!bypass) {
                if (!insertAllowed || Arrays.stream(ItemResourceHandler.this.inputSlots).noneMatch(value -> slot == value))
                    return stack;
            }

            if (stack.isEmpty())
                return ItemStack.EMPTY;

            ItemStack stackInSlot = ItemResourceHandler.this.getItem(slot);

            int m;
            if (!stackInSlot.isEmpty()) {
                if (stackInSlot.getCount() >= Math.min(stackInSlot.getMaxStackSize(), getSlotLimit(slot)))
                    return stack;

                if (!ItemStack.isSameItemSameComponents(stack, stackInSlot))
                    return stack;

                if (!ItemResourceHandler.this.canPlaceItem(slot, stack))
                    return stack;

                m = Math.min(stack.getMaxStackSize(), getSlotLimit(slot)) - stackInSlot.getCount();

                if (stack.getCount() <= m) {
                    if (!simulate) {
                        ItemStack copy = stack.copy();
                        copy.grow(stackInSlot.getCount());
                        ItemResourceHandler.this.setItem(slot, copy);
                        ItemResourceHandler.this.setChanged();
                    }

                    return ItemStack.EMPTY;
                } else {
                    stack = stack.copy();
                    if (!simulate) {
                        ItemStack copy = stack.split(m);
                        copy.grow(stackInSlot.getCount());
                        ItemResourceHandler.this.setItem(slot, copy);
                        ItemResourceHandler.this.setChanged();
                        return stack;
                    } else {
                        stack.shrink(m);
                        return stack;
                    }
                }
            } else {
                if (!ItemResourceHandler.this.canPlaceItem(slot, stack))
                    return stack;

                m = Math.min(stack.getMaxStackSize(), getSlotLimit(slot));
                if (m < stack.getCount()) {
                    stack = stack.copy();
                    if (!simulate) {
                        ItemResourceHandler.this.setItem(slot, stack.split(m));
                        ItemResourceHandler.this.setChanged();
                        return stack;
                    } else {
                        stack.shrink(m);
                        return stack;
                    }
                } else {
                    if (!simulate) {
                        ItemResourceHandler.this.setItem(slot, stack);
                        ItemResourceHandler.this.setChanged();
                    }
                    return ItemStack.EMPTY;
                }
            }
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (!bypass) {
                if (!extractAllowed || Arrays.stream(ItemResourceHandler.this.outputSlots).noneMatch(value -> slot == value))
                    return ItemStack.EMPTY;
            }

            if (amount == 0)
                return ItemStack.EMPTY;

            ItemStack stackInSlot = ItemResourceHandler.this.getItem(slot);

            if (stackInSlot.isEmpty())
                return ItemStack.EMPTY;

            if (simulate) {
                if (stackInSlot.getCount() < amount) {
                    return stackInSlot.copy();
                } else {
                    ItemStack copy = stackInSlot.copy();
                    copy.setCount(amount);
                    return copy;
                }
            } else {
                int m = Math.min(stackInSlot.getCount(), amount);

                ItemStack extracted = ItemResourceHandler.this.removeItem(slot, m);
                if (ItemResourceHandler.this.getItem(slot).getCount() == 0) {
                    ItemResourceHandler.this.setItem(slot, ItemStack.EMPTY);
                }
                ItemResourceHandler.this.setChanged();
                return extracted;
            }
        }

        @Override
        public int getSlotLimit(int slot) {
            return ItemResourceHandler.this.getMaxStackSize();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return ItemResourceHandler.this.canPlaceItem(slot, stack);
        }

        public void setInsertAllowed(boolean insertAllowed) {
            this.insertAllowed = insertAllowed;
        }

        public void setExtractAllowed(boolean extractAllowed) {
            this.extractAllowed = extractAllowed;
        }
    }
}
