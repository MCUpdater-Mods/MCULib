package com.mcupdater.mculib.network;

import com.mcupdater.mculib.block.AbstractConfigurableBlockEntity;
import com.mcupdater.mculib.inventory.SideSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SideConfigUpdatePacket {
    private BlockPos blockPos;
    private Direction side;
    private String resourceType;
    private boolean inbound;
    private SideSetting setting;
    private Direction sneakySide;
    private Byte priority;

    public SideConfigUpdatePacket(BlockPos blockPos, Direction side, String resourceType, boolean inbound, SideSetting setting, Direction sneakySide, Byte priority) {
        this.blockPos = blockPos;
        this.side = side;
        this.resourceType = resourceType;
        this.inbound = inbound;
        this.setting = setting;
        this.sneakySide = sneakySide;
        this.priority = priority;
    }

    public static void toBytes(SideConfigUpdatePacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.blockPos);
        buf.writeByte(msg.side.ordinal());
        buf.writeUtf(msg.resourceType, 20);
        buf.writeBoolean(msg.inbound);
        buf.writeByte(msg.setting.ordinal());
        buf.writeByte(msg.sneakySide.ordinal());
        buf.writeByte(msg.priority);
    }

    public static SideConfigUpdatePacket fromBytes(FriendlyByteBuf buf) {
        return new SideConfigUpdatePacket(buf.readBlockPos(), Direction.values()[buf.readByte()], buf.readUtf(20), buf.readBoolean(), SideSetting.values()[buf.readByte()], Direction.values()[buf.readByte()], buf.readByte());
    }

    public static void handle(SideConfigUpdatePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getSender() != null) {
                if (ctx.get().getSender().getLevel().getBlockEntity(msg.blockPos) instanceof AbstractConfigurableBlockEntity configurableBlockEntity) {
                    configurableBlockEntity.updateSideConfig(msg.resourceType, msg.side, msg.inbound, msg.setting, msg.sneakySide, msg.priority);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
