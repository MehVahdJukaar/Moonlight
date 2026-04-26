package net.mehvahdjukaar.moonlight.core.integration.platform;


import com.mrcrayfish.configured.api.IModConfig;
import net.mehvahdjukaar.moonlight.api.integration.configured.CustomConfigScreen;
import net.mehvahdjukaar.moonlight.api.integration.configured.CustomConfigSelectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

//credits to MrCrayfish's Configured Mod
public class ModConfigScreen extends CustomConfigScreen {

    public ModConfigScreen(CustomConfigSelectScreen parent, IModConfig config) {
        super(parent, config);
    }

    public ModConfigScreen(String modId, ItemStack mainIcon, Component title,
                           Screen parent, IModConfig config) {
        super(modId, mainIcon, title, parent, config);
    }

    @Override
    public void onSave() {

    }

    @Override
    public Factory getSubScreenFactory() {
        return ModConfigScreen::new;
    }


}