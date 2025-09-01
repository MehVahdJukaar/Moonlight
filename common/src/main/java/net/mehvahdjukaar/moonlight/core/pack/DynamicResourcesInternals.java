package net.mehvahdjukaar.moonlight.core.pack;

import com.google.common.base.Stopwatch;
import net.mehvahdjukaar.moonlight.api.events.EarlyPackReloadEvent;
import net.mehvahdjukaar.moonlight.api.events.MoonlightEventsHelper;
import net.mehvahdjukaar.moonlight.api.misc.IProgressTracker;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynResourceGenerator;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicResourcePack;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicResourcesProvider;
import net.mehvahdjukaar.moonlight.core.CommonConfigs;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.server.packs.PackType;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ApiStatus.Internal
public class DynamicResourcesInternals {

    private static final Set<DynResourceGenerator<?>> GENERATORS = new HashSet<>();

    public static void init() {
        MoonlightEventsHelper.addListener(earlyPackReloadEvent -> {
            PackType type = earlyPackReloadEvent.type();
            List<DynResourceGenerator<?>> validGen = GENERATORS.stream()
                    .filter(gen -> gen.dynamicPack.getPackType() == type)
                    .toList();
            List<String> modIds = GENERATORS.stream()
                    .map(DynResourceGenerator::getModId).toList();
            Moonlight.LOGGER.info("Starting runtime resource generation for pack type {} with generators from mods {}: {}",
                    type, modIds, validGen);

            if (CommonConfigs.EXTRA_DEBUG.get())
                Moonlight.LOGGER.info("Current stack trace:", new Throwable("Stack trace dump to see who fired me"));


            Stopwatch stopwatch = Stopwatch.createStarted();

            IProgressTracker reporter = earlyPackReloadEvent.progress();
            //These are not parallel. pass flat
            for (var gen : validGen) {
                gen.onEarlyReload(earlyPackReloadEvent, reporter); // run synchronously
            }

            Moonlight.LOGGER.info("Finished runtime resources generation for {} packs in a total of {} ms",
                    GENERATORS.size(), stopwatch.elapsed().toMillis());
        }, EarlyPackReloadEvent.class);
    }

    public static void addGenerator(DynResourceGenerator<?> generator) {
        GENERATORS.add(generator);
    }


    @ApiStatus.Internal
    public static void clearAfterReload(PackType targetType) {
        //not used anymore
    }

    @ApiStatus.Internal
    public static void clearBeforeReload(PackType targetType) {
        Set<DynamicResourcePack> packs = new HashSet<>();
        for (var g : GENERATORS) {
            if (g.dynamicPack.getPackType() == targetType && g.shouldClearOnReload()) {
                packs.add(g.dynamicPack);
            }
        }
        for (var p : packs) {
            p.clearAllContent();
        }
    }

    public static void registerProvider(DynamicResourcesProvider provider) {

    }
}
