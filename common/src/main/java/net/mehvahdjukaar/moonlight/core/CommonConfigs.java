package net.mehvahdjukaar.moonlight.core;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import net.mehvahdjukaar.codecui.SchemaCodec;
import net.mehvahdjukaar.codecui.SchemaCodecs;
import net.mehvahdjukaar.codecui.SchemaRecord;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.moonlight.api.platform.configs.ModConfigHolder;
import net.mehvahdjukaar.moonlight.api.util.math.Range;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.Supplier;

public class CommonConfigs {

    public static final Supplier<Boolean> EXTRA_DEBUG;
    public static final Supplier<Boolean> EXTRA_CHILDREN_DEBUG;
    public static final Supplier<String> GLOBAL_DATAPACKS_DIR;
    public static final Supplier<Boolean> FASTER_CACHE_SEARCH;
    public static final Supplier<Boolean> MULTI_THREADED_GENERATION;

    public static final ModConfigHolder CONFIG;

    // Dev-only demo for defineObject (below): a codec that carries a CodecUI schema, so the native config screen can
    // build a real form for it instead of the "edit file" placeholder. Declared before the static block that uses it.
    private static final SchemaCodec<Nested> NESTED_SCHEMA = SchemaRecord.create(Nested.class, i -> i.group(
            i.field("x", SchemaCodecs.intRange(-16, 16), Nested::x),
            i.field("weight", SchemaCodecs.doubleRange(0, 1), Nested::weight)
    ).apply(i, Nested::new));

