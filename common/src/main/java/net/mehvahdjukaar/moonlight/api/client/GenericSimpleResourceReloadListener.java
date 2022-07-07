package net.mehvahdjukaar.moonlight.api.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public abstract class GenericSimpleResourceReloadListener implements PreparableReloadListener {
    private final String pathSuffix;
    private final int suffixLength;
    private final String directory;

    public GenericSimpleResourceReloadListener(String path, String suffix) {
        this.directory = path;
        this.pathSuffix = suffix;
        this.suffixLength = pathSuffix.length();
    }

    @Override
    final public CompletableFuture<Void> reload(PreparationBarrier stage, ResourceManager manager,
                                                ProfilerFiller workerProfiler, ProfilerFiller mainProfiler,
                                                Executor workerExecutor, Executor mainExecutor) {
        var list = prepare(manager, mainProfiler);
        this.apply(list, manager, workerProfiler);

        return CompletableFuture.supplyAsync(() -> null, workerExecutor)
                .thenCompose(stage::wait)
                .thenAcceptAsync((noResult) -> {
                }, mainExecutor);
    }

    protected List<ResourceLocation> prepare(ResourceManager manager, ProfilerFiller profilerFiller) {
        List<ResourceLocation> list = new ArrayList<>();
        int i = this.directory.length() + 1;

        for (Map.Entry<ResourceLocation, Resource> entry : manager.listResources(this.directory,
                (l) -> l.getPath().endsWith(pathSuffix)).entrySet()) {
            ResourceLocation resourcelocation = entry.getKey();
            String s = resourcelocation.getPath();
            list.add(new ResourceLocation(resourcelocation.getNamespace(), s.substring(i, s.length() - suffixLength)));
        }

        return list;
    }

    public abstract void apply(List<ResourceLocation> locations, ResourceManager manager, ProfilerFiller filler);
}
