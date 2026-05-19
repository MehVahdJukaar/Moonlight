package net.mehvahdjukaar.moonlight.api.platform.configs.platform.values;

import com.google.gson.JsonObject;
import net.mehvahdjukaar.moonlight.core.Moonlight;

public class EnumConfigValue<T extends Enum<T>> extends ConfigValue<T> {

    private final T[] acceptedValues;

    public EnumConfigValue(String name, T defaultValue) {
        super(name, defaultValue);
        this.acceptedValues = defaultValue.getDeclaringClass().getEnumConstants();
    }

    @Override
    public boolean isValid(T value) {
        return true;
    }

    public Class<T> getEnumClass(){
        return this.defaultValue.getDeclaringClass();
    }

    @Override
    public boolean loadFromJson(JsonObject element) {
        if (element.has(this.name)) {
            try {
                String s = element.get(this.name).getAsString();
                T newValue = null;
                for(var v : acceptedValues){
                    if(v.name().equals(s)){
                        newValue = v;
                        break;
                    }
                }
                if (newValue == null) {
                    //if not valid it defaults
                    newValue = defaultValue;
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
        object.addProperty(this.name, this.value.name());
    }


    @Override
    public String getExtraInfo() {
        return "Accepted values: " + String.join(", ",
                java.util.Arrays.stream(acceptedValues).map(Enum::name).toList());
    }
}
