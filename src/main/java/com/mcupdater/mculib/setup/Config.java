package com.mcupdater.mculib.setup;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    public static ForgeConfigSpec COMMON_CONFIG;
    public static final String CATEGORY_GENERAL = "general";
    public static final ForgeConfigSpec.BooleanValue OVERDRIVE_ENABLED;

    static {
        ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
        COMMON_BUILDER.comment("General settings").push(CATEGORY_GENERAL);
        OVERDRIVE_ENABLED = COMMON_BUILDER.comment("Allow machines to use more power when power storage is more filled").define("overdrive",false);

        COMMON_CONFIG = COMMON_BUILDER.build();
    }
}
