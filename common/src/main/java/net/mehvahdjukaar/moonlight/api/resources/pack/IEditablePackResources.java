package net.mehvahdjukaar.moonlight.api.resources.pack;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;

import java.util.concurrent.Executor;

public interface IEditablePackResources extends PackResources {

    void addNamespaces(String... namespaces);

    void addRootResource(String name, byte[] resource);

    void addResource(Identifier id, byte[] bytes);

    void removeResource(Identifier id);

    void removeRootResource(String name);

    boolean clearAllResources();

    PackType getPackType();

    boolean isEmpty();

    default void commitChanges() {
    }

    default boolean initializeIfValid() {
        return true;
    }
}
