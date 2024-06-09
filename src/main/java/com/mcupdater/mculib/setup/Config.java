package com.mcupdater.mculib.setup;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    public static ForgeConfigSpec COMMON_CONFIG;
    public static final String CATEGORY_GENERAL = "general";
    public static final ForgeConfigSpec.BooleanValue OVERDRIVE_ENABLED;
    public static final ForgeConfigSpec.IntValue SLOTS_PER_TICK;

    static {
        ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
        COMMON_BUILDER.comment("General settings").push(CATEGORY_GENERAL);
        OVERDRIVE_ENABLED = COMMON_BUILDER.comment("Allow machines to use more power when power storage is more filled").define("overdrive",false);
        SLOTS_PER_TICK = COMMON_BUILDER.comment("Maximum slots per tick for item inventories (High values will impact performance)").defineInRange("slots_per_tick", 5, 1, Integer.MAX_VALUE);

        COMMON_CONFIG = COMMON_BUILDER.build();
    }
}
