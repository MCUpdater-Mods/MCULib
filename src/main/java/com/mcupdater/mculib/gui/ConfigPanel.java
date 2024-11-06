package com.mcupdater.mculib.gui;

import com.mcupdater.mculib.MCULib;
import com.mcupdater.mculib.block.AbstractConfigurableBlockEntity;
import com.mcupdater.mculib.block.IConfigurableMenu;
import com.mcupdater.mculib.inventory.InputOutputSettings;
import com.mcupdater.mculib.inventory.SideSetting;
import com.mcupdater.mculib.network.ChannelRegistration;
import com.mcupdater.mculib.network.SideConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConfigPanel extends AbstractParentWidget {

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
    TabWidget itemsTab;
    TabWidget energyTab;
    TabWidget fluidsTab;
    String selectedResource;
    private List<TabWidget> tabs = new ArrayList<>();
    private List<SideButtonGroup> buttons = new ArrayList<>();

    public ConfigPanel(IConfigurableMenu srcMenu, int leftPos, int topPos, int width, int height) {
        super(leftPos, topPos, width, height, CommonComponents.EMPTY, COLOR_BACKGROUND);
        this.font = Minecraft.getInstance().font;
        this.menu = srcMenu;
        AbstractConfigurableBlockEntity self = this.menu.getBlockEntity();

        // Generate tabs
        int hOffset = 0;
        if (self.getInventory() != null) {
            itemsTab = new TabWidget(leftPos + hOffset, topPos - 22, 22, 22, 0xff969696, 0xffd6d6d6, ITEMS, Component.translatable("gui.processenhancement.items"), this::clickItemTab);
            itemsTab.selected = true;
            itemsTab.active = false;
            selectedResource = "items";
            this.addTab(itemsTab);
            hOffset += 23;
        }
        if (self.getEnergyStorage() != null) {
            energyTab = new TabWidget(leftPos + hOffset, topPos - 22, 22, 22, 0xff969696, 0xffd6d6d6, ENERGY, Component.translatable("gui.processenhancement.energy"), this::clickEnergyTab);
            if (itemsTab == null) {
                energyTab.selected = true;
                energyTab.active = false;
                selectedResource = "power";
            }
            this.addTab(energyTab);
            hOffset += 23;
        }
        if (self.getFluidHandler() != null) {
            fluidsTab = new TabWidget(leftPos + hOffset, topPos - 22, 22, 22, 0xff969696, 0xffd6d6d6, FLUIDS, Component.translatable("gui.processenhancement.fluids"), this::clickFluidTab);
            if (itemsTab == null && energyTab == null) {
                fluidsTab.selected = true;
                fluidsTab.active = false;
                selectedResource = "fluids";
            }
            this.addTab(fluidsTab);
            hOffset += 23;  // Increase offset for any future tabs
        }

        // Generate buttons
        int vOffset = 9;
        for (Direction side : Direction.values()) {
            /*
            UpdatableImageButton testButton = new UpdatableImageButton(this.x + 5, this.y + 5 + vOffset, 14, 14, 16, 16, Component.empty(), (button) -> {});
            testButton.setResourceLocation(AUTOMATED);
            testButton.setTooltip(Component.translatable("gui.processenhancement.automated"));
            this.buttons.add(testButton);*
             */
            buttons.add(new SideButtonGroup(side, vOffset));
            vOffset += 26;
        }
    }

    private void clickItemTab(double x, double y) {
        itemsTab.active = false;
        selectedResource = "items";
        if (energyTab != null) {
            energyTab.active = true;
            energyTab.selected = false;
        }
        if (fluidsTab != null) {
            fluidsTab.active = true;
            fluidsTab.selected = false;
        }
    }

    private void clickEnergyTab(double x, double y) {
        energyTab.active = false;
        selectedResource = "power";
        if (itemsTab != null) {
            itemsTab.active = true;
            itemsTab.selected = false;
        }
        if (fluidsTab != null) {
            fluidsTab.active = true;
            fluidsTab.selected = false;
        }
    }

    private void clickFluidTab(double x, double y) {
        fluidsTab.active = false;
        selectedResource = "fluids";
        if (itemsTab != null) {
            itemsTab.active = true;
            itemsTab.selected = false;
        }
        if (energyTab != null) {
            energyTab.active = true;
            energyTab.selected = false;
        }
    }

    private void addTab(TabWidget newTab) {
        this.tabs.add(newTab);
    }

    @Override
    public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        if (this.isVisible()) {
            AbstractConfigurableBlockEntity blockEntity = this.menu.getBlockEntity();
            // Render tabs in reverse order to ensure tooltips render properly
            List<TabWidget> reverseTabs = new ArrayList<>(this.tabs);
            Collections.reverse(reverseTabs);
            for (TabWidget tab : reverseTabs) {
                tab.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
            }

            Level level = blockEntity.getLevel();
            BlockPos blockPos = blockEntity.getBlockPos();
            int yOffset = 0;
            pGuiGraphics.drawString(font, Component.literal("D: ").append(menu.getSideName(Direction.DOWN)), this.x + 5, this.y + 4 + yOffset, 0xff000000);
            renderCaps(pGuiGraphics, yOffset, level, blockPos.below());
            yOffset += 10;
            yOffset += 16;
            pGuiGraphics.fillGradient(this.x+1, this.y + yOffset + 3, this.x + this.width - 2, this.y + yOffset + 29, 0x33000000, 0x33000000);
            pGuiGraphics.drawString(font, Component.literal("U: ").append(menu.getSideName(Direction.UP)), this.x + 5, this.y + 4 + yOffset, 0xff000000);
            renderCaps(pGuiGraphics, yOffset, level, blockPos.above());
            yOffset += 10;
            yOffset += 16;
            pGuiGraphics.drawString(font, Component.literal("N: ").append(menu.getSideName(Direction.NORTH)), this.x + 5, this.y + 4 + yOffset, 0xff000000);
            renderCaps(pGuiGraphics, yOffset, level, blockPos.north());
            yOffset += 10;
            yOffset += 16;
            pGuiGraphics.fillGradient(this.x+1, this.y + yOffset + 3, this.x + this.width - 2, this.y + yOffset + 29, 0x33000000, 0x33000000);
            pGuiGraphics.drawString(font, Component.literal("S: ").append(menu.getSideName(Direction.SOUTH)), this.x + 5, this.y + 4 + yOffset, 0xff000000);
            renderCaps(pGuiGraphics, yOffset, level, blockPos.south());
            yOffset += 10;
            yOffset += 16;
            pGuiGraphics.drawString(font, Component.literal("W: ").append(menu.getSideName(Direction.WEST)), this.x + 5, this.y + 4 + yOffset, 0xff000000);
            renderCaps(pGuiGraphics, yOffset, level, blockPos.west());
            yOffset += 10;
            yOffset += 16;
            pGuiGraphics.fillGradient(this.x+1, this.y + yOffset + 3, this.x + this.width - 2, this.y + yOffset + 29, 0x33000000, 0x33000000);
            pGuiGraphics.drawString(font, Component.literal("E: ").append(menu.getSideName(Direction.EAST)), this.x + 5, this.y + 4 + yOffset, 0xff000000);
            renderCaps(pGuiGraphics, yOffset, level, blockPos.east());
            yOffset += 10;
            for (SideButtonGroup group : this.buttons) {
                group.updateIOSettings(blockEntity.getResourceHandler(this.selectedResource).getIOSettings(group.getSide()));
                group.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
            }
            this.renderTooltips(pGuiGraphics, pMouseX, pMouseY);
        }
    }

    private void renderTooltips(GuiGraphics pPoseStack, int pMouseX, int pMouseY) {
       for (SideButtonGroup group : this.buttons) {
           group.renderTooltips(pPoseStack, pMouseX, pMouseY);
       }
    }

    private boolean testForCapability(BlockCapability capability, Level level, BlockPos blockPos) {
        return level.getCapability(capability, blockPos) != null;
    }

    private void renderCaps(GuiGraphics guiGraphics, int yOffset, Level level, BlockPos blockPos) {
        if (testForCapability(Capabilities.ItemHandler.BLOCK, level, blockPos)) {
            guiGraphics.blit(ITEMS,this.x + this.width - 36, this.y + 3 + yOffset, 10, 10, 0f, 0f, 16, 16, 16, 16);
        }
        if (testForCapability(Capabilities.EnergyStorage.BLOCK, level, blockPos)) {
            guiGraphics.blit(ENERGY,this.x + this.width - 24, this.y + 3 + yOffset, 10, 10, 0f, 0f, 16, 16, 16, 16);
        }
        if (testForCapability(Capabilities.FluidHandler.BLOCK, level, blockPos)) {
            guiGraphics.blit(FLUIDS,this.x + this.width - 12, this.y + 3 + yOffset, 10, 10, 0f, 0f, 16, 16, 16, 16);
        }
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        for (TabWidget child : this.tabs) {
            if (child.mouseClicked(pMouseX,pMouseY,pButton))
                return true;
        }
        for (SideButtonGroup group : this.buttons) {
            if (group.mouseClicked(pMouseX, pMouseY, pButton))
                return true;
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    public BlockCapability mapCapability() {
        return switch (selectedResource) {
            default -> Capabilities.ItemHandler.BLOCK;
            case "power" -> Capabilities.EnergyStorage.BLOCK;
            case "fluids" -> Capabilities.FluidHandler.BLOCK;
        };
    }

    public class SideButtonGroup {
        private final Direction side;
        private final int yOffset;
        private TextButton priorityButton;
        private UpdatableImageButton inputModeButton;
        private UpdatableImageButton outputModeButton;
        private TextButton inputSideButton;
        private TextButton outputSideButton;

        public SideButtonGroup(Direction side, int yOffset) {
            this.side = side;
            this.yOffset = yOffset;
            this.priorityButton = new TextButton(ConfigPanel.this.x + 47, ConfigPanel.this.y + 5 + yOffset, 14, 14, Component.empty(), button -> {
                int delta = Screen.hasShiftDown() ? -1 : 1;
                AbstractConfigurableBlockEntity entity = ConfigPanel.this.menu.getBlockEntity();
                BlockPos pos = entity.getBlockPos();
                InputOutputSettings ioSettings = entity.getResourceHandler(ConfigPanel.this.selectedResource).getIOSettings(this.side);
                Byte newValue = (byte) ((ioSettings.getPriority()+delta) % 6);
                if (newValue < 0) newValue = 5; // No negative values allowed
                ChannelRegistration.MCULIB_CHANNEL.sendToServer(new SideConfig(pos, this.side, ConfigPanel.this.selectedResource, true, ioSettings.getInputSetting(), ioSettings.getInputAutomatedSide(), newValue));
            }, Component.translatable("side.priority.tooltip"));
            this.inputModeButton = new UpdatableImageButton(ConfigPanel.this.x + 89, ConfigPanel.this.y + 5 + yOffset, 14, 14, 16, 16, Component.empty(), button -> {
                int delta = Screen.hasShiftDown() ? -1 : 1;
                AbstractConfigurableBlockEntity entity = ConfigPanel.this.menu.getBlockEntity();
                BlockPos pos = entity.getBlockPos();
                InputOutputSettings ioSettings = entity.getResourceHandler(ConfigPanel.this.selectedResource).getIOSettings(this.side);
                Byte newOrdinal = (byte) ((ioSettings.getInputSetting().ordinal()+delta) % SideSetting.values().length);
                if (newOrdinal < 0) newOrdinal = 2; // No negative values allowed
                SideSetting newValue = SideSetting.values()[newOrdinal];
                ChannelRegistration.MCULIB_CHANNEL.sendToServer(new SideConfig(pos, this.side, ConfigPanel.this.selectedResource, true, newValue, ioSettings.getInputAutomatedSide(), ioSettings.getPriority()));
            });
            this.inputSideButton = new TextButton(ConfigPanel.this.x + 105, ConfigPanel.this.y + 5 + yOffset, 14, 14, Component.empty(), button -> {
                int delta = Screen.hasShiftDown() ? -1 : 1;
                AbstractConfigurableBlockEntity entity = ConfigPanel.this.menu.getBlockEntity();
                BlockPos pos = entity.getBlockPos();
                InputOutputSettings ioSettings = entity.getResourceHandler(ConfigPanel.this.selectedResource).getIOSettings(this.side);
                Byte newOrdinal = (byte) ((ioSettings.getInputAutomatedSide().ordinal()+delta) % Direction.values().length);
                if (newOrdinal < 0) newOrdinal = 5; // No negative values allowed
                Direction newValue = Direction.values()[newOrdinal];
                ChannelRegistration.MCULIB_CHANNEL.sendToServer(new SideConfig(pos, this.side, ConfigPanel.this.selectedResource, true, ioSettings.getInputSetting(), newValue, ioSettings.getPriority()));
            }, Component.translatable("side.sneaky.tooltip"));
            this.outputModeButton = new UpdatableImageButton(ConfigPanel.this.x + 143, ConfigPanel.this.y + 5 + yOffset, 14, 14, 16, 16, Component.empty(), (button) -> {
                int delta = Screen.hasShiftDown() ? -1 : 1;
                AbstractConfigurableBlockEntity entity = ConfigPanel.this.menu.getBlockEntity();
                BlockPos pos = entity.getBlockPos();
                InputOutputSettings ioSettings = entity.getResourceHandler(ConfigPanel.this.selectedResource).getIOSettings(this.side);
                Byte newOrdinal = (byte) ((ioSettings.getOutputSetting().ordinal()+delta) % SideSetting.values().length);
                if (newOrdinal < 0) newOrdinal = 2; // No negative values allowed
                SideSetting newValue = SideSetting.values()[newOrdinal];
                ChannelRegistration.MCULIB_CHANNEL.sendToServer(new SideConfig(pos, this.side, ConfigPanel.this.selectedResource, false, newValue, ioSettings.getOutputAutomatedSide(), ioSettings.getPriority()));
            });
            this.outputSideButton = new TextButton(ConfigPanel.this.x + 159, ConfigPanel.this.y + 5 + yOffset, 14, 14, Component.empty(), (button) -> {
                int delta = Screen.hasShiftDown() ? -1 : 1;
                AbstractConfigurableBlockEntity entity = ConfigPanel.this.menu.getBlockEntity();
                BlockPos pos = entity.getBlockPos();
                InputOutputSettings ioSettings = entity.getResourceHandler(ConfigPanel.this.selectedResource).getIOSettings(this.side);
                Byte newOrdinal = (byte) ((ioSettings.getOutputAutomatedSide().ordinal()+delta) % Direction.values().length);
                if (newOrdinal < 0) newOrdinal = 5; // No negative values allowed
                Direction newValue = Direction.values()[newOrdinal];
                ChannelRegistration.MCULIB_CHANNEL.sendToServer(new SideConfig(pos, this.side, ConfigPanel.this.selectedResource, false, ioSettings.getOutputSetting(), newValue, ioSettings.getPriority()));
            }, Component.translatable("side.sneaky.tooltip"));
            setTestValues();
        }

        private void setTestValues() {
            inputModeButton.setResourceLocation(ALLOWED);
            inputSideButton.setMessage(Component.literal(StringUtils.capitalize(side.getName()).substring(0,1)));
            outputModeButton.setResourceLocation(CLOSED);
            outputSideButton.setMessage(Component.literal(StringUtils.capitalize(side.getOpposite().getName()).substring(0,1)));
        }

        public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
            Font font = Minecraft.getInstance().font;
            guiGraphics.drawString(font, "Priority:", ConfigPanel.this.x + 5, ConfigPanel.this.y + 8 + yOffset, 0xff000000);
            this.priorityButton.render(guiGraphics, pMouseX, pMouseY, pPartialTick);
            guiGraphics.drawString(font, "In:", ConfigPanel.this.x + 76, ConfigPanel.this.y + 8 + yOffset, 0xff000000);
            this.inputModeButton.render(guiGraphics, pMouseX, pMouseY, pPartialTick);
            this.inputSideButton.render(guiGraphics, pMouseX, pMouseY, pPartialTick);
            guiGraphics.drawString(font, "Out:", ConfigPanel.this.x + 124, ConfigPanel.this.y + 8 + yOffset, 0xff000000);
            this.outputModeButton.render(guiGraphics, pMouseX, pMouseY, pPartialTick);
            this.outputSideButton.render(guiGraphics, pMouseX, pMouseY, pPartialTick);
        }

        public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
            if (this.priorityButton.mouseClicked(pMouseX,pMouseY,pButton) ||
                    this.inputModeButton.mouseClicked(pMouseX,pMouseY,pButton) ||
                    this.inputSideButton.mouseClicked(pMouseX,pMouseY,pButton) ||
                    this.outputModeButton.mouseClicked(pMouseX,pMouseY,pButton) ||
                    this.outputSideButton.mouseClicked(pMouseX,pMouseY,pButton)
            )
                return true;

            return false;
        }

        public void updateIOSettings(InputOutputSettings ioSettings) {
            inputModeButton.setResourceLocation(switch (ioSettings.getInputSetting()) {
                default -> CLOSED;
                case PASSIVE -> ALLOWED;
                case AUTOMATED -> AUTOMATED;
            });
            inputModeButton.setTooltip(switch (ioSettings.getInputSetting()) {
                default -> Component.translatable("side.closed.tooltip");
                case PASSIVE -> Component.translatable("side.passive.tooltip");
                case AUTOMATED -> Component.translatable("side.automated.tooltip");
            });
            outputModeButton.setResourceLocation(switch (ioSettings.getOutputSetting()) {
                default -> CLOSED;
                case PASSIVE -> ALLOWED;
                case AUTOMATED -> AUTOMATED;
            });
            outputModeButton.setTooltip(switch (ioSettings.getOutputSetting()) {
                default -> Component.translatable("side.closed.tooltip");
                case PASSIVE -> Component.translatable("side.passive.tooltip");
                case AUTOMATED -> Component.translatable("side.automated.tooltip");
            });
            priorityButton.setMessage(Component.literal(ioSettings.getPriority().toString()));
            inputSideButton.setMessage(Component.literal(StringUtils.capitalize(ioSettings.getInputAutomatedSide().getName()).substring(0,1)));
            outputSideButton.setMessage(Component.literal(StringUtils.capitalize(ioSettings.getOutputAutomatedSide().getName()).substring(0,1)));
        }

        public Direction getSide() {
            return this.side;
        }

        public void renderTooltips(GuiGraphics guiGraphics, int pMouseX, int pMouseY) {
            if (priorityButton.isHoveredOrFocused()) priorityButton.renderToolTip(guiGraphics, pMouseX, pMouseY);
            if (inputModeButton.isHoveredOrFocused()) inputModeButton.renderToolTip(guiGraphics, pMouseX, pMouseY);
            if (inputSideButton.isHoveredOrFocused()) inputSideButton.renderToolTip(guiGraphics, pMouseX, pMouseY);
            if (outputModeButton.isHoveredOrFocused()) outputModeButton.renderToolTip(guiGraphics, pMouseX, pMouseY);
            if (outputSideButton.isHoveredOrFocused()) outputSideButton.renderToolTip(guiGraphics, pMouseX, pMouseY);
        }
    }
}
