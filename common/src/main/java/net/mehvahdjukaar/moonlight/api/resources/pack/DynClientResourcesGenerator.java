package net.mehvahdjukaar.moonlight.api.resources.pack;

import net.mehvahdjukaar.moonlight.api.events.AfterLanguageLoadEvent;
import net.mehvahdjukaar.moonlight.api.events.MoonlightEventsHelper;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.resources.ResType;
import net.mehvahdjukaar.moonlight.api.resources.textures.TextureImage;
import net.mehvahdjukaar.moonlight.core.MoonlightClient;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.function.Supplier;

/**
 * Class responsible to generate assets and manage your dynamic resource texture pack (client)
 * Handles and registers your dynamic pack instance, which needs to be provided
 */
public abstract class DynClientResourcesGenerator extends DynResourceGenerator<DynamicTexturePack> {
    protected DynClientResourcesGenerator(DynamicTexturePack pack) {
        super(MoonlightClient.maybeMergePack(pack), pack.mainNamespace);

        //run data could give a null minecraft here...
        if (!PlatHelper.isData()) {
            //unused now...
            ClientHelper.addClientReloadListener(() -> this,
                    new ResourceLocation(this.modId, "dyn_resources_generator_" + index++));
        }
        MoonlightEventsHelper.addListener(this::addDynamicTranslations, AfterLanguageLoadEvent.class);
    }

    //hack for fabric id
    private static Integer index = 0;

    @Override
    protected PackRepository getRepository() {
        return Minecraft.getInstance().getResourcePackRepository();
    }

    public boolean alreadyHasTextureAtLocation(ResourceManager manager, ResourceLocation res) {
        return alreadyHasAssetAtLocation(manager, res, ResType.TEXTURES);
    }

    public void addTextureIfNotPresent(ResourceManager manager, String relativePath, Supplier<TextureImage> textureSupplier) {
        addTextureIfNotPresent(manager, relativePath, textureSupplier, true);
    }

    public void addTextureIfNotPresent(ResourceManager manager, String relativePath, Supplier<TextureImage> textureSupplier, boolean isOnAtlas) {

        ResourceLocation res = relativePath.contains(":") ? new ResourceLocation(relativePath) :
                new ResourceLocation(this.modId, relativePath);
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
                this.dynamicPack.addAndCloseTexture(res, textureImage, isOnAtlas);
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
