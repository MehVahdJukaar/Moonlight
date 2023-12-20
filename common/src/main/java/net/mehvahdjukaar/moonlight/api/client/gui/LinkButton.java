package net.mehvahdjukaar.moonlight.api.client.gui;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

import java.util.Calendar;

public class LinkButton {

    public static Button create(Screen parent, int x, int y, String url, String tooltip) {
        return create(14, 14, parent, x, y, url, tooltip);
    }

    public static Button create(int iconW, int iconH,
                                Screen parent, int x, int y, String url, String tooltip) {

        String finalUrl = getLink(url);
        Button.OnPress onPress = (op) -> {
            Style style = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, finalUrl));
            parent.handleComponentClicked(style);
        };

        return SpriteIconButton.builder(CommonComponents.EMPTY, onPress)
                .tooltip(Tooltip.create(Component.literal(tooltip)))
                .pos(x, y)
                .size(iconW + 6, iconH + 6)
                .build();
    }

    private static String getLink(String original) {
        return LOL ? "https://www.youtube.com/watch?v=dQw4w9WgXcQ" : original;
    }

    private static final boolean LOL;

    static {
        Calendar calendar = Calendar.getInstance();
        LOL = calendar.get(Calendar.MONTH) == Calendar.APRIL && calendar.get(Calendar.DATE) == 1;
    }
}