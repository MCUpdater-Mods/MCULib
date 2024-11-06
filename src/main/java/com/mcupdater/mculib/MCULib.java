package com.mcupdater.mculib;

import com.mcupdater.mculib.setup.Config;
import com.mcupdater.mculib.setup.ModSetup;
import com.mcupdater.mculib.setup.Registration;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod("mculib")
public class MCULib
{
    public static final String MODID = "mculib";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MCULib(IEventBus modEventBus, ModContainer modContainer) {

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.COMMON_CONFIG);

        Registration.init(modEventBus);
        modEventBus.addListener(ModSetup::init);
    }
}
