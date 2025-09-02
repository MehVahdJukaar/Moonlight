package net.mehvahdjukaar.moonlight.api.resources.pack;

import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;

import java.util.Collection;

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
            return true;
        }

        @Override
        public IEditablePackResources createPackResources(PackLocationInfo info, PackType type) {
            return new InMemoryPackResources(info, type);
        }
    };

    PackGenerationStrategy NO_OP = new PackGenerationStrategy() {

        @Override
        public boolean needsRegeneration() {
            return false;
        }

        @Override
        public IEditablePackResources createPackResources(PackLocationInfo info, PackType type) {
            return new InMemoryPackResources(info, type);
        }
    };

    PackGenerationStrategy CACHED = new GlobalCachedStrategy();

    PackGenerationStrategy CACHED_ZIPPED = new GlobalCachedStrategy() {

        @Override
        public IEditablePackResources createPackResources(PackLocationInfo info, PackType type) {
            return new CacheZipPackResources(info, type,
                    getPath(type).resolve(info.id().replace(":", "-")));
        }
    };

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
