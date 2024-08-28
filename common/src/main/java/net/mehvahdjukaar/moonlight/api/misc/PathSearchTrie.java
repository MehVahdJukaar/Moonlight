package net.mehvahdjukaar.moonlight.api.misc;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class PathSearchTrie<T> extends SearchTrie<String, T> {

    // List all folder names under a given path
    public Collection<String> listFolders(String path) {
        return super.listKeys(splitPath(path));
    }

    public void insert(String path, T object) {
        super.insert(splitPath(path), object);
    }

    public Collection<T> search(String path) {
        return super.search(splitPath(path));
    }

    public boolean remove(String path) {
        return super.remove(splitPath(path));
    }

    private static List<String> splitPath(String path) {
        return Arrays.asList(path.split("/"));
    }

}
