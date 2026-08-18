package net.mehvahdjukaar.moonlight.api.client.gui.widget;

import com.mojang.serialization.Codec;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.util.TextHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.client.MoonlightHubInfo;
import net.mehvahdjukaar.moonlight.api.client.gui.MoonlightIcons;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntFunction;

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
        MARKETPLACE(),
        GITHUB(),
        // server-host partners
        AKLIZ(),
        BISECT(),
        GENERIC_SERVER(),
        // generic link
        LINK();

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

    /**
     * A button slot the remote allow-list can turn on and off. One per MediaIcon, except SERVER, which is a single
     * slot whose icon depends on the host. A type not in the hub config's allow-list isn't drawn.
     */
    public enum ButtonType implements StringRepresentable {
        YOUTUBE,
        TWITTER,
        DISCORD,
        PATREON,
        KO_FI,
        CURSEFORGE,
        MODRINTH,
        MARKETPLACE,
        GITHUB,
        SERVER;

        public static final Codec<ButtonType> CODEC = StringRepresentable.fromValues(ButtonType::values);

        private final String name = this.toString().toLowerCase(Locale.ROOT);

        @Override
        public String getSerializedName() { return name; }
    }

    /** @return true if the hub allow-list currently permits this button type. */
    private static boolean enabled(ButtonType type) {
        return MoonlightHubInfo.INSTANCE.isButtonEnabled(type);
    }

    private static final String OWN_PACKAGE = "net/mehvahdjukaar";
    private static final Map<String, Boolean> OWN_MODS = new HashMap<>();

    // whether a mod is one of ours, by looking for our package in its jar. The social buttons point at our own pages,
    // so on somebody else's mod they'd advertise the wrong author
    public static boolean isOwnMod(String modId) {
        return OWN_MODS.computeIfAbsent(modId, id -> PlatHelper.findModResource(id, OWN_PACKAGE) != null);
    }

    public static final ResourceLocation YOUTUBE = MediaIcon.YOUTUBE.sprite();
    public static final ResourceLocation TWITTER = MediaIcon.TWITTER.sprite();
    public static final ResourceLocation DISCORD = MediaIcon.DISCORD.sprite();
    public static final ResourceLocation PATREON = MediaIcon.PATREON.sprite();
    public static final ResourceLocation KO_FI = MediaIcon.KO_FI.sprite();
    public static final ResourceLocation CURSEFORGE = MediaIcon.CURSEFORGE.sprite();
    public static final ResourceLocation MODRINTH = MediaIcon.MODRINTH.sprite();
    public static final ResourceLocation MARKETPLACE = MediaIcon.MARKETPLACE.sprite();
    public static final ResourceLocation GITHUB = MediaIcon.GITHUB.sprite();
    public static final ResourceLocation AKLIZ = MediaIcon.AKLIZ.sprite();
    public static final ResourceLocation BISECT = MediaIcon.BISECT.sprite();
    public static final ResourceLocation LINK = MediaIcon.LINK.sprite();

    public static final ResourceLocation YES = MoonlightIcons.YES;
    public static final ResourceLocation NO = MoonlightIcons.NO;

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

    // redirects to the fetched url if the given one is the canonical url we used to ship
    private static String swap(String url, String old, String fetched) {
        return old.equals(url) ? fetched : url;
    }

    public static Button youtube(Screen parent, int x, int y, String url) {
        if (!enabled(ButtonType.YOUTUBE)) return placeholderButton(x, y);
        String redirected = swap(url, MoonlightHubInfo.OLD_SIGNATURE.youtube(), MoonlightHubInfo.INSTANCE.youtube());
        return create(parent, x, y, YOUTUBE, redirected,
                Component.translatable("tooltip.moonlight.media.youtube"));
    }

    public static Button twitter(Screen parent, int x, int y, String url) {
        if (!enabled(ButtonType.TWITTER)) return placeholderButton(x, y);
        String redirected = swap(url, MoonlightHubInfo.OLD_SIGNATURE.twitter(), MoonlightHubInfo.INSTANCE.twitter());
        return create(parent, x, y, TWITTER, redirected,
                Component.translatable("tooltip.moonlight.media.twitter"));
    }

    public static Button discord(Screen parent, int x, int y, String url) {
        if (!enabled(ButtonType.DISCORD)) return placeholderButton(x, y);
        String redirected = swap(url, MoonlightHubInfo.OLD_SIGNATURE.discord(), MoonlightHubInfo.INSTANCE.discord());
        return create(parent, x, y, DISCORD, redirected,
                Component.translatable("tooltip.moonlight.media.discord"));
    }

    public static Button patreon(Screen parent, int x, int y, String url) {
        if (!enabled(ButtonType.PATREON)) return placeholderButton(x, y);
        String redirected = swap(url, MoonlightHubInfo.OLD_SIGNATURE.patreon(), MoonlightHubInfo.INSTANCE.patreon());
        return create(parent, x, y, PATREON, redirected,
                Component.translatable("tooltip.moonlight.media.patreon"));
    }

    public static Button koFi(Screen parent, int x, int y, String url) {
        if (!enabled(ButtonType.KO_FI)) return placeholderButton(x, y);
        String redirected = swap(url, MoonlightHubInfo.OLD_SIGNATURE.koFi(), MoonlightHubInfo.INSTANCE.koFi());
        return create(parent, x, y, KO_FI, redirected,
                Component.translatable("tooltip.moonlight.media.ko_fi"));
    }

    public static Button curseForge(Screen parent, int x, int y, String url) {
        if (!enabled(ButtonType.CURSEFORGE)) return placeholderButton(x, y);
        return create(parent, x, y, CURSEFORGE, url,
                Component.translatable("tooltip.moonlight.media.curseforge"));
    }

    public static Button modrinth(Screen parent, int x, int y, String url) {
        if (!enabled(ButtonType.MODRINTH)) return placeholderButton(x, y);
        return create(parent, x, y, MODRINTH, url,
                Component.translatable("tooltip.moonlight.media.modrinth"));
    }

    public static Button github(Screen parent, int x, int y, String url) {
        if (!enabled(ButtonType.GITHUB)) return placeholderButton(x, y);
        return create(parent, x, y, GITHUB, url,
                Component.translatable("tooltip.moonlight.media.github"));
    }

    public static Button marketplace(Screen parent, int x, int y, String url) {
        if (!enabled(ButtonType.MARKETPLACE)) return placeholderButton(x, y);
        return create(parent, x, y, MARKETPLACE, url,
                Component.translatable("tooltip.moonlight.media.marketplace"));
    }

    @Deprecated(forRemoval = true)
    public static Button akliz(Screen parent, int x, int y, String url, String tooltip) {
        return akliz(parent, x, y, url);
    }

    /**
     * Old Akliz button. If the url is the old canonical akliz one it hands off to serverProvider(), or to an
     * invisible button of the same size when no partner is set, so old layouts don't shift around. Any other url
     * just gets a plain akliz button.
     */
    public static Button akliz(Screen parent, int x, int y, String url) {
        MoonlightHubInfo.PartnerServerProvider oldInfo = MoonlightHubInfo.OLD_SIGNATURE.partnerServer();
        if (oldInfo != null && oldInfo.url().equals(url)) {
            Button sp = serverProvider(parent, x, y);
            return sp != null ? sp : placeholderButton(x, y);
        }
        // plain akliz-branded button still belongs to the single SERVER slot
        if (!enabled(ButtonType.SERVER)) return placeholderButton(x, y);
        return create(parent, x, y, AKLIZ, url,
                Component.translatable("tooltip.moonlight.media.akliz"));
    }

    private static final List<MediaIcon> MOD_PAGE_ORDER = List.of(MediaIcon.CURSEFORGE, MediaIcon.MODRINTH,
            MediaIcon.GITHUB, MediaIcon.DISCORD, MediaIcon.YOUTUBE, MediaIcon.TWITTER);

    private static final List<MediaIcon> HUB_ICONS = List.of(MediaIcon.DISCORD, MediaIcon.YOUTUBE, MediaIcon.TWITTER);

    private static final int MAX_UNKNOWN_LINKS = 2;

    private static Button forIcon(Screen parent, int x, int y, MediaIcon icon, String url) {
        return switch (icon) {
            case CURSEFORGE -> curseForge(parent, x, y, url);
            case MODRINTH -> modrinth(parent, x, y, url);
            case GITHUB -> github(parent, x, y, url);
            case DISCORD -> discord(parent, x, y, url);
            case YOUTUBE -> youtube(parent, x, y, url);
            case TWITTER -> twitter(parent, x, y, url);
            default -> link(parent, x, y, url);
        };
    }

    // the allow-list is keyed by button slot. A plain link has no slot, so it always shows
    private static boolean enabled(MediaIcon icon) {
        return switch (icon) {
            case CURSEFORGE -> enabled(ButtonType.CURSEFORGE);
            case MODRINTH -> enabled(ButtonType.MODRINTH);
            case GITHUB -> enabled(ButtonType.GITHUB);
            case DISCORD -> enabled(ButtonType.DISCORD);
            case YOUTUBE -> enabled(ButtonType.YOUTUBE);
            case TWITTER -> enabled(ButtonType.TWITTER);
            default -> true;
        };
    }

    /** A url whose host we don't recognise: a plain globe that just opens it. */
    public static Button link(Screen parent, int x, int y, String url) {
        return create(parent, x, y, LINK, url, Component.translatable("tooltip.moonlight.media.link"));
    }

    /**
     * Which media a url belongs to, going by its host. Null when nothing recognises it, in which case it's just a
     * website and link() is the button for it. Neither loader tags its urls, so this is all we have to go on.
     */
    @Nullable
    public static MediaIcon iconForUrl(String url) {
        String host = TextHelper.urlHost(url);
        if (host == null) return null;
        if (host.endsWith("curseforge.com")) return MediaIcon.CURSEFORGE;
        if (host.endsWith("modrinth.com")) return MediaIcon.MODRINTH;
        if (host.endsWith("github.com")) return MediaIcon.GITHUB;
        if (host.endsWith("discord.gg") || host.endsWith("discord.com") || host.endsWith("discordapp.com")) return MediaIcon.DISCORD;
        if (host.endsWith("patreon.com")) return MediaIcon.PATREON;
        if (host.endsWith("ko-fi.com")) return MediaIcon.KO_FI;
        if (host.endsWith("youtube.com") || host.equals("youtu.be")) return MediaIcon.YOUTUBE;
        if (host.endsWith("twitter.com") || host.equals("x.com")) return MediaIcon.TWITTER;
        return null;
    }

    /** Invisible, inactive button with the same footprint as a sprite button. */
    private static Button placeholderButton(int x, int y) {
        Button b = Button.builder(CommonComponents.EMPTY, op -> {}).bounds(x, y, 20, 20).build();
        b.visible = false;
        b.active = false;
        return b;
    }

    /**
     * Partner server button. Its icon, name and url come from the hub config fetched at startup. Null when no
     * partner is set, in which case callers skip the slot or fall back to akliz().
     */
    @Nullable
    public static Button serverProvider(Screen parent, int x, int y) {
        if (!enabled(ButtonType.SERVER)) return null;
        MoonlightHubInfo.PartnerServerProvider info = MoonlightHubInfo.INSTANCE.partnerServer();
        if (info == null) return null;
        Component tooltip = Component.translatable("tooltip.moonlight.media.partner_server",
                info.providerName());
        return create(parent, x, y, info.icon().sprite(), info.url(), tooltip);
    }

    /**
     * The usual Moonlight bottom bar: a centered Back button with the author's media buttons on either side. Support
     * links go left and socials right, and the mod pages fill up the emptier side so both halves stay even. Per mod
     * urls left null are read from the loader metadata instead, and buttons with no url are skipped.
     *
     * @param adder usually screen::addRenderableWidget
     */
    public static void addAuthorMediaButtons(Screen parent, Consumer<Button> adder,
                                             int centerX, int y, int spacing,
                                             String modId,
                                             @Nullable String curseforgeUrl,
                                             @Nullable String modrinthUrl,
                                             @Nullable String modSourceUrl,
                                             Runnable onBack) {
        MoonlightHubInfo hub = MoonlightHubInfo.INSTANCE;
        // our socials only belong on our own mods; the per-mod pages are fine on anyone's
        boolean ours = isOwnMod(modId);

        // the loader hands us a pile of untagged urls, so each one is sorted by its host. Explicit arguments win
        Map<MediaIcon, String> byIcon = new LinkedHashMap<>();
        List<String> unknownHosts = new ArrayList<>();
        for (String url : PlatHelper.getModLinks(modId)) {
            MediaIcon icon = iconForUrl(url);
            if (icon == null) {
                if (!unknownHosts.contains(url)) unknownHosts.add(url);
            } else {
                byIcon.putIfAbsent(icon, url);
            }
        }
        if (curseforgeUrl != null) byIcon.put(MediaIcon.CURSEFORGE, curseforgeUrl);
        if (modrinthUrl != null) byIcon.put(MediaIcon.MODRINTH, modrinthUrl);
        if (modSourceUrl != null) byIcon.put(MediaIcon.GITHUB, modSourceUrl);

        adder.accept(Button.builder(CommonComponents.GUI_BACK, b -> onBack.run())
                .bounds(centerX - 45, y, 90, 20).build());

        // support goes left and socials right, but only on our own mods
        List<IntFunction<Button>> support = new ArrayList<>();
        List<IntFunction<Button>> socials = new ArrayList<>();
        if (ours) {
            addIfEnabled(support, ButtonType.PATREON, x -> patreon(parent, x, y, hub.patreon()));
            addIfEnabled(support, ButtonType.KO_FI, x -> koFi(parent, x, y, hub.koFi()));
            addIfEnabled(socials, ButtonType.DISCORD, x -> discord(parent, x, y, hub.discord()));
            addIfEnabled(socials, ButtonType.YOUTUBE, x -> youtube(parent, x, y, hub.youtube()));
            addIfEnabled(socials, ButtonType.TWITTER, x -> twitter(parent, x, y, hub.twitter()));
            addIfEnabled(socials, ButtonType.MARKETPLACE, x -> marketplace(parent, x, y, hub.marketplace()));
            if (hub.partnerServer() != null) {
                addIfEnabled(socials, ButtonType.SERVER, x -> serverProvider(parent, x, y));
            }
        }

        List<ModLink> pages = new ArrayList<>();
        for (MediaIcon icon : MOD_PAGE_ORDER) {
            String url = byIcon.get(icon);
            // on our own mods the socials are already on the right, no point repeating them
            if (url == null || (ours && HUB_ICONS.contains(icon)) || !enabled(icon)) continue;
            pages.add(new ModLink(icon, url));
        }
        for (String url : unknownHosts.stream().limit(MAX_UNKNOWN_LINKS).toList()) {
            pages.add(new ModLink(MediaIcon.LINK, url));
        }

        // each page joins whichever side has fewer buttons, so the bar comes out even no matter how many there are
        List<IntFunction<Button>> left = new ArrayList<>();
        List<IntFunction<Button>> right = new ArrayList<>();
        int leftCount = support.size();
        int rightCount = socials.size();
        for (ModLink page : pages) {
            if (leftCount <= rightCount) {
                left.add(x -> forIcon(parent, x, y, page.icon(), page.url()));
                leftCount++;
            } else {
                right.add(x -> forIcon(parent, x, y, page.icon(), page.url()));
                rightCount++;
            }
        }
        // mod pages hug the back button, the fixed ones trail off outwards
        left.addAll(support);
        right.addAll(socials);

        placeRow(adder, left, centerX - 45 - spacing, -spacing);
        placeRow(adder, right, centerX + 45 + 2, spacing);
    }

    private static void addIfEnabled(List<IntFunction<Button>> out, ButtonType type, IntFunction<Button> factory) {
        if (enabled(type)) out.add(factory);
    }

    private static void placeRow(Consumer<Button> adder, List<IntFunction<Button>> buttons, int startX, int step) {
        int x = startX;
        for (IntFunction<Button> button : buttons) {
            adder.accept(button.apply(x));
            x += step;
        }
    }

    private record ModLink(MediaIcon icon, String url) {
    }

    /** Auto-resolves all per-mod urls from loader metadata. */
    public static void addAuthorMediaButtons(Screen parent, Consumer<Button> adder,
                                             int centerX, int y, int spacing,
                                             String modId, Runnable onBack) {
        addAuthorMediaButtons(parent, adder, centerX, y, spacing, modId, null, null, null, onBack);
    }

    /** Explicit CF + MR urls; the mod-page url is inferred from loader metadata. */
    public static void addAuthorMediaButtons(Screen parent, Consumer<Button> adder,
                                             int centerX, int y, int spacing,
                                             String modId,
                                             @Nullable String curseforgeUrl,
                                             @Nullable String modrinthUrl,
                                             Runnable onBack) {
        addAuthorMediaButtons(parent, adder, centerX, y, spacing, modId, curseforgeUrl, modrinthUrl, null, onBack);
    }
}
