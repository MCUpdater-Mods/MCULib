package com.mcupdater.mculib.capabilities;

import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.IntReferenceHolder;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public abstract class ContainerPowered extends Container {
    protected TileEntityPowered tileEntity;

    protected ContainerPowered(ContainerType<?> type, int id) {
        super(type, id);
    }

    protected void trackPower() {
        if (tileEntity != null) {
            trackInt(new IntReferenceHolder() {
                @Override
                public int get() {
                    return getEnergy() & 0xffff;
                }

                @Override
                public void set(int value) {
                    tileEntity.getCapability(CapabilityEnergy.ENERGY).ifPresent(h -> {
                        int energyStored = h.getEnergyStored() & 0xffff0000;
                        ((SerializedEnergyStorage) h).setEnergy(energyStored + (value & 0xffff));
                    });
                }
            });
            trackInt(new IntReferenceHolder() {
                @Override
                public int get() {
                    return (getEnergy() >> 16) & 0xffff;
                }

                @Override
                public void set(int value) {
                    tileEntity.getCapability(CapabilityEnergy.ENERGY).ifPresent(h -> {
                        int energyStored = h.getEnergyStored() & 0x0000ffff;
                        ((SerializedEnergyStorage) h).setEnergy(energyStored | (value << 16));
                    });
                }
            });
        }
    }

    public int getEnergy() {
        return tileEntity.getCapability(CapabilityEnergy.ENERGY).map(IEnergyStorage::getEnergyStored).orElse(0);
    }

    public int getMaxEnergy() {
        return tileEntity.getCapability(CapabilityEnergy.ENERGY).map(IEnergyStorage::getMaxEnergyStored).orElse(0);
    }

    public IEnergyStorage getEnergyHandler() {
        return tileEntity.getEnergyHandler();
    }

}
