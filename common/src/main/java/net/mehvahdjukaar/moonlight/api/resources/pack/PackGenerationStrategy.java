package net.mehvahdjukaar.moonlight.api.resources.pack;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.pack.DynamicResourcesInternals;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

//very ugly and confused class
public interface PackGenerationStrategy {

    @Deprecated(forRemoval = true)
    default boolean needsRegeneration(IEditablePackResources packResources, Collection<PackResources> loadedPacks) {
        return true;
    }

    @Deprecated(forRemoval = true)
    default void beforeRegenerate(IEditablePackResources packResources, Collection<PackResources> loadedPacks) {
    }

    boolean needsRegeneration();

    IEditablePackResources createPackResources(PackLocationInfo info, PackType type);


    PackGenerationStrategy REGEN_ON_EVERY_RELOAD = new PackGenerationStrategy() {

        @Override
        public boolean needsRegeneration() {
            return false;
        }

        @Override
        public IEditablePackResources createPackResources(PackLocationInfo info, PackType type) {
            return new InMemoryPackResources(info, type);
        }
    };

    PackGenerationStrategy CACHED = GlobalCachedStrategy.INSTANCE;

    static PackGenerationStrategy runOnce() {
        return new PackGenerationStrategy() {
            private boolean done = false;

            @Override
            public IEditablePackResources createPackResources(PackLocationInfo info, PackType type) {
                return new InMemoryPackResources(info, type);
            }

            @Override
            public boolean needsRegeneration() {
                if (!done) {
                    done = true;
                    return true;
                }
                return false;
            }
        };
    }

}
