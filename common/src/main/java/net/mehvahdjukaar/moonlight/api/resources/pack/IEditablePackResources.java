package net.mehvahdjukaar.moonlight.api.resources.pack;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;

import java.util.concurrent.Executor;

public interface IEditablePackResources extends PackResources {

    void addNamespaces(String... namespaces);

    void addRootResource(String name, byte[] resource);

    void addResource(ResourceLocation id, byte[] bytes);

    void removeResource(ResourceLocation id);

    void removeRootResource(String name);

    boolean clearAllResources();

    PackType getPackType();

    boolean isEmpty();

    default void commitChanges(Executor executor) {
    }

    default boolean checkPathValidity() {
        return true;
    }
}
