package net.mehvahdjukaar.moonlight.api.client.gui;

import net.mehvahdjukaar.moonlight.core.Moonlight;
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

public class MediaButton {

    public static final ResourceLocation YOUTUBE = Moonlight.res("media/youtube");
    public static final ResourceLocation TWITTER = Moonlight.res("media/twitter");
    public static final ResourceLocation DISCORD = Moonlight.res("media/discord");
    public static final ResourceLocation PATREON = Moonlight.res("media/patreon");
    public static final ResourceLocation KO_FI = Moonlight.res("media/ko_fi");
    public static final ResourceLocation CURSEFORGE = Moonlight.res("media/curseforge");
    public static final ResourceLocation GITHUB = Moonlight.res("media/github");
    public static final ResourceLocation AKLIZ = Moonlight.res("media/akliz");

    public static final ResourceLocation YES = Moonlight.res("yes");
    public static final ResourceLocation NO = Moonlight.res("no");

    public static Button create(Screen parent, int x, int y, ResourceLocation texture,
                                String url, String tooltip) {
        return create(14, 14, texture, parent, x, y, url, tooltip);
    }

    public static Button create(int iconW, int iconH, ResourceLocation texture,
                                Screen parent, int x, int y, String url, String tooltip) {

        String finalUrl = getLink(url);
        Button.OnPress onPress = (op) -> {
            Style style = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, finalUrl));
            parent.handleComponentClicked(style);
        };

        var button = new SpriteIconButton.Builder(CommonComponents.EMPTY, onPress, true)
                .sprite(texture, 0, 0)
                .size(iconW + 6, iconH + 6)
                .build();

        button.setTooltip(Tooltip.create(Component.literal(tooltip)));
        button.setPosition(x, y);

        return button;

    }

    private static String getLink(String original) {
        return LOL ? "https://www.youtube.com/watch?v=dQw4w9WgXcQ" : original;
    }

    private static final boolean LOL;

    static {
        Calendar calendar = Calendar.getInstance();
        LOL = calendar.get(Calendar.MONTH) == Calendar.APRIL && calendar.get(Calendar.DATE) == 1;
    }


    public static Button youtube(Screen parent, int x, int y, String url) {
        return create(parent, x, y, YOUTUBE, url, "Youtube Channel");
    }

    public static Button twitter(Screen parent, int x, int y, String url) {
        return create(parent, x, y, TWITTER, url, "Twitter Page");
    }

    public static Button discord(Screen parent, int x, int y, String url) {
        return create(parent, x, y, DISCORD, url, "Mod Discord");
    }

    public static Button patreon(Screen parent, int x, int y, String url) {
        return create(parent, x, y, PATREON, url, "Support me on Patreon :D");
    }

    public static Button koFi(Screen parent, int x, int y, String url) {
        return create(parent, x, y, KO_FI, url, "Donate a Coffee");
    }

    public static Button curseForge(Screen parent, int x, int y, String url) {
        return create(parent, x, y, CURSEFORGE, url, "CurseForge Page");
    }

    public static Button github(Screen parent, int x, int y, String url) {
        return create(parent, x, y, GITHUB, url, "Mod Wiki");
    }

    public static Button akliz(Screen parent, int x, int y, String url, String tooltip) {
        return create(parent, x, y, AKLIZ, url, tooltip);
    }



}