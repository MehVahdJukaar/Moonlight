package net.mehvahdjukaar.moonlight.api.client.gui;

import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.client.MoonlightHubInfo;
import net.mehvahdjukaar.moonlight.core.client.MoonlightHubInfo.MediaIcon;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.TextAndImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Calendar;

public class LinkButton {

    public static final ResourceLocation MISC_ICONS = Moonlight.res("textures/gui/misc_icons.png");

    public static TextAndImageButton create(
                                    Screen parent, int x, int y, int uInd, int vInd, String url, String tooltip) {
        return create(MISC_ICONS, 64, 64, 14, 14, parent, x, y, uInd, vInd, url, tooltip);
    }

    public static TextAndImageButton create(ResourceLocation texture, int textureW, int textureH, int iconW, int iconH,
                              Screen parent, int x, int y, int uInd, int vInd, String url, String tooltip) {

        // Hijack: if the mod hardcoded one of the canonical legacy urls, redirect to
        // whatever the hub config currently says and swap the icon to our own sprite
        // (so even mods that called this with misc_icons uvs get a fresh icon and the
        // partner-server slot can switch hosts without the caller updating anything).
        Hijack h = detectHijack(url);
        if (h != null) {
            return buildButton(h.icon.texture(), iconW, iconH, iconW, iconH,
                    parent, x, y, 0, 0, h.url, h.tooltip != null ? h.tooltip : Component.literal(tooltip));
        }

        return buildButton(texture, textureW, textureH, iconW, iconH,
                parent, x, y, uInd, vInd, url, Component.literal(tooltip));
    }

    @Nullable
    private static Hijack detectHijack(String url) {
        MoonlightHubInfo old = MoonlightHubInfo.OLD_SIGNATURE;
        MoonlightHubInfo cur = MoonlightHubInfo.INSTANCE;

        MoonlightHubInfo.PartnerServerProvider oldPartner = old.partnerServer();
        if (oldPartner != null && oldPartner.url().equals(url)) {
            MoonlightHubInfo.PartnerServerProvider curPartner = cur.partnerServer();
            if (curPartner == null) return null;
            return new Hijack(curPartner.url(), curPartner.icon(),
                    Component.translatable("tooltip.moonlight.media.partner_server", curPartner.providerName()));
        }
        if (old.youtube().equals(url)) return new Hijack(cur.youtube(), MediaIcon.YOUTUBE, null);
        if (old.twitter().equals(url)) return new Hijack(cur.twitter(), MediaIcon.TWITTER, null);
        if (old.discord().equals(url)) return new Hijack(cur.discord(), MediaIcon.DISCORD, null);
        if (old.patreon().equals(url)) return new Hijack(cur.patreon(), MediaIcon.PATREON, null);
        if (old.koFi().equals(url))    return new Hijack(cur.koFi(),    MediaIcon.KO_FI,   null);
        return null;
    }

    private record Hijack(String url, MediaIcon icon, @Nullable Component tooltip) {}

    private static TextAndImageButton buildButton(ResourceLocation texture, int textureW, int textureH,
                                                  int iconW, int iconH, Screen parent, int x, int y,
                                                  int uInd, int vInd, String url, Component tooltip) {
        String finalUrl = getLink(url);
        Button.OnPress onPress = (op) -> {
            Style style = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, finalUrl));
            parent.handleComponentClicked(style);
        };

        var button = TextAndImageButton.builder(CommonComponents.EMPTY, texture, onPress)
                .usedTextureSize(iconW, iconH)
                .textureSize(textureW, textureH)
                .offset(0, 3)
                .texStart(uInd * iconW, vInd * iconH)
                .build();
        button.setPosition(x, y);
        button.setWidth(iconW + 6);
        button.height = iconH + 6;

        button.setTooltip(Tooltip.create(tooltip));
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
}
