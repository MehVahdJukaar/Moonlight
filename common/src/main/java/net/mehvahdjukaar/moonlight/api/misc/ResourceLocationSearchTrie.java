package net.mehvahdjukaar.moonlight.api.misc;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class ResourceLocationSearchTrie extends PathSearchTrie<Identifier> {

    // Insert a Identifier (namespace + path) into the trie
    public void insert(Identifier objectToAdd) {
        super.insert( getResPath(objectToAdd), objectToAdd);
    }

    public void insertPath(String fullPath){
        super.insert(fullPath, fromPath(fullPath));
    }

    public static @NotNull String getResPath(Identifier objectToAdd) {
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

    private static @NotNull Identifier fromPath(@NotNull String folderPath) {
        int firstSlash = folderPath.indexOf('/');

        if (firstSlash == -1) {
            // No slash: treat whole string as namespace with empty path
            return  Identifier.fromNamespaceAndPath(folderPath, "");
        }

        String namespace = folderPath.substring(0, firstSlash);
        String path = folderPath.substring(firstSlash + 1);
        return  Identifier.fromNamespaceAndPath(namespace, path);
    }

    // Remove entries based on Identifier
    public boolean remove(Identifier object) {
        return super.remove(getResPath(object));
    }
}
