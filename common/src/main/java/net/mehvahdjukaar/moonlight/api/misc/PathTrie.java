package net.mehvahdjukaar.moonlight.api.misc;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PathTrie<T> {
    private final TrieNode<T> root;

    public PathTrie() {
        root = new TrieNode<>();
    }

    public void insert(ResourceLocation resourceLocation, T object) {
        insert(resourceLocation.getNamespace() + "/" + resourceLocation.getPath(), object);
    }

    public void insert(String path, T object) {
        String[] folders = path.split("/");
        TrieNode<T> current = root;

        // Traverse the trie to insert the path
        for (int i = 0; i < folders.length - 1; i++) {
            String folder = folders[i];
            current.children.putIfAbsent(folder, new TrieNode<>());
            current = current.children.get(folder);
        }

        // Add the object to the final node
        current.objects.add(object);
    }

    public Collection<T> search(ResourceLocation id) {
        return search(id.getNamespace() + "/" + id.getPath());
    }

    public Collection<T> search(String path) {
        TrieNode<T> current = getNode(path);
        if (current == null) return Collections.emptyList();
        // Once at the target node, collect all objects from this node and its children
        return current.collectObjects();
    }

    public boolean remove(ResourceLocation resourceLocation) {
        return remove(resourceLocation.getNamespace() + "/" + resourceLocation.getPath());
    }

    public boolean remove(String path) {
        TrieNode<T> current = getNode(path);
        if (current == null) return false;
        // Once at the target node, clear all its contents
        current.children.clear();
        current.objects.clear();
        return true;
    }

    @Nullable
    private TrieNode<T> getNode(String path) {
        String[] folders = path.split("/");
        TrieNode<T> current = root;
        for (String folder : folders) {
            current = current.children.get(folder);
            if (current == null) {
                return null; // Path doesn't exist
            }
        }
        return current;
    }

    public void clear() {
        root.children.clear();
        root.objects.clear();
    }


    public Collection<String> listFolders(String path) {
        TrieNode<T> startNode = getNode(path);
        if (startNode != null) {
            return startNode.children.keySet();
        }
        return Collections.emptyList();
    }

    private static class TrieNode<T> {
        Map<String, TrieNode<T>> children = new HashMap<>();
        List<T> objects = new ArrayList<>();

        public TrieNode() {
        }

        public List<T> collectObjects() {
            List<T> result = new ArrayList<>(objects);

            for (TrieNode<T> child : children.values()) {
                result.addAll(child.collectObjects());
            }

            return result;
        }
    }
}
