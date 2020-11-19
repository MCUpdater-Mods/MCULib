package com.mcupdater.mculib.helpers;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

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

    public static IItemHandler getWrapper(TileEntity tileEntity, Direction side) {
        if (tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side).isPresent()) {
            return tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side).orElse(EmptyHandler.INSTANCE);
        } else if (tileEntity instanceof ISidedInventory) {
            return new SidedInvWrapper((ISidedInventory) tileEntity, side);
        } else if (tileEntity instanceof IInventory) {
            return new InvWrapper((IInventory) tileEntity);
        }
        return EmptyHandler.INSTANCE;
    }

    public static boolean addToPriorityInventory(World world, BlockPos pos, ItemStack stack, List<Direction> sides) {
        //List<EnumFacing> sides = getSideList(pos, ((TileRecon)world.getTileEntity(pos)).getOrientation());
        for (Direction side : sides) {
            TileEntity target;
            target = world.getTileEntity(pos.offset(side));
            if (target != null) {
                IItemHandler invOutput = getWrapper(target, side.getOpposite());
                if (invOutput != EmptyHandler.INSTANCE) {
                    if (canStackFitInInventory(invOutput, stack)) {
                        ItemStack remain = insertItemStackIntoInventory(invOutput, stack);
                        if (remain.isEmpty()) {
                            return true;
                        }
                        return false;
                    }
                }
            }
        }
        return false;
    }

    public static boolean canStacksMerge(ItemStack stack1, ItemStack stack2) {
        if (stack1 == null || stack2 == null)
            return false;
        if (!stack1.isItemEqual(stack2))
            return false;
        if (!ItemStack.areItemStackTagsEqual(stack1, stack2))
            return false;
        return true;

    }

    public static List<Direction> getSideList(BlockPos pos, Direction facing) {
        switch (facing) {
            case NORTH:
                return new ArrayList<>(Arrays.asList(Direction.DOWN, Direction.WEST, Direction.SOUTH, Direction.EAST, Direction.UP));
            case EAST:
                return new ArrayList<>(Arrays.asList(Direction.DOWN, Direction.NORTH, Direction.WEST, Direction.SOUTH, Direction.UP));
            case SOUTH:
                return new ArrayList<>(Arrays.asList(Direction.DOWN, Direction.EAST, Direction.NORTH, Direction.WEST, Direction.UP));
            case WEST:
                return new ArrayList<>(Arrays.asList(Direction.DOWN, Direction.SOUTH, Direction.EAST, Direction.NORTH, Direction.UP));
        }
        return new ArrayList<>(Arrays.asList(Direction.DOWN, Direction.SOUTH, Direction.EAST, Direction.NORTH, Direction.UP));
    }
}
