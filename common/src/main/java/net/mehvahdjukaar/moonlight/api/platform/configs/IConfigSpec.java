package net.mehvahdjukaar.moonlight.api.platform.configs;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public interface IConfigSpec {

    void loadFromFile();

    ConfigType getConfigType();

    void register();

    @Nullable
    @Environment(EnvType.CLIENT)
    default Screen makeScreen(Screen parent){
        return makeScreen(parent,null);
    };

    @Nullable
    @Environment(EnvType.CLIENT)
    Screen makeScreen(Screen parent, @Nullable ResourceLocation background);

}
