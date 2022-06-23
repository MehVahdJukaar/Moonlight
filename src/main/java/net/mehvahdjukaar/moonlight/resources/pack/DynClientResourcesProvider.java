package net.mehvahdjukaar.moonlight.resources.pack;

import net.mehvahdjukaar.moonlight.client.language.AfterLanguageLoadEvent;
import net.mehvahdjukaar.moonlight.client.textures.TextureImage;
import net.mehvahdjukaar.moonlight.resources.DynamicLanguageHandler;
import net.mehvahdjukaar.moonlight.resources.ResType;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.loading.FMLLoader;

import java.util.function.Supplier;

/**
 * Class responsible to generate assets and manage your dynamic resource texture pack (client)
 * Handles and registers your dynamic pack instance which needs to be provides
 */
public abstract class DynClientResourcesProvider extends DynResourceProvider<DynamicTexturePack> {

    protected DynClientResourcesProvider(DynamicTexturePack pack) {
        super(pack);
    }

    /**
     * Remember to call this during mod init
     */
    @Override
    public void register(IEventBus bus) {
        super.register(bus);
        //run data could give a null minecraft here...
        if (!FMLLoader.getLaunchHandler().isData()) {
            Minecraft mc = Minecraft.getInstance();
            ((ReloadableResourceManager) mc.getResourceManager())
                    .registerReloadListener(this);
        }
        //MinecraftForge.EVENT_BUS.addListener(this::temp);
        DynamicLanguageHandler.register(this);
    }

    @Override
    protected PackRepository getRepository() {
        return Minecraft.getInstance().getResourcePackRepository();
    }

    public boolean alreadyHasTextureAtLocation(ResourceManager manager, ResourceLocation res) {
        return alreadyHasAssetAtLocation(manager, res, ResType.TEXTURES);
    }


    public void addTextureIfNotPresent(ResourceManager manager, String relativePath, Supplier<TextureImage> textureSupplier) {

        ResourceLocation res = relativePath.contains(":") ? new ResourceLocation(relativePath) :
                new ResourceLocation(this.dynamicPack.mainNamespace, relativePath);
        if (!alreadyHasTextureAtLocation(manager, res)) {
            TextureImage textureImage = null;
            try {
                textureImage = textureSupplier.get();
            } catch (Exception e) {
                getLogger().error("Failed to generate texture {}: {}", res, e);
            }
            if (textureImage == null) {
                getLogger().warn("Could not generate texture {}", res);
            } else {
                this.dynamicPack.addAndCloseTexture(res, textureImage);
            }
        }
    }

    /**
     * Use this method to add language entries that are dynamic and can be created based off existing entries.
     *
     * @param languageEvent object used to access all currently registered language entries for the current lang file and add new ones
     */
    public void addDynamicTranslations(AfterLanguageLoadEvent languageEvent) {
    }

}
