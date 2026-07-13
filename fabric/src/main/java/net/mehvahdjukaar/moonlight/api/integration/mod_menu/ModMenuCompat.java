package net.mehvahdjukaar.moonlight.api.integration.mod_menu;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.mehvahdjukaar.moonlight.core.client.config.MoonlightConfigSelectScreen;
import net.mehvahdjukaar.moonlight.core.ClientConfigs;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

public class ModMenuCompat implements ModMenuApi {

    // isolated here so Mod Menu classes are only referenced when the mod is actually present (see ClientHelperImpl)
    @Nullable
    public static Screen getModConfigScreen(String modId, Screen parent) {
        return ModMenu.getConfigScreen(modId, parent);
    }

    public static boolean hasModConfigScreen(String modId) {
        return ModMenu.hasConfigScreen(modId);
    }

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