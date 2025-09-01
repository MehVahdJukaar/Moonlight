package net.mehvahdjukaar.moonlight.api.resources.pack;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;

public interface SimplePackProvider {

    Pack createPack();
}
