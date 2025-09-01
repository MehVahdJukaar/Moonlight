package net.mehvahdjukaar.moonlight.api.resources.pack;

import com.google.common.base.Stopwatch;
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

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
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
    protected final PackGenerationStrategy generationStrategy;

    private boolean needsRegeneration = true;

    public DynamicResourcesProvider(ResourceLocation name, PackType packType, PackGenerationStrategy generationPolicy) {
        this.name = name;
        this.packType = packType;
        this.generationStrategy = generationPolicy;
        //TODO:maybe make factory with these?
        //TODO:make these configurable
        this.locationInfo = new PackLocationInfo(
                name.toString(),    // id
                Component.translatable(LangBuilder.getReadableName(name.toString())), // title
                PackSource.BUILT_IN,
                Optional.empty() //no clue what this is
        );
        this.selectionConfig = new PackSelectionConfig(
                true,    // required -- this MAY need to be true for the pack to be enabled by default
                Pack.Position.TOP,
                false // fixed position
        );
        PackMetadataSection metadata = new PackMetadataSection(Component.literal(name.toString()),
                SharedConstants.getCurrentVersion().getPackVersion(packType), Optional.empty());


        this.packResources = generationPolicy.createPackResources(locationInfo, packType, metadata);
        this.packResources.addNamespaces(gatherSupportedNamespaces().toArray(new String[0]));
        this.packResources.addNamespaces(name.getNamespace());
    }

    public ResourceLocation getName() {
        return name;
    }

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

    public void prepare() {
        if (generationStrategy.needsRegeneration(this.packResources,
                this.getPackRepository().getSelectedPacks())) {
            this.packResources.clearAllResources();
            this.needsRegeneration = true;
        }
    }

    public void reload(ResourceManager manager, IProgressTracker reporter) {
        try {
            if (this.needsRegeneration) {
                this.needsRegeneration = false;
                Collection<Pack> selected = getPackRepository().getSelectedPacks();

                String reason = "cache strategy requested refresh";
                Moonlight.LOGGER.info("Regenerating {} due to {}", this, reason);

                Stopwatch watch = Stopwatch.createStarted();

                runGenerationPipeline(manager, reporter);

                generationStrategy.afterRegenerate(this.packResources, selected);

                Moonlight.LOGGER.info("Generated runtime {} for pack {} in {} ms",
                        this.getPackType(), this.packResources.packId(),
                        watch.elapsed().toMillis());

                //ugly here but whatever
                if (this.generateDebugResources() && this.packResources instanceof IDebugDumpable d) {
                    getExecutorService().execute(() -> {
                        d.dumpToDisk(Paths.get("debug", "generated_resource_pack"));
                    });
                }

            } else {
                Moonlight.LOGGER.info("Skipping regeneration for {} (cache up-to-date)", this);
            }
        } catch (Exception e) {
            Moonlight.LOGGER.error("An error occurred while trying to generate dynamic assets for {}", this, e);
        }
    }


    /**
     * just deprecated as it shouldn't be overwritten anymore and will become final private
     */
    private void runGenerationPipeline(ResourceManager manager, IProgressTracker progressTracker) {
        List<ResourceGenTask> genTasks = new ArrayList<>();


        try {
            regenerateDynamicAssets(genTasks::add);
        } catch (Exception e) {
            Moonlight.LOGGER.error("Failed to add tasks to dynamic resource gen: ", e);
        }

        int totalTasks = genTasks.size();
        var reporter = progressTracker.subtask(totalTasks);

        List<CompletableFuture<ResourceSink>> futures = genTasks.stream()

                .map(task -> CompletableFuture.supplyAsync(() -> {
                            ResourceSink sink = new ResourceSink(this.name.getNamespace(), this.packResources.packId());
                            task.accept(manager, sink);               // let exceptions bubble
                            return sink;
                        }, getExecutorService()
                ).whenComplete((sink, ex) -> {
                    reporter.step();
                    if (ex != null) Moonlight.LOGGER.error("Resource Gen Task failed", ex);
                }))
                .toList();

        try {
            List<ResourceSink> list = futures.stream()
                    .map(CompletableFuture::join)
                    .toList();

            ResourceSink.acceptSinks(this.packResources, list);

        } catch (Exception e) {
            throw new RuntimeException("Task failed", e);
        }

    }

    protected Executor getExecutorService() {
        return EXECUTOR_SERVICE;
    }


    protected boolean generateDebugResources() {
        return false;
    }

    protected abstract Collection<String> gatherSupportedNamespaces();

    protected abstract void regenerateDynamicAssets(Consumer<ResourceGenTask> executor);

    protected abstract PackRepository getPackRepository();


}
