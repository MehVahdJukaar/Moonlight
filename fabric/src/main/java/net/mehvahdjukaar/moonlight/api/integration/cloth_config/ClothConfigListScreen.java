package net.mehvahdjukaar.moonlight.api.integration.cloth_config;

import net.mehvahdjukaar.moonlight.api.platform.configs.fabric.FabricConfigListScreen;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigSpec;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@Deprecated(forRemoval = true)
public abstract class ClothConfigListScreen extends FabricConfigListScreen {
    public ClothConfigListScreen(String modId, ItemStack mainIcon, Component displayName, @Nullable ResourceLocation background, Screen parent, ConfigSpec... specs) {
        super(modId, mainIcon, displayName, background, parent, specs);
    }
}
