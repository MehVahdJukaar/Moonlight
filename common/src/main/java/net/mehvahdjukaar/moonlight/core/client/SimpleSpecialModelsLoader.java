package net.mehvahdjukaar.moonlight.core.client;


import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.ArrayList;
import java.util.List;

public class SimpleSpecialModelsLoader extends SimplePreparableReloadListener<Void> {

    private final List<ResourceLocation> specialModels = new ArrayList<>();

    @Override
    protected Void prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        specialModels.clear();
        String name = "models/special_models";
        FileToIdConverter fileToIdConverter = FileToIdConverter.json(name);
        specialModels.addAll(fileToIdConverter.listMatchingResources(resourceManager).keySet().stream()
                .map(s -> s.withPath(s.getPath().substring(7, s.getPath().length() - 5))).toList());
        return null;
    }

    @Override
    protected void apply(Void object, ResourceManager resourceManager, ProfilerFiller profiler) {
    }

    public Iterable<ResourceLocation> getSpecialModels() {
        return specialModels;
    }
}
