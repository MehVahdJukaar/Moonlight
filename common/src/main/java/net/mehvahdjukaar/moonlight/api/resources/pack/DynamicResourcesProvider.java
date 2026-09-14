package net.mehvahdjukaar.moonlight.api.resources.pack;

import com.google.common.base.Stopwatch;
import net.mehvahdjukaar.moonlight.api.misc.IProgressTracker;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.util.TextHelper;
import net.mehvahdjukaar.moonlight.core.CommonConfigs;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public abstract class DynamicResourcesProvider implements SimplePackProvider {

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    private final ResourceLocation name;
    private final PackLocationInfo locationInfo;
    private final PackType packType;

    protected final IEditablePackResources packResources;
    protected final PackGenerationStrategy generationStrategy;

    private volatile boolean needsRegeneration = true;

    public DynamicResourcesProvider(ResourceLocation name, PackType packType, PackGenerationStrategy generationPolicy) {
        this.name = name;
        this.packType = packType;
        this.generationStrategy = generationPolicy;
        //TODO:maybe make factory with these?
        //TODO:make these configurable
        this.locationInfo = new PackLocationInfo(
                name.toString(),    // id
                Component.translatable(TextHelper.getReadableName(name.toString())), // title
                PackSource.BUILT_IN,
                Optional.empty() //no clue what this is
        );


        this.packResources = generationPolicy.createPackResources(locationInfo, packType);
        this.packResources.addNamespaces(gatherSupportedNamespaces().toArray(new String[0]));
        this.packResources.addNamespaces(name.getNamespace());
    }

    /**
     * @return true if the assets here can depend on external resource packs (eg. user added texture packs assets) and not just vanilla + mod resources.
     * In other words, for instance, if textures generated here can depend on external texture packs
     */
    public boolean canUseExternalResourcePacks() {
        return false;
    }

    //TODO: turn these info field access
    public final IEditablePackResources getPackResources() {
        return packResources;
    }

    public final ResourceLocation getName() {
        return name;
    }

    public final PackLocationInfo getLocationInfo() {
        return locationInfo;
    }

    public final PackType getPackType() {
        return packType;
    }

    //override if needed
    public PackSelectionConfig createSelectionConfig() {
        return new PackSelectionConfig(
                true,    // required -- this MAY need to be true for the pack to be enabled by default
                Pack.Position.TOP,
                false // fixed position
        );
    }

    @Override
    public String toString() {
        return "Dynamic " + getPackType() + " Resources Provider [" + name + "]";
    }


    public final void prepare() {
        this.needsRegeneration = needsToRegenerate();
    }

    public boolean needsToRegenerate() {
        if (this.generationStrategy.needsRegeneration(packType)) {
            return this.packResources.clearAllResources();
        } else {
            boolean shouldRegenDueToInvalid = !this.packResources.initializeIfValid();
            if (shouldRegenDueToInvalid) {
                Moonlight.LOGGER.info("Cache for {} at {} is invalid or absent, will regenerate", this, this.packResources);
            }
            return shouldRegenDueToInvalid;
        }
    }

    //called on every reload
    public void reload(ResourceManager manager, IProgressTracker reporter) {
        if (this.needsRegeneration) {
            this.needsRegeneration = false;
            try {
                Moonlight.LOGGER.info("Regenerating {}, requested by strategy {}", this, generationStrategy);

                Stopwatch watch = Stopwatch.createStarted();

                runGenerationPipeline(manager, reporter);

                Moonlight.LOGGER.info("Generated runtime {} for pack {} in {}",
                        this.getPackType(), this.packResources.packId(), watch);

            } catch (Exception e) {
                Moonlight.LOGGER.error("An error occurred while trying to generate dynamic assets for {}", this, e);
            } finally {
                this.packResources.commitChanges();

                //ugly here but whatever
                if (this.generateDebugResources() && this.packResources instanceof IDebugDumpable d) {
                    getExecutorService().execute(() -> {
                        d.dumpToDisk(Paths.get("debug", "generated_resource_pack"));
                    });
                }
            }
        } else {
            Moonlight.LOGGER.info("Skipping regeneration for {} (cache up-to-date)", this);
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
                            task.accept(manager, sink); // may throw
                            return sink;
                        }, getExecutorService()
                ).handle((sink, ex) -> {
                    reporter.step();
                    if (ex != null) {
                        Moonlight.LOGGER.error("Resource Gen Task failed", ex);
                        return null;
                    }
                    return sink;
                }))
                .toList();

        List<ResourceSink> successful = futures.stream()
                .map(CompletableFuture::join)   // safe: handle() guarantees normal completion
                .filter(Objects::nonNull)//ignore failed tasks
                .toList();

        if (successful.isEmpty()) {
            Moonlight.LOGGER.warn("No resource sinks produced; all tasks failed or none were scheduled.");
            return;
        }

        try {
            ResourceSink.acceptSinks(this.packResources, successful);
        } catch (Exception e) {
            Moonlight.LOGGER.error("Failed to accept generated resource sinks", e);
        }
    }


    protected Executor getExecutorService() {
        if (CommonConfigs.MULTI_THREADED_GENERATION.get()) {
            return EXECUTOR_SERVICE;
        }
        return Runnable::run;
    }


    protected boolean generateDebugResources() {
        return PlatHelper.isDev();
    }

    protected abstract Collection<String> gatherSupportedNamespaces();

    public void addSupportedNamespaces(String... namespace) {
        this.packResources.addNamespaces(namespace);
    }

    protected abstract void regenerateDynamicAssets(Consumer<ResourceGenTask> executor);


    @Override
    public Pack createPack() {
        IEditablePackResources resources = this.packResources;
        return Pack.readMetaAndCreate(
                this.getLocationInfo(),
                new Pack.ResourcesSupplier() {
                    @Override
                    public PackResources openPrimary(PackLocationInfo location) {
                        return resources;
                    }

                    @Override
                    public PackResources openFull(PackLocationInfo location, Pack.Metadata metadata) {
                        return resources;
                    }
                },
                this.getPackType(),
                this.createSelectionConfig()
        );
    }
}
