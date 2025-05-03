package net.mehvahdjukaar.moonlight.api.resources.pack;

import net.minecraft.server.packs.resources.ResourceManager;

import java.util.function.BiConsumer;

public interface ResourceGenTask extends BiConsumer<ResourceManager, ResourceSink> {
    @Override
    void accept(ResourceManager manager, ResourceSink sink);
}