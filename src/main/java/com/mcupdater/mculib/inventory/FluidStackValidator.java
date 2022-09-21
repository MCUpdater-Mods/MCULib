package com.mcupdater.mculib.inventory;


import net.minecraftforge.fluids.FluidStack;

@FunctionalInterface
public interface FluidStackValidator {
    boolean isStackValid(int tank, FluidStack fluidStack);
}
