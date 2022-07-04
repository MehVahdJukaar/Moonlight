package net.mehvahdjukaar.moonlight.resources.pack;

import com.google.common.base.Stopwatch;
import net.mehvahdjukaar.moonlight.resources.ResType;
import net.mehvahdjukaar.moonlight.resources.VanillaResourceManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public abstract class DynResourceProvider<T extends DynamicResourcePack> implements PreparableReloadListener {

    public final T dynamicPack;
    private boolean hasBeenInitialized;

    //creates this object and registers it
    protected DynResourceProvider(T pack) {
        this.dynamicPack = pack;
    }

    /**
     * Called on Mod Init
     */
    public void register() {
        dynamicPack.registerPack();
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
        var resource = manager.getResource(fullRes);
        return resource.filter(value -> !value.sourcePackId().equals(this.dynamicPack.getName())).isPresent();
    }

}
