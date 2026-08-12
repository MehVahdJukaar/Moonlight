package net.mehvahdjukaar.moonlight.api.platform.configs.platform.values;

import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigMetadata;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.mehvahdjukaar.moonlight.api.util.math.ColorUtils;

public class ColorConfigValue extends IntConfigValue {

    private final boolean hasAlpha;

    public ColorConfigValue(String name, int defaultValue, ConfigMetadata meta) {
        this(name, defaultValue, true, meta);
    }

    public ColorConfigValue(String name, int defaultValue, boolean hasAlpha, ConfigMetadata meta) {
        super(name, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE, meta);
        this.hasAlpha = hasAlpha;
    }

    public boolean hasAlpha() {
        return hasAlpha;
    }

    @Override
    protected Integer parseValue(JsonElement element) {
        String s = element.getAsString();
        var result = codec().decode(JsonOps.INSTANCE, new JsonPrimitive(s)).result();
        return result.map(com.mojang.datafixers.util.Pair::getFirst).orElse(defaultValue);
    }

    @Override
    protected JsonElement encodeValue(Integer value) {
        return codec().encodeStart(JsonOps.INSTANCE, value).result().orElseThrow();
    }

    private Codec<Integer> codec() {
        return ColorUtils.codec(hasAlpha);
    }
}
