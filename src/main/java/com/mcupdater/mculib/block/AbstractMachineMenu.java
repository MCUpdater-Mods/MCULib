package com.mcupdater.mculib.block;

import com.mcupdater.mculib.capabilities.PowerTrackingMenu;
import com.mcupdater.mculib.inventory.MachineInputSlot;
import com.mcupdater.mculib.inventory.MachineOutputSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import java.util.Map;

public abstract class AbstractMachineMenu<MACHINE extends AbstractMachineBlockEntity> extends PowerTrackingMenu implements IConfigurableMenu {
    protected final Player player;
    protected final IItemHandler playerInventory;
    protected final ContainerData data;
    private final Map<Direction, Component> adjacentNames;
    protected MACHINE machineEntity;

    protected AbstractMachineMenu(MACHINE sourceEntity, MenuType<?> type, int id, Level level, BlockPos blockPos, Inventory inventory, Player player, ContainerData data, Map<Direction,Component> adjacentNames) {
        super(type, id);
        this.machineEntity = sourceEntity;
        this.tileEntity = sourceEntity;
        this.player = player;
        this.playerInventory = new InvWrapper(inventory);
        this.data = data;
        this.adjacentNames = adjacentNames;

        this.addMachineSlots();
        layoutPlayerInventorySlots(8,84);
        trackPower();
        addDataSlots(data);
    }

    protected void addMachineSlots() {
        addSlot(new MachineInputSlot(this.machineEntity, new InvWrapper(this.machineEntity), 0, 62, 37));
        addSlot(new MachineOutputSlot(this.machineEntity, new InvWrapper(this.machineEntity), 1, 98, 37));
    }

    @Override
    public BlockEntity getBlockEntity() {
        return machineEntity;
    }

    protected void layoutPlayerInventorySlots(int leftCol, int topRow) {
        // Player inventory
        addSlotBox(playerInventory, 9, leftCol, topRow, 9, 18, 3, 18);

        // Hotbar
        topRow += 58;
        addSlotRange(playerInventory, 0, leftCol, topRow, 9, 18);
    }

    protected int addSlotBox(IItemHandler handler, int index, int x, int y, int horAmount, int dx, int verAmount, int dy) {
        for (int j = 0; j < verAmount; j++) {
            index = addSlotRange(handler, index, x, y, horAmount, dx);
            y += dy;
        }
        return index;
    }

    protected int addSlotRange(IItemHandler handler, int index, int x, int y, int amount, int dx) {
        for (int i = 0; i < amount; i++) {
            addSlot(new SlotItemHandler(handler, index, x, y));
            x += dx;
            index++;
        }
        return index;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            itemstack = stackInSlot.copy();
            if (index == 0 || index == 1) { // Input slot (0) or Output slot (1)
                if (!this.moveItemStackTo(stackInSlot, 2,38, true)) {
                    return ItemStack.EMPTY;
                }
            } else { // Player inventory slots
                if (this.machineEntity.canPlaceItem(0, stackInSlot)) { // Insert fuel
                    if (!this.moveItemStackTo(stackInSlot, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= 2 && index < 29) { // Move to hotbar
                    if (!this.moveItemStackTo(stackInSlot, 29, 38, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= 29 && index < 38 && !this.moveItemStackTo(stackInSlot, 2, 29, false)) { // Move to inventory
                    return ItemStack.EMPTY;
                }
            }

            if (stackInSlot.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stackInSlot.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, stackInSlot);
        }
        return itemstack;
    }

    public boolean isWorking() {
        return this.data.get(0) > 0;
    }

    public int getWorkProgress() {
        int maxWork = this.data.get(1);
        if (maxWork == 0) {
            maxWork = 200;
        }
        return this.data.get(0) * 18 / maxWork;
    }

    @Override
    public Component getSideName(Direction side) {
        return this.adjacentNames.get(side);
    }
}
