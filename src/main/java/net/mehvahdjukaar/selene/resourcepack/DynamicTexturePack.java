package net.mehvahdjukaar.selene.resourcepack;

import com.google.gson.JsonElement;
import com.mojang.blaze3d.platform.NativeImage;
import net.mehvahdjukaar.selene.resourcepack.asset_generators.LangBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.resource.PathResourcePack;
import net.minecraftforge.resource.ResourcePackLoader;

import java.io.IOException;

public class DynamicTexturePack extends DynamicResourcePack {

    public DynamicTexturePack(ResourceLocation name, PackType type, Pack.Position position, boolean fixed, boolean hidden) {
        super(name, PackType.CLIENT_RESOURCES, position, fixed, hidden);
    }

    public DynamicTexturePack(ResourceLocation name) {
        super(name, PackType.CLIENT_RESOURCES);
    }

    void addPackLogo() {
        ModList.get().getModContainerById(this.mainNamespace).ifPresent(m -> {

            IModInfo mod = m.getModInfo();

            mod.getLogoFile().ifPresent(logo -> {
                final PathResourcePack resourcePack = ResourcePackLoader.getPackFor(mod.getModId())
                        .orElse(ResourcePackLoader.getPackFor("forge").
                                orElseThrow(() -> new RuntimeException("Can't find forge, WHAT!")));
                try {
                    var b = resourcePack.getRootResource(logo).readAllBytes();
                    this.addRootResource("pack.png", b);
                } catch (IOException ignored) {
                }
            });
        });
    }

    protected void addImage(ResourceLocation path, NativeImage image, ResType resType) {
        try (image) {
            this.addBytes(path, image.asByteArray(), resType);
        } catch (Exception e) {
            LOGGER.warn("Failed to add image {} to resource pack {}.", path, this.resourcePackName, e);
        }
    }

    /**
     * Adds a new textures and closes the passed native image
     */
    public void addTexture(ResourceLocation textureLocation, NativeImage image) {
        this.addImage(textureLocation, image, ResType.TEXTURES);
    }

    public void addBlockModel(ResourceLocation modelLocation, JsonElement model) {
        this.addJson(modelLocation, model, ResType.BLOCK_MODELS);
    }

    public void addItemModel(ResourceLocation modelLocation, JsonElement model) {
        this.addJson(modelLocation, model, ResType.ITEM_MODELS);
    }

    public void addBlockState(ResourceLocation modelLocation, JsonElement model) {
        this.addJson(modelLocation, model, ResType.BLOCKSTATES);
    }

    public void addLang(ResourceLocation langName, JsonElement language) {
        this.addJson(langName, language, ResType.LANG);
    }

    public void addLang(ResourceLocation langName, LangBuilder builder) {
        this.addJson(langName, builder.build(), ResType.LANG);
    }
}
