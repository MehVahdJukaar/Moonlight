package net.mehvahdjukaar.selene.resourcepack;

import com.mojang.blaze3d.platform.NativeImage;
import net.mehvahdjukaar.selene.resourcepack.asset_generators.textures.Respriter;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.loading.FMLLoader;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * Class responsible to generate assets and manage your dynamic resource texture pack (client)
 * Handles and registers your dynamic pack instance which needs to be provides
 */
public abstract class RPAwareDynamicTextureProvider extends RPAwareDynamicResourceProvider<DynamicTexturePack> {

    protected RPAwareDynamicTextureProvider(DynamicTexturePack pack) {
        super(pack);
    }

    /**
     * Remember to call this during mod init
     */
    @Override
    public void register(IEventBus bus){
        super.register(bus);
        //run data fuckery could give a null minecraft here...
        if(!FMLLoader.getLaunchHandler().isData()) {
            Minecraft mc = Minecraft.getInstance();
            ((ReloadableResourceManager) mc.getResourceManager())
                    .registerReloadListener(this);
        }
    }

    @Override
    protected PackRepository getRepository() {
        return Minecraft.getInstance().getResourcePackRepository();
    }

    protected boolean alreadyHasTextureAtLocation(ResourceManager manager, ResourceLocation res) {
        return alreadyHasAssetAtLocation(manager, res, ResType.TEXTURES);
    }

}
