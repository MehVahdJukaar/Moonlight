package net.mehvahdjukaar.moonlight.api.platform.configs.fabric;

import net.fabricmc.loader.api.FabricLoader;
import net.mehvahdjukaar.moonlight.api.platform.configs.ModConfigHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Main config screen
 */
@Deprecated(forRemoval = true)
public class FabricConfigListScreen extends net.mehvahdjukaar.moonlight.api.platform.configs.platform.FabricConfigListScreen {

    public FabricConfigListScreen(String modId, ItemStack mainIcon, Component displayName, @Nullable ResourceLocation background, Screen parent, ModConfigHolder... specs) {
        super(modId, mainIcon, displayName, background, parent, specs);
    }
}