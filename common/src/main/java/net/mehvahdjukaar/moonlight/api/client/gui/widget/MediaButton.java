package net.mehvahdjukaar.moonlight.api.client.gui.widget;

import com.mojang.serialization.Codec;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
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
        MARKETPLACE(),
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
     * The usual Moonlight bottom bar: a centered Back button with the author's media buttons on either side, support
     * and mod pages going left, socials going right. Per mod urls left null are read from the loader metadata
     * instead, and buttons with no url are skipped.
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
        if (curseforgeUrl == null) curseforgeUrl = PlatHelper.getModCurseforgeUrl(modId);
        if (modrinthUrl == null)   modrinthUrl   = PlatHelper.getModModrinthUrl(modId);
        if (modSourceUrl == null)  modSourceUrl  = PlatHelper.getModSourcesUrl(modId);
        MoonlightHubInfo hub = MoonlightHubInfo.INSTANCE;
        // our socials only belong on our own mods; the per-mod pages are fine on anyone's
        boolean ours = isOwnMod(modId);

        adder.accept(Button.builder(CommonComponents.GUI_BACK, b -> onBack.run())
                .bounds(centerX - 45, y, 90, 20).build());

        // going leftward from the back button
        int left = centerX - 45 - spacing;
        if (ours) {
            adder.accept(patreon(parent, left, y, hub.patreon())); left -= spacing;
            adder.accept(koFi(parent, left, y, hub.koFi()));       left -= spacing;
        }
        if (curseforgeUrl != null) { adder.accept(curseForge(parent, left, y, curseforgeUrl)); left -= spacing; }
        if (modrinthUrl != null)   { adder.accept(modrinth(parent, left, y, modrinthUrl));     left -= spacing; }
        if (modSourceUrl != null)  { adder.accept(github(parent, left, y, modSourceUrl));      left -= spacing; }

        if (!ours) return;
        // going rightward from the back button
        int right = centerX + 45 + 2;
        adder.accept(discord(parent, right, y, hub.discord()));         right += spacing;
        adder.accept(youtube(parent, right, y, hub.youtube()));         right += spacing;
        adder.accept(twitter(parent, right, y, hub.twitter()));         right += spacing;
        adder.accept(marketplace(parent, right, y, hub.marketplace())); right += spacing;
        Button sp = serverProvider(parent, right, y);
        if (sp != null) { adder.accept(sp); }
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
