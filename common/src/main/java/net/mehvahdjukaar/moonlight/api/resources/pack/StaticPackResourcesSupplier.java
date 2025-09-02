package net.mehvahdjukaar.moonlight.api.resources.pack;

import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;

public record StaticPackResourcesSupplier(PackResources resources) implements Pack.ResourcesSupplier {

    @Override
    public PackResources openPrimary(PackLocationInfo location) {
        return resources;
    }

    @Override
    public PackResources openFull(PackLocationInfo location, Pack.Metadata metadata) {
        return resources;
    }
}
