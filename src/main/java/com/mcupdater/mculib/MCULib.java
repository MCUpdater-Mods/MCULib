package com.mcupdater.mculib;

import com.mcupdater.mculib.setup.ModSetup;
import com.mcupdater.mculib.setup.Registration;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod("mculib")
public class MCULib
{
    public static final String MODID = "mculib";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MCULib() {
        Registration.init();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ModSetup::init);
    }
}
