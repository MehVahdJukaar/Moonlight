package net.mehvahdjukaar.moonlight.core;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.moonlight.api.platform.configs.ModConfigHolder;
import net.minecraft.world.item.TooltipFlag;

import java.util.function.Supplier;

public class ClientConfigs {

    public static final Supplier<Boolean> MERGE_PACKS;
    public static final Supplier<Boolean> LAZY_MAP_DATA;
    public static final Supplier<Integer> MAPS_MIPMAP;
    public static final Supplier<ShadeFix> FIX_SHADE;
    public static final Supplier<TooltipMode> TAGS_TOOLTIP;
    public static final Supplier<Boolean> CUSTOM_CONFIG_SCREEN;
    public static final Supplier<Boolean> SHOW_ALL_MOD_CONFIGS;
    public static final Supplier<ForeignConfigMode> CONVERT_FOREIGN_CONFIGS;
    public static final Supplier<Boolean> CONFIG_ITEM_CAROUSEL;

    public static final ModConfigHolder CONFIG;

    static {
        ConfigBuilder builder = ConfigBuilder.create(Moonlight.MOD_ID, ConfigType.CLIENT);
        builder.push("dynamic_pack");
        MERGE_PACKS = builder.comment("Merge all dynamic resource packs from all mods that use this library into a single pack")
                .define("merge_dynamic_resource_packs", true);
        builder.pop();

        builder.push("general");
        LAZY_MAP_DATA = builder.comment("Prevents map texture from being upladed to GPU when only map markers have changed." +
                        "Could increase performance")
                .define("lazy_map_upload", true);
        MAPS_MIPMAP = builder.comment("Renders map textures using mipmap. Vastly improves look from afar as well when inside a Map Atlas from Map Atlases or similar. Set to 0 to have no mipmap like vanilla")
                .define("maps_mipmap", 3, 0, 4);
        FIX_SHADE = builder.comment("Fix minecraft entity shading to be exactly the same that blocks use. (1 for up,0.8 for north, 0.6 for west and 0.5 for down)." +
                        "This means that if you have a model and render it with a tile renderer or entity it will appear identical as one rendered via baked models." +
                        "Using no gui will prevent it from changing item rendered in GUIs, in case you dont like that look." +
                        "Note there is a known compat issue with Figura mod. Keep this True or False with that one")
                .define("consistent_entity_renderer_shading", ShadeFix.NO_GUI);

        TAGS_TOOLTIP = builder.comment("Show Item and Block tags on item tooltip")
                .define("tags_tooltips", PlatHelper.isDev() ? TooltipMode.ON : TooltipMode.OFF);

        builder.pop();

        builder.push("config_screen");
        CUSTOM_CONFIG_SCREEN = builder.comment("Use Moonlight's config screen. When off, configs open the loader's screen instead: NeoForge's own (or Configured). Fabric has no such screen, so there configs can't be edited in game")
                .define("custom_config_screen", true);
        SHOW_ALL_MOD_CONFIGS = builder.comment("Give every installed mod with a config screen a tile, not just the ones using Moonlight. Their own screen opens when clicked")
                .define("show_all_mod_configs", false);
        CONVERT_FOREIGN_CONFIGS = builder.comment("Draw other mods' configs inside Moonlight's screen instead of theirs (NeoForge only). GENERIC_ONLY covers just the mods that never wrote a screen of their own, ALWAYS covers every mod. Per world server configs are only shown while a world is open. Best effort: options we can't show are left as they are")
                .define("convert_foreign_configs", ForeignConfigMode.GENERIC_ONLY);
        CONFIG_ITEM_CAROUSEL = builder.comment("Show a slowly panning strip of a mod's items on its config screen")
                .define("config_item_carousel", true);
        builder.pop();
        CONFIG = builder.build();
        CONFIG.forceLoad();
    }

    public static void init() {
    }

    public enum ForeignConfigMode {
        NEVER,
        GENERIC_ONLY,
        ALWAYS;

        public boolean isOn() {
            return this != NEVER;
        }
    }

    public enum ShadeFix {
        FALSE,
        NO_GUI,
        TRUE
    }

    public enum TooltipMode {
        OFF, ON, ADVANCED_ONLY;

        public boolean isOn(TooltipFlag flag) {
            return switch (this) {
                case ON -> true;
                case ADVANCED_ONLY -> flag.isAdvanced();
                default -> false;
            };
        }
    }
}
