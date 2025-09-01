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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

/**
 * Class responsible to generate assets and manage your dynamic resource texture pack (client)
 * Handles and registers your dynamic pack instance, which needs to be provided
 */

@Deprecated(forRemoval = true)
public abstract class DynClientResourcesGenerator extends DynResourceGenerator<DynamicTexturePack> {
    protected DynClientResourcesGenerator(DynamicTexturePack pack) {
        super(pack, pack.mainNamespace);

        if (PlatHelper.getPhysicalSide().isServer()) {
            throw new IllegalStateException("Client only class registered on server side! Issue from mod" + pack.mainNamespace);
        }
        MoonlightEventsHelper.addListener(this::addDynamicTranslations, AfterLanguageLoadEvent.class);
    }

    @Override
    protected PackRepository getRepository() {
        return Minecraft.getInstance().getResourcePackRepository();
    }


    /**
     * Use this method to add language entries that are dynamic and can be created based off existing entries.
     *
     * @param languageEvent object used to access all currently registered language entries for the current lang file and add new ones
     */
    public void addDynamicTranslations(AfterLanguageLoadEvent languageEvent) {
    }


    protected void onFirstReload() {
        Path logoPath = ClientHelper.getModIcon(this.dynamicPack.mainNamespace);
        if (logoPath != null) {
            try {
                this.dynamicPack.addRootResource("pack.png", Files.readAllBytes(logoPath));
            } catch (Exception ignored) {
            }
        }
    }


    @Deprecated(forRemoval = true)
    public boolean alreadyHasTextureAtLocation(ResourceManager manager, ResourceLocation res) {
        return alreadyHasAssetAtLocation(manager, res, ResType.TEXTURES);
    }

    @Deprecated(forRemoval = true)
    public void addTextureIfNotPresent(ResourceManager manager, String relativePath, Supplier<TextureImage> textureSupplier) {
        addTextureIfNotPresent(manager, relativePath, textureSupplier, true);
    }

    @Deprecated(forRemoval = true)
    public void addTextureIfNotPresent(ResourceManager manager, String relativePath, Supplier<TextureImage> textureSupplier, boolean isOnAtlas) {

        ResourceLocation res = relativePath.contains(":") ? ResourceLocation.parse(relativePath) :
                ResourceLocation.fromNamespaceAndPath(this.modId, relativePath);
        if (!alreadyHasTextureAtLocation(manager, res)) {
            try (TextureImage textureImage = textureSupplier.get()) {
                this.dynamicPack.addAndCloseTexture(res, textureImage, isOnAtlas);
            } catch (Exception e) {
                getLogger().error("Failed to generate texture {}: {}", res, e);
                if (PlatHelper.isDev()) throw new AssertionError(e);
            }
        }
    }

}
