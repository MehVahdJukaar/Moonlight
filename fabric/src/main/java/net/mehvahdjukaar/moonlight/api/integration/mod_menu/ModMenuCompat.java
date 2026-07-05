package net.mehvahdjukaar.moonlight.api.integration.mod_menu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.mehvahdjukaar.moonlight.core.client.config.MoonlightConfigSelectScreen;
import net.mehvahdjukaar.moonlight.core.ClientConfigs;
import net.mehvahdjukaar.moonlight.core.Moonlight;

public class ModMenuCompat implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            // custom screen disabled: fall back to the old Cloth Config / YACL screen for the client config
            if (!ClientConfigs.CUSTOM_CONFIG_SCREEN.get()) {
                return ClientConfigs.CONFIG.makeScreen(parent);
            }
            // list all of Moonlight's registered configs (common + client), not just the client one
            return MoonlightConfigSelectScreen.create(Moonlight.MOD_ID, parent, null);
        };
    }
}