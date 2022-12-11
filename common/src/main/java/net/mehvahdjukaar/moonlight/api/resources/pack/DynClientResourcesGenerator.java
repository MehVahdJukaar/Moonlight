package net.mehvahdjukaar.moonlight.api.resources.pack;

import net.mehvahdjukaar.moonlight.api.events.AfterLanguageLoadEvent;
import net.mehvahdjukaar.moonlight.api.platform.ClientPlatformHelper;
import net.mehvahdjukaar.moonlight.api.resources.textures.TextureImage;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.api.events.MoonlightEventsHelper;
import net.mehvahdjukaar.moonlight.api.resources.ResType;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.function.Supplier;

/**
 * Class responsible to generate assets and manage your dynamic resource texture pack (client)
 * Handles and registers your dynamic pack instance which needs to be provides
 */
public abstract class DynClientResourcesGenerator extends DynResourceGenerator<DynamicTexturePack> {

    protected DynClientResourcesGenerator(DynamicTexturePack pack) {
        super(pack);
    }

    /**
     * Remember to call this during mod init
     */
    @Override
    public void register() {
        super.register();
        //run data could give a null minecraft here...
        if (!PlatformHelper.isData()) {
            //unused now...
            ClientPlatformHelper.addClientReloadListener(this, this.dynamicPack.resourcePackName);
        }
        MoonlightEventsHelper.addListener(this::addDynamicTranslations, AfterLanguageLoadEvent.class);
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
