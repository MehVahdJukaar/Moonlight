package net.mehvahdjukaar.moonlight.api.platform.configs.fabric.values;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import net.mehvahdjukaar.moonlight.api.client.util.ColorUtil;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.util.math.colors.BaseColor;
import net.mehvahdjukaar.moonlight.core.Moonlight;

import java.util.function.Predicate;

public class ColorConfigValue extends IntConfigValue {

    public ColorConfigValue(String name, int defaultValue) {
        super(name, defaultValue, 0, 0xffffff);
    }

    @Override
    public void loadFromJson(JsonObject element) {
        if (element.has(this.name)) {
            try {
                String s = element.get(this.name).getAsString();
                var result = ColorUtil.CODEC.decode(JsonOps.INSTANCE, new JsonPrimitive(s)).result();
                if (result.isPresent()){
                    this.value = result.get().getFirst();
                    return;
                }
                //if not valid it defaults
                this.value = defaultValue;
            } catch (Exception ignored) {
            }
            Moonlight.LOGGER.warn("Config file had incorrect entry {}, correcting", this.name);
        } else {
            Moonlight.LOGGER.warn("Config file had missing entry {}", this.name);
        }
    }

    @Override
    public void saveToJson(JsonObject object) {
        if (this.value == null) this.value = defaultValue;
        object.addProperty(this.name, ColorUtil.CODEC.encodeStart(JsonOps.INSTANCE, this.value)
                .result().get().getAsString());
    }


}
