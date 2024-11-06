package com.mcupdater.mculib.network;

import com.mcupdater.mculib.MCULib;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ChannelRegistration {

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(MCULib.MODID).versioned("1.0");
                registrar.playToServer(SideConfig.TYPE, SideConfig.STREAM_CODEC, SideConfig.PayloadHandler::handle);
                registrar.playToServer(XpExtract.TYPE, XpExtract.STREAM_CODEC, XpExtract.PayloadHandler::handle);
    }
}
