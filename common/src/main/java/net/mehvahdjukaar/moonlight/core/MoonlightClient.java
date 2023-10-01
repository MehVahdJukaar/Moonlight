package net.mehvahdjukaar.moonlight.core;

import net.mehvahdjukaar.moonlight.api.map.client.MapDecorationClientManager;
import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicResourcePack;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicTexturePack;
import net.mehvahdjukaar.moonlight.core.client.SoftFluidParticleColors;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;

import java.util.Set;

@ApiStatus.Internal
public class MoonlightClient {

    private static final ThreadLocal<Boolean> MAP_MIPMAP = ThreadLocal.withInitial(() -> false);
    private static MergedDynamicTexturePack mergedDynamicPack;

    public static void initClient() {
        ClientHelper.addClientReloadListener(SoftFluidParticleColors::new, Moonlight.res("soft_fluids"));
        ClientHelper.addClientReloadListener(MapDecorationClientManager::new, Moonlight.res("map_markers"));
        ClientConfigs.init();
    }


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
        if (ClientConfigs.MAPS_MIPMAP.get() == 0) {
            b = false;
        }
        MAP_MIPMAP.set(b);
    }

    public static boolean isMapMipMap() {
        return MAP_MIPMAP.get();
    }

}
