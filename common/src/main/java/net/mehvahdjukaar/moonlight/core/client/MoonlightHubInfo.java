package net.mehvahdjukaar.moonlight.core.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.client.gui.widget.MediaButton;
import net.mehvahdjukaar.moonlight.api.util.FileDownloadUtils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@ApiStatus.Internal
public record MoonlightHubInfo(@Nullable PartnerServerProvider partnerServer, String patreon, String koFi,
                               String youtube, String twitter, String discord, String marketplace,
                               Set<MediaButton.ButtonType> buttons) {

    /** @return true if this button should be shown (i.e. it's in the remote allow-list) */
    public boolean isButtonEnabled(MediaButton.ButtonType type) {
        return buttons.contains(type);
    }

    // by default every button is allowed; the remote config can narrow this down
    public static final Set<MediaButton.ButtonType> ALL_BUTTONS = Set.of(MediaButton.ButtonType.values());

    private static final String MARKETPLACE_URL =
            "https://www.minecraft.net/en-us/marketplace/pdp/razzleberries/supplementaries/c18ca233-28af-416b-9618-c0c59b64569d";

    //default offline safe instance
    public static MoonlightHubInfo INSTANCE = new MoonlightHubInfo(
            null,
            "https://www.patreon.com/user?u=53696377",
            "https://ko-fi.com/mehvahdjukaar",
            "https://www.youtube.com/@MehVahdJukaar",
            "https://twitter.com/Supplementariez",
            "https://discord.com/invite/qdKRTDf8Cv",
            MARKETPLACE_URL,
            ALL_BUTTONS
    );

    //default one
    public static MoonlightHubInfo OLD_SIGNATURE = new MoonlightHubInfo(
            new PartnerServerProvider(MediaButton.MediaIcon.AKLIZ, "Akliz", "https://www.akliz.net/supplementaries"),
            "https://www.patreon.com/user?u=53696377",
            "https://ko-fi.com/mehvahdjukaar",
            "https://www.youtube.com/watch?v=LSPNAtAEn28&t=1s",
            "https://twitter.com/Supplementariez?s=09",
            "https://discord.com/invite/qdKRTDf8Cv",
            "", // marketplace is a new button, not part of the legacy redirect signature
            ALL_BUTTONS
    );

    private static final String FETCH_URL =
            "https://raw.githubusercontent.com/MehVahdJukaar/Moonlight/1.21/supplementaries_team_info.json";

    public record PartnerServerProvider(MediaButton.MediaIcon icon, String providerName, String url) {
        public static final Codec<PartnerServerProvider> CODEC = RecordCodecBuilder.create(i -> i.group(
                // lenient so an unknown/future icon id falls back to the generic one instead of failing the whole config
                MediaButton.MediaIcon.CODEC.lenientOptionalFieldOf("icon", MediaButton.MediaIcon.GENERIC_SERVER).forGetter(PartnerServerProvider::icon),
                Codec.STRING.fieldOf("provider_name").forGetter(PartnerServerProvider::providerName),
                Codec.STRING.fieldOf("url").forGetter(PartnerServerProvider::url)
        ).apply(i, PartnerServerProvider::new));
    }

    private static final List<String> DEFAULT_BUTTON_NAMES = Arrays.stream(MediaButton.ButtonType.values())
            .map(MediaButton.ButtonType::getSerializedName).toList();

    /** Resolves button ids, silently dropping any this (possibly older) client doesn't recognise. */
    private static Set<MediaButton.ButtonType> toButtons(List<String> names) {
        EnumSet<MediaButton.ButtonType> set = EnumSet.noneOf(MediaButton.ButtonType.class);
        for (String n : names) {
            for (MediaButton.ButtonType b : MediaButton.ButtonType.values()) {
                if (b.getSerializedName().equals(n)) {
                    set.add(b);
                    break;
                }
            }
        }
        return set;
    }

    public static final Codec<MoonlightHubInfo> CODEC = RecordCodecBuilder.create(i -> i.group(
            PartnerServerProvider.CODEC.optionalFieldOf("partner_server").forGetter(p -> Optional.ofNullable(p.partnerServer)),
            Codec.STRING.fieldOf("patreon").forGetter(p -> p.patreon),
            Codec.STRING.fieldOf("ko_fi").forGetter(p -> p.koFi),
            Codec.STRING.fieldOf("youtube").forGetter(p -> p.youtube),
            Codec.STRING.fieldOf("twitter").forGetter(p -> p.twitter),
            Codec.STRING.fieldOf("discord").forGetter(p -> p.discord),
            Codec.STRING.optionalFieldOf("marketplace", MARKETPLACE_URL).forGetter(p -> p.marketplace),
            // parsed as raw strings, not an enum codec, so an unknown/future button id can't fail the whole config
            Codec.STRING.listOf().optionalFieldOf("buttons", DEFAULT_BUTTON_NAMES)
                    .forGetter(p -> p.buttons.stream().map(MediaButton.ButtonType::getSerializedName).toList())
    ).apply(i, (ps, pat, kf, yt, tw, dc, mk, btnNames) ->
            new MoonlightHubInfo(ps.orElse(null), pat, kf, yt, tw, dc, mk, toButtons(btnNames))));

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping()
            .registerTypeAdapter(MoonlightHubInfo.class, (JsonDeserializer<MoonlightHubInfo>)
                    (json, type, ctx) -> CODEC.parse(JsonOps.INSTANCE, json).getOrThrow()).create();


    public static void fetchFromServer() {
        Thread t = new Thread(() -> {
            try {
                INSTANCE = GSON.fromJson(FileDownloadUtils.readString(FETCH_URL), MoonlightHubInfo.class);
            } catch (Exception e) {
                Moonlight.LOGGER.warn("Failed to fetch hub info from {}: {}", FETCH_URL, e.toString());
            }
        }, "Moonlight Hub Fetcher");
        t.setDaemon(true);
        t.start();
    }
}
