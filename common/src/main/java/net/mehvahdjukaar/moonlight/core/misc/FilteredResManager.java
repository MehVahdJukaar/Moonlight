package net.mehvahdjukaar.moonlight.core.misc;

import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.MultiPackResourceManager;

import java.util.List;
import java.util.Set;

//resource manager that only contains vanilla stuff
public class FilteredResManager extends MultiPackResourceManager {

    public FilteredResManager(PackType packType, List<PackResources> list) {
        super(packType, list);
    }

    public static FilteredResManager including(PackRepository original, PackType packType, String... packs) {
        Set<String> whitelist = Set.of(packs);
        var list = original.getAvailablePacks().stream().filter(p -> whitelist.contains(p.getId()))
                .map(Pack::open).toList();
        return new FilteredResManager(packType, list);
    }

    public static FilteredResManager excluding(PackRepository original, PackType packType, String... packs) {
        Set<String> whitelist = Set.of(packs);
        var list = original.getAvailablePacks().stream().filter(p -> !whitelist.contains(p.getId()))
                .map(Pack::open).toList();
        return new FilteredResManager(packType, list);
    }
}
