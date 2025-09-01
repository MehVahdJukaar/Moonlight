package net.mehvahdjukaar.moonlight.api.resources.pack;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;

@Deprecated(forRemoval = true)
public class DynamicDataPack extends DynamicResourcePack {

    public DynamicDataPack(ResourceLocation name, Pack.Position position, boolean fixed, boolean hidden) {
        super(name, PackType.SERVER_DATA, position, fixed, hidden);
    }

    public DynamicDataPack(ResourceLocation name) {
        super(name, PackType.SERVER_DATA);
    }


}
