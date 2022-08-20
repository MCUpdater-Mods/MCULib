package com.mcupdater.mculib;

import com.mcupdater.mculib.setup.Config;
import com.mcupdater.mculib.setup.ModSetup;
import com.mcupdater.mculib.setup.Registration;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod("mculib")
public class MCULib
{
    public static final String MODID = "mculib";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MCULib() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_CONFIG);

        Registration.init();
        Registration.register(eventBus);

        eventBus.addListener(ModSetup::init);
    }
}
