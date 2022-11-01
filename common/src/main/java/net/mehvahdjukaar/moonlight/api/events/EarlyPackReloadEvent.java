package net.mehvahdjukaar.moonlight.api.events;

import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.List;

public record EarlyPackReloadEvent(List<PackResources> packs, ResourceManager manager, PackType type) implements SimpleEvent {

}
