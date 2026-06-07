package net.mehvahdjukaar.moonlight.core.integration.platform;


import net.mehvahdjukaar.moonlight.api.client.gui.MediaButton;
import net.mehvahdjukaar.moonlight.api.integration.configured.CustomConfigSelectScreen;
import net.mehvahdjukaar.moonlight.core.ClientConfigs;
import net.mehvahdjukaar.moonlight.core.CommonConfigs;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.world.item.Items;

public class ModConfigSelectScreen extends CustomConfigSelectScreen {


    public ModConfigSelectScreen(Screen parent) {
        super(Moonlight.MOD_ID, Items.AIR.getDefaultInstance(),
                "§3Moonlight Configured", parent,
                ModConfigScreen::new, ClientConfigs.CONFIG, CommonConfigs.CONFIG);
    }
    @Override
    protected void init() {
        super.init();
        Button found = null;
        for (var c : this.children()) {
            if (c instanceof Button button) {
                if (button.getWidth() == 150) found = button;
            }
        }
        if (found != null) this.removeWidget(found);

        int y = this.height - 29;
        int centerX = this.width / 2;
        int spacing = 22;

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, (button) -> this.minecraft.setScreen(this.parent))
                .bounds(centerX - 45, y, 90, 20).build());

        MediaButton.addAuthorMediaButtonsLeft(this, this::addRenderableWidget,
                centerX - 45 - spacing, y, spacing, Moonlight.MOD_ID,
                "https://www.curseforge.com/minecraft/mc-mods/moonlight-lib",
                "https://modrinth.com/mod/moonlight");

        MediaButton.addAuthorMediaButtonsRight(this, this::addRenderableWidget,
                centerX + 45 + 2, y, spacing);
    }
}
