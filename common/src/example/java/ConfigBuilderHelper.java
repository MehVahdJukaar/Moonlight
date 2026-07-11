import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.codecui.SchemaCodec;
import net.mehvahdjukaar.codecui.SchemaCodecs;
import net.mehvahdjukaar.codecui.SchemaRecord;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ModConfigHolder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.Supplier;

public class ConfigBuilderHelper {

    public static void init() {
        // Just loads the class, so we can have this all static final
    }

    public record MyObj(int first, int second) {
    }

    public static final Codec<MyObj> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.fieldOf("first").forGetter(m -> m.first),
            Codec.INT.fieldOf("second").forGetter(m -> m.second)
    ).apply(inst, MyObj::new));

    // Same object, but declared with a CodecUI schema (via the codecui jar Moonlight bundles). A SchemaCodec IS a
    // Codec, so the wire format is identical to CODEC above — but because it carries an edit surface, defineSchema
    // below can build a real in-game form for it (here: two 0..100 int sliders) instead of the "edit file" placeholder.
    public static final SchemaCodec<MyObj> SCHEMA_CODEC = SchemaRecord.create(MyObj.class, i -> i.group(
            i.field("first", SchemaCodecs.intRange(0, 100), MyObj::first),
            i.field("second", SchemaCodecs.intRange(0, 100), MyObj::second)
    ).apply(i, MyObj::new));


    public static final Supplier<Boolean> BOOL_CONFIG;
    public static final Supplier<Integer> COLOR_CONFIG;
    public static final Supplier<ResourceLocation> RESOURCE_CONFIG;
    public static final Supplier<Direction> ENUM_CONFIG;
    public static final Supplier<List<String>> LIST_CONFIG;
    public static final Supplier<MyObj> OBJECT_CONFIG;
    public static final Supplier<MyObj> SCHEMA_CONFIG;

    public static final ModConfigHolder CONFIG_SPEC;

    static {
        // Creates a common config builder. Works pretty much like forge one with a few added features
        ConfigBuilder builder = ConfigBuilder.create(Moonlight.MOD_ID, ConfigType.COMMON_SYNCED);
        // push a new category
        builder.push("misc");
        BOOL_CONFIG = builder.comment("This is a boolean config").define("bool_config", true);
        COLOR_CONFIG = builder.comment("Hex color config").defineColor("color", 0xff0000);
        RESOURCE_CONFIG = builder.comment("Resource location config").define("res", ResourceLocation.parse("hello"));
        ENUM_CONFIG = builder.comment("Enum config").define("direction", Direction.UP);
        LIST_CONFIG = builder.comment("This is a list").define("list_config", List.of("dog"));
        OBJECT_CONFIG = builder.comment("Custom object. Note that this wont show up on config screens")
                .defineObject("custom_object", () -> new MyObj(2, 4), CODEC);
        SCHEMA_CONFIG = builder.comment("Custom object with a declared schema - editable via a generated form")
                .defineSchema("schema_object", () -> new MyObj(2, 4), SCHEMA_CODEC);
        builder.pop();


        // Builds and register out config
        CONFIG_SPEC = builder.build();
        // If this is called, the config file wil be loaded immediately
        CONFIG_SPEC.forceLoad();
    }


}
