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

public abstract class RPAwareDynamicResourceProvider<T extends DynamicResourcePack> implements PreparableReloadListener {

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

    public T getPack() {
        return dynamicPack;
    }

    public abstract boolean dependsOnLoadedPacks();

    public abstract void regenerateDynamicAssets(ResourceManager manager);

    public void generateStaticAssetsOnStartup(ResourceManager manager) {
    }

    @Override
    final public CompletableFuture<Void> reload(PreparationBarrier stage, ResourceManager manager,
                                                ProfilerFiller workerProfiler, ProfilerFiller mainProfiler,
                                                Executor workerExecutor, Executor mainExecutor) {
        reloadResources(manager);

        return CompletableFuture.supplyAsync(() -> null, workerExecutor)
                .thenCompose(stage::wait)
                .thenAcceptAsync((noResult) -> {
                }, mainExecutor);
    }

    protected void reloadResources(ResourceManager manager) {
        Stopwatch watch = Stopwatch.createStarted();

        boolean resourcePackSupport = this.dependsOnLoadedPacks();
        //TODO: cleanup this logic
        if (!this.hasBeenInitialized) {
            this.hasBeenInitialized = true;
            generateStaticAssetsOnStartup(manager);
            if (this.dynamicPack instanceof DynamicTexturePack tp) tp.addPackLogo();
            if (!resourcePackSupport) {
                var pack = this.getRepository();
                if (pack != null) {
                    VanillaResourceManager vanillaManager = new VanillaResourceManager(pack);
                    this.regenerateDynamicAssets(vanillaManager);
                    vanillaManager.close();
                } else {
                    this.regenerateDynamicAssets(manager);
                }
            }
        }

        //generate textures
        if (resourcePackSupport) {
            this.regenerateDynamicAssets(manager);
        }

        getLogger().info("Generated runtime {} for pack {} in: {} ms",
                this.dynamicPack.getPackType(),
                this.dynamicPack.getName(),
                watch.elapsed().toMillis());
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

}
