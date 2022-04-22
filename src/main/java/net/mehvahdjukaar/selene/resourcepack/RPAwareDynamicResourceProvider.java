package net.mehvahdjukaar.selene.resourcepack;

import com.google.common.base.Stopwatch;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.eventbus.api.IEventBus;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

abstract class RPAwareDynamicResourceProvider<T extends DynamicResourcePack> implements PreparableReloadListener {

    public final T dynamicPack;
    private boolean hasBeenInitialized;

    //creates this object and registers it
    protected RPAwareDynamicResourceProvider(T pack) {
        this.dynamicPack = pack;
    }

    public void register(IEventBus bus) {
        dynamicPack.registerPack(bus);
    }

    public abstract Logger getLogger();

    public abstract boolean dependsOnLoadedPacks();

    public abstract void regenerateDynamicAssets(ResourceManager manager);

    public void generateStaticAssetsOnStartup(ResourceManager manager) {
    }

    @Override
    final public CompletableFuture<Void> reload(PreparationBarrier stage, ResourceManager manager,
                                                ProfilerFiller workerProfiler, ProfilerFiller mainProfiler,
                                                Executor workerExecutor, Executor mainExecutor) {
        Stopwatch watch = Stopwatch.createStarted();

        boolean resourcePackSupport = this.dependsOnLoadedPacks();
        //TODO: cleanup this logic
        if (!this.hasBeenInitialized) {
            this.hasBeenInitialized = true;
            generateStaticAssetsOnStartup(manager);
            if (!resourcePackSupport) {
                var pack = this.getRepository();
                if(pack != null) {
                    VanillaResourceManager vanillaManager = new VanillaResourceManager(pack);
                    this.regenerateDynamicAssets(vanillaManager);
                    vanillaManager.close();
                }
                else{
                    this.regenerateDynamicAssets(manager);
                }
            }
        }

        //generate textures
        if (resourcePackSupport) {
            this.regenerateDynamicAssets(manager);
        }

        getLogger().info("Generated runtime resources for pack {} in: {} ms",
                this.dynamicPack.getName(),
                watch.elapsed().toMillis());

        return CompletableFuture.supplyAsync(() -> null, workerExecutor)
                .thenCompose(stage::wait)
                .thenAcceptAsync((noResult) -> {
                }, mainExecutor);
    }

    @Nullable
    protected abstract PackRepository getRepository();

    public boolean alreadyHasAssetAtLocation(ResourceManager manager, ResourceLocation res, ResType type) {
        ResourceLocation fullRes = type.getPath(res);
        if (manager.hasResource(fullRes)) {
            try (var r = manager.getResource(fullRes)) {
                return !r.getSourceName().equals(this.dynamicPack.getName());
            } catch (IOException ignored) {
            }
        }
        return false;
    }

    @Nullable
    public StaticResource getResOrLog(ResourceManager manager, ResourceLocation location) {
        try {
            return new StaticResource(manager.getResource(location));
        } catch (Exception var4) {
            this.getLogger().error("Could not find resource {} while generating dynamic resource pack", location);
            return null;
        }
    }
}
