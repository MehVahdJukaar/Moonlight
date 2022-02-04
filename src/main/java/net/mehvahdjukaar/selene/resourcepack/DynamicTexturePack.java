package net.mehvahdjukaar.selene.resourcepack;

import com.google.gson.JsonElement;
import com.mojang.blaze3d.platform.NativeImage;
import net.mehvahdjukaar.selene.resourcepack.RPUtils.LangBuilder;
import net.mehvahdjukaar.selene.resourcepack.RPUtils.ResType;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

public class DynamicTexturePack extends DynamicResourcePack {

    public DynamicTexturePack(ResourceLocation name) {
        super(name, PackType.CLIENT_RESOURCES);
    }

    /**
     * Needs to be called to register the pack. Call from forge event
     *
     * @param clientConstructEvent FMLConstructModEvent event
     */
    public void register(FMLConstructModEvent clientConstructEvent) {
        PackRepository packs = Minecraft.getInstance().getResourcePackRepository();
        this.addPackToRepository(packs);
    }

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
