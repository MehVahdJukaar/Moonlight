package net.mehvahdjukaar.moonlight.core.client;

import com.google.gson.JsonObject;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SpecialModelsFolder {

    private static final String FOLDER = "special_models";
    private static final FileToIdConverter LISTER = FileToIdConverter.json("models/" + FOLDER);

    //called while models load. Cant be a reload listner
    public static List<ResourceLocation> scan() {
        ResourceManager manager = Minecraft.getInstance().getResourceManager();
        List<ResourceLocation> models = new ArrayList<>();
        for (Map.Entry<ResourceLocation, Resource> entry : LISTER.listMatchingResources(manager).entrySet()) {
            ResourceLocation file = entry.getKey();
            try (Reader reader = entry.getValue().openAsReader()) {
                JsonObject json = GsonHelper.parse(reader);
                String mod = GsonHelper.getAsString(json, "required_mod", "");
                if (!mod.isEmpty() && !PlatHelper.isModLoaded(mod)) continue;
            } catch (Exception e) {
                Moonlight.LOGGER.error("Couldn't parse special model file {}:", file, e);
                continue;
            }
            models.add(LISTER.fileToId(file).withPrefix(FOLDER + "/"));
        }
        return models;
    }
}
