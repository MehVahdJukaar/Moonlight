package net.mehvahdjukaar.moonlight.fabric;


import net.mehvahdjukaar.moonlight.api.platform.fabric.RegHelperImpl;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagManager;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.*;

public class FabricHooks {


    static final List<Runnable> COMMON_SETUP = new ArrayList<>();
    static final List<Runnable> CLIENT_SETUP = new ArrayList<>();
    private static MinecraftServer currentServer = null;
    private static WeakReference<RegistryAccess> registryAccess = null;
    private static TagContext tagContext = null;

    @Nullable
    public static RegistryAccess getRegistryAccess() {
        if (registryAccess == null) return null;
        return registryAccess.get();
    }

    @Nullable
    public static MinecraftServer getCurrentServer() {
        return currentServer;
    }

    @Nullable
    public static TagContext getTagContext() {
        return tagContext;
    }

    @ApiStatus.Internal
    public static void setRegistryAccess(RegistryAccess registryAccess) {
        FabricHooks.registryAccess = new WeakReference<>(registryAccess);
    }

    @ApiStatus.Internal
    public static void setTagContext(TagManager tagManager) {
        tagContext = new TagContext(tagManager);
    }

    @ApiStatus.Internal
    public static void setCurrentServer(MinecraftServer serverLevel) {
        currentServer = serverLevel;
    }

    public static class TagContext {
        private final WeakReference<TagManager> reference;
        private Map<ResourceKey<?>, Map<ResourceLocation, Collection<Holder<?>>>> loadedTags = null;

        public TagContext(TagManager tagManager) {
            this.reference = new WeakReference<>(tagManager);
        }

        public <T> Map<ResourceLocation, Collection<Holder<T>>> getAllTags(ResourceKey<? extends Registry<T>> registry) {
            var tagManager = reference.get();
            if(tagManager == null)return Map.of();
            if (this.loadedTags == null) {
                List<TagManager.LoadResult<?>> tags = tagManager.getResult();
                if (tags.isEmpty()) throw new IllegalStateException("Tags have not been loaded yet.");
                loadedTags = new IdentityHashMap<>();
                for (var loadResult : tags) {
                    Map<ResourceLocation, Collection<? extends Holder<?>>> map = Collections.unmodifiableMap(loadResult.tags());
                    loadedTags.put(loadResult.key(), (Map) map);
                }
            }
            return (Map) loadedTags.getOrDefault(registry, Collections.emptyMap());
        }
    }


    /**
     * Equivalent of forge common setup. called by this mod client initializer and server initializer
     */
    public static void addCommonSetup(Runnable runnable) {
        COMMON_SETUP.add(runnable);
    }

    /**
     * Equivalent of forge client setup. called by this mod client initializer
     */
    public static void addClientSetup(Runnable runnable) {
        CLIENT_SETUP.add(runnable);
    }

    /**
     * Initializes the registries
     */
    public static void finishModInit(String modId){
        RegHelperImpl.finishRegistration(modId);
    }
}
