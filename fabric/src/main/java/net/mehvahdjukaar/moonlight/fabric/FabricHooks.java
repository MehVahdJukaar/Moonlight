package net.mehvahdjukaar.moonlight.fabric;


import net.mehvahdjukaar.moonlight.core.mixins.fabric.SelfCustomBakedModel;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagManager;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.*;

public class FabricHooks {

    private static WeakReference<RegistryAccess> REGISTRY_ACCESS = null;
    private static TagContext TAG_CONTEXT = null;

    @Nullable
    public static RegistryAccess getRegistryAccess() {
        if (REGISTRY_ACCESS == null) return null;
        return REGISTRY_ACCESS.get();
    }
    @Nullable
    public static TagContext getTagContext() {
        return TAG_CONTEXT;
    }

    @ApiStatus.Internal
    public static void setRegistryAccess(RegistryAccess registryAccess) {
        REGISTRY_ACCESS = new WeakReference<>(registryAccess);
    }

    @ApiStatus.Internal
    public static void setTagContext(TagManager tagManager) {
        TAG_CONTEXT = new TagContext(tagManager);
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
}
