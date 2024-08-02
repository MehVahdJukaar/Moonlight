package net.mehvahdjukaar.moonlight.api.integration.mod_menu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.mehvahdjukaar.moonlight.core.ClientConfigs;

public class ModMenuCompat implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ClientConfigs.CONFIG::makeScreen;
    }
}