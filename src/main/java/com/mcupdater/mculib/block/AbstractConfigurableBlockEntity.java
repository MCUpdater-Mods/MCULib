package com.mcupdater.mculib.block;

import com.mcupdater.mculib.capabilities.AbstractResourceHandler;
import com.mcupdater.mculib.capabilities.EnergyResourceHandler;
import com.mcupdater.mculib.capabilities.ItemResourceHandler;
import com.mcupdater.mculib.inventory.InputOutputSettings;
import com.mcupdater.mculib.inventory.SideSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractConfigurableBlockEntity extends BlockEntity implements Nameable, MenuProvider {
    protected Component name;
    protected Map<String, AbstractResourceHandler> configMap = new HashMap<>();

    public AbstractConfigurableBlockEntity(BlockEntityType<?> pType, BlockPos pWorldPosition, BlockState pBlockState) {
        super(pType, pWorldPosition, pBlockState);

    }

    @Override
    public Component getName() {
        return this.hasCustomName() ? this.getCustomName() : this.getDefaultName();
    }

    @Override
    public Component getDisplayName() {
        return this.getName();
    }

    protected abstract Component getDefaultName();

    public void setCustomName(Component hoverName) {
        this.name = hoverName;
    }

    @Nullable
    @Override
    public Component getCustomName() {
        return this.name;
    }

    public void updateSideConfig(String resourceType, Direction side, boolean inbound, SideSetting sideSetting, Direction sneakySide) {
        AbstractResourceHandler handler = this.configMap.get(resourceType);
        if (handler != null) {
            InputOutputSettings ioSettings = handler.getIOSettings(side);
            if (inbound) {
                ioSettings.setInputSetting(sideSetting);
                ioSettings.setInputAutomatedSide(sneakySide);
            } else {
                ioSettings.setOutputSetting(sideSetting);
                ioSettings.setOutputAutomatedSide(sneakySide);
            }
            handler.updateIOSettings(side, ioSettings);
            this.configMap.put(resourceType, handler);
            this.setChanged();
            this.notifyClients();
        }
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        if (pTag.contains("CustomName", 8)) {
            this.name = Component.Serializer.fromJson(pTag.getString("CustomName"));
        }
        if (pTag.contains("SideConfigs")) {
            CompoundTag configs = pTag.getCompound("SideConfigs");
            if (configs.contains("power")) {
                CompoundTag power = configs.getCompound("power");
                AbstractResourceHandler handler = this.configMap.get("power");
                handler.load(power);
            }
            if (configs.contains("items")) {
                CompoundTag items = configs.getCompound("items");
                AbstractResourceHandler handler = this.configMap.get("items");
                handler.load(items);
            }
            if (configs.contains("fluids")) {
                CompoundTag fluids = configs.getCompound("fluids");
                AbstractResourceHandler handler = this.configMap.get("fluids");
                handler.load(fluids);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        if (this.name != null) {
            pTag.putString("CustomName", Component.Serializer.toJson(this.name));
        }
        CompoundTag configs = new CompoundTag();
        for (Map.Entry<String,AbstractResourceHandler> entry : this.configMap.entrySet()) {
            String type = entry.getKey();
            AbstractResourceHandler handler = entry.getValue();
            CompoundTag configType = new CompoundTag();
            handler.save(configType);
            configs.put(type,configType);
        }
        pTag.put("SideConfigs", configs);
        super.saveAdditional(pTag);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    public void notifyClients() {
        if (this.level != null && !this.level.isClientSide) {
            BlockState blockState = this.level.getBlockState(this.worldPosition);
            this.level.sendBlockUpdated(this.worldPosition, blockState, blockState, 3);
        }
    }

    protected void tick() {
        for (AbstractResourceHandler handler : this.configMap.values()) {
            if (handler.tickHandler(this.level, this.worldPosition)) {
                this.setChanged();
                this.notifyClients();
            }
        }
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        for (AbstractResourceHandler handler : this.configMap.values()) {
            if (handler.getCapability(cap, side).isPresent()) {
                return handler.getCapability(cap, side);
            }
        }
        return super.getCapability(cap, side);
    }

    public AbstractResourceHandler getResourceHandler(String resourceType) {
        return this.configMap.get(resourceType);
    }

    public EnergyResourceHandler getEnergyStorage() {
        if (this.configMap.get("power") != null)
            return (EnergyResourceHandler) this.configMap.get("power");

        return null;
    }

    public Container getInventory() {
        if (this.configMap.get("items") != null)
            return (ItemResourceHandler) this.configMap.get("items");

        return null;
    }

    public IFluidHandler getFluidHandler() {
        if (this.configMap.get("fluids") != null)
            return this.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).orElse(null);

        return null;
    }
}
