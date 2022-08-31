package com.mcupdater.mculib.network;

import com.mcupdater.mculib.block.AbstractMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class XpExtractPacket {
    private BlockPos blockPos;

    public XpExtractPacket(BlockPos blockPos) {
        this.blockPos = blockPos;
    }
    public static void toBytes(XpExtractPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.blockPos);
    }

    public static XpExtractPacket fromBytes(FriendlyByteBuf buf) {
        return new XpExtractPacket(buf.readBlockPos());
    }

    public static void handle(XpExtractPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getSender() != null) {
                if (ctx.get().getSender().getLevel().getBlockEntity(msg.blockPos) instanceof AbstractMachineBlockEntity machineBlockEntity) {
                    int xp = machineBlockEntity.extractExperience();
                    ExperienceOrb.award(ctx.get().getSender().getLevel(), ctx.get().getSender().position(), xp);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
