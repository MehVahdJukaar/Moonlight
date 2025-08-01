package net.mehvahdjukaar.moonlight.api.events;

import net.mehvahdjukaar.moonlight.api.misc.ITaskProgress;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.List;

//TODO:remove packs from here, they can be gathered from the resource manager
public record EarlyPackReloadEvent(@Deprecated(forRemoval = true) List<PackResources> packs, ResourceManager manager,
                                   PackType type, ITaskProgress progress) implements SimpleEvent {

}
