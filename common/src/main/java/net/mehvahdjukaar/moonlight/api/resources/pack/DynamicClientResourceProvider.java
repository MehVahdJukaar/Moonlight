package net.mehvahdjukaar.moonlight.api.resources.pack;

import net.mehvahdjukaar.moonlight.api.events.AfterLanguageLoadEvent;
import net.mehvahdjukaar.moonlight.api.events.MoonlightEventsHelper;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;

import java.nio.file.Files;
import java.nio.file.Path;

public abstract class DynamicClientResourceProvider extends DynamicResourcesProvider {

    protected DynamicClientResourceProvider(ResourceLocation name, PackGenerationStrategy generationPolicy) {
        super(name, PackType.CLIENT_RESOURCES, generationPolicy);
        if (PlatHelper.getPhysicalSide().isServer()) {
            throw new IllegalStateException("Client only class registered on server side! Issue from mod" + name);
        }
        MoonlightEventsHelper.addListener(this::addDynamicTranslations, AfterLanguageLoadEvent.class);

        Path logoPath = ClientHelper.getModIcon(name.getNamespace());
        if (logoPath != null) {
            try {
                this.packResources.addRootResource("pack.png", Files.readAllBytes(logoPath));
            } catch (Exception ignored) {
            }
        }
    }

    protected abstract void addDynamicTranslations(AfterLanguageLoadEvent afterLanguageLoadEvent);
}
