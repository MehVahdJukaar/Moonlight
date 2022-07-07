package net.mehvahdjukaar.moonlight.api.integration;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.moonlight.api.platform.configs.fabric.FabricConfigSpec;

public class ModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> ClothConfigCompat.makeScreen(parent, FabricConfigSpec.getSpec("moonlight", ConfigType.COMMON));
    }
}
