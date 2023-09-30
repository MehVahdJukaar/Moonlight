package net.mehvahdjukaar.moonlight.core;

import net.mehvahdjukaar.moonlight.api.map.client.MapDecorationClientManager;
import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicResourcePack;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicTexturePack;
import net.mehvahdjukaar.moonlight.core.client.SoftFluidParticleColors;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;

import java.util.Set;
import java.util.function.Supplier;

@ApiStatus.Internal
public class MoonlightClient {

    public static final Supplier<Boolean> MERGE_PACKS;
    public static final Supplier<Boolean> LAZY_MAP_DATA;
    public static final Supplier<Integer> MAPS_MIPMAP;
    private static final ThreadLocal<Boolean> MAP_MIPMAP = ThreadLocal.withInitial(() -> false);

    static {
        ConfigBuilder builder = ConfigBuilder.create(Moonlight.MOD_ID, ConfigType.CLIENT);
        MERGE_PACKS = builder.comment("Merge all dynamic resource packs from all mods that use this library into a single pack")
                .define("merge_dynamic_packs", true);
        LAZY_MAP_DATA = builder.comment("Prevents map texture from being upladed to GPU when only map markers have changed." +
                        "Could increase performance")
                .define("lazy_map_upload", true);
        MAPS_MIPMAP = builder.comment("Renders map textures using mipmap. Vastly improves look from afar as well when inside a Map Atlas from Map Atlases or similar. Set to 0 to have no mipmap like vanilla")
                .define("maps_mipmap", 4, 0, 4);
        builder.buildAndRegister().loadFromFile();
    }

    public static void initClient() {
        ClientHelper.addClientReloadListener(SoftFluidParticleColors::new, Moonlight.res("soft_fluids"));
        ClientHelper.addClientReloadListener(MapDecorationClientManager::new, Moonlight.res("map_markers"));
    }

    private static MergedDynamicTexturePack mergedDynamicPack;

    public static DynamicTexturePack maybeMergePack(DynamicTexturePack pack) {
        if (mergedDynamicPack == null) {
            mergedDynamicPack = new MergedDynamicTexturePack() {

            };
        }
        Set<String> nameSpaces = pack.getNamespaces(pack.getPackType());
        for (var n : nameSpaces) mergedDynamicPack.addNamespaces(n);
        mergedDynamicPack.mods++;
        return mergedDynamicPack;
    }

    private static class MergedDynamicTexturePack extends DynamicTexturePack {
        int mods = 0;

        public MergedDynamicTexturePack() {
            super(Moonlight.res("mods_dynamic_assets"));
        }

        @Override
        public Component makeDescription() {
            return Component.literal("Dynamic resources for " + mods + (mods == 1 ? " mod" : " mods"));
        }
    }

    @EventCalled
    public static void afterTextureReload() {
        DynamicResourcePack.clearAfterReload(true);
    }

    public static void setMipMap(boolean b) {
        if (MAPS_MIPMAP.get() == 0) {
            b = false;
        }
        MAP_MIPMAP.set(b);
    }

    public static boolean isMapMipMap() {
        return MAP_MIPMAP.get();
    }

}
