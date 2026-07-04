package net.mehvahdjukaar.moonlight.api.platform.configs.platform.values;

import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigMeta;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import net.mehvahdjukaar.moonlight.api.util.math.ColorUtils;

public class ColorConfigValue extends IntConfigValue {

    public ColorConfigValue(String name, int defaultValue, ConfigMeta meta) {
        super(name, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE, meta);
    }

    @Override
    protected Integer parseValue(JsonElement element) {
        String s = element.getAsString();
        var result = ColorUtils.CODEC.decode(JsonOps.INSTANCE, new JsonPrimitive(s)).result();
        return result.map(com.mojang.datafixers.util.Pair::getFirst).orElse(defaultValue);
    }

    @Override
    protected JsonElement encodeValue(Integer value) {
        return ColorUtils.CODEC.encodeStart(JsonOps.INSTANCE, value).result().orElseThrow();
    }

    @Override
    public String getExtraInfo() {
        return "Accepted format: Hexadecimal color code (e.g., #RRGGBB or #AARRGGBB)";
    }
}
