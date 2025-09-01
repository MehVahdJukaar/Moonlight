package net.mehvahdjukaar.moonlight.core;

import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.moonlight.api.platform.configs.ModConfigHolder;

import java.util.function.Supplier;

public class CommonConfigs {

    public static final Supplier<Boolean> EXTRA_DEBUG;

    public static final ModConfigHolder CONFIG;

    static {
        ConfigBuilder builder = ConfigBuilder.create(Moonlight.MOD_ID, ConfigType.COMMON_SYNCED);
        builder.push("general");
        EXTRA_DEBUG = builder.comment("ONLY for debugging purpose. Turns one some debug functionality like more logging or blocktypes_debug.txt, the file can be found in ~/.minecraft/debug/dynamic_registry_dump...")
                .define("extra_debug", false);
        builder.pop();

        CONFIG = builder.build();
        CONFIG.forceLoad();
    }

    public static void init() {
    }
}
