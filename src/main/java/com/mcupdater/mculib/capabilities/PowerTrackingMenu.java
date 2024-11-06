package com.mcupdater.mculib.capabilities;

import com.mcupdater.mculib.block.AbstractConfigurableBlockEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.energy.IEnergyStorage;

public abstract class PowerTrackingMenu extends AbstractContainerMenu {
    protected AbstractConfigurableBlockEntity tileEntity;

    protected PowerTrackingMenu(MenuType<?> type, int id) {
        super(type, id);
    }

    protected void trackPower() {
        if (tileEntity != null) {
            addDataSlot(new DataSlot() {
                @Override
                public int get() {
                    return getEnergy();
                }

                @Override
                public void set(int value) {
                        int energyStored = getEnergyHandler().getEnergyStored() & 0xffff0000;
                        tileEntity.getEnergyStorage().setEnergy(energyStored + (value & 0xffff));
                }
            });
            addDataSlot(new DataSlot() {
                @Override
                public int get() {
                    return (getEnergy() >> 16) & 0xffff;
                }

                @Override
                public void set(int value) {
                    int energyStored = getEnergyHandler().getEnergyStored() & 0x0000ffff;
                    tileEntity.getEnergyStorage().setEnergy(energyStored | (value << 16));
                }
            });
        }
    }

    public int getEnergy() {
        return tileEntity.getEnergyStorage().getStoredEnergy();
    }

    public int getMaxEnergy() {
        return tileEntity.getEnergyStorage().getCapacity();
    }

    public IEnergyStorage getEnergyHandler() {
        return tileEntity.getEnergyStorage().getInternalHandler();
    }

}
