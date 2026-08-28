package net.mehvahdjukaar.moonlight.core.pack;

import com.google.common.base.Stopwatch;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.mehvahdjukaar.moonlight.api.events.EarlyPackReloadEvent;
import net.mehvahdjukaar.moonlight.api.events.MoonlightEventsHelper;
import net.mehvahdjukaar.moonlight.api.misc.IProgressTracker;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicResourcesProvider;
import net.mehvahdjukaar.moonlight.api.resources.pack.GlobalCachedStrategy;
import net.mehvahdjukaar.moonlight.core.CommonConfigs;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.misc.FilteredResManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ApiStatus.Internal
public class DynamicResourcesInternals {

    private static final Multimap<PackType, DynamicResourcesProvider> PROVIDERS = HashMultimap.create();

    public static void init() {
        MoonlightEventsHelper.addListener(earlyPackReloadEvent -> {
            PackType type = earlyPackReloadEvent.type();
            Collection<DynamicResourcesProvider> validGen = PROVIDERS.get(type);
            if (validGen.isEmpty()) return;

            ResourceManager manager = earlyPackReloadEvent.manager();

            GlobalCachedStrategy.refreshState(type, manager.listPacks().toList());

            for (var p : PROVIDERS.get(type)) {
                p.prepare();
            }


            List<Identifier> modIds = validGen.stream()
                    .map(DynamicResourcesProvider::getName).toList();
            Moonlight.LOGGER.info("Starting runtime resource generation for pack type {} with generators {}",
                    type, modIds);

            if (CommonConfigs.EXTRA_DEBUG.get()) {
                Moonlight.LOGGER.info("Current stack trace:", new Throwable("EXTRA_DEBUG is enabled. Stack trace dump to see who fired me"));
            }

            Stopwatch stopwatch = Stopwatch.createStarted();

            IProgressTracker reporter = earlyPackReloadEvent.progress();
            ResourceManager vanillaManager = null;
            //These are not parallel. pass flat
            for (var gen : validGen) {
                if (!gen.canUseExternalResourcePacks()) {
                    if (vanillaManager == null) {
                        vanillaManager = FilteredResManager.vanilla(manager, type);
                    }
                    gen.reload(vanillaManager, reporter); // run synchronously
                } else gen.reload(manager, reporter); // run synchronously
            }

            Moonlight.LOGGER.info("Finished runtime resources generation for {} packs in a total of {} ",
                    validGen.size(), stopwatch);

            GlobalCachedStrategy.writeNewState(type);

        }, EarlyPackReloadEvent.class);

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
    }

    public static void clearAfterReload(PackType targetType) {
        //not used anymore
    }
}
