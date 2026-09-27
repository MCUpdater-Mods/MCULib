package com.mcupdater.mculib.gui;

import com.mcupdater.mculib.MCULib;
import com.mcupdater.mculib.block.AbstractConfigurableBlockEntity;
import com.mcupdater.mculib.block.IConfigurableMenu;
import com.mcupdater.mculib.network.RedstoneConfig;
import com.mcupdater.mculib.redstone.ComparatorBehavior;
import com.mcupdater.mculib.redstone.SignalBehavior;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.redstone.Redstone;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Set;

public class RedstonePanel extends AbstractParentWidget {

    private static final int COLOR_BACKGROUND = 0xffc6c6c6;
    private final Font font;
    private final IConfigurableMenu menu;

    // Icon definitions
    protected final ResourceLocation CLOSED = ResourceLocation.fromNamespaceAndPath(MCULib.MODID, "textures/gui/icon/prohibition.png");
    protected final ResourceLocation ALLOWED = ResourceLocation.fromNamespaceAndPath(MCULib.MODID, "textures/gui/icon/arrow.png");
    protected final ResourceLocation AUTOMATED = ResourceLocation.fromNamespaceAndPath(MCULib.MODID, "textures/gui/icon/gear-arrow.png");
    protected final ResourceLocation ITEMS = ResourceLocation.fromNamespaceAndPath(MCULib.MODID, "textures/gui/icon/box.png");
    protected final ResourceLocation ENERGY = ResourceLocation.fromNamespaceAndPath(MCULib.MODID, "textures/gui/icon/lightning.png");
    protected final ResourceLocation FLUIDS = ResourceLocation.fromNamespaceAndPath(MCULib.MODID, "textures/gui/icon/flask.png");
    private final TextButton behaviorButton;
    private final TextButton comparatorResourceButton;
    private final TextButton comparatorInvertedButton;

    public RedstonePanel(IConfigurableMenu srcMenu, int leftPos, int topPos, int width, int height) {
        super(leftPos, topPos, width, height, CommonComponents.EMPTY, COLOR_BACKGROUND);
        MCULib.LOGGER.trace("leftPos: %d, topPos: %d, width: %d, height: %d",leftPos,topPos,width,height);
        this.font = Minecraft.getInstance().font;
        this.menu = srcMenu;
        AbstractConfigurableBlockEntity self = this.menu.getBlockEntity();
        this.behaviorButton = new TextButton(this.getX() + 5, this.getY() + 13, 75, 14, Component.literal("Ignored"), button -> {
            int delta = Screen.hasShiftDown() ? 2 : 1;
            AbstractConfigurableBlockEntity entity = RedstonePanel.this.menu.getBlockEntity();
            BlockPos pos = entity.getBlockPos();
            SignalBehavior currentBehavior = entity.getSignalBehavior();
            Byte newValue = (byte) ((currentBehavior.ordinal() + delta) % SignalBehavior.values().length);
            PacketDistributor.sendToServer(new RedstoneConfig(pos, SignalBehavior.values()[newValue], entity.getComparatorBehavior()));
        }, Component.translatable("gui.mculib.redstone.behavior.tooltip"));
        this.comparatorResourceButton = new TextButton(this.getX() + 5, this.getY() + 50, 75, 14, Component.literal("Energy"), button -> {
            AbstractConfigurableBlockEntity entity = RedstonePanel.this.menu.getBlockEntity();
            BlockPos pos = entity.getBlockPos();
            List<String> resources = entity.getAvailableResources().stream().toList();
            int delta = Screen.hasShiftDown() ? resources.size()-1 : 1;
            ComparatorBehavior currentBehavior = entity.getComparatorBehavior();
            int ordinal = (resources.indexOf(currentBehavior.resourceType()) + delta) % resources.size();
            ComparatorBehavior newBehavior = new ComparatorBehavior(resources.get(ordinal), currentBehavior.inverted());
            PacketDistributor.sendToServer(new RedstoneConfig(pos, entity.getSignalBehavior(), newBehavior));
        }, Component.translatable("gui.mculib.redstone.comparator.tooltip"));
        this.comparatorInvertedButton = new TextButton( this.getX() + 5, this.getY() + 65, 75, 14, Component.literal("Normal"), button -> {
            AbstractConfigurableBlockEntity entity = RedstonePanel.this.menu.getBlockEntity();
            BlockPos pos = entity.getBlockPos();
            PacketDistributor.sendToServer(new RedstoneConfig(pos, entity.getSignalBehavior(), new ComparatorBehavior(entity.getComparatorBehavior().resourceType(), !entity.getComparatorBehavior().inverted())));
        }, Component.translatable("gui.mculib.redstone.comparator.highlow"));
    }

    @Override
    public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        MCULib.LOGGER.trace("RedstonePanel - x: %d, y: %d, width: %d, height: %d",this.getX(), this.getY(), this.width, this.height);

        if (this.isVisible()) {
            AbstractConfigurableBlockEntity blockEntity = this.menu.getBlockEntity();
            updateLabels(blockEntity);
            int yOffset = 0;
            pGuiGraphics.drawString(font, Component.literal("Redstone behavior:"), this.getX() + 5, this.getY() + 4 + yOffset, 0xffffffff);
            yOffset += 36;
            behaviorButton.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
            pGuiGraphics.drawString(font, Component.literal("Comparator behavior:"), this.getX() + 5, this.getY() + 4 + yOffset, 0xffffffff);
            comparatorResourceButton.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
            comparatorInvertedButton.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        }
    }

    private void updateLabels(AbstractConfigurableBlockEntity blockEntity) {
        this.behaviorButton.setMessage(switch (blockEntity.getSignalBehavior()) {
            case IGNORE -> Component.translatable("gui.mculib.redstone.behavior.ignore");
            case REQUIRED -> Component.translatable("gui.mculib.redstone.behavior.required");
            case INVERTED -> Component.translatable("gui.mculib.redstone.behavior.inverted");
        });
        this.comparatorResourceButton.setMessage(Component.translatable("gui.mculib.redstone.comparator." + blockEntity.getComparatorBehavior().resourceType()));
        this.comparatorInvertedButton.setMessage(Component.translatable(blockEntity.getComparatorBehavior().inverted() ? "gui.mculib.redstone.comparator.inverted" : "gui.mculib.redstone.comparator.normal"));
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        return this.behaviorButton.mouseClicked(pMouseX, pMouseY, pButton) ||
                this.comparatorResourceButton.mouseClicked(pMouseX, pMouseY, pButton) ||
                this.comparatorInvertedButton.mouseClicked(pMouseX, pMouseY, pButton);
    }

    private boolean testForCapability(BlockCapability capability, Level level, BlockPos blockPos) {
        return level.getCapability(capability, blockPos) != null;
    }

}
