package com.mcupdater.mculib.gui;

import com.mcupdater.mculib.block.AbstractMachineMenu;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.CapabilityItemHandler;

public class ConfigPanel extends AbstractWidget {

    private final int COLOR_BACKGROUND = 0xffc6c6c6;
    private final int COLOR_SHADOW = 0x7f373737;
    private final int COLOR_HIGHLIGHT = 0x7fffffff;
    private final Font font;
    private final AbstractMachineMenu menu;

    public ConfigPanel(AbstractMachineMenu srcMenu, int leftPos, int topPos, int width, int height) {
        super(leftPos, topPos, width, height, new TranslatableComponent("configuration"));
        this.font = Minecraft.getInstance().font;
        this.menu = srcMenu;
    }

    @Override
    public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        this.fill(pPoseStack, x, y, x + width, y + height, COLOR_BACKGROUND);
        this.hLine(pPoseStack, x, x + width - 1, y, COLOR_HIGHLIGHT);
        this.vLine(pPoseStack, x, y, y + height - 1, COLOR_HIGHLIGHT);
        this.hLine(pPoseStack, x, x+ width -1, y + height - 1, COLOR_SHADOW);
        this.vLine(pPoseStack, x + width - 1, y, y + height - 1, COLOR_SHADOW);

        Level level = this.menu.getBlockEntity().getLevel();
        BlockPos blockPos = this.menu.getBlockEntity().getBlockPos();
        BlockEntity tempEntity = level.getBlockEntity(blockPos.above());
        int yOffset = 0;
        this.font.draw(pPoseStack, new TextComponent("U: ").append(menu.getSideName(Direction.UP)).append(new TextComponent(" " + (tempEntity != null ? Boolean.toString(tempEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.DOWN).isPresent()) : "Invalid"))), this.x + 5, this.y + 5 + yOffset, 0xff000000);
        tempEntity = level.getBlockEntity(blockPos.below());
        yOffset += 10;
        this.font.draw(pPoseStack, new TextComponent("D: ").append(menu.getSideName(Direction.DOWN)).append(new TextComponent(" " + (tempEntity != null ? Boolean.toString(tempEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.UP).isPresent()) : "Invalid"))), this.x + 5, this.y + 5 + yOffset, 0xff000000);
        tempEntity = level.getBlockEntity(blockPos.north());
        yOffset += 10;
        this.font.draw(pPoseStack, new TextComponent("N: ").append(menu.getSideName(Direction.NORTH)).append(new TextComponent(" " + (tempEntity != null ? Boolean.toString(tempEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.SOUTH).isPresent()) : "Invalid"))), this.x + 5, this.y + 5 + yOffset, 0xff000000);
        tempEntity = level.getBlockEntity(blockPos.south());
        yOffset += 10;
        this.font.draw(pPoseStack, new TextComponent("S: ").append(menu.getSideName(Direction.SOUTH)).append(new TextComponent(" " + (tempEntity != null ? Boolean.toString(tempEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.NORTH).isPresent()) : "Invalid"))), this.x + 5, this.y + 5 + yOffset, 0xff000000);
        tempEntity = level.getBlockEntity(blockPos.east());
        yOffset += 10;
        this.font.draw(pPoseStack, new TextComponent("E: ").append(menu.getSideName(Direction.EAST)).append(new TextComponent(" " + (tempEntity != null ? Boolean.toString(tempEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.WEST).isPresent()) : "Invalid"))), this.x + 5, this.y + 5 + yOffset, 0xff000000);
        tempEntity = level.getBlockEntity(blockPos.west());
        yOffset += 10;
        this.font.draw(pPoseStack, new TextComponent("W: ").append(menu.getSideName(Direction.WEST)).append(new TextComponent(" " + (tempEntity != null ? Boolean.toString(tempEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.EAST).isPresent()) : "Invalid"))), this.x + 5, this.y + 5 + yOffset, 0xff000000);
    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {

    }
}
