package net.mehvahdjukaar.moonlight.api.resources.pack;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;

public abstract class DynamicServerResourceProvider extends DynamicResourcesProvider {

    protected DynamicServerResourceProvider(ResourceLocation name, PackGenerationStrategy generationPolicy) {
        super(name, PackType.SERVER_DATA, generationPolicy);
    }

    @Override
    protected PackRepository getPackRepository() {
        var s = PlatHelper.getCurrentServer();
        if (s != null) return s.getPackRepository();
        return null;
    }
}
