package net.mehvahdjukaar.moonlight.api.platform.configs.platform.values;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import net.mehvahdjukaar.moonlight.api.util.math.ColorUtils;
import net.mehvahdjukaar.moonlight.core.Moonlight;

public class ColorConfigValue extends IntConfigValue {

    public ColorConfigValue(String name, int defaultValue) {
        super(name, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    @Override
    public boolean loadFromJson(JsonObject element) {
        if (element.has(this.name)) {
            try {
                String s = element.get(this.name).getAsString();
                var result = ColorUtils.CODEC.decode(JsonOps.INSTANCE, new JsonPrimitive(s)).result();
                int newValue = defaultValue;
                if (result.isPresent()){
                    newValue = result.get().getFirst();
                }
                boolean changed = this.setAndTrack(newValue);
                this.markLoaded();
                return this.affectsDynamicPacks() && changed;
            } catch (Exception ignored) {
            }
            Moonlight.LOGGER.warn("Config file had incorrect entry {}, correcting", this.name);
        } else {
            Moonlight.LOGGER.warn("Config file had missing entry {}", this.name);
        }
        this.markLoaded();
        return false;
    }

    @Override
    public void saveToJson(JsonObject object) {
        if (this.value == null) this.value = defaultValue;
        object.addProperty(this.name, ColorUtils.CODEC.encodeStart(JsonOps.INSTANCE, this.value)
                .result().orElseThrow().getAsString());
    }


    @Override
    public String getExtraInfo() {
        return "Accepted format: Hexadecimal color code (e.g., #RRGGBB or #AARRGGBB)";
    }
}
