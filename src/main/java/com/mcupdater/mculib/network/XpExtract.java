package com.mcupdater.mculib.network;

import com.mcupdater.mculib.MCULib;
import com.mcupdater.mculib.block.AbstractMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.ServerPayloadContext;

public record XpExtract(BlockPos blockPos) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<XpExtract> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MCULib.MODID, "xp_extract"));

    public static final StreamCodec<FriendlyByteBuf, XpExtract> STREAM_CODEC = StreamCodec.of(
            XpExtract::encode,
            XpExtract::decode
    );

    private static XpExtract decode(FriendlyByteBuf byteBuf) {
        return new XpExtract(
                byteBuf.readBlockPos()
        );
    }

    private static void encode(FriendlyByteBuf byteBuf, XpExtract xpExtract) {
        byteBuf.writeBlockPos(xpExtract.blockPos);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class PayloadHandler {
        public static void handle(final XpExtract xpExtract, final IPayloadContext context) {
            if (context instanceof ServerPayloadContext serverPayloadContext) {
                ServerPlayer player = serverPayloadContext.player();
                if (player.level().getBlockEntity(xpExtract.blockPos) instanceof AbstractMachineBlockEntity machineBlockEntity) {
                    int xp = machineBlockEntity.extractExperience();
                    ExperienceOrb.award(player.serverLevel(), player.position(), xp);
                }
            }
        }
    }
}
