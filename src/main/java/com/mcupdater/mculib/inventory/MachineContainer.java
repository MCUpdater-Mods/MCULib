package com.mcupdater.mculib.inventory;

import com.mcupdater.mculib.block.AbstractMachineBlockEntity;
import com.mcupdater.mculib.capabilities.EnergyResourceHandler;
import com.mcupdater.mculib.capabilities.FluidResourceHandler;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public class MachineContainer implements RecipeInput, Container {


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
	public int size() {
		return machine.getItemHandler().getContainerSize();
	}

	@Override
	public int getContainerSize() {
		return this.size();
	}

	@Override
	public boolean isEmpty() {
		return machine.getItemHandler().isEmpty();
	}

	@Override
	public ItemStack getItem(int pSlot) {
		return machine.getItemHandler().getItem(pSlot);
	}

	@Override
	public ItemStack removeItem(int pSlot, int pAmount) {
		return machine.getItemHandler().removeItem(pSlot, pAmount);
	}

	@Override
	public ItemStack removeItemNoUpdate(int pSlot) {
		return machine.getItemHandler().removeItemNoUpdate(pSlot);
	}

	@Override
	public void setItem(int pSlot, ItemStack pStack) {
		machine.getItemHandler().setItem(pSlot, pStack);
	}

	@Override
	public void setChanged() {
		machine.getItemHandler().setChanged();
	}

	@Override
	public boolean stillValid(Player pPlayer) {
		return machine.getItemHandler().stillValid(pPlayer);
	}

	@Override
	public void clearContent() {
		machine.getItemHandler().clearContent();
	}
}
