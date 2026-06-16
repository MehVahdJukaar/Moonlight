package net.mehvahdjukaar.moonlight.core.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

@ApiStatus.Internal
public record MoonlightHubInfo(@Nullable PartnerServerProvider partnerServer, String patreon, String koFi,
                               String youtube, String twitter, String discord) {

    public enum MediaIcon implements StringRepresentable {
        YOUTUBE(),
        TWITTER(),
        DISCORD(),
        PATREON(),
        KO_FI(),
        CURSEFORGE(),
        MODRINTH(),
        GITHUB(),
        AKLIZ(),
        BISECT(),
        GENERIC_SERVER();

        public static final Codec<MediaIcon> CODEC = StringRepresentable.fromEnum(MediaIcon::values);

        private final String name;
        private final ResourceLocation texture;

        MediaIcon() {
            this.name = this.toString().toLowerCase(Locale.ROOT);
            this.texture = Moonlight.res("textures/gui/sprites/media/" + name + ".png");
        }

        public ResourceLocation texture() { return texture; }

        @Override
        public String getSerializedName() { return name; }
    }

    public static MoonlightHubInfo INSTANCE = new MoonlightHubInfo(
            null,
            "https://www.patreon.com/user?u=53696377",
            "https://ko-fi.com/mehvahdjukaar",
            "https://www.youtube.com/@MehVahdJukaar",
            "https://twitter.com/Supplementariez",
            "https://discord.com/invite/qdKRTDf8Cv"
    );

    public static MoonlightHubInfo OLD_SIGNATURE = new MoonlightHubInfo(
            new PartnerServerProvider(MediaIcon.AKLIZ, "Akliz", "https://www.akliz.net/supplementaries"),
            "https://www.patreon.com/user?u=53696377",
            "https://ko-fi.com/mehvahdjukaar",
            "https://www.youtube.com/watch?v=LSPNAtAEn28&t=1s",
            "https://twitter.com/Supplementariez?s=09",
            "https://discord.com/invite/qdKRTDf8Cv"
    );

    private static final String FETCH_URL =
            "https://raw.githubusercontent.com/MehVahdJukaar/Moonlight/1.21/supplementaries_team_info.json";

    public record PartnerServerProvider(MediaIcon icon, String providerName, String url) {
        public static final Codec<PartnerServerProvider> CODEC = RecordCodecBuilder.create(i -> i.group(
                MediaIcon.CODEC.optionalFieldOf("icon", MediaIcon.GENERIC_SERVER).forGetter(PartnerServerProvider::icon),
                Codec.STRING.fieldOf("provider_name").forGetter(PartnerServerProvider::providerName),
                Codec.STRING.fieldOf("url").forGetter(PartnerServerProvider::url)
        ).apply(i, PartnerServerProvider::new));
    }

    public static final Codec<MoonlightHubInfo> CODEC = RecordCodecBuilder.create(i -> i.group(
            PartnerServerProvider.CODEC.optionalFieldOf("partner_server").forGetter(p -> Optional.ofNullable(p.partnerServer)),
            Codec.STRING.fieldOf("patreon").forGetter(p -> p.patreon),
            Codec.STRING.fieldOf("ko_fi").forGetter(p -> p.koFi),
            Codec.STRING.fieldOf("youtube").forGetter(p -> p.youtube),
            Codec.STRING.fieldOf("twitter").forGetter(p -> p.twitter),
            Codec.STRING.fieldOf("discord").forGetter(p -> p.discord)
    ).apply(i, (ps, pat, kf, yt, tw, dc) -> new MoonlightHubInfo(ps.orElse(null), pat, kf, yt, tw, dc)));

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping()
            .registerTypeAdapter(MoonlightHubInfo.class, (JsonDeserializer<MoonlightHubInfo>)
                    (json, type, ctx) -> CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, s -> {})).create();


    public static void fetchFromServer() {
        Thread t = new Thread(() -> {
            try {
                INSTANCE = readFromURL(FETCH_URL, r -> GSON.fromJson(r, MoonlightHubInfo.class));
            } catch (Exception e) {
                Moonlight.LOGGER.warn("Failed to fetch hub info from {}: {}", FETCH_URL, e.toString());
            }
        }, "Moonlight Hub Fetcher");
        t.setDaemon(true);
        t.start();
    }

    private static <T> T readFromURL(String link, Function<Reader, T> consumer) throws Exception {
        URL url = new URL(link);
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(4000);
        connection.setReadTimeout(4000);
        String encoding = connection.getContentEncoding();
        Charset charset = (encoding == null) ? StandardCharsets.UTF_8 : Charset.forName(encoding);
        try (Reader r = new BufferedReader(new InputStreamReader(connection.getInputStream(), charset))) {
            return consumer.apply(r);
        }
    }
}
