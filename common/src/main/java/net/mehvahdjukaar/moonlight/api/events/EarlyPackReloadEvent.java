package net.mehvahdjukaar.moonlight.api.events;

import net.mehvahdjukaar.moonlight.api.misc.IProgressTracker;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.Collection;

public record EarlyPackReloadEvent(ResourceManager manager, PackType type, IProgressTracker progress) implements SimpleEvent {
}
