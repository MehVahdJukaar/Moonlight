package net.mehvahdjukaar.moonlight.api.integration;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.moonlight.api.platform.configs.fabric.ConfigSpec;
import net.mehvahdjukaar.moonlight.core.Moonlight;

public class ModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> ClothConfigCompat.makeScreen(parent,ConfigSpec.getSpec("moonlight", ConfigType.COMMON));
    }
}
