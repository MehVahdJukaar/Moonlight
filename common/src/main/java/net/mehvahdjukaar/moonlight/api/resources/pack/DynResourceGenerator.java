package net.mehvahdjukaar.moonlight.api.resources.pack;

import com.google.common.base.Stopwatch;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.mehvahdjukaar.moonlight.api.events.EarlyPackReloadEvent;
import net.mehvahdjukaar.moonlight.api.events.MoonlightEventsHelper;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.resources.ResType;
import net.mehvahdjukaar.moonlight.api.resources.SimpleTagBuilder;
import net.mehvahdjukaar.moonlight.api.resources.StaticResource;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.misc.FilteredResManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
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
        this.dynamicPack.addNamespaces(additionalNamespaces().toArray(new String[0]));
        this.dynamicPack.addNamespaces(modId);
        this.dynamicPack.registerPack();

        GENERATORS.add(this);
    }

    /**
     * Called on Mod Init. just loads this class
     */
    public final void register() {
    }

    public abstract Logger getLogger();

    /**
     * @return list of additional namespaces that should be included in the pack.
     * Only generate assets for these namespaces
     */
    //TODO: make abstract
    public Collection<String> additionalNamespaces() {
        return List.of();
    }

    public T getPack() {
        return dynamicPack;
    }

    /**
     * If this pack should be cleared on reload. Overrie if you need to have your pack never cleared, for example when making a pack hat just loads once
     */
    public boolean shouldClearOnReload() {
        return runsOnEveryReload();
    }

    public boolean runsOnEveryReload() {
        return true;
    }

    @Deprecated(forRemoval = true) //just deprecated as it shouldnt be overritten aymore and will become final private
    public void regenerateDynamicAssets(ResourceManager manager) {
        var tasks = new ArrayList<ResourceGenTask>();
        regenerateDynamicAssets(tasks::add);

        Stopwatch watch = Stopwatch.createStarted();

        List<CompletableFuture<ResourceSink>> futures = tasks.stream()
                .map(task -> CompletableFuture.supplyAsync(() -> {
                    var localSink = new ResourceSink(this.modId, this.dynamicPack.packId());
                    task.accept(manager, localSink);
                    return localSink;
                }, getExecutors()))
                .toList();

        // Proper join using CompletableFuture
        CompletableFuture<Void> allDone = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        try {
            Multimap<TagKey<?>, SimpleTagBuilder> tags = HashMultimap.create();
            allDone.join(); // joins all futures
            for (CompletableFuture<ResourceSink> future : futures) {
                ResourceSink sink = future.join();
                sink.resources.forEach(this.dynamicPack::addBytes);
                sink.notClearable.forEach(this.dynamicPack::markNotClearable);
                for (var e : sink.tags.entrySet()) {
                    tags.put(e.getKey(), e.getValue());
                }
            }
            //adds tags
            for (var key : tags.keySet()) {
                var it = tags.get(key).iterator();
                if (it.hasNext()) {
                    SimpleTagBuilder tag = it.next();
                    while (it.hasNext()) {
                        tag.merge(it.next());
                    }
                    this.dynamicPack.addTag(tag, key.registry());
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Task failed", e);
        }

        getLogger().info("Generated runtime {} for pack {} ({}) in: {} ms{} (multithreaded)",
                this.dynamicPack.getPackType(), this.dynamicPack.packId(), this.modId,
                watch.elapsed().toMillis(),
                this.dynamicPack.generateDebugResources ? " (debug resource dump on)" : "");
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
        //first clear all pack content if it should be cleared

        boolean wasFirstReload = false;
        if (!this.hasBeenInitialized) {
            wasFirstReload = true;
            this.hasBeenInitialized = true;
            if (this.dynamicPack instanceof DynamicTexturePack tp) tp.addPackLogo();
        }

        //generate textures
        if (runsOnEveryReload() || wasFirstReload) {
            var repository = this.getRepository();
            if (repository != null) {
                Moonlight.CAN_EARLY_RELOAD_HACK.set(false);
                FilteredResManager nonSelfManager = FilteredResManager.excluding(repository, this.dynamicPack.packType,
                        dynamicPack.packId());
                Moonlight.CAN_EARLY_RELOAD_HACK.set(true);
                this.regenerateDynamicAssets(nonSelfManager);
                nonSelfManager.close();
            } else this.regenerateDynamicAssets(manager);
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
        ResourceLocation newRes = ResourceLocation.fromNamespaceAndPath(this.modId, builder.toString());
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

    @ApiStatus.Internal
    public static void clearAfterReload(PackType targetType) {
        //this will be called multiple times. shunt be an issue I hope
        Set<DynamicResourcePack> packs = new HashSet<>();
        for (var g : GENERATORS) {
            if (g.dynamicPack.packType == targetType && g.shouldClearOnReload()) {
                packs.add(g.dynamicPack);
            }
        }
        for (var p : packs) {
            p.clearNonStatic();
        }
    }

    @ApiStatus.Internal
    public static void clearBeforeReload(PackType targetType) {
        Set<DynamicResourcePack> packs = new HashSet<>();
        for (var g : GENERATORS) {
            if (g.dynamicPack.packType == targetType && g.shouldClearOnReload()) {
                packs.add(g.dynamicPack);
            }
        }
        for (var p : packs) {
            p.clearAllContent();
        }
    }

}
