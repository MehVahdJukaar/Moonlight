package net.mehvahdjukaar.moonlight.api.resources.pack;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;

public abstract class DynamicServerResourceProvider extends DynamicResourcesProvider {

    protected DynamicServerResourceProvider(ResourceLocation name, PackGenerationStrategy generationPolicy) {
        super(name, PackType.SERVER_DATA, generationPolicy);
    }
}
