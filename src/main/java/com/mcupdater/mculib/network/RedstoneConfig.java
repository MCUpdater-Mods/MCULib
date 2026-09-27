package com.mcupdater.mculib.network;

import com.mcupdater.mculib.MCULib;
import com.mcupdater.mculib.block.AbstractConfigurableBlockEntity;
import com.mcupdater.mculib.redstone.ComparatorBehavior;
import com.mcupdater.mculib.redstone.SignalBehavior;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.ServerPayloadContext;

public record RedstoneConfig(BlockPos blockPos, SignalBehavior signalBehavior, ComparatorBehavior comparatorBehavior) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RedstoneConfig> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MCULib.MODID, "redstone_config"));

    public static final StreamCodec<FriendlyByteBuf, RedstoneConfig> STREAM_CODEC = StreamCodec.of(
            RedstoneConfig::encode,
            RedstoneConfig::decode
    );

    private static RedstoneConfig decode(FriendlyByteBuf byteBuf) {
        return new RedstoneConfig(
                byteBuf.readBlockPos(),
                SignalBehavior.values()[byteBuf.readByte()],
                new ComparatorBehavior(byteBuf.readUtf(), byteBuf.readBoolean())
        );
    }

    private static void encode(FriendlyByteBuf byteBuf, RedstoneConfig config) {
        byteBuf.writeBlockPos(config.blockPos);
        byteBuf.writeByte(config.signalBehavior().ordinal());
        byteBuf.writeUtf(config.comparatorBehavior.resourceType());
        byteBuf.writeBoolean(config.comparatorBehavior.inverted());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static class PayloadHandler {
        public static void handle(final RedstoneConfig redstoneConfig, final IPayloadContext context) {
            if (context instanceof ServerPayloadContext serverPayloadContext) {
                MCULib.LOGGER.info("RedstoneConfig: {}, {}, {}",redstoneConfig.signalBehavior.name(), redstoneConfig.comparatorBehavior.resourceType(), redstoneConfig.comparatorBehavior.inverted());
                ServerPlayer player = serverPayloadContext.player();
                if (player.getServer() != null) {
                    if (player.level().getBlockEntity(redstoneConfig.blockPos) instanceof AbstractConfigurableBlockEntity configurableBlockEntity) {
                        configurableBlockEntity.updateRedstoneConfig(redstoneConfig.signalBehavior, redstoneConfig.comparatorBehavior);
                    }
                }
            }
        }
    }
}
