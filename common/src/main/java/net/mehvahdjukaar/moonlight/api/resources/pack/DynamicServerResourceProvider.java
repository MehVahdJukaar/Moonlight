package net.mehvahdjukaar.moonlight.api.resources.pack;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;

public abstract class DynamicServerResourceProvider extends DynamicResourcesProvider {

    protected DynamicServerResourceProvider(Identifier name, PackGenerationStrategy generationPolicy) {
        super(name, PackType.SERVER_DATA, generationPolicy);
    }
}
