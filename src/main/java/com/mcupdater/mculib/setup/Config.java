package com.mcupdater.mculib.setup;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    public static ModConfigSpec COMMON_CONFIG;
    public static final String CATEGORY_GENERAL = "general";
    public static final ModConfigSpec.BooleanValue OVERDRIVE_ENABLED;
    public static final ModConfigSpec.IntValue SLOTS_PER_TICK;
    public static final ModConfigSpec.BooleanValue DEBUG;

    static {
        ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();
        COMMON_BUILDER.comment("General settings").push(CATEGORY_GENERAL);
        OVERDRIVE_ENABLED = COMMON_BUILDER.comment("Allow machines to use more power when power storage is more filled").define("overdrive",false);
        SLOTS_PER_TICK = COMMON_BUILDER.comment("Maximum slots per tick for item inventories (High values will impact performance)").defineInRange("slots_per_tick", 5, 1, Integer.MAX_VALUE);
        COMMON_BUILDER.pop();
        DEBUG = COMMON_BUILDER.comment("Enable debug logging").define("debug", false);
        COMMON_CONFIG = COMMON_BUILDER.build();
    }
}
