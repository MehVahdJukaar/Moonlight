package net.mehvahdjukaar.moonlight.api.client.gui;

import com.mojang.serialization.Codec;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.client.MoonlightHubInfo;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Nullable;

import java.util.Calendar;
import java.util.Locale;
import java.util.function.Consumer;

public class MediaButton {

    public enum MediaIcon implements StringRepresentable {
        // socials
        YOUTUBE(),
        TWITTER(),
        DISCORD(),
        PATREON(),
        KO_FI(),
        CURSEFORGE(),
        MODRINTH(),
        GITHUB(),
        // server-host partners
        AKLIZ(),
        BISECT(),
        GENERIC_SERVER();

        public static final Codec<MediaIcon> CODEC = StringRepresentable.fromValues(MediaIcon::values);

        private final String name;
        private final ResourceLocation sprite;

        MediaIcon() {
            this.name = this.toString().toLowerCase(Locale.ROOT);
            this.sprite = Moonlight.res("media/" + name);
        }

        public ResourceLocation sprite() { return sprite; }

        @Override
        public String getSerializedName() { return name; }
    }

    public static final ResourceLocation YOUTUBE = MediaIcon.YOUTUBE.sprite();
    public static final ResourceLocation TWITTER = MediaIcon.TWITTER.sprite();
    public static final ResourceLocation DISCORD = MediaIcon.DISCORD.sprite();
    public static final ResourceLocation PATREON = MediaIcon.PATREON.sprite();
    public static final ResourceLocation KO_FI = MediaIcon.KO_FI.sprite();
    public static final ResourceLocation CURSEFORGE = MediaIcon.CURSEFORGE.sprite();
    public static final ResourceLocation MODRINTH = MediaIcon.MODRINTH.sprite();
    public static final ResourceLocation GITHUB = MediaIcon.GITHUB.sprite();
    public static final ResourceLocation AKLIZ = MediaIcon.AKLIZ.sprite();
    public static final ResourceLocation BISECT = MediaIcon.BISECT.sprite();

    public static final ResourceLocation YES = Moonlight.res("yes");
    public static final ResourceLocation NO = Moonlight.res("no");

    public static Button create(Screen parent, int x, int y, ResourceLocation texture,
                                String url, String tooltip) {
        return create(parent, x, y, texture, url, Component.literal(tooltip));
    }

    public static Button create(Screen parent, int x, int y, ResourceLocation texture,
                                String url, Component tooltip) {
        return create(14, 14, texture, parent, x, y, url, tooltip);
    }

    public static Button create(int iconW, int iconH, ResourceLocation texture,
                                Screen parent, int x, int y, String url, String tooltip) {
        return create(iconW, iconH, texture, parent, x, y, url, Component.literal(tooltip));
    }

