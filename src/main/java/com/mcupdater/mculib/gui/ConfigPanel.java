package com.mcupdater.mculib.gui;

import com.mcupdater.mculib.MCULib;
import com.mcupdater.mculib.block.AbstractConfigurableBlockEntity;
import com.mcupdater.mculib.block.IConfigurableMenu;
import com.mcupdater.mculib.inventory.InputOutputSettings;
import com.mcupdater.mculib.inventory.SideSetting;
import com.mcupdater.mculib.network.ChannelRegistration;
import com.mcupdater.mculib.network.SideConfigUpdatePacket;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConfigPanel extends AbstractContainerWidget {

    private static final int COLOR_BACKGROUND = 0xffc6c6c6;
    private final Font font;
    private final IConfigurableMenu menu;

    // Icon definitions
    protected final ResourceLocation CLOSED = new ResourceLocation(MCULib.MODID, "textures/gui/icon/prohibition.png");
    protected final ResourceLocation ALLOWED = new ResourceLocation(MCULib.MODID, "textures/gui/icon/arrow.png");
    protected final ResourceLocation AUTOMATED = new ResourceLocation(MCULib.MODID, "textures/gui/icon/gear-arrow.png");
    protected final ResourceLocation ITEMS = new ResourceLocation(MCULib.MODID, "textures/gui/icon/box.png");
    protected final ResourceLocation ENERGY = new ResourceLocation(MCULib.MODID, "textures/gui/icon/lightning.png");
    protected final ResourceLocation FLUIDS = new ResourceLocation(MCULib.MODID, "textures/gui/icon/flask.png");
    TabWidget itemsTab;
    TabWidget energyTab;
    TabWidget fluidsTab;
    String selectedResource;
    private List<TabWidget> tabs = new ArrayList<>();
    private List<SideButtonGroup> buttons = new ArrayList<>();

    public ConfigPanel(IConfigurableMenu srcMenu, int leftPos, int topPos, int width, int height) {
        super(leftPos, topPos, width, height, COLOR_BACKGROUND);
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
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);

        if (this.isVisible()) {
            AbstractConfigurableBlockEntity blockEntity = this.menu.getBlockEntity();
            for (SideButtonGroup group : this.buttons) {
                group.updateIOSettings(blockEntity.getResourceHandler(this.selectedResource).getIOSettings(group.getSide()));
                group.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
            }
            // Render tabs in reverse order to ensure tooltips render properly
            List<TabWidget> reverseTabs = new ArrayList<>(this.tabs);
            Collections.reverse(reverseTabs);
            for (TabWidget tab : reverseTabs) {
                tab.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
            }

            Level level = blockEntity.getLevel();
            BlockPos blockPos = blockEntity.getBlockPos();
            BlockEntity tempEntity = level.getBlockEntity(blockPos.below());
            int yOffset = 0;
            font.draw(pPoseStack, Component.literal("D: ").append(menu.getSideName(Direction.DOWN)), this.x + 5, this.y + 4 + yOffset, 0xff000000);
            if (tempEntity != null)
                renderCaps(pPoseStack, yOffset, tempEntity);
            yOffset += 10;
            /*
            if (tempEntity != null && tempEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent()) {
                RenderSystem.setShaderTexture(0, ITEMS);
                blit(pPoseStack, this.x + 5, this.y + 5 + yOffset, 10, 10, 0f, 0f, 16, 16, 16, 16);
                this.font.draw(pPoseStack, Component.literal("Items"), this.x + 21, this.y + 5 + yOffset, 0xff000000);
            }
            */
            tempEntity = level.getBlockEntity(blockPos.above());
            yOffset += 16;
            font.draw(pPoseStack, Component.literal("U: ").append(menu.getSideName(Direction.UP)), this.x + 5, this.y + 4 + yOffset, 0xff000000);
            if (tempEntity != null)
                renderCaps(pPoseStack, yOffset, tempEntity);
            yOffset += 10;
            tempEntity = level.getBlockEntity(blockPos.north());
            yOffset += 16;
            font.draw(pPoseStack, Component.literal("N: ").append(menu.getSideName(Direction.NORTH)), this.x + 5, this.y + 4 + yOffset, 0xff000000);
            if (tempEntity != null)
                renderCaps(pPoseStack, yOffset, tempEntity);
            yOffset += 10;
            tempEntity = level.getBlockEntity(blockPos.south());
            yOffset += 16;
            font.draw(pPoseStack, Component.literal("S: ").append(menu.getSideName(Direction.SOUTH)), this.x + 5, this.y + 4 + yOffset, 0xff000000);
            if (tempEntity != null)
                renderCaps(pPoseStack, yOffset, tempEntity);
            yOffset += 10;
            tempEntity = level.getBlockEntity(blockPos.west());
            yOffset += 16;
            font.draw(pPoseStack, Component.literal("W: ").append(menu.getSideName(Direction.WEST)), this.x + 5, this.y + 4 + yOffset, 0xff000000);
            if (tempEntity != null)
                renderCaps(pPoseStack, yOffset, tempEntity);
            yOffset += 10;
            tempEntity = level.getBlockEntity(blockPos.east());
            yOffset += 16;
            font.draw(pPoseStack, Component.literal("E: ").append(menu.getSideName(Direction.EAST)), this.x + 5, this.y + 4 + yOffset, 0xff000000);
            if (tempEntity != null)
                renderCaps(pPoseStack, yOffset, tempEntity);
            yOffset += 10;
            this.renderTooltips(pPoseStack, pMouseX, pMouseY);
        }
    }

    private void renderTooltips(PoseStack pPoseStack, int pMouseX, int pMouseY) {
       for (SideButtonGroup group : this.buttons) {
           group.renderTooltips(pPoseStack, pMouseX, pMouseY);
       }
    }

    private void renderCaps(PoseStack pPoseStack, int yOffset, BlockEntity tempEntity) {
        if (tempEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent()) {
            RenderSystem.setShaderTexture(0, ITEMS);
            blit(pPoseStack, this.x + this.width - 36, this.y + 3 + yOffset, 10, 10, 0f, 0f, 16, 16, 16, 16);
        }
        if (tempEntity.getCapability(ForgeCapabilities.ENERGY).isPresent()) {
            RenderSystem.setShaderTexture(0, ENERGY);
            blit(pPoseStack, this.x + this.width - 24, this.y + 3 + yOffset, 10, 10, 0f, 0f, 16, 16, 16, 16);
        }
        if (tempEntity.getCapability(ForgeCapabilities.FLUID_HANDLER).isPresent()) {
            RenderSystem.setShaderTexture(0, FLUIDS);
            blit(pPoseStack, this.x + this.width - 12, this.y + 3 + yOffset, 10, 10, 0f, 0f, 16, 16, 16, 16);
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

    public Capability mapCapability() {
        return switch (selectedResource) {
            default -> ForgeCapabilities.ITEM_HANDLER;
            case "power" -> ForgeCapabilities.ENERGY;
            case "fluids" -> ForgeCapabilities.FLUID_HANDLER;
        };
    }

    public class SideButtonGroup {
        private final Direction side;
        private final int yOffset;
        private UpdatableImageButton inputModeButton;
        private UpdatableImageButton outputModeButton;
        private TextButton inputSideButton;
        private TextButton outputSideButton;

        public SideButtonGroup(Direction side, int yOffset) {
            this.side = side;
            this.yOffset = yOffset;
            this.inputModeButton = new UpdatableImageButton(ConfigPanel.this.x + 20, ConfigPanel.this.y + 5 + yOffset, 14, 14, 16, 16, Component.empty(), button -> {
                AbstractConfigurableBlockEntity entity = ConfigPanel.this.menu.getBlockEntity();
                BlockPos pos = entity.getBlockPos();
                InputOutputSettings ioSettings = entity.getResourceHandler(ConfigPanel.this.selectedResource).getIOSettings(this.side);
                SideSetting newValue = SideSetting.values()[(ioSettings.getInputSetting().ordinal()+1) % SideSetting.values().length];
                ChannelRegistration.MCULIB_CHANNEL.sendToServer(new SideConfigUpdatePacket(pos, this.side, ConfigPanel.this.selectedResource, true, newValue, ioSettings.getInputAutomatedSide()));
            });
            this.inputSideButton = new TextButton(ConfigPanel.this.x + 40, ConfigPanel.this.y + 5 + yOffset, 40, 14, Component.empty(), button -> {
                AbstractConfigurableBlockEntity entity = ConfigPanel.this.menu.getBlockEntity();
                BlockPos pos = entity.getBlockPos();
                InputOutputSettings ioSettings = entity.getResourceHandler(ConfigPanel.this.selectedResource).getIOSettings(this.side);
                Direction newValue = Direction.values()[(ioSettings.getInputAutomatedSide().ordinal()+1) % Direction.values().length];
                ChannelRegistration.MCULIB_CHANNEL.sendToServer(new SideConfigUpdatePacket(pos, this.side, ConfigPanel.this.selectedResource, true, ioSettings.getInputSetting(), newValue));
            }, Component.translatable("side.sneaky.tooltip"));
            this.outputModeButton = new UpdatableImageButton(ConfigPanel.this.x + 105, ConfigPanel.this.y + 5 + yOffset, 14, 14, 16, 16, Component.empty(), (button) -> {
                AbstractConfigurableBlockEntity entity = ConfigPanel.this.menu.getBlockEntity();
                BlockPos pos = entity.getBlockPos();
                InputOutputSettings ioSettings = entity.getResourceHandler(ConfigPanel.this.selectedResource).getIOSettings(this.side);
                SideSetting newValue = SideSetting.values()[(ioSettings.getOutputSetting().ordinal()+1) % SideSetting.values().length];
                ChannelRegistration.MCULIB_CHANNEL.sendToServer(new SideConfigUpdatePacket(pos, this.side, ConfigPanel.this.selectedResource, false, newValue, ioSettings.getOutputAutomatedSide()));
            });
            this.outputSideButton = new TextButton(ConfigPanel.this.x + 125, ConfigPanel.this.y + 5 + yOffset, 40, 14, Component.empty(), (button) -> {
                AbstractConfigurableBlockEntity entity = ConfigPanel.this.menu.getBlockEntity();
                BlockPos pos = entity.getBlockPos();
                InputOutputSettings ioSettings = entity.getResourceHandler(ConfigPanel.this.selectedResource).getIOSettings(this.side);
                Direction newValue = Direction.values()[(ioSettings.getOutputAutomatedSide().ordinal()+1) % Direction.values().length];
                ChannelRegistration.MCULIB_CHANNEL.sendToServer(new SideConfigUpdatePacket(pos, this.side, ConfigPanel.this.selectedResource, false, ioSettings.getOutputSetting(), newValue));
            }, Component.translatable("side.sneaky.tooltip"));
            setTestValues();
        }

        private void setTestValues() {
            inputModeButton.setResourceLocation(ALLOWED);
            inputSideButton.setMessage(Component.literal(StringUtils.capitalize(side.getName())));
            outputModeButton.setResourceLocation(CLOSED);
            outputSideButton.setMessage(Component.literal(StringUtils.capitalize(side.getOpposite().getName())));
        }

        public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
            Font font = Minecraft.getInstance().font;
            font.draw(pPoseStack, "In:", ConfigPanel.this.x + 5, ConfigPanel.this.y + 8 + yOffset, 0xff000000);
            this.inputModeButton.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
            this.inputSideButton.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
            font.draw(pPoseStack, "Out:", ConfigPanel.this.x + 85, ConfigPanel.this.y + 8 + yOffset, 0xff000000);
            this.outputModeButton.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
            this.outputSideButton.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        }

        public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
            if (this.inputModeButton.mouseClicked(pMouseX,pMouseY,pButton) ||
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
            inputSideButton.setMessage(Component.literal(StringUtils.capitalize(ioSettings.getInputAutomatedSide().getName())));
            outputSideButton.setMessage(Component.literal(StringUtils.capitalize(ioSettings.getOutputAutomatedSide().getName())));
        }

        public Direction getSide() {
            return this.side;
        }

        public void renderTooltips(PoseStack pPoseStack, int pMouseX, int pMouseY) {
            if (inputModeButton.isHoveredOrFocused()) inputModeButton.renderToolTip(pPoseStack, pMouseX, pMouseY);
            if (inputSideButton.isHoveredOrFocused()) inputSideButton.renderToolTip(pPoseStack, pMouseX, pMouseY);
            if (outputModeButton.isHoveredOrFocused()) outputModeButton.renderToolTip(pPoseStack, pMouseX, pMouseY);
            if (outputSideButton.isHoveredOrFocused()) outputSideButton.renderToolTip(pPoseStack, pMouseX, pMouseY);
        }
    }
}
