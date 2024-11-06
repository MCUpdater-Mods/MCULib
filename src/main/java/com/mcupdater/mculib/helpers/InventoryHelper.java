package com.mcupdater.mculib.helpers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InventoryHelper {
    public static boolean canStackFitInInventory(IItemHandler target, ItemStack toInsert) {
        if (toInsert.isEmpty()) {
            return false;
        }
        ItemStack remainingItems = toInsert.copy();
        for (int slot = 0; slot < target.getSlots() && !remainingItems.isEmpty(); slot++ ) {
            remainingItems = target.insertItem(slot, remainingItems, true);
        }
        return (remainingItems.isEmpty());
    }

    public static ItemStack insertItemStackIntoInventory(IItemHandler target, ItemStack toInsert) {
        if (toInsert.isEmpty()) {
            return toInsert;
        }
        for (int slot = 0; slot < target.getSlots() && !toInsert.isEmpty(); slot++ ) {
            toInsert = target.insertItem(slot, toInsert, false);
        }
        return toInsert;
    }

    public static IItemHandler getWrapper(Level level, BlockEntity blockEntity, Direction side) {
        @Nullable IItemHandler cap = level.getCapability(Capabilities.ItemHandler.BLOCK, blockEntity.getBlockPos(), null, blockEntity, side);
        if (cap != null) {
            return cap;
        } else if (blockEntity instanceof WorldlyContainer) {
            return new SidedInvWrapper((WorldlyContainer) blockEntity, side);
        } else if (blockEntity instanceof Container) {
            return new InvWrapper((Container) blockEntity);
        }
        return null;
    }

    public static boolean addToPriorityInventory(Level level, BlockPos pos, ItemStack stack, List<Direction> sides) {
        //List<EnumFacing> sides = getSideList(pos, ((TileRecon)level.getBlockEntity(pos)).getOrientation());
        for (Direction side : sides) {
            BlockEntity target;
            target = level.getBlockEntity(pos.relative(side));
            if (target != null) {
                IItemHandler invOutput = getWrapper(level, target, side.getOpposite());
                if (invOutput != null) {
                    if (canStackFitInInventory(invOutput, stack)) {
                        ItemStack remain = insertItemStackIntoInventory(invOutput, stack);
                        return remain.isEmpty();
                    }
                }
            }
        }
        return false;
    }

    public static boolean canStacksMerge(ItemStack stack1, ItemStack stack2) {
        if (stack1 == null || stack2 == null)
            return false;
        if (!ItemStack.isSameItem(stack1,stack2))
            return false;
        return ItemStack.isSameItemSameComponents(stack1, stack2);

    }

    public static List<Direction> getSideList(BlockPos pos, Direction facing) {
        return switch (facing) {
            case NORTH ->
                    new ArrayList<>(Arrays.asList(Direction.DOWN, Direction.WEST, Direction.SOUTH, Direction.EAST, Direction.UP));
            case EAST ->
                    new ArrayList<>(Arrays.asList(Direction.DOWN, Direction.NORTH, Direction.WEST, Direction.SOUTH, Direction.UP));
            case SOUTH ->
                    new ArrayList<>(Arrays.asList(Direction.DOWN, Direction.EAST, Direction.NORTH, Direction.WEST, Direction.UP));
            case WEST ->
                    new ArrayList<>(Arrays.asList(Direction.DOWN, Direction.SOUTH, Direction.EAST, Direction.NORTH, Direction.UP));
            default ->
                    new ArrayList<>(Arrays.asList(Direction.DOWN, Direction.SOUTH, Direction.EAST, Direction.NORTH, Direction.UP));
        };
    }
}
