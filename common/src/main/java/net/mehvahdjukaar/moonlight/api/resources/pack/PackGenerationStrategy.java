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

    boolean needsRegeneration(PackType packType);

    IEditablePackResources createPackResources(PackLocationInfo info, PackType type);


    PackGenerationStrategy REGEN_ON_EVERY_RELOAD = new PackGenerationStrategy() {

        @Override
        public boolean needsRegeneration(PackType packType) {
            return true;
        }

        @Override
        public IEditablePackResources createPackResources(PackLocationInfo info, PackType type) {
            return new InMemoryPackResources(info, type);
        }

        @Override
        public String toString() {
            return "REGEN_ON_EVERY_RELOAD";
        }
    };

    PackGenerationStrategy NO_OP = new PackGenerationStrategy() {

        @Override
        public boolean needsRegeneration(PackType packType) {
            return false;
        }

        @Override
        public IEditablePackResources createPackResources(PackLocationInfo info, PackType type) {
            return new InMemoryPackResources(info, type);
        }

        @Override
        public String toString() {
            return "NO_OP";
        }
    };

    PackGenerationStrategy CACHED = new GlobalCachedStrategy();

    PackGenerationStrategy CACHED_ZIPPED = new GlobalCachedStrategy() {

        @Override
        public IEditablePackResources createPackResources(PackLocationInfo info, PackType type) {
            return new CacheZipPackResources(info, type,
                    getPath(type).resolve(info.id().replace(":", "-")));

        }

        @Override
        public String toString() {
            return "CACHED_ZIPPED";
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
            public boolean needsRegeneration(PackType packType) {
                if (!done) {
                    done = true;
                    return true;
                }
                return false;
            }

            @Override
            public String toString() {
                return "RUN_ONCE";
            }
        };
    }

}
