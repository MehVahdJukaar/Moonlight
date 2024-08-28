package net.mehvahdjukaar.moonlight.api.misc;

import net.minecraft.resources.ResourceLocation;

import java.util.Collection;

public class ResourceLocationSearchTrie extends PathSearchTrie<ResourceLocation> {

    // Insert a ResourceLocation (namespace + path) into the trie
    public void insert(ResourceLocation objectToAdd) {
        String path = objectToAdd.getNamespace() + "/" + objectToAdd.getPath();
        path = getFolderPath(path);
        super.insert(path, objectToAdd);
    }

    private static String getFolderPath(String path) {
        int lastIndex = path.lastIndexOf('/');

        // If there is no '/', return the original path (or empty if needed)
        if (lastIndex == -1) {
            return ""; // or return path if you want to keep single segment paths
        }

        return path.substring(0, lastIndex);
    }

    // Search using a ResourceLocation (namespace + path)
    public Collection<ResourceLocation> search(ResourceLocation folder) {
        String path = folder.getNamespace() + "/" + folder.getPath();
        return super.search(path);
    }

    // Remove entries based on ResourceLocation
    public boolean remove(ResourceLocation folder) {
        String path = folder.getNamespace() + "/" + folder.getPath();
        return super.remove(path);
    }
}
