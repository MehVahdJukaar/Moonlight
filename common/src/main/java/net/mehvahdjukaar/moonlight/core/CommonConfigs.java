package net.mehvahdjukaar.moonlight.core;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.moonlight.api.platform.configs.ModConfigHolder;

import java.util.function.Supplier;

public class CommonConfigs {

    public static final Supplier<Boolean> EXTRA_DEBUG;
    public static final Supplier<Boolean> EXTRA_CHILDREN_DEBUG;
    public static final Supplier<String> GLOBAL_DATAPACKS_DIR;
    public static final Supplier<Boolean> FASTER_CACHE_SEARCH;
    public static final Supplier<Boolean> MULTI_THREADED_GENERATION;

    public static final ModConfigHolder CONFIG;

    static {
        ConfigBuilder builder = ConfigBuilder.create(Moonlight.MOD_ID, ConfigType.COMMON_SYNCED);
        builder.push("general");
        MULTI_THREADED_GENERATION = builder.comment("Enables multi-threaded generation for dynamic assets (if supported). This could improve performance on systems with more cores available.")
                .define("multi_threaded_generation", true);
        EXTRA_DEBUG = builder.comment("ONLY for debugging purpose. Turns one some debug functionality like more logging or blocktypes_debug.txt, the file can be found in ~/.minecraft/debug/dynamic_registry_dump...")
                .define("extra_debug", false);
        EXTRA_CHILDREN_DEBUG = builder.comment("Enable this will list each BlockTypes' Children. The List of BlockTypes' children will be also in the same file via EXTRA_DEBUG. NOTE: To enable this, EXTRA_DEBUG must be enabled, too.")
                .define("extra_children_debug", false);
        FASTER_CACHE_SEARCH = builder.comment("Makes the dynamic assets cache use a tree structure for indexing, drastically speeds up query time but could cost some ram.")
                .define("faster_cache_search", true);
        GLOBAL_DATAPACKS_DIR = builder.comment("Global datapack folder. A folder where you can store and load datapacks for all your worlds automatically. Set to empty string to disable")
                .worldReload()
                .define("global_datapacks_folder", "moonlight-global-datapacks");


        if(PlatHelper.isDev()){
            builder.push("test_category");
            builder.comment("category comment");

            builder
                    .comment("test comment 1")
                    .define("test", false);
            builder
                    .comment("test comment 2 ")
                    .define("test", 2.0, 0, 22);
            builder.pop();
        }

        builder.pop();

        CONFIG = builder.build();
        CONFIG.forceLoad();
    }

    public static void init() {
    }
}
