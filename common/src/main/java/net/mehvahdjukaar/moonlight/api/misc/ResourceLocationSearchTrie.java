package net.mehvahdjukaar.moonlight.api.misc;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class ResourceLocationSearchTrie extends PathSearchTrie<ResourceLocation> {

    // Insert a ResourceLocation (namespace + path) into the trie
    public void insert(ResourceLocation objectToAdd) {
        super.insert( getPath(objectToAdd), objectToAdd);
    }

    private static @NotNull String getPath(ResourceLocation objectToAdd) {
        String path = objectToAdd.getNamespace() + "/" + objectToAdd.getPath();
        path = getFolderPath(path);
        return path;
    }

    private static String getFolderPath(String path) {
        int lastIndex = path.lastIndexOf('/');

        // If there is no '/', return the original path (or empty if needed)
        if (lastIndex == -1) {
            return ""; // or return path if you want to keep single segment paths
        }

        return path.substring(0, lastIndex);
    }

    // Remove entries based on ResourceLocation
    public boolean remove(ResourceLocation object) {
        return super.remove(getPath(object));
    }
}
