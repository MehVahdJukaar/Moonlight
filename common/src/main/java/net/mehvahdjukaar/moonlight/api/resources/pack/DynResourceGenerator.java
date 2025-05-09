package net.mehvahdjukaar.moonlight.api.resources.pack;

import com.google.common.base.Stopwatch;
import net.mehvahdjukaar.moonlight.api.events.EarlyPackReloadEvent;
import net.mehvahdjukaar.moonlight.api.events.MoonlightEventsHelper;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.resources.ResType;
import net.mehvahdjukaar.moonlight.api.resources.SimpleTagBuilder;
import net.mehvahdjukaar.moonlight.api.resources.StaticResource;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.misc.FilteredResManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class DynResourceGenerator<T extends DynamicResourcePack> implements PreparableReloadListener {

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();


    public final T dynamicPack;
    protected final String modId;
    private boolean hasBeenInitialized;

    //creates this object and registers it
    protected DynResourceGenerator(T pack, String modId) {
        this.dynamicPack = pack;
        this.modId = modId;
        this.dynamicPack.registerPack();

        GENERATORS.add(this);
    }

    /**
     * Called on Mod Init
     * Yes this just loads the class
     */
    public final void register() {
    }

    public abstract Logger getLogger();

    public T getPack() {
        return dynamicPack;
    }

    /**
     * @return if this pack assets can depend on other loaded resource packs, aka we need to access some textures that aren't strictly the vanilla ones
     */
    public abstract boolean dependsOnLoadedPacks();

    @Deprecated(forRemoval = true) //just deprecated as it shouldnt be overritten aymore and will become final private
    public void regenerateDynamicAssets(ResourceManager manager) {
        var tasks = new ArrayList<ResourceGenTask>();
        regenerateDynamicAssets(tasks::add);

        Stopwatch watch = Stopwatch.createStarted();

        List<CompletableFuture<ResourceSink>> futures = tasks.stream()
                .map(task -> CompletableFuture.supplyAsync(() -> {
                    var localSink = createLocalSink();
                    task.accept(manager, localSink);
                    return localSink;
                }, getExecutors()))
                .toList();

        // Proper join using CompletableFuture
        CompletableFuture<Void> allDone = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        try {
            allDone.join(); // joins all futures
            getLogger().info("Tasks finished in: {} ms", watch.elapsed().toMillis());
            addAllResourceSinks(futures.stream().map(CompletableFuture::join).toList());

        } catch (Exception e) {
            throw new RuntimeException("Task failed", e);
        }

        getLogger().info("Generated runtime {} for pack {} ({}) in: {} ms{} (multithreaded)",
                this.dynamicPack.getPackType(), this.dynamicPack.packId(), this.modId,
                watch.elapsed().toMillis(),
                this.dynamicPack.generateDebugResources ? " (debug resource dump on)" : "");
    }

    protected void addAllResourceSinks(List<ResourceSink> sinks) {
        Map<TagKey<?>, SimpleTagBuilder> allTags = new HashMap<>();
        for (ResourceSink sink : sinks) {
            sink.resources.forEach(this.dynamicPack::addBytes);
            sink.notClearable.forEach(this.dynamicPack::markNotClearable);
            for (var e : sink.tags.entrySet()) {
                allTags.merge(e.getKey(),  e.getValue(), SimpleTagBuilder::merge);
            }
        }

        //adds tags
        for (var e : allTags.entrySet()) {
            this.dynamicPack.addTag(e.getValue(), e.getKey().registry());
        }
    }

    //override if you really need to
    protected @NotNull ResourceSink createLocalSink() {
        return new ResourceSink(this.modId, this.dynamicPack.packId());
    }

    protected @NotNull ExecutorService getExecutors() {
        return EXECUTOR_SERVICE;
    }

    public void regenerateDynamicAssets(Consumer<ResourceGenTask> executor) {
        //implement this for multi thead
    }

    @Override
    public final @NotNull CompletableFuture<Void> reload(PreparationBarrier stage, ResourceManager manager,
                                                         ProfilerFiller workerProfiler, ProfilerFiller mainProfiler,
                                                         Executor workerExecutor, Executor mainExecutor) {
        //not used anymore. Loading early instead
        if (Moonlight.HAS_BEEN_INIT && PlatHelper.isModLoadingValid()) { //fail safe since some mods for some god damn reason run a reload event before blocks are registered...
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

    protected final void onEarlyReload(EarlyPackReloadEvent event) {
        if (event.type() == dynamicPack.packType) {
            try {
                this.reloadResources(event.manager());
            } catch (Exception e) {
                Moonlight.LOGGER.error("An error occurred while trying to generate dynamic assets for {}:", this.dynamicPack, e);
            }
        }
    }

    protected final void reloadResources(ResourceManager manager) {
        boolean resourcePackSupport = this.dependsOnLoadedPacks();
        //first clear all pack content if it should be cleared

        if (!this.hasBeenInitialized) {
            this.hasBeenInitialized = true;
            //TODO: figure out why this is need. I got no clue but we get missing models if not.
            if (this.dynamicPack instanceof DynamicTexturePack tp) tp.addPackLogo();
            if (!resourcePackSupport) {
                var repository = this.getRepository();
                if (repository != null) {
                    Moonlight.CAN_EARLY_RELOAD_HACK.set(false);
                    //no resource pack support, just include these
                    FilteredResManager vanillaManager = FilteredResManager.including(repository, this.dynamicPack.packType,
                            "vanilla", "mod_resources");
                    Moonlight.CAN_EARLY_RELOAD_HACK.set(true);
                    this.regenerateDynamicAssets(vanillaManager);
                    vanillaManager.close();
                } else {
                    this.regenerateDynamicAssets(manager);
                }
            }
        }

        //generate textures
        if (resourcePackSupport) {
            var repository = this.getRepository();
            // only needed on second reload since there will be no pack on first
            // and only if the pack itself doesn't get cleared
            boolean clearOnReload = true;
            if (repository != null && hasBeenInitialized && !clearOnReload) {
                Moonlight.CAN_EARLY_RELOAD_HACK.set(false);
                FilteredResManager nonSelfManager = FilteredResManager.excluding(repository, this.dynamicPack.packType,
                        dynamicPack.packId());
                Moonlight.CAN_EARLY_RELOAD_HACK.set(true);
                this.regenerateDynamicAssets(nonSelfManager);
                nonSelfManager.close();
            }
            this.regenerateDynamicAssets(manager);
        }
    }

    @Nullable
    protected abstract PackRepository getRepository();

    @Deprecated(forRemoval = true)
    public boolean alreadyHasAssetAtLocation(ResourceManager manager, ResourceLocation res, ResType type) {
        return alreadyHasAssetAtLocation(manager, type.getPath(res));
    }

    @Deprecated(forRemoval = true)
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
    @Deprecated(forRemoval = true)
    public void addSimilarJsonResource(ResourceManager manager, StaticResource resource, String keyword, String replaceWith) throws NoSuchElementException {
        addSimilarJsonResource(manager, resource, s -> s.replace(keyword, replaceWith));
    }

    @Deprecated(forRemoval = true)
    public void addSimilarJsonResource(ResourceManager manager, StaticResource resource, Function<String, String> textTransform) throws NoSuchElementException {
        addSimilarJsonResource(manager, resource, textTransform, textTransform);
    }

    @Deprecated(forRemoval = true)
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
        ResourceLocation newRes = new ResourceLocation(this.modId, builder.toString());
        if (!alreadyHasAssetAtLocation(manager, newRes)) {

            String fullText = new String(resource.data, StandardCharsets.UTF_8);


            fullText = textTransform.apply(fullText);

            this.dynamicPack.addBytes(newRes, fullText.getBytes());
        }
    }

    @Deprecated(forRemoval = true)
    public void addResourceIfNotPresent(ResourceManager manager, StaticResource resource) {
        if (!alreadyHasAssetAtLocation(manager, resource.location)) {
            this.dynamicPack.addResource(resource);
        }
    }


    private static final Set<DynResourceGenerator<?>> GENERATORS = new HashSet<>();

    static {
        MoonlightEventsHelper.addListener(earlyPackReloadEvent -> {
            var stopwatch = Stopwatch.createStarted();

            List<CompletableFuture<Void>> futures = GENERATORS.stream()
                    .filter(gen -> gen.dynamicPack.packType == earlyPackReloadEvent.type())
                    .map(gen -> CompletableFuture.runAsync(() -> gen.onEarlyReload(earlyPackReloadEvent), EXECUTOR_SERVICE))
                    .toList();


            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            Moonlight.LOGGER.info("Generated runtime resources for {} packs in a total of: {} ms",
                    GENERATORS.size(), stopwatch.elapsed().toMillis());

        }, EarlyPackReloadEvent.class);

    }

}
