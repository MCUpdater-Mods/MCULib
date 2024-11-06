package com.mcupdater.mculib.inventory;

import com.mcupdater.mculib.block.AbstractMachineBlockEntity;
import com.mcupdater.mculib.capabilities.EnergyResourceHandler;
import com.mcupdater.mculib.capabilities.FluidResourceHandler;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class MachineContainer implements Container {


	private final AbstractMachineBlockEntity machine;

	public MachineContainer(AbstractMachineBlockEntity entity) {
		this.machine = entity;
	}

	public FluidResourceHandler getFluidHandler() {
		return machine.getFluidHandler();
	}

	public EnergyResourceHandler getEnergyStorage() {
		return machine.getEnergyStorage();
	}

	@Override
	public int getContainerSize() {
		return machine.getInventory().getContainerSize();
	}

	@Override
	public boolean isEmpty() {
		return machine.getInventory().isEmpty();
	}

	@Override
	public ItemStack getItem(int pSlot) {
		return machine.getInventory().getItem(pSlot);
	}

	@Override
	public ItemStack removeItem(int pSlot, int pAmount) {
		return machine.getInventory().removeItem(pSlot, pAmount);
	}

	@Override
	public ItemStack removeItemNoUpdate(int pSlot) {
		return machine.getInventory().removeItemNoUpdate(pSlot);
	}

	@Override
	public void setItem(int pSlot, ItemStack pStack) {
		machine.getInventory().setItem(pSlot, pStack);
	}

	@Override
	public void setChanged() {
		machine.getInventory().setChanged();
	}

	@Override
	public boolean stillValid(Player pPlayer) {
		return machine.getInventory().stillValid(pPlayer);
	}

	@Override
	public void clearContent() {
		machine.getInventory().clearContent();
	}
}
