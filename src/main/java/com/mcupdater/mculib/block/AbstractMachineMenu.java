package com.mcupdater.mculib.block;

import com.mcupdater.mculib.capabilities.ItemResourceHandler;
import com.mcupdater.mculib.capabilities.PowerTrackingMenu;
import com.mcupdater.mculib.inventory.MachineInputSlot;
import com.mcupdater.mculib.inventory.MachineOutputSlot;
import com.mcupdater.mculib.inventory.PhantomSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public abstract class AbstractMachineMenu<MACHINE extends AbstractMachineBlockEntity> extends PowerTrackingMenu implements IConfigurableMenu {
    protected final Player player;
    protected final IItemHandler playerInventory;
    protected final ContainerData data;
    private final Map<Direction, String> adjacentNames;

    protected MACHINE machineEntity;

    protected AbstractMachineMenu(MACHINE sourceEntity, MenuType<?> type, int id, Level level, BlockPos blockPos, Inventory inventory, Player player, ContainerData data, Map<Direction,String> adjacentNames) {
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
        ItemResourceHandler resourceHandler = (ItemResourceHandler) this.machineEntity.configMap.get("items");
        addSlot(new MachineInputSlot(this.machineEntity, resourceHandler.getInternalHandler(), 0, 62, 37));
        addSlot(new MachineOutputSlot(this.machineEntity, resourceHandler.getInternalHandler(), 1, 98, 37));
    }

    @Override
    public AbstractConfigurableBlockEntity getBlockEntity() {
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
        int invOffset = 2; // Number of slots that are not part of the player inventory
        int hotbarOffset = invOffset + 27;
        ItemStack itemstack = ItemStack.EMPTY;
        ItemResourceHandler resourceHandler = (ItemResourceHandler) this.machineEntity.configMap.get("items");
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            itemstack = stackInSlot.copy();
            if (index < invOffset) { // Input slot (0) or Output slot (1)
                if (!this.moveItemStackTo(stackInSlot, invOffset,invOffset + 36, true)) {
                    return ItemStack.EMPTY;
                }
            } else { // Player inventory slots
                if (resourceHandler.canPlaceItem(0, stackInSlot)) { // Insert fuel
                    if (!this.moveItemStackTo(stackInSlot, 0, invOffset, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= invOffset && index < invOffset + 27) { // Move to hotbar
                    if (!this.moveItemStackTo(stackInSlot, hotbarOffset, hotbarOffset + 9, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= hotbarOffset && index < hotbarOffset + 9 && !this.moveItemStackTo(stackInSlot, invOffset, hotbarOffset, false)) { // Move to inventory
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
    public String getSideName(Direction side) {
        return this.adjacentNames.get(side);
    }

    @Override
    public void doClick(int slotId, int button, ClickType clickType, Player player) {
        // Don't fill phantom slots while drag-clicking (prevents loss of items)
        if (slotId >= 0 && this.getSlot(slotId) instanceof PhantomSlot && clickType == ClickType.QUICK_CRAFT && getQuickcraftHeader(button) == 1) {
            return;
        }
        super.doClick(slotId, button, clickType, player);
    }
}
