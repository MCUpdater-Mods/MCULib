package com.mcupdater.mculib.inventory;


import net.neoforged.neoforge.fluids.FluidStack;

@FunctionalInterface
public interface FluidStackValidator {
    boolean isStackValid(int tank, FluidStack fluidStack);
}
