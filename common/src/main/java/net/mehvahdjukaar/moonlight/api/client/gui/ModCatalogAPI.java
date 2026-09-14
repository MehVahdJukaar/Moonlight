package net.mehvahdjukaar.moonlight.api.client.gui;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The mod lists behind the "Discover Mods" screen. Register one and your mods show up there next to everybody else's,
 * the ones that aren't installed grayed out. With more than one catalog the screen groups them by author.
 */
public final class ModCatalogAPI {

    public record Entry(String modId, String name, String description,
                        @Nullable String iconUrl, @Nullable String curseforgeUrl, @Nullable String modrinthUrl) {

        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("id").forGetter(Entry::modId),
                Codec.STRING.fieldOf("name").forGetter(Entry::name),
                Codec.STRING.optionalFieldOf("description", "").forGetter(Entry::description),
                Codec.STRING.optionalFieldOf("icon").forGetter(e -> Optional.ofNullable(e.iconUrl)),
                Codec.STRING.optionalFieldOf("curseforge").forGetter(e -> Optional.ofNullable(e.curseforgeUrl)),
                Codec.STRING.optionalFieldOf("modrinth").forGetter(e -> Optional.ofNullable(e.modrinthUrl))
        ).apply(i, (id, name, desc, icon, cf, mr) ->
                new Entry(id, name, desc, icon.orElse(null), cf.orElse(null), mr.orElse(null))));
    }

    public interface Catalog {

        Component author();

        List<Entry> mods();

        default boolean isLoading() {
            return false;
        }

        /** Called every time the screen opens. Kick off a lazy fetch here. */
        default void onScreenOpened() {
        }
    }

    private static final List<Catalog> CATALOGS = new ArrayList<>();

    public static synchronized void register(Catalog catalog) {
        CATALOGS.add(catalog);
    }

    /** For a list you ship with the mod instead of hosting it somewhere. */
    public static synchronized void register(Component author, List<Entry> mods) {
        List<Entry> copy = List.copyOf(mods);
        register(new Catalog() {
            @Override
            public Component author() {
                return author;
            }

            @Override
            public List<Entry> mods() {
                return copy;
            }
        });
    }

    @ApiStatus.Internal
    public static synchronized List<Catalog> getCatalogs() {
        return List.copyOf(CATALOGS);
    }
}