    public static Button create(int iconW, int iconH, ResourceLocation texture,
                                Screen parent, int x, int y, String url, Component tooltip) {

        String finalUrl = getLink(url);
        Button.OnPress onPress = (op) -> {
            Style style = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, finalUrl));
            parent.handleComponentClicked(style);
        };

        var button = new SpriteIconButton.Builder(CommonComponents.EMPTY, onPress, true)
                .sprite(texture, iconW, iconH)
                .size(iconW + 6, iconH + 6)
                .build();

        button.setTooltip(Tooltip.create(tooltip));
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

    /** Redirects {@code url} to {@code fetched} if it matches the previously-shipped canonical url. */
    private static String swap(String url, String old, String fetched) {
        return old.equals(url) ? fetched : url;
    }

    public static Button youtube(Screen parent, int x, int y, String url) {
        String redirected = swap(url, MoonlightHubInfo.OLD_SIGNATURE.youtube(), MoonlightHubInfo.INSTANCE.youtube());
        return create(parent, x, y, YOUTUBE, redirected,
                Component.translatable("tooltip.moonlight.media.youtube"));
    }

    public static Button twitter(Screen parent, int x, int y, String url) {
        String redirected = swap(url, MoonlightHubInfo.OLD_SIGNATURE.twitter(), MoonlightHubInfo.INSTANCE.twitter());
        return create(parent, x, y, TWITTER, redirected,
                Component.translatable("tooltip.moonlight.media.twitter"));
    }

    public static Button discord(Screen parent, int x, int y, String url) {
        String redirected = swap(url, MoonlightHubInfo.OLD_SIGNATURE.discord(), MoonlightHubInfo.INSTANCE.discord());
        return create(parent, x, y, DISCORD, redirected,
                Component.translatable("tooltip.moonlight.media.discord"));
    }

    public static Button patreon(Screen parent, int x, int y, String url) {
        String redirected = swap(url, MoonlightHubInfo.OLD_SIGNATURE.patreon(), MoonlightHubInfo.INSTANCE.patreon());
        return create(parent, x, y, PATREON, redirected,
                Component.translatable("tooltip.moonlight.media.patreon"));
    }

    public static Button koFi(Screen parent, int x, int y, String url) {
        String redirected = swap(url, MoonlightHubInfo.OLD_SIGNATURE.koFi(), MoonlightHubInfo.INSTANCE.koFi());
        return create(parent, x, y, KO_FI, redirected,
                Component.translatable("tooltip.moonlight.media.ko_fi"));
    }

    public static Button curseForge(Screen parent, int x, int y, String url) {
        return create(parent, x, y, CURSEFORGE, url,
                Component.translatable("tooltip.moonlight.media.curseforge"));
    }

    public static Button modrinth(Screen parent, int x, int y, String url) {
        return create(parent, x, y, MODRINTH, url,
                Component.translatable("tooltip.moonlight.media.modrinth"));
    }

    public static Button github(Screen parent, int x, int y, String url) {
        return create(parent, x, y, GITHUB, url,
                Component.translatable("tooltip.moonlight.media.github"));
    }

    @Deprecated(forRemoval = true)
    public static Button akliz(Screen parent, int x, int y, String url, String tooltip) {
        return akliz(parent, x, y, url);
    }

    /**
     * Legacy Akliz button. When the passed {@code url} matches the canonical
     * old akliz signature it delegates to {@link #serverProvider(Screen, int, int)};
     * if no partner is currently configured, returns an invisible placeholder
     * widget of the same dimensions so existing layouts stay intact. When the
     * url does not match the legacy signature, renders a plain akliz-branded
     * button with the url passed in.
     */
    public static Button akliz(Screen parent, int x, int y, String url) {
        MoonlightHubInfo.PartnerServerProvider oldInfo = MoonlightHubInfo.OLD_SIGNATURE.partnerServer();
        if (oldInfo != null && oldInfo.url().equals(url)) {
            Button sp = serverProvider(parent, x, y);
            return sp != null ? sp : placeholderButton(x, y);
        }
        return create(parent, x, y, AKLIZ, url,
                Component.translatable("tooltip.moonlight.media.akliz"));
    }

    /** Invisible, inactive button of the same footprint as a sprite button. */
    private static Button placeholderButton(int x, int y) {
        Button b = Button.builder(CommonComponents.EMPTY, op -> {}).bounds(x, y, 20, 20).build();
        b.visible = false;
        b.active = false;
        return b;
    }

    /**
     * Dynamic partner-server button. Icon, provider name and url come from the
     * hub config fetched on startup. Returns {@code null} when no partner is
     * currently configured; callers should skip the slot in that case (or use
     * {@link #akliz(Screen, int, int, String)} which falls back to a plain
     * akliz button).
     */
    @Nullable
    public static Button serverProvider(Screen parent, int x, int y) {
        MoonlightHubInfo.PartnerServerProvider info = MoonlightHubInfo.INSTANCE.partnerServer();
        if (info == null) return null;
        Component tooltip = Component.translatable("tooltip.moonlight.media.partner_server",
                info.providerName());
        return create(parent, x, y, info.icon().sprite(), info.url(), tooltip);
    }

    /**
     * Adds the author's standard media buttons in a single row: patreon, ko-fi,
     * youtube, twitter, discord, current partner-server, plus the per-mod
     * curseforge, modrinth and mod page (github wiki) buttons. Author-wide urls
     * come from the fetched hub config; per-mod urls fall back to whatever is
     * declared in the loader metadata ({@code fabric.mod.json} or
     * {@code neoforge.mods.toml}) when {@code null} is passed.
     * <p>Buttons for urls that remain unresolved (null in args and not declared
     * in metadata) are silently skipped.
     *
     * @param adder        typically {@code screen::addRenderableWidget}
     * @param modId        mod id used to resolve metadata fallbacks
     * @param curseforgeUrl explicit CurseForge page, or {@code null} to look up via metadata
     * @param modrinthUrl   explicit Modrinth page, or {@code null} to look up via metadata
     * @param modSourceUrl    explicit mod home/wiki url, or {@code null} to look up via metadata
     * @return the next x position after the last placed button
     */
    public static int addAuthorMediaRow(Screen parent, Consumer<Button> adder, int x, int y, int spacing,
                                        String modId,
                                        @Nullable String curseforgeUrl,
                                        @Nullable String modrinthUrl,
                                        @Nullable String modSourceUrl) {
        if (curseforgeUrl == null) curseforgeUrl = PlatHelper.getModCurseforgeUrl(modId);
        if (modrinthUrl == null)   modrinthUrl   = PlatHelper.getModModrinthUrl(modId);
        if (modSourceUrl == null)    modSourceUrl    = PlatHelper.getModSourcesUrl(modId);
        MoonlightHubInfo hub = MoonlightHubInfo.INSTANCE;
        int cur = x;
        adder.accept(patreon(parent, cur, y, hub.patreon()));   cur += spacing;
        adder.accept(koFi(parent, cur, y, hub.koFi()));         cur += spacing;
        if (curseforgeUrl != null) { adder.accept(curseForge(parent, cur, y, curseforgeUrl)); cur += spacing; }
        if (modrinthUrl != null)   { adder.accept(modrinth(parent, cur, y, modrinthUrl));     cur += spacing; }
        if (modSourceUrl != null)    { adder.accept(github(parent, cur, y, modSourceUrl));        cur += spacing; }
        adder.accept(youtube(parent, cur, y, hub.youtube()));   cur += spacing;
        adder.accept(twitter(parent, cur, y, hub.twitter()));   cur += spacing;
        adder.accept(discord(parent, cur, y, hub.discord()));   cur += spacing;
        Button sp = serverProvider(parent, cur, y);
        if (sp != null) { adder.accept(sp); cur += spacing; }
        return cur;
    }

    /** Auto-resolves all per-mod urls from loader metadata. */
    public static int addAuthorMediaRow(Screen parent, Consumer<Button> adder, int x, int y, int spacing,
                                        String modId) {
        return addAuthorMediaRow(parent, adder, x, y, spacing, modId, null, null, null);
    }

    /**
     * Explicit CurseForge + Modrinth urls; the mod-page (github) url is inferred
     * from loader metadata.
     */
    public static int addAuthorMediaRow(Screen parent, Consumer<Button> adder, int x, int y, int spacing,
                                        String modId,
                                        @Nullable String curseforgeUrl,
                                        @Nullable String modrinthUrl) {
        return addAuthorMediaRow(parent, adder, x, y, spacing, modId, curseforgeUrl, modrinthUrl, null);
    }

    /**
     * Places the per-mod and support buttons RIGHT-to-LEFT starting at {@code x}:
     * patreon (at x), ko-fi, curseforge, modrinth, mod page. Designed to sit on
     * the LEFT of a centered widget (e.g. a Back button).
     * <p>Per-mod urls fall back to loader metadata when {@code null}; buttons
     * whose url is unresolved are skipped (no slot consumed).
     */
    public static int addAuthorMediaButtonsLeft(Screen parent, Consumer<Button> adder, int x, int y, int spacing,
                                                String modId,
                                                @Nullable String curseforgeUrl,
                                                @Nullable String modrinthUrl,
                                                @Nullable String modSourceUrl) {
        if (curseforgeUrl == null) curseforgeUrl = PlatHelper.getModCurseforgeUrl(modId);
        if (modrinthUrl == null)   modrinthUrl   = PlatHelper.getModModrinthUrl(modId);
        if (modSourceUrl == null)  modSourceUrl  = PlatHelper.getModSourcesUrl(modId);
        MoonlightHubInfo hub = MoonlightHubInfo.INSTANCE;
        int cur = x;
        adder.accept(patreon(parent, cur, y, hub.patreon())); cur -= spacing;
        adder.accept(koFi(parent, cur, y, hub.koFi()));       cur -= spacing;
        if (curseforgeUrl != null) { adder.accept(curseForge(parent, cur, y, curseforgeUrl)); cur -= spacing; }
        if (modrinthUrl != null)   { adder.accept(modrinth(parent, cur, y, modrinthUrl));     cur -= spacing; }
        if (modSourceUrl != null)  { adder.accept(github(parent, cur, y, modSourceUrl));      cur -= spacing; }
        return cur;
    }

    /** Auto-resolves all per-mod urls from loader metadata. */
    public static int addAuthorMediaButtonsLeft(Screen parent, Consumer<Button> adder, int x, int y, int spacing,
                                                String modId) {
        return addAuthorMediaButtonsLeft(parent, adder, x, y, spacing, modId, null, null, null);
    }

    /** Explicit CF + MR urls; the mod-page url is inferred from loader metadata. */
    public static int addAuthorMediaButtonsLeft(Screen parent, Consumer<Button> adder, int x, int y, int spacing,
                                                String modId,
                                                @Nullable String curseforgeUrl,
                                                @Nullable String modrinthUrl) {
        return addAuthorMediaButtonsLeft(parent, adder, x, y, spacing, modId, curseforgeUrl, modrinthUrl, null);
    }

    /**
     * Places the social + partner-server buttons LEFT-to-RIGHT starting at
     * {@code x}: discord (at x), youtube, twitter, partner-server. Designed to
     * sit on the RIGHT of a centered widget. The partner-server slot is skipped
     * when no partner is configured in the hub.
     */
    public static int addAuthorMediaButtonsRight(Screen parent, Consumer<Button> adder, int x, int y, int spacing) {
        MoonlightHubInfo hub = MoonlightHubInfo.INSTANCE;
        int cur = x;
        adder.accept(discord(parent, cur, y, hub.discord())); cur += spacing;
        adder.accept(youtube(parent, cur, y, hub.youtube())); cur += spacing;
        adder.accept(twitter(parent, cur, y, hub.twitter())); cur += spacing;
        Button sp = serverProvider(parent, cur, y);
        if (sp != null) { adder.accept(sp); cur += spacing; }
        return cur;
    }
}
