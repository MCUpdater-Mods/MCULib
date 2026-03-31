package com.mcupdater.mculib.item;

import com.mcupdater.mculib.block.AbstractMachineBlock;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MachineBlockItem extends BlockItem {

    public MachineBlockItem(@NotNull AbstractMachineBlock machineBlock, Item.Properties properties) {
        super(machineBlock, properties);
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pFlag) {
        super.appendHoverText(pStack, pContext, pTooltipComponents, pFlag);

        IEnergyStorage energyStorage = pStack.getCapability(Capabilities.EnergyStorage.ITEM, null);
        if (energyStorage != null) {
                pTooltipComponents.add(Component.literal(String.format("%d / %d FE",energyStorage.getEnergyStored(),energyStorage.getMaxEnergyStored())));
        }
    }
}
