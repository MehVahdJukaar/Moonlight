package net.mehvahdjukaar.moonlight.api.resources.pack;

import com.google.common.base.Stopwatch;
import net.mehvahdjukaar.moonlight.api.events.EarlyPackReloadEvent;
import net.mehvahdjukaar.moonlight.api.events.MoonlightEventsHelper;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.api.resources.ResType;
import net.mehvahdjukaar.moonlight.api.resources.StaticResource;
import net.mehvahdjukaar.moonlight.api.resources.textures.TextureImage;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.misc.VanillaResourceManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class DynResourceGenerator<T extends DynamicResourcePack> implements PreparableReloadListener {

    public final T dynamicPack;
    private boolean hasBeenInitialized;

    //creates this object and registers it
    protected DynResourceGenerator(T pack) {
        this.dynamicPack = pack;
    }

    /**
     * Called on Mod Init
     */
    public void register() {
        dynamicPack.registerPack();

        MoonlightEventsHelper.addListener(this::onEarlyReload, EarlyPackReloadEvent.class);
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
    public final CompletableFuture<Void> reload(PreparationBarrier stage, ResourceManager manager,
                                                ProfilerFiller workerProfiler, ProfilerFiller mainProfiler,
                                                Executor workerExecutor, Executor mainExecutor) {
        //not used anymore. Loading early instead
        if (Moonlight.HAS_BEEN_INIT && PlatformHelper.isModLoadingValid()) { //fail safe since some mods for some god damn reason run a reload event before blocks are registered...
            onNormalReload(manager);
        } else {
            Moonlight.LOGGER.error("Cowardly refusing generate assets for a broken mod state");
        }

        return CompletableFuture.supplyAsync(() -> null, workerExecutor)
                .thenCompose(stage::wait)
                .thenAcceptAsync((noResult) -> {
                }, mainExecutor);
    }

    protected void onNormalReload(ResourceManager manager) {
    }

    protected void onEarlyReload(EarlyPackReloadEvent event) {
        if (event.type() == dynamicPack.packType) {
            try {
                this.reloadResources(event.manager());
            } catch (Exception e) {
                Moonlight.LOGGER.error("An error occurred while trying to generate dynamic assets for {}:", this.dynamicPack, e);
            }
        }
    }

    protected final void reloadResources(ResourceManager manager) {
        Stopwatch watch = Stopwatch.createStarted();

        boolean resourcePackSupport = this.dependsOnLoadedPacks();

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
        getLogger().info("Generated runtime {} for pack {} in: {} ms" +
                        (this.dynamicPack.generateDebugResources ? " (debug resource dump on)" : ""),
                this.dynamicPack.getPackType(),
                this.dynamicPack.packId(),
                watch.elapsed().toMillis());
    }

    @Nullable
    protected abstract PackRepository getRepository();

    public boolean alreadyHasAssetAtLocation(ResourceManager manager, ResourceLocation res, ResType type) {
        return alreadyHasAssetAtLocation(manager, type.getPath(res));
    }

    public boolean alreadyHasAssetAtLocation(ResourceManager manager, ResourceLocation res) {
        var resource = manager.getResource(res);
        return resource.filter(value -> !value.sourcePackId().equals(this.dynamicPack.packId())).isPresent();
    }

    /**
     * This is a handy method for dynamic resource pack since it allows to specify the name of an existing resource
     * that will then be copied and modified replacing a certain keyword in it with another.
     * This is useful when adding new woodtypes as one can simply manually add a default wood json and provide the method with the
     * default woodtype name and the target name
     * The target location will the one of this pack while its path will be the original one modified following the same principle as the json itself
     *
     * @param resource    target resource that will be copied, modified and saved back
     * @param keyword     keyword to replace
     * @param replaceWith word to replace the keyword with
     */
    public void addSimilarJsonResource(ResourceManager manager, StaticResource resource, String keyword, String replaceWith) throws NoSuchElementException {
        addSimilarJsonResource(manager, resource, s -> s.replace(keyword, replaceWith));
    }

    public void addSimilarJsonResource(ResourceManager manager, StaticResource resource, Function<String, String> textTransform) throws NoSuchElementException {
        addSimilarJsonResource(manager, resource, textTransform, textTransform);
    }

    public void addSimilarJsonResource(ResourceManager manager, StaticResource resource, Function<String, String> textTransform, Function<String, String> pathTransform) throws NoSuchElementException {
        ResourceLocation fullPath = resource.location;

        //calculates new path
        StringBuilder builder = new StringBuilder();
        String[] partial = fullPath.getPath().split("/");
        for (int i = 0; i < partial.length; i++) {
            if (i != 0) builder.append("/");
            if (i == partial.length - 1) {
                builder.append(pathTransform.apply(partial[i]));
            } else builder.append(partial[i]);
        }
        //adds modified under my namespace
        ResourceLocation newRes = new ResourceLocation(this.dynamicPack.resourcePackName.getNamespace(), builder.toString());
        if (!alreadyHasAssetAtLocation(manager, newRes)) {

            String fullText = new String(resource.data, StandardCharsets.UTF_8);


            fullText = textTransform.apply(fullText);

            this.dynamicPack.addBytes(newRes, fullText.getBytes());
        }
    }

    public void addResourceIfNotPresent(ResourceManager manager, StaticResource resource) {
        if (!alreadyHasAssetAtLocation(manager, resource.location)) {
            this.dynamicPack.addResource(resource);
        }
    }

}
