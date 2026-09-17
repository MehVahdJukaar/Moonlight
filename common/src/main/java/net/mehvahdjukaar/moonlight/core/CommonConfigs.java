package net.mehvahdjukaar.moonlight.core;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.mehvahdjukaar.codecui.Schema;
import net.mehvahdjukaar.codecui.SchemaCodec;
import net.mehvahdjukaar.codecui.SchemaCodecs;
import net.mehvahdjukaar.codecui.SchemaMapCodec;
import net.mehvahdjukaar.codecui.SchemaRecord;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.moonlight.api.platform.configs.ModConfigHolder;
import net.mehvahdjukaar.moonlight.api.util.math.ColorUtils;
import net.mehvahdjukaar.moonlight.api.util.math.Range;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceLocation;

import java.time.LocalTime;
import java.time.MonthDay;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class CommonConfigs {

    public static final Supplier<Boolean> EXTRA_DEBUG;
    public static final Supplier<Boolean> EXTRA_CHILDREN_DEBUG;
    public static final Supplier<String> GLOBAL_DATAPACKS_DIR;
    public static final Supplier<Boolean> FASTER_CACHE_SEARCH;
    public static final Supplier<Boolean> MULTI_THREADED_GENERATION;

    public static final ModConfigHolder CONFIG;

    private static final SchemaCodec<Nested> NESTED_SCHEMA = SchemaRecord.create(Nested.class, i -> i.group(
            i.field("x", SchemaCodecs.intRange(-16, 16), Nested::x),
            i.field("weight", SchemaCodecs.doubleRange(0, 1), Nested::weight),
            i.field("speed", SchemaCodecs.floatRange(0, 10), Nested::speed)
    ).apply(i, Nested::new));

    private static final SchemaCodec<Circle> CIRCLE_SCHEMA = SchemaRecord.create(Circle.class, i -> i.group(
            i.field("radius", SchemaCodecs.doubleRange(0, 64), Circle::radius)
    ).apply(i, Circle::new));

    private static final SchemaCodec<Box> BOX_SCHEMA = SchemaRecord.create(Box.class, i -> i.group(
            i.field("width", SchemaCodecs.intRange(1, 64), Box::width),
            i.field("height", SchemaCodecs.intRange(1, 64), Box::height)
    ).apply(i, Box::new));

    private static final SchemaCodec<Shape> SHAPE_SCHEMA = SchemaCodecs.dispatch("type", Shape::type, Map.of(
            "circle", SchemaMapCodec.of(MapCodec.assumeMapUnsafe(CIRCLE_SCHEMA), CIRCLE_SCHEMA.schema()),
            "box", SchemaMapCodec.of(MapCodec.assumeMapUnsafe(BOX_SCHEMA), BOX_SCHEMA.schema())));

    private static final SchemaCodec<String> SLUG_SCHEMA = SchemaCodec.of(Codec.STRING,
            new Schema.Str(1, 12, Pattern.compile("[a-z_]+")));

    private static final SchemaCodec<Map<String, Object>> VALUE_BY_KEY_SCHEMA = SchemaCodec.of(
            Codec.dispatchedMap(Codec.STRING, key -> key.equals("count") ? Codec.INT : Codec.STRING),
            new Schema.DispatchedMapOf<>(Schema.str(), key -> key.equals("count") ? Schema.intRange(0, 64) : Schema.str()));

    private static final Schema.Ref<Tree> TREE_REF = new Schema.Ref<>();
    private static final SchemaCodec<Tree> TREE_SCHEMA = SchemaCodec.of(Codec.recursive("tree", self -> {
        SchemaCodec<Tree> tree = SchemaRecord.create(Tree.class, i -> i.group(
                i.field("name", SchemaCodecs.STRING, Tree::name),
                i.field("children", SchemaCodecs.list(SchemaCodec.of(self, TREE_REF)), Tree::children)
        ).apply(i, Tree::new));
        TREE_REF.bind(tree.schema());
        return tree;
    }), TREE_REF);

    private static final SchemaCodec<SchemaTest> SCHEMA_TEST = SchemaRecord.create(SchemaTest.class, i -> i.group(
            i.field("slug", SLUG_SCHEMA, SchemaTest::slug),
            i.field("enabled", SchemaCodecs.BOOL, SchemaTest::enabled),
            i.field("level", SchemaCodecs.intRange(0, 100), SchemaTest::level),
            i.field("ticks", SchemaCodecs.LONG, SchemaTest::ticks),
            i.field("facing", SchemaCodecs.enumeration(Direction.CODEC, List.of(Direction.values()), Direction::getSerializedName), SchemaTest::facing),
            i.field("color", SchemaCodecs.colorArgb(Codec.INT), SchemaTest::color),
            i.field("tint", ColorUtils.RGB_CODEC, SchemaTest::tint),
            i.field("item", SchemaCodecs.registryEntry(Registries.ITEM, BuiltInRegistries.ITEM.byNameCodec()), SchemaTest::item),
            i.field("tag", SchemaCodecs.tag(Registries.ITEM), SchemaTest::tag),
            i.optional("note", SchemaCodecs.STRING, "", SchemaTest::note),
            i.optional("bonus", NESTED_SCHEMA, SchemaTest::bonus),
            i.field("few_tags", SchemaCodecs.list(SchemaCodecs.STRING, 1, 4), SchemaTest::fewTags),
            i.field("entries", SchemaCodecs.list(NESTED_SCHEMA), SchemaTest::entries),
            i.field("by_name", SchemaCodecs.map(SchemaCodecs.STRING, NESTED_SCHEMA), SchemaTest::byName),
            i.field("value_by_key", VALUE_BY_KEY_SCHEMA, SchemaTest::valueByKey),
            i.field("pair", SchemaCodecs.pair(NESTED_SCHEMA, CIRCLE_SCHEMA), SchemaTest::pair),
            i.field("shape", SHAPE_SCHEMA, SchemaTest::shape),
            i.field("int_or_text", SchemaCodecs.either(SchemaCodecs.INT, SchemaCodecs.STRING), SchemaTest::intOrText),
            i.field("tree", TREE_SCHEMA, SchemaTest::tree),
            i.field("offset", Vec3.CODEC, SchemaTest::offset)
    ).apply(i, SchemaTest::new));

    static {
        ConfigBuilder builder = ConfigBuilder.create(Moonlight.MOD_ID, ConfigType.COMMON_SYNCED);
        builder.push("general");
        MULTI_THREADED_GENERATION = builder.comment("Enables multi-threaded generation for dynamic assets (if supported). This could improve performance on systems with more cores available.")
                .define("multi_threaded_generation", true);
        EXTRA_DEBUG = builder.comment("ONLY for debugging purpose. Turns one some debug functionality like more logging or blocktypes_debug.txt, the file can be found in ~/.minecraft/debug/dynamic_registry_dump...")
                .define("extra_debug", false);
        EXTRA_CHILDREN_DEBUG = builder.comment("Enable this will list each BlockTypes' Children. The List of BlockTypes' children will be also in the same file via EXTRA_DEBUG. NOTE: To enable this, EXTRA_DEBUG must be enabled, too.")
                .define("extra_children_debug", false);
        FASTER_CACHE_SEARCH = builder.comment("Makes the dynamic assets cache use a tree structure for indexing, drastically speeds up query time but could cost some ram.")
                .define("faster_cache_search", true);
        GLOBAL_DATAPACKS_DIR = builder.comment("Global datapack folder. A folder where you can store and load datapacks for all your worlds automatically. Set to empty string to disable")
                .worldReload()
                .define("global_datapacks_folder", "moonlight-global-datapacks");


        if (PlatHelper.isDev()) {
            builder.push("test_category");
            builder.comment("dev only");

            builder.icon("minecraft:lever").comment("feature toggle").feature("test_bool", true);
            builder.comment("plain bool, world reload").worldReload().define("test_plain_bool", true);
            builder.comment("int, game restart").gameRestart().define("test_int", 5, 0, 100);
            builder.comment("int slider").defineSlider("test_int_slider", 50, 0, 100);
            builder.comment("double").define("test_double", 2.0, 0, 22);
            builder.comment("double slider").defineSlider("test_double_slider", 0.5, 0.0, 1.0);
            builder.comment("percent").definePercentage("test_percent", 0.5);
            builder.comment("item").defineItem("test_item", ResourceLocation.parse("minecraft:diamond"));
            builder.comment("range").defineRange("test_range", Range.of(2, 8), 0, 10);
            builder.comment("vec3").defineVec3("test_vec3", new Vec3(0.5, 1.0, -0.5), -10, 10);
            builder.comment("vec3i").defineVec3i("test_vec3i", new Vec3i(1, 2, 3), -16, 16);
            builder.comment("date").defineDate("test_date", MonthDay.of(12, 25));
            builder.comment("time").defineTime("test_time", LocalTime.of(18, 30));
            builder.comment("enum").define("test_enum", Direction.NORTH);
            builder.comment("dropdown")
                    .defineDropdown("test_dropdown", "medium", List.of("potato", "low", "medium", "high", "ultra", "extreme", "overkill", "ludicrous", "maximum"));
            builder.comment("string").define("test_string", "hello");
            builder.comment("regex").defineRegex("test_regex", "\\d+(foo|bar)?");
            builder.comment("color").defineColor("test_color", 0xFFFF5555);

            builder.define("test_after_comment", false);
            builder.comment("comment after define");

            builder.comment("string list").define("test_list", List.of("a", "b", "c"));
            builder.comment("item list")
                    .defineItemList("test_item_list",
                            List.of(ResourceLocation.parse("minecraft:diamond"), ResourceLocation.parse("minecraft:emerald")));

            JsonObject json = new JsonObject();
            json.addProperty("example", 42);
            json.addProperty("enabled", true);
            builder.comment("raw json").defineJson("test_json", json);
            builder.comment("bean").defineBean("test_bean", new TestBean());
            builder.comment("schema form")
                    .defineObject("test_schema", () -> new SchemaTest("oak_log", true, 5, 72000L, Direction.NORTH, 0xFFFF5555, 0x2A77EA,
                            Items.DIAMOND, ItemTags.LOGS, "", Optional.empty(), List.of("alpha"),
                            List.of(new Nested(2, 0.25, 2), new Nested(3, 0.75, 4)), Map.of("small", new Nested(1, 0.1, 1)),
                            Map.of("count", 4, "label", "four"), Pair.of(new Nested(1, 0.5, 1), new Circle(2.5)), new Box(2, 3),
                            Either.left(3), new Tree("root", List.of(new Tree("leaf", List.of()))), new Vec3(0, 1, 0)), SCHEMA_TEST);
            builder.comment("schema list")
                    .defineObjectList("test_schema_list", () -> List.of(new Nested(4, 0.5, 1)), NESTED_SCHEMA);

            builder.icon("minecraft:oak_log").push("nested");
            builder.comment("nested float").define("nested_float", 0.5f, 0f, 1f);
            builder.pop();

            builder.icon("minecraft:redstone").pushFeature("test_feature", true);
            builder.comment("greys out with the feature").define("feature_speed", 1.0, 0, 10);
            builder.pushFeature("test_sub_feature", true);
            builder.comment("off when the parent is off").define("sub_power", 3, 0, 9);
            builder.pop();
            builder.pop();

            builder.pop();
        }

        builder.pop();

        CONFIG = builder.build();
        CONFIG.forceLoad();
    }

    public static void init() {
    }

    public static class TestBean {
        public String name = "hello";
        public int count = 3;
        public boolean flag = true;
    }

    public record SchemaTest(String slug, boolean enabled, int level, long ticks, Direction facing, int color, int tint,
                             Item item, TagKey<Item> tag, String note, Optional<Nested> bonus, List<String> fewTags,
                             List<Nested> entries, Map<String, Nested> byName, Map<String, Object> valueByKey,
                             Pair<Nested, Circle> pair, Shape shape, Either<Integer, String> intOrText,
                             Tree tree, Vec3 offset) {
    }

    public record Nested(int x, double weight, float speed) {
    }

    public sealed interface Shape {
        String type();
    }

    public record Circle(double radius) implements Shape {
        @Override
        public String type() {
            return "circle";
        }
    }

    public record Box(int width, int height) implements Shape {
        @Override
        public String type() {
            return "box";
        }
    }

    public record Tree(String name, List<Tree> children) {
    }
}
