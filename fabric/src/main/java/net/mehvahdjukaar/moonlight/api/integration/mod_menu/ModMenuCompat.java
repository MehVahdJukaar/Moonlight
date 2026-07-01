package net.mehvahdjukaar.moonlight.api.integration.mod_menu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.mehvahdjukaar.moonlight.api.client.config.MoonlightConfigSelectScreen;
import net.mehvahdjukaar.moonlight.core.Moonlight;

public class ModMenuCompat implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        // list all of Moonlight's registered configs (common + client), not just the client one
        return parent -> MoonlightConfigSelectScreen.create(Moonlight.MOD_ID, parent, null);
    }
}