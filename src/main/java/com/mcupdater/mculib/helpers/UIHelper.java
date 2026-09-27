package com.mcupdater.mculib.helpers;

import com.mcupdater.mculib.block.AbstractMachineBlockEntity;
import com.mcupdater.mculib.block.AbstractMachineMenu;
import com.mcupdater.mculib.gui.UpdatableImageButton;
import com.mcupdater.mculib.network.XpExtract;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

public class UIHelper {
    private static final ResourceLocation XPBOTTLE = ResourceLocation.withDefaultNamespace("textures/item/experience_bottle.png");

    public static UpdatableImageButton createXPButton(AbstractMachineMenu<?> machineMenu, int x, int y) {
        var xpButton = new UpdatableImageButton(x, y, 14, 14, 16, 16, Component.empty(), button -> {
            PacketDistributor.sendToServer(new XpExtract(machineMenu.getBlockEntity().getBlockPos()));
        });
        xpButton.setResourceLocation(XPBOTTLE);
        xpButton.active = true;
        xpButton.visible = true;
        return xpButton;
    }
}
