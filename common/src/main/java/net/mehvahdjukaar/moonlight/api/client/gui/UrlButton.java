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
import org.jetbrains.annotations.Nullable;

import java.util.Calendar;
import java.util.Locale;

public class UrlButton {

    public static Button create(Type type, Screen parent, int x, int y, String url) {
        return create(type.sprite(), type.size(), type.size(), parent, x, y, url, type.tooltip);
    }

    public static Button create(ResourceLocation sprite, Screen parent, int x, int y, String url, String tooltip) {
        return create(sprite, 14, 14, parent, x, y, url, tooltip);
    }

    public static Button create(ResourceLocation sprite, int iconW, int iconH,
                                Screen parent, int x, int y, String url, String tooltip) {

        String finalUrl = getLink(url);
        Button.OnPress onPress = (op) -> {
            Style style = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, finalUrl));
            parent.handleComponentClicked(style);
        };

        var button = SpriteIconButton.builder(CommonComponents.EMPTY, onPress, false)
                .sprite(sprite, iconW, iconH)
                .size(iconW + 6, iconH + 6)
                .build();
        if (tooltip != null) {
            button.setTooltip(Tooltip.create(Component.translatable(tooltip), Component.translatable(tooltip)));
        }
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

    public enum Type {
        ON(null),
        OFF(null),
        CURSEFORGE("CurseForge Page"),
        DISCORD("Mod Discord"),
        GITHUB("Mod Wiki"),
        KOFI("Donate a Coffee"),
        PATREON("Support me on Patreon :D"),
        TWITTER("Twitter Page"),
        YOUTUBE("Youtube Channel"),
        AKLIZ("Need a server? Get one with Akliz");

        @Nullable
        final String tooltip;

        Type(String tooltip) {
            this.tooltip = tooltip;
        }

        public int size() {
            return this == ON || this == OFF ? 12 : 14;
        }

        public ResourceLocation sprite() {
            return Moonlight.res("misc_icons/" + this.name().toLowerCase(Locale.ROOT));
        }
    }

    public static void addMyMediaButtons(Screen screen, int centerX, int y,
                                         String curseId, String githubId) {
        addMediaButtons(screen, centerX, y,
                "https://www.patreon.com/user?u=53696377",
                "https://ko-fi.com/mehvahdjukaar",
                "https://www.curseforge.com/minecraft/mc-mods/" + curseId,
                "https://github.com/MehVahdJukaar/" + githubId,
                "https://discord.com/invite/qdKRTDf8Cv",
                "https://www.youtube.com/watch?v=LSPNAtAEn28&t=1s",
                "https://twitter.com/Supplementariez?s=09",
                "https://www.akliz.net/supplementaries"
        );
    }

    public static void addMediaButtons(Screen screen, int centerX, int y,
                                       String patreon, String kofi, String curseforge,
                                       String wiki, String discord, String youtube,
                                       String twitter, String akliz) {
        screen.addRenderableWidget(UrlButton.create(Type.PATREON, screen, centerX - 45 - 22, y, patreon));
        screen.addRenderableWidget(UrlButton.create(Type.KOFI, screen, centerX - 45 - 22 * 2, y, kofi));
        screen.addRenderableWidget(UrlButton.create(Type.CURSEFORGE, screen, centerX - 45 - 22 * 3, y, curseforge));
        screen.addRenderableWidget(UrlButton.create(Type.GITHUB, screen, centerX - 45 - 22 * 4, y, wiki));

        screen.addRenderableWidget(UrlButton.create(Type.DISCORD, screen, +45 + 2, y, discord));
        screen.addRenderableWidget(UrlButton.create(Type.YOUTUBE, screen, centerX + 45 + 2 + 22, y, youtube));
        screen.addRenderableWidget(UrlButton.create(Type.TWITTER, screen, centerX + 45 + 2 + 22 * 2, y, twitter));
        screen.addRenderableWidget(UrlButton.create(Type.AKLIZ, screen, centerX + 45 + 2 + 22 * 3, y, akliz));
    }
}