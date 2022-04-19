package net.mehvahdjukaar.selene.resourcepack;

import com.mojang.blaze3d.platform.NativeImage;
import net.mehvahdjukaar.selene.resourcepack.asset_generators.textures.Respriter;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.eventbus.api.IEventBus;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * Class responsible to generate assets and manage your dynamic resource texture pack (client)
 * Handles and registers your dynamic pack instance which needs to be provides
 */
public abstract class RPAwareDynamicTextureProvider extends RPAwareDynamicResourceProvider {

    protected RPAwareDynamicTextureProvider(DynamicTexturePack pack) {
        super(pack);
    }

    /**
     * Remember to call this during mod init
     */
    @Override
    public void register(IEventBus bus){
        super.register(bus);
        ((ReloadableResourceManager) Minecraft.getInstance().getResourceManager())
                .registerReloadListener(this);
    }

    protected boolean alreadyHasTextureAtLocation(ResourceManager manager, ResourceLocation res) {
        return alreadyHasAssetAtLocation(manager, res, ResType.TEXTURES);
    }

}
