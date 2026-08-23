import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.mehvahdjukaar.moonlight.api.client.gui.ModCatalogAPI;
import net.mehvahdjukaar.moonlight.api.util.FileDownloadUtils;
import net.minecraft.network.chat.Component;

import java.util.List;

// for ModCatalogAPI class. Puts your mods in the "Discover Mods" screen
public class ModCatalogExample implements ModCatalogAPI.Catalog {

    private static final Component AUTHOR = Component.literal("Me");

    // Call on client init
    public static void init() {
        // a list you ship with the mod
        ModCatalogAPI.register(AUTHOR, List.of(
                new ModCatalogAPI.Entry("my_mod", "My Mod", "Does things",
                        "https://example.com/icon.png", "https://curseforge.com/my_mod", null)));

        // or one you host somewhere and download yourself
        ModCatalogAPI.register(new ModCatalogExample());
    }

    private static final String URL = "https://example.com/my_mods.json";
    private static final Codec<List<ModCatalogAPI.Entry>> CODEC =
            ModCatalogAPI.Entry.CODEC.listOf().fieldOf("mods").codec();

    private volatile List<ModCatalogAPI.Entry> mods = List.of();
    private volatile boolean loading;
    private boolean started;

    @Override
    public Component author() {
        return AUTHOR;
    }

    @Override
    public List<ModCatalogAPI.Entry> mods() {
        return mods;
    }

    @Override
    public boolean isLoading() {
        return loading;
    }

    @Override
    public void onScreenOpened() {
        if (started) return;
        started = true;
        loading = true;
        Thread t = new Thread(() -> {
            try {
                JsonElement json = new Gson().fromJson(FileDownloadUtils.readString(URL), JsonElement.class);
                mods = CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
            } catch (Exception e) {
                // no list, no rows. This is just a mod ad, never fail hard over it
            }
            loading = false;
        }, "My Mods List Fetcher");
        t.setDaemon(true);
        t.start();
    }
}
