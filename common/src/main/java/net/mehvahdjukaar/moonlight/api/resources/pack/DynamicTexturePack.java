package net.mehvahdjukaar.moonlight.api.resources.pack;

import com.google.gson.JsonElement;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.resources.ResType;
import net.mehvahdjukaar.moonlight.api.resources.assets.LangBuilder;
import net.mehvahdjukaar.moonlight.api.resources.textures.TextureImage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;

import java.nio.file.Files;
import java.nio.file.Path;

public class DynamicTexturePack extends DynamicResourcePack {

    //TODO: make static constructor and private this. merge packs here instead
    public DynamicTexturePack(ResourceLocation name, Pack.Position position, boolean fixed, boolean hidden) {
        super(name, PackType.CLIENT_RESOURCES, position, fixed, hidden);
    }

    public DynamicTexturePack(ResourceLocation name) {
        super(name, PackType.CLIENT_RESOURCES);
    }

    void addPackLogo() {
        Path logoPath = ClientHelper.getModIcon(this.mainNamespace);
        if (logoPath != null) {
            try {
                this.addRootResource("pack.png", Files.readAllBytes(logoPath));
            } catch (Exception ignored) {
            }
        }
    }

    @Deprecated(forRemoval = true)
    public void addAndCloseTexture(ResourceLocation path, TextureImage image) {
        addAndCloseTexture(path, image, true);
    }

    /**
     * Adds a new textures and closes the passed native image
     * Last boolean is for textures that aren't stitched so won't be cleared automatically after stitching
     * Use it for textures such as entity textures of GUI
     */
    @Deprecated(forRemoval = true)
    public void addAndCloseTexture(ResourceLocation path, TextureImage image, boolean isOnAtlas) {
        try (image) {
            this.addBytes(path, image.getImage().asByteArray(), ResType.TEXTURES);
            if (!isOnAtlas) this.markNotClearable(ResType.TEXTURES.getPath(path));
            if (image.getMcMeta() != null){
                this.addJson(path, image.getMcMeta().toJson(), ResType.MCMETA);
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to add image {} to resource pack {}.", path, this, e);
        }
    }

    @Deprecated(forRemoval = true)
    public void addBlockModel(ResourceLocation modelLocation, JsonElement model) {
        this.addJson(modelLocation, model, ResType.BLOCK_MODELS);
    }

    @Deprecated(forRemoval = true)
    public void addItemModel(ResourceLocation modelLocation, JsonElement model) {
        this.addJson(modelLocation, model, ResType.ITEM_MODELS);
    }

    @Deprecated(forRemoval = true)
    public void addBlockState(ResourceLocation modelLocation, JsonElement model) {
        this.addJson(modelLocation, model, ResType.BLOCKSTATES);
    }

    @Deprecated(forRemoval = true)
    public void addLang(ResourceLocation langName, JsonElement language) {
        this.addJson(langName, language, ResType.LANG);
    }

    @Deprecated(forRemoval = true)
    public void addLang(ResourceLocation langName, LangBuilder builder) {
        this.addJson(langName, builder.build(), ResType.LANG);
    }
}
