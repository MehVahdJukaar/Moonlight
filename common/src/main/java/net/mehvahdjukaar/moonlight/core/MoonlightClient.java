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

    static {
        ConfigBuilder builder = ConfigBuilder.create(Moonlight.MOD_ID, ConfigType.CLIENT);
        MERGE_PACKS = builder.comment("Merge all dynamic resource packs from all mods that use this library into a single pack")
                .define("merge_dynamic_packs", true);
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


}
