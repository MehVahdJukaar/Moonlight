package net.mehvahdjukaar.moonlight.core.client;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.util.FileDownloadUtils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * The author's full catalog of mods, fetched on demand from the {@code mod_pages} repo the first time the
 * "discover mods" screen is opened. Purely cosmetic: it powers a screen that advertises the other mods, showing
 * the ones you don't have installed grayed out. Never fails hard - if the fetch dies the screen just shows an
 * "offline" message.
 */
@ApiStatus.Internal
public final class OurModsList {

    public enum State {NOT_STARTED, LOADING, LOADED, FAILED}

    /**
     * One catalog entry. {@code modId} is what we check against the loaded mod list to decide installed-or-not.
     * The urls/icon are optional; a missing icon falls back to a letter tile, missing urls just aren't clickable.
     */
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

    private static final Codec<List<Entry>> LIST_CODEC = Entry.CODEC.listOf().fieldOf("mods").codec();

    private static final String FETCH_URL = "https://raw.githubusercontent.com/MehVahdJukaar/mod_pages/heads/master/moonlight_mods.json";

    private static final Gson GSON = new Gson();

    private static volatile State state = State.NOT_STARTED;
    private static volatile List<Entry> mods = List.of();

    public static State getState() {
        return state;
    }

    public static List<Entry> getMods() {
        return mods;
    }

    /**
     * Kicks off the one-time background fetch. Safe to call every time the screen opens; only the first does work.
     */
    public static synchronized void fetchIfNeeded() {
        if (state == State.LOADING || state == State.LOADED) return;
        state = State.LOADING;
        Thread t = new Thread(() -> {
            try {
                JsonElement json = GSON.fromJson(FileDownloadUtils.readString(FETCH_URL), JsonElement.class);
                mods = LIST_CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
                state = State.LOADED;
            } catch (Exception e) {
                Moonlight.LOGGER.warn("Failed to fetch mods list from {}: {}", FETCH_URL, e.toString());
                state = State.FAILED;
            }
        }, "Moonlight Mods List Fetcher");
        t.setDaemon(true);
        t.start();
    }
}
