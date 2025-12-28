package net.mehvahdjukaar.moonlight.core.client;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SimpleSpecialModelsLoader extends SimplePreparableReloadListener<Void> {

    private final List<ResourceLocation> specialModels = new ArrayList<>();

    @Override
    protected Void prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        specialModels.clear();
        Gson gson = new Gson();
        String name = "models/special_models";
        FileToIdConverter fileToIdConverter = FileToIdConverter.json(name);
        Map<ResourceLocation, Resource> resourceLocationResourceMap = fileToIdConverter.listMatchingResources(resourceManager);
        List<ResourceLocation> models = new ArrayList<>();
        for (Map.Entry<ResourceLocation, Resource> entry : resourceLocationResourceMap.entrySet()) {
            try {
                Reader reader = entry.getValue().openAsReader();
                JsonElement jsonelement = GsonHelper.fromJson(gson, reader, JsonElement.class);
                if (jsonelement instanceof JsonObject jo) {
                    String mod = GsonHelper.getAsString(jo, "required_mod", "");
                    if (!mod.isEmpty() && !PlatHelper.isModLoaded(mod)) {
                        continue;
                    }
                }
            } catch (Exception e) {
                Moonlight.LOGGER.error("Couldn't parse special model file {}:", entry.getKey(), e);
            }
            models.add(entry.getKey());
        }
        specialModels.addAll(models.stream().map(s -> s.withPath(s.getPath().substring(7, s.getPath().length() - 5))).toList());
        return null;
    }

    @Override
    protected void apply(Void object, ResourceManager resourceManager, ProfilerFiller profiler) {
    }

    public Iterable<ResourceLocation> getSpecialModels() {
        return specialModels;
    }
}
