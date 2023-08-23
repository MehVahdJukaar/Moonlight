package net.mehvahdjukaar.moonlight.core;

import net.mehvahdjukaar.moonlight.api.map.client.MapDecorationClientManager;
import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicResourcePack;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicTexturePack;
import net.mehvahdjukaar.moonlight.api.util.DispenserHelper;
import net.mehvahdjukaar.moonlight.core.client.SoftFluidParticleColors;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.Pack;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

@ApiStatus.Internal
public class MoonlightClient {

    public static final Supplier<Boolean> MERGE_PACKS;
    static{
        ConfigBuilder builder = ConfigBuilder.create(Moonlight.MOD_ID, ConfigType.CLIENT);
        MERGE_PACKS = builder.comment("Merge all dynamic resource packs from all mods that use this library into a single pack")
                .define("merge_dynamic_packs", true);
        builder.buildAndRegister();
    }

    public static void initClient() {
        ClientHelper.addClientReloadListener(SoftFluidParticleColors::new, Moonlight.res("soft_fluids"));
        ClientHelper.addClientReloadListener(MapDecorationClientManager::new, Moonlight.res("map_markers"));
    }

    private static DynamicTexturePack mergedDynamicPack;

    public static DynamicTexturePack maybeMergePack(DynamicTexturePack pack){
        if(mergedDynamicPack != null){
            mergedDynamicPack = new DynamicTexturePack(Moonlight.res("mod_dynamic_assets"));
        }
        return mergedDynamicPack;
    }

    @EventCalled
    public static void afterTextureReload() {
        DynamicResourcePack.clearAfterReload(true);
    }



}
