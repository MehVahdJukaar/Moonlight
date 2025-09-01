package net.mehvahdjukaar.moonlight.api.resources.pack;

import com.google.common.base.Stopwatch;
import net.mehvahdjukaar.moonlight.api.events.EarlyPackReloadEvent;
import net.mehvahdjukaar.moonlight.api.misc.IProgressTracker;
import net.mehvahdjukaar.moonlight.api.resources.assets.LangBuilder;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.ApiStatus;

import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public abstract class DynamicResourcesProvider implements Pack.ResourcesSupplier {

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    private final ResourceLocation name;
    private final PackLocationInfo locationInfo;
    private final PackSelectionConfig selectionConfig;
    private final PackType packType;

    protected final IEditablePackResources packResources;
    protected final PackCacheStrategy cacheStrategy;

    private boolean hasBeenInitialized;

    public DynamicResourcesProvider(ResourceLocation name, PackType packType, PackCacheStrategy cacheStrategy) {
        this.name = name;
        //TODO:maybe make factory with these?
        this.locationInfo = new PackLocationInfo(
                name.toString(),    // id
                Component.translatable(LangBuilder.getReadableName(name.toString())), // title
                PackSource.BUILT_IN,
                Optional.empty() //no clue what this is
        );
        this.packType = packType;
        this.selectionConfig = new PackSelectionConfig(
                true,    // required -- this MAY need to be true for the pack to be enabled by default
                Pack.Position.TOP,
                false // fixed position
        );
        PackMetadataSection metadata = new PackMetadataSection(Component.literal(name.toString()),
                SharedConstants.getCurrentVersion().getPackVersion(packType), Optional.empty());

        this.cacheStrategy = cacheStrategy;
        this.packResources = cacheStrategy.createPackResources(locationInfo, packType, metadata);

        this.packResources.addNamespaces(gatherAdditionalNamespaces().toArray(new String[0]));
        this.packResources.addNamespaces(name.getNamespace());
    }

    public abstract Collection<String> gatherAdditionalNamespaces();

    public abstract boolean runsOnEveryReload();

    public PackLocationInfo getLocationInfo() {
        return locationInfo;
    }

    public PackType getPackType() {
        return packType;
    }

    public PackSelectionConfig getSelectionConfig() {
        return selectionConfig;
    }

    @Override
    public String toString() {
        return "Dynamic " + getPackType() + " Resources Provider [" + name + "]";
    }

    @Override
    public PackResources openPrimary(PackLocationInfo location) {
        return packResources;
    }

    @Override
    public PackResources openFull(PackLocationInfo location, Pack.Metadata metadata) {
        return packResources;
    }


    @ApiStatus.Internal
    public final void onEarlyReload(EarlyPackReloadEvent event, IProgressTracker localReporter) {
        if (event.type() == this.getPackType()) {
            try {
                this.reloadResources(event.manager(), localReporter);
            } catch (Exception e) {
                Moonlight.LOGGER.error("An error occurred while trying to generate dynamic assets for {}", this, e);
            }
        }
    }

    private void reloadResources(ResourceManager manager, IProgressTracker reporter) {
        //first clear all pack content if it should be cleared

        boolean wasFirstReload = false;
        if (!this.hasBeenInitialized) {
            wasFirstReload = true;
            this.hasBeenInitialized = true;
            //  this.onFirstReload();
        }
        //generate textures
        Collection<Pack> selectedPacks = this.getPackRepository().getSelectedPacks();
        boolean shouldRunGen = runsOnEveryReload() || wasFirstReload || cacheStrategy
                .needsRegeneration(this.packResources, selectedPacks);
        if (shouldRunGen) {
            Moonlight.LOGGER.info("Generating runtime assets for pack {}", this);
            this.regenerateDynamicAssets(manager, reporter);

            this.cacheStrategy.markRegenerated(this.packResources, selectedPacks);
        }
    }


    /**
     * just deprecated as it shouldn't be overwritten anymore and will become final private
     */
    private void regenerateDynamicAssets(ResourceManager manager, IProgressTracker progressTracker) {
        List<ResourceGenTask> genTasks = new ArrayList<>();

        Stopwatch watch = Stopwatch.createStarted();

        try {
            regenerateDynamicAssets(genTasks::add);
        } catch (Exception e) {
            Moonlight.LOGGER.error("Failed to add tasks to dynamic resource gen: ", e);
        }

        int totalTasks = genTasks.size();
        var reporter = progressTracker.subtask(totalTasks);

        List<CompletableFuture<ResourceSink>> futures = genTasks.stream()
                .map(task -> CompletableFuture.supplyAsync(() -> {
                    try {
                        ResourceSink localSink = new ResourceSink(this.modId, this.packResources.packId());
                        task.accept(manager, localSink);
                        return localSink;
                    } catch (Exception e) {
                        Moonlight.LOGGER.error("Resource Gen Task failed", e);
                        return null;
                    } finally {
                        reporter.step();
                    }
                }, getExecutorService()))
                .toList();

        try {
            List<ResourceSink> list = futures.stream()
                    .map(CompletableFuture::join)
                    .filter(Objects::nonNull)
                    .toList();

            ResourceSink.acceptSinks(this.packResources, list);

        } catch (Exception e) {
            throw new RuntimeException("Task failed", e);
        }

        boolean hasDebugGen = this.generateDebugResources();
        if (hasDebugGen) {
            dynamicPack.saveToFile(Paths.get("debug", "generated_resource_pack"));
        }

        Moonlight.LOGGER.info("Generated runtime {} for pack {} ({}) in: {} ms{} (multithreaded)",
                this.getPackType(), this.packResources.packId(), this.modId,
                watch.elapsed().toMillis(),
                hasDebugGen ? " (debug resource dump on)" : "");
    }

    protected Executor getExecutorService() {
        return EXECUTOR_SERVICE;
    }

    protected abstract void regenerateDynamicAssets(Consumer<ResourceGenTask> executor);

    protected abstract PackRepository getPackRepository();

}
