package com.mcupdater.mculib.inventory;

import com.mcupdater.mculib.block.MachineBlockEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class MachineOutputSlot extends SlotItemHandler {
    private final int index;
    private final MachineBlockEntity blockEntity;

    public MachineOutputSlot(MachineBlockEntity blockEntity, IItemHandler itemHandler, int parentIndex, int xPosition, int yPosition) {
        super(itemHandler, parentIndex, xPosition, yPosition);
        this.blockEntity = blockEntity;
        this.index = parentIndex;
    }

    @Override
    public boolean mayPickup(Player player) {
        return true;
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        int exp = blockEntity.extractExperience();
        ExperienceOrb.award((ServerLevel) blockEntity.getLevel(), player.position(), exp);
        super.onTake(player, stack);
    }
}
