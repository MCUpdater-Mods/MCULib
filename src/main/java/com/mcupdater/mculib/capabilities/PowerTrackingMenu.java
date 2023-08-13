package com.mcupdater.mculib.capabilities;

import com.mcupdater.mculib.block.AbstractConfigurableBlockEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;

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
        return tileEntity.getCapability(ForgeCapabilities.ENERGY, null).orElse(new EnergyStorage(0)).getEnergyStored();
    }

    public int getMaxEnergy() {
        return tileEntity.getCapability(ForgeCapabilities.ENERGY, null).orElse(new EnergyStorage(0)).getMaxEnergyStored();
    }

    public IEnergyStorage getEnergyHandler() {
        return tileEntity.getCapability(ForgeCapabilities.ENERGY, null).orElse(new EnergyStorage(0));
    }

}
