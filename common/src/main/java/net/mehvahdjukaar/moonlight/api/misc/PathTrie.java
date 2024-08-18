package net.mehvahdjukaar.moonlight.api.misc;

import net.minecraft.resources.ResourceLocation;

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
        String[] folders = path.split("/");
        TrieNode<T> current = root;

        // Traverse the trie to the target node
        for (String folder : folders) {
            current = current.children.get(folder);
            if (current == null) {
                return Collections.emptyList(); // Return empty if the path doesn't exist
            }
        }

        // Once at the target node, collect all objects from this node and its children
        return current.collectObjects();
    }

    public boolean remove(ResourceLocation resourceLocation) {
        return remove(resourceLocation.getNamespace() + "/" + resourceLocation.getPath());
    }

    public boolean remove(String path) {
        String[] folders = path.split("/");
        TrieNode<T> current = root;

        // Traverse the trie to the target node
        for (String folder : folders) {
            current = current.children.get(folder);
            if (current == null) {
                return false; // Path doesn't exist
            }
        }

        // Once at the target node, clear all its contents
        current.children.clear();
        current.objects.clear();
        return true;
    }

    public void clear() {
        root.children.clear();
        root.objects.clear();
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
