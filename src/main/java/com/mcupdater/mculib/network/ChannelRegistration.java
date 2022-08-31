package com.mcupdater.mculib.network;

import com.mcupdater.mculib.MCULib;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ChannelRegistration {
    private static final String PROTOCOL = "1";
    public static SimpleChannel MCULIB_CHANNEL;
    public static int SIDE_UPDATE = 0;
    public static int XP_EXTRACT = 1;

    public static void init() {
        MCULIB_CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation(MCULib.MODID,"network"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

        MCULIB_CHANNEL.registerMessage(SIDE_UPDATE,
                SideConfigUpdatePacket.class,
                SideConfigUpdatePacket::toBytes,
                SideConfigUpdatePacket::fromBytes,
                SideConfigUpdatePacket::handle
        );

        MCULIB_CHANNEL.registerMessage(XP_EXTRACT,
                XpExtractPacket.class,
                XpExtractPacket::toBytes,
                XpExtractPacket::fromBytes,
                XpExtractPacket::handle
        );
    }
}
