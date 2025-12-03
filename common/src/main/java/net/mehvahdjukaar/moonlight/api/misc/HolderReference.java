package net.mehvahdjukaar.moonlight.api.misc;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

@Deprecated(forRemoval = true)
public class HolderReference<A> extends HolderRef<A>{
    protected HolderReference(ResourceKey<Registry<A>> registryKey, ResourceKey<A> key) {
        super(registryKey, key);
    }
}
