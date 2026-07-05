package net.mehvahdjukaar.moonlight.api.client.gui;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * @deprecated moved to {@link net.mehvahdjukaar.moonlight.api.client.gui.widget.MediaButton}. Kept only as a
 * compatibility alias and scheduled for removal — use the {@code widget} class directly. It {@code extends} the new
 * class so the public sprite constants ({@code YOUTUBE}, {@code AKLIZ}, …) and the nested {@code MediaIcon}/
 * {@code ButtonType} enums stay accessible here, and every public factory method is re-declared below as a
 * deprecated delegator so existing source and compiled (binary) call sites against this class keep resolving.
 */
@Deprecated(forRemoval = true)
public class MediaButton extends net.mehvahdjukaar.moonlight.api.client.gui.widget.MediaButton {

    @Deprecated
    public static Button create(Screen parent, int x, int y, ResourceLocation texture, String url, String tooltip) {
        return net.mehvahdjukaar.moonlight.api.client.gui.widget.MediaButton.create(parent, x, y, texture, url, tooltip);
    }

    @Deprecated
    public static Button create(Screen parent, int x, int y, ResourceLocation texture, String url, Component tooltip) {
        return net.mehvahdjukaar.moonlight.api.client.gui.widget.MediaButton.create(parent, x, y, texture, url, tooltip);
    }

    @Deprecated
    public static Button create(int iconW, int iconH, ResourceLocation texture, Screen parent, int x, int y, String url, String tooltip) {
        return net.mehvahdjukaar.moonlight.api.client.gui.widget.MediaButton.create(iconW, iconH, texture, parent, x, y, url, tooltip);
    }

    @Deprecated
    public static Button create(int iconW, int iconH, ResourceLocation texture, Screen parent, int x, int y, String url, Component tooltip) {
        return net.mehvahdjukaar.moonlight.api.client.gui.widget.MediaButton.create(iconW, iconH, texture, parent, x, y, url, tooltip);
    }

    @Deprecated
    public static Button youtube(Screen parent, int x, int y, String url) {
        return net.mehvahdjukaar.moonlight.api.client.gui.widget.MediaButton.youtube(parent, x, y, url);
    }

    @Deprecated
    public static Button twitter(Screen parent, int x, int y, String url) {
        return net.mehvahdjukaar.moonlight.api.client.gui.widget.MediaButton.twitter(parent, x, y, url);
    }

    @Deprecated
    public static Button discord(Screen parent, int x, int y, String url) {
        return net.mehvahdjukaar.moonlight.api.client.gui.widget.MediaButton.discord(parent, x, y, url);
    }

    @Deprecated
    public static Button patreon(Screen parent, int x, int y, String url) {
        return net.mehvahdjukaar.moonlight.api.client.gui.widget.MediaButton.patreon(parent, x, y, url);
    }

    @Deprecated
    public static Button koFi(Screen parent, int x, int y, String url) {
        return net.mehvahdjukaar.moonlight.api.client.gui.widget.MediaButton.koFi(parent, x, y, url);
    }

    @Deprecated
    public static Button curseForge(Screen parent, int x, int y, String url) {
        return net.mehvahdjukaar.moonlight.api.client.gui.widget.MediaButton.curseForge(parent, x, y, url);
    }

    @Deprecated
    public static Button modrinth(Screen parent, int x, int y, String url) {
        return net.mehvahdjukaar.moonlight.api.client.gui.widget.MediaButton.modrinth(parent, x, y, url);
    }

    @Deprecated
    public static Button github(Screen parent, int x, int y, String url) {
        return net.mehvahdjukaar.moonlight.api.client.gui.widget.MediaButton.github(parent, x, y, url);
    }

    @Deprecated
    public static Button marketplace(Screen parent, int x, int y, String url) {
        return net.mehvahdjukaar.moonlight.api.client.gui.widget.MediaButton.marketplace(parent, x, y, url);
    }

    @Deprecated(forRemoval = true)
    public static Button akliz(Screen parent, int x, int y, String url, String tooltip) {
        // the legacy 5-arg form always ignored its tooltip; forward to the current 4-arg method
        return net.mehvahdjukaar.moonlight.api.client.gui.widget.MediaButton.akliz(parent, x, y, url);
    }

    @Deprecated
    public static Button akliz(Screen parent, int x, int y, String url) {
        return net.mehvahdjukaar.moonlight.api.client.gui.widget.MediaButton.akliz(parent, x, y, url);
    }

    @Nullable
    @Deprecated
    public static Button serverProvider(Screen parent, int x, int y) {
        return net.mehvahdjukaar.moonlight.api.client.gui.widget.MediaButton.serverProvider(parent, x, y);
    }

    @Deprecated
    public static void addAuthorMediaButtons(Screen parent, Consumer<Button> adder,
                                             int centerX, int y, int spacing,
                                             String modId,
                                             @Nullable String curseforgeUrl,
                                             @Nullable String modrinthUrl,
                                             @Nullable String modSourceUrl,
                                             Runnable onBack) {
        net.mehvahdjukaar.moonlight.api.client.gui.widget.MediaButton.addAuthorMediaButtons(
                parent, adder, centerX, y, spacing, modId, curseforgeUrl, modrinthUrl, modSourceUrl, onBack);
    }

    @Deprecated
    public static void addAuthorMediaButtons(Screen parent, Consumer<Button> adder,
                                             int centerX, int y, int spacing,
                                             String modId, Runnable onBack) {
        net.mehvahdjukaar.moonlight.api.client.gui.widget.MediaButton.addAuthorMediaButtons(
                parent, adder, centerX, y, spacing, modId, onBack);
    }

    @Deprecated
    public static void addAuthorMediaButtons(Screen parent, Consumer<Button> adder,
                                             int centerX, int y, int spacing,
                                             String modId,
                                             @Nullable String curseforgeUrl,
                                             @Nullable String modrinthUrl,
                                             Runnable onBack) {
        net.mehvahdjukaar.moonlight.api.client.gui.widget.MediaButton.addAuthorMediaButtons(
                parent, adder, centerX, y, spacing, modId, curseforgeUrl, modrinthUrl, onBack);
    }
}
