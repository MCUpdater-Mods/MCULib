package com.mcupdater.mculib.network;

import com.mcupdater.mculib.MCULib;
import com.mcupdater.mculib.block.AbstractConfigurableBlockEntity;
import com.mcupdater.mculib.inventory.SideSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.ServerPayloadContext;

public record SideConfig(BlockPos blockPos, Direction side, String resourceType, boolean inbound, SideSetting setting, Direction sneakySide, Byte priority) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SideConfig> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MCULib.MODID, "side_update"));

    public static final StreamCodec<FriendlyByteBuf, SideConfig> STREAM_CODEC = StreamCodec.of(
            SideConfig::encode,
            SideConfig::decode
    );

    private static SideConfig decode(FriendlyByteBuf byteBuf) {
        return new SideConfig(
                byteBuf.readBlockPos(),
                Direction.values()[byteBuf.readByte()],
                byteBuf.readUtf(20),
                byteBuf.readBoolean(),
                SideSetting.values()[byteBuf.readByte()],
                Direction.values()[byteBuf.readByte()],
                byteBuf.readByte()
        );
    }

    private static void encode(FriendlyByteBuf byteBuf, SideConfig sideConfig) {
        byteBuf.writeBlockPos(sideConfig.blockPos);
        byteBuf.writeByte(sideConfig.side.ordinal());
        byteBuf.writeUtf(sideConfig.resourceType, 20);
        byteBuf.writeBoolean(sideConfig.inbound);
        byteBuf.writeByte(sideConfig.setting.ordinal());
        byteBuf.writeByte(sideConfig.sneakySide.ordinal());
        byteBuf.writeByte(sideConfig.priority);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class PayloadHandler {
        public static void handle(final SideConfig sideConfig, final IPayloadContext context) {
            if (context instanceof ServerPayloadContext serverPayloadContext) {
                ServerPlayer player = serverPayloadContext.player();
                if (player.getServer() != null) {
                    if (player.level().getBlockEntity(sideConfig.blockPos) instanceof AbstractConfigurableBlockEntity configurableBlockEntity) {
                        configurableBlockEntity.updateSideConfig(sideConfig.resourceType, sideConfig.side, sideConfig.inbound, sideConfig.setting, sideConfig.sneakySide, sideConfig.priority);
                    }
                }
            }
        }
    }
}
