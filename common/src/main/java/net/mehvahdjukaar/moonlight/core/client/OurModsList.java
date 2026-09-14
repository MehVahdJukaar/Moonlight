package net.mehvahdjukaar.moonlight.core.client;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.mehvahdjukaar.moonlight.api.client.gui.ModCatalogAPI;
import net.mehvahdjukaar.moonlight.api.util.FileDownloadUtils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

@ApiStatus.Internal
public final class OurModsList implements ModCatalogAPI.Catalog {

    public static final OurModsList INSTANCE = new OurModsList();

    private static final String FETCH_URL = "https://raw.githubusercontent.com/MehVahdJukaar/mod_pages/heads/master/moonlight_mods.json";
    private static final Codec<List<ModCatalogAPI.Entry>> LIST_CODEC = ModCatalogAPI.Entry.CODEC.listOf().fieldOf("mods").codec();
    private static final Gson GSON = new Gson();

    private enum State {NOT_STARTED, LOADING, DONE}

    private volatile State state = State.NOT_STARTED;
    private volatile List<ModCatalogAPI.Entry> mods = List.of();

    @Override
    public Component author() {
        return Component.literal("Supplementaries Team");
    }

    @Override
    public List<ModCatalogAPI.Entry> mods() {
        return mods;
    }

    @Override
    public boolean isLoading() {
        return state == State.LOADING;
    }

    @Override
    public synchronized void onScreenOpened() {
        if (state != State.NOT_STARTED) return;
        state = State.LOADING;
        Thread t = new Thread(() -> {
            try {
                JsonElement json = GSON.fromJson(FileDownloadUtils.readString(FETCH_URL), JsonElement.class);
                mods = LIST_CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
            } catch (Exception e) {
                Moonlight.LOGGER.warn("Failed to fetch mods list from {}: {}", FETCH_URL, e.toString());
            }
            state = State.DONE;
        }, "Moonlight Mods List Fetcher");
        t.setDaemon(true);
        t.start();
    }
}
