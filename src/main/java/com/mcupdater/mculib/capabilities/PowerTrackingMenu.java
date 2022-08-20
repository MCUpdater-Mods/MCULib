package com.mcupdater.mculib.capabilities;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public abstract class PowerTrackingMenu extends AbstractContainerMenu {
    protected PoweredBlockEntity tileEntity;

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
                        tileEntity.energyStorage.setEnergy(energyStored + (value & 0xffff));
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
                    tileEntity.energyStorage.setEnergy(energyStored | (value << 16));
                }
            });
        }
    }

    public int getEnergy() {
        return tileEntity.energyStorage.getInternalHandler().getEnergyStored();
    }

    public int getMaxEnergy() {
        return tileEntity.energyStorage.getInternalHandler().getMaxEnergyStored();
    }

    public IEnergyStorage getEnergyHandler() {
        return tileEntity.getEnergyHandler();
    }

}