    private static final SchemaCodec<SchemaTest> SCHEMA_TEST = SchemaRecord.create(SchemaTest.class, i -> i.group(
            i.field("name", SchemaCodecs.STRING, SchemaTest::name),
            i.field("enabled", SchemaCodecs.BOOL, SchemaTest::enabled),
            i.field("level", SchemaCodecs.intRange(0, 100), SchemaTest::level),
            i.field("facing", SchemaCodecs.enumeration(Direction.CODEC, List.of(Direction.values()), Direction::getSerializedName), SchemaTest::facing),
            i.field("color", SchemaCodecs.colorArgb(Codec.INT), SchemaTest::color),
            i.field("tags", SchemaCodecs.list(SchemaCodecs.STRING), SchemaTest::tags),
            i.field("nested", NESTED_SCHEMA, SchemaTest::nested)
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
            // Dev only playground for the native config screen: one of every control type, plus a check that
            // comment(...) works both before and after its define(...). Only shows up in a dev environment.
            builder.push("test_category");
            builder.comment("A dev only section used to test the config screen. It doesn't ship to players.");

            builder.icon("minecraft:lever").comment("A boolean feature: drawn as a ✓/✗ toggle with its icon next to the symbol").feature("test_bool", true);
            builder.comment("A plain boolean, drawn as an ON/OFF button").define("test_plain_bool", true);
            builder.comment("A value that needs a world reload (shows a globe icon)").worldReload().define("test_world_reload", true);
            builder.comment("A value that needs a game restart (shows a power icon)").gameRestart().define("test_game_restart", true);
            builder.comment("An integer, edited as a text field").define("test_int", 5, 0, 100);
            builder.comment("An integer, edited as a slider").defineSlider("test_int_slider", 50, 0, 100);
            builder.comment("A double, edited as a text field").define("test_double", 2.0, 0, 22);
            builder.comment("A double, edited as a slider").defineSlider("test_double_slider", 0.5, 0.0, 1.0);
            builder.comment("A percentage, edited as a slider showing %").definePercentage("test_percent", 0.5);
            builder.comment("An item picked from the registry, with icon").defineItem("test_item", ResourceLocation.parse("minecraft:diamond"));
            builder.comment("A block picked from the registry, with icon").defineBlock("test_block", ResourceLocation.parse("minecraft:stone"));
            builder.comment("A min/max range shown as two fields on one row").defineRange("test_range", Range.of(2, 8), 0, 10);
            builder.comment("A Vec3 shown as three x/y/z fields on one row").defineVec3("test_vec3", new Vec3(0.5, 1.0, -0.5), -10, 10);
            builder.comment("A Vec3i shown as three x/y/z integer fields on one row").defineVec3i("test_vec3i", new Vec3i(1, 2, 3), -16, 16);
            builder.comment("An enum, edited as a cycle button").define("test_enum", Direction.NORTH);
            builder.comment("A value picked from a dropdown list")
                    .defineDropdown("test_dropdown", "medium", List.of("potato", "low", "medium", "high", "ultra", "extreme", "overkill", "ludicrous", "maximum"));
            builder.comment("A string field").define("test_string", "hello");
            builder.comment("A regex pattern with live syntax highlighting").defineRegex("test_regex", "\\d+(foo|bar)?");
            builder.comment("An ARGB color, edited as a hex field").defineColor("test_color", 0xFFFF5555);

            // comment declared AFTER its define: still ends up on the row (lenient ordering)
            builder.define("test_after_comment", false);
            builder.comment("This comment was declared after its own define call");

            builder.comment("A free-text string list, edited on a sub page").define("test_list", List.of("a", "b", "c"));
            builder.comment("A string list whose entries are each picked from a dropdown")
                    .defineList("test_dropdown_list", List.of("medium"), List.of("low", "medium", "high", "ultra"));
            builder.comment("An item list, each entry picked from an item dropdown with icons")
                    .defineItemList("test_item_list",
                            List.of(ResourceLocation.parse("minecraft:diamond"), ResourceLocation.parse("minecraft:emerald")));

            JsonObject json = new JsonObject();
            json.addProperty("example", 42);
            json.addProperty("enabled", true);
            builder.comment("A raw JSON value, edited in a text box with syntax highlighting").defineJson("test_json", json);
            builder.comment("A plain Java bean (no codec needed), stored and edited as JSON").defineBean("test_bean", new TestBean());
            builder.comment("A record bean, also round-tripped through Gson").defineBean("test_record_bean", new TestRecordBean("world", 7));
            builder.comment("A codec object with a declared CodecUI schema, edited via a generated form: records become navigable sub categories, the string list falls back to the JSON editor")
                    .defineObject("test_schema", () -> new SchemaTest("hello", true, 5, Direction.NORTH, 0xFFFF5555,
                            List.of("alpha", "beta"), new Nested(1, 0.5)), SCHEMA_TEST);

            builder.icon("minecraft:oak_log").push("nested");
            builder.comment("A float value living in a nested sub category").define("nested_float", 0.5f, 0f, 1f);
            builder.pop();

            // Feature gating demo: a category with an enable toggle (shown inline on its row). Its children grey out
            // when it's off, and the returned supplier reads false whenever an ancestor feature is off — via supplier
            // composition, without ever rewriting the stored child values. (Suppliers unused here, just demonstrating.)
            builder.icon("minecraft:redstone").pushFeature("test_feature", true);
            builder.comment("Only meaningful while the feature is on").define("feature_speed", 1.0, 0, 10);
            builder.pushFeature("test_sub_feature", true);
            builder.comment("This feature reads false whenever the parent feature is off").define("sub_power", 3, 0, 9);
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

    /** Dev-only sample bean for {@code defineBean}: a plain POJO Gson can round-trip. */
    public static class TestBean {
        public String name = "hello";
        public int count = 3;
        public boolean flag = true;
    }

    /** Dev-only sample record bean: Gson (2.10+) round-trips records via their canonical constructor. */
    public record TestRecordBean(String label, int amount) {
    }

    /** Dev-only sample for {@code defineObject}: a codec object whose fields drive a generated form. */
    public record SchemaTest(String name, boolean enabled, int level, Direction facing, int color,
                             List<String> tags, Nested nested) {
    }

    /** Nested record inside {@link SchemaTest}: rendered as its own navigable sub category. */
    public record Nested(int x, double weight) {
    }
}
