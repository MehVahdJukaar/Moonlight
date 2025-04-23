package net.mehvahdjukaar.moonlight.core;

import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.moonlight.api.platform.configs.ModConfigHolder;

import java.util.function.Supplier;

public class CommonConfigs {

    public static final Supplier<Boolean> CLEAR_RESOURCES;

    public static final ModConfigHolder CONFIG;

    static {
        ConfigBuilder builder = ConfigBuilder.create(Moonlight.MOD_ID, ConfigType.COMMON_SYNCED);
        builder.push("general");
        CLEAR_RESOURCES = builder.comment("Clears dynamic models and textures from the mod dynamic pack once resource reload is done. This can save up some RAM. Turning off if you notice inconsistencies with pack loading")
                .define("clear_dynamic_resources", false);
        builder.pop();

        CONFIG = builder.build();
    }

    public static void init() {
    }
}
