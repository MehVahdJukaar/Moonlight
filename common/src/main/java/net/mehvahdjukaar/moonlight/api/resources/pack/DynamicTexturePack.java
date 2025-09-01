package net.mehvahdjukaar.moonlight.api.resources.pack;

import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;

import java.nio.file.Files;
import java.nio.file.Path;

@Deprecated(forRemoval = true)
public class DynamicTexturePack extends DynamicResourcePack {

    public DynamicTexturePack(ResourceLocation name, Pack.Position position, boolean fixed, boolean hidden) {
        super(name, PackType.CLIENT_RESOURCES, position, fixed, hidden);
    }

    public DynamicTexturePack(ResourceLocation name) {
        super(name, PackType.CLIENT_RESOURCES);
    }

    @Override
    public void registerPack() {
        addIcon();
        super.registerPack();
    }

    private void addIcon() {
        Path logoPath = ClientHelper.getModIcon(this.mainNamespace);
        if (logoPath != null) {
            try {
                this.addRootResource("pack.png", Files.readAllBytes(logoPath));
            } catch (Exception ignored) {
            }
        }else{
            Moonlight.LOGGER.error("Failed to find mod icon for{}", this.mainNamespace);
        }

    }

}
