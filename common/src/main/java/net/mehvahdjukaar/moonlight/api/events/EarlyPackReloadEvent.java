package net.mehvahdjukaar.moonlight.api.events;

import net.mehvahdjukaar.moonlight.api.misc.IProgressTracker;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.Collection;
import java.util.List;

//TODO:remove packs from here, they can be gathered from the resource manager
public record EarlyPackReloadEvent(Collection<PackResources> selectedPacks, ResourceManager manager,
                                   PackType type, IProgressTracker progress) implements SimpleEvent {

}
