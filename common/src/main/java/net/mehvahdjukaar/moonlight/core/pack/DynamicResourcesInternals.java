package net.mehvahdjukaar.moonlight.core.pack;

import com.google.common.base.Stopwatch;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.mehvahdjukaar.moonlight.api.events.EarlyPackReloadEvent;
import net.mehvahdjukaar.moonlight.api.events.MoonlightEventsHelper;
import net.mehvahdjukaar.moonlight.api.misc.IProgressTracker;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynResourceGenerator;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicResourcePack;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicResourcesProvider;
import net.mehvahdjukaar.moonlight.api.resources.pack.GlobalCachedStrategy;
import net.mehvahdjukaar.moonlight.core.CommonConfigs;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ApiStatus.Internal
public class DynamicResourcesInternals {

    @Deprecated(forRemoval = true)
    private static final Set<DynResourceGenerator<?>> GENERATORS = new HashSet<>();

    private static final Multimap<PackType, DynamicResourcesProvider> PROVIDERS = HashMultimap.create();
    private static final Set<String> DYNAMIC_PACK_IDS = new HashSet<>();

    public static void init() {
        MoonlightEventsHelper.addListener(earlyPackReloadEvent -> {
            PackType type = earlyPackReloadEvent.type();
            Set<DynamicResourcePack> packs = new HashSet<>();
            for (var g : GENERATORS) {
                if (g.dynamicPack.getPackType() == type && g.shouldClearOnReload()) {
                    packs.add(g.dynamicPack);
                }
            }
            for (var p : packs) {
                p.clearAllContent();
            }

            List<DynResourceGenerator<?>> validGen = GENERATORS.stream()
                    .filter(gen -> gen.dynamicPack.getPackType() == type)
                    .toList();
            if (validGen.isEmpty()) return;
            List<String> modIds = validGen.stream()
                    .map(DynResourceGenerator::getModId).toList();
            Moonlight.LOGGER.info("Starting Legacy Runtime Resource Generation for pack type {} with generators from mods {}: {}",
                    type, modIds, validGen);

            Stopwatch stopwatch = Stopwatch.createStarted();

            IProgressTracker reporter = earlyPackReloadEvent.progress();
            //These are not parallel. pass flat
            for (var gen : validGen) {
                gen.onEarlyReload(earlyPackReloadEvent, reporter); // run synchronously
            }

            Moonlight.LOGGER.info("Finished Legacy Runtime Resources Generation for {} packs in a total of {} ms",
                    GENERATORS.size(), stopwatch.elapsed().toMillis());
        }, EarlyPackReloadEvent.class);


        MoonlightEventsHelper.addListener(earlyPackReloadEvent -> {
            PackType type = earlyPackReloadEvent.type();
            Collection<DynamicResourcesProvider> validGen = PROVIDERS.get(type);
            if (validGen.isEmpty()) return;

            var selectedPacks = earlyPackReloadEvent.selectedPacks();


            GlobalCachedStrategy.refreshState(type, selectedPacks);

            for (var p : PROVIDERS.get(type)) {
                p.prepare();
            }


            List<ResourceLocation> modIds = validGen.stream()
                    .map(DynamicResourcesProvider::getName).toList();
            Moonlight.LOGGER.info("Starting runtime resource generation for pack type {} with generators {}",
                    type, modIds);

            if (CommonConfigs.EXTRA_DEBUG.get()) {
                Moonlight.LOGGER.info("Current stack trace:", new Throwable("EXTRA_DEBUG is enabled. Stack trace dump to see who fired me"));
            }

            Stopwatch stopwatch = Stopwatch.createStarted();

            IProgressTracker reporter = earlyPackReloadEvent.progress();
            ResourceManager manager = earlyPackReloadEvent.manager();
            //These are not parallel. pass flat
            for (var gen : validGen) {
                gen.reload(manager, reporter); // run synchronously
            }

            Moonlight.LOGGER.info("Finished runtime resources generation for {} packs in a total of {} ",
                    validGen.size(), stopwatch);

            GlobalCachedStrategy.writeNewState(type);

        }, EarlyPackReloadEvent.class);

    }

    public static void addGenerator(DynResourceGenerator<?> generator) {
        GENERATORS.add(generator);
        DYNAMIC_PACK_IDS.add(generator.dynamicPack.packId());
    }

    public static void registerProvider(DynamicResourcesProvider provider) {
        PackType packType = provider.getPackType();
        //validate no other with same ID exist
        for (var p : PROVIDERS.get(packType)) {
            if (p != provider && p.getName().equals(provider.getName())) {
                throw new IllegalStateException("Duplicate Dynamic Resource Provider ID: " + provider.getName());
            }
        }
        PROVIDERS.put(packType, provider);
        DYNAMIC_PACK_IDS.add(provider.getLocationInfo().id());
    }

    public static void clearAfterReload(PackType targetType) {
        //not used anymore
    }


    //not great...
    public static boolean isKnownDynamicPack(String id) {
        return DYNAMIC_PACK_IDS.contains(id);
    }
}
