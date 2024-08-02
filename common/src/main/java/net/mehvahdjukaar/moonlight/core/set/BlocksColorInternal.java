package net.mehvahdjukaar.moonlight.core.set;

import com.google.common.base.Stopwatch;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.mehvahdjukaar.moonlight.api.MoonlightRegistry;
import net.mehvahdjukaar.moonlight.api.MoonlightTags;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BlocksColorInternal {
    public static final List<DyeColor> VANILLA_COLORS = List.of(DyeColor.WHITE,
            DyeColor.ORANGE, DyeColor.MAGENTA, DyeColor.LIGHT_BLUE, DyeColor.YELLOW, DyeColor.LIME, DyeColor.PINK, DyeColor.GRAY,
            DyeColor.LIGHT_GRAY, DyeColor.CYAN, DyeColor.PURPLE, DyeColor.BLUE, DyeColor.BROWN, DyeColor.GREEN, DyeColor.RED, DyeColor.BLACK);
    public static final List<DyeColor> MODDED_COLORS = List.of(Arrays.stream(DyeColor.values()).filter(v -> !VANILLA_COLORS.contains(v)).toArray(DyeColor[]::new));

    private static final Map<String, ColoredSet<Block>> BLOCK_COLOR_SETS = new HashMap<>();
    private static final Map<String, ColoredSet<Item>> ITEM_COLOR_SETS = new HashMap<>();

    private static final Object2ObjectOpenHashMap<Object, DyeColor> OBJ_TO_COLORS = new Object2ObjectOpenHashMap<>();
    private static final Object2ObjectOpenHashMap<Object, String> OBJ_TO_TYPE = new Object2ObjectOpenHashMap<>();


    public static void setup() {
        Stopwatch sw = Stopwatch.createStarted();

        Map<String, DyeColor> colors = new HashMap<>();
        VANILLA_COLORS.forEach(d -> colors.put(d.getName(), d));
        List<String> colorPriority = new ArrayList<>(colors.keySet().stream().toList());

        addColoredFromRegistry(colors, colorPriority, BuiltInRegistries.BLOCK, BLOCK_COLOR_SETS);
        addColoredFromRegistry(colors, colorPriority, BuiltInRegistries.ITEM, ITEM_COLOR_SETS);

        Moonlight.LOGGER.info("Initialized color sets in {}ms", sw.elapsed().toMillis());
    }

    public static void registerBlockColorSet(ResourceLocation key, EnumMap<DyeColor, Block> blocks, @Nullable Block defaultBlock) {
        BLOCK_COLOR_SETS.put(key.toString(), new ColoredSet<>(key, blocks, BuiltInRegistries.BLOCK, defaultBlock));
    }

    public static void registerItemColorSet(ResourceLocation key, EnumMap<DyeColor, Item> items, @Nullable Item defaultItem) {
        ITEM_COLOR_SETS.put(key.toString(), new ColoredSet<>(key, items, BuiltInRegistries.ITEM, defaultItem));
    }

    private static <T> void addColoredFromRegistry(Map<String, DyeColor> colors, List<String> colorPriority,
                                                   Registry<T> registry, Map<String, ColoredSet<T>> colorSetMap) {
        Map<ResourceLocation, EnumMap<DyeColor, T>> groupedByType = new HashMap<>();
        colorPriority.sort(Comparator.comparingInt(String::length));
        Collections.reverse(colorPriority);
        //group by color
        loop1:
        for (var e : registry.entrySet()) {
            ResourceLocation id = e.getKey().location();
            String name = id.getPath();
            if (!name.contains("_")) continue;

            for (var c : colorPriority) {
                ResourceLocation newId = null;
                if (name.startsWith(c + "_")) {
                    newId = id.withPath(name.substring((c + "_").length()));
                }
                if (name.endsWith("_" + c)) {
                    newId = id.withPath(name.substring(0, name.length() - ("_" + c).length()));
                }
                if (newId != null) {
                    DyeColor dyeColor = colors.get(c);
                    groupedByType.computeIfAbsent(newId, a -> new EnumMap<>(DyeColor.class)).put(dyeColor, e.getValue());
                    continue loop1;
                }
            }
        }

        //to qualify all vanilla colors must be found
        for (var j : groupedByType.entrySet()) {
            var map = j.getValue();
            ResourceLocation id = j.getKey();
            if (isBlacklisted(id)) continue;
            if (map.keySet().containsAll(VANILLA_COLORS)) {
                var set = new ColoredSet<>(id, map, registry);
                colorSetMap.put(id.toString(), set);

                for (var v : set.colorsToObj.entrySet()) {
                    OBJ_TO_COLORS.put(v.getValue(), v.getKey());
                    OBJ_TO_TYPE.put(v.getValue(), id.toString());
                }
                OBJ_TO_TYPE.put(set.defaultObj, id.toString());
            }
        }
    }

    private static boolean isBlacklisted(ResourceLocation id) {
        String modId = id.getNamespace();
        return modId.equals("energeticsheep") || modId.equals("xycraft_world") || modId.equals("botania") || modId.equals("spectrum");
    }

    @Nullable
    public static DyeColor getColor(Block block) {
        return OBJ_TO_COLORS.get(block);
    }

    @Nullable
    public static DyeColor getColor(Item item) {
        return OBJ_TO_COLORS.get(item);
    }

    @Nullable
    public static Item getColoredItem(String key, @Nullable DyeColor color) {
        var set = getItemSet(key);
        if (set != null) {
            return set.with(color);
        }
        return null;
    }

    @Nullable
    public static Block getColoredBlock(String key, @Nullable DyeColor color) {
        var set = getBlockSet(key);
        if (set != null) {
            return set.with(color);
        }
        return null;
    }

    public static Set<String> getBlockKeys() {
        return BLOCK_COLOR_SETS.keySet();
    }

    public static Set<String> getItemKeys() {
        return ITEM_COLOR_SETS.keySet();
    }

    /**
     * Changes this block color
     * If the given color is null it will yield the default colored block, usually uncolored or white
     * Will return null if no block can be found using that color
     */
    @Nullable
    public static Block changeColor(Block old, @Nullable DyeColor newColor) {
        if (old.builtInRegistryHolder().is(MoonlightTags.NON_RECOLORABLE_BLOCKS_TAG)) return null;
        String key = getKey(old);
        if (key != null) {
            var set = getBlockSet(key);
            if (set != null) {
                var b = set.with(newColor);
                if (b != old) return b;
            }
        }
        return null;
    }

    /**
     * Changes this item color
     * If the given color is null it will yield the default colored item, usually uncolored or white
     * Will return null if no item can be found using that color
     */
    @Nullable
    public static Item changeColor(Item old, @Nullable DyeColor newColor) {
        if (old.builtInRegistryHolder().is(MoonlightTags.NON_RECOLORABLE_ITEMS_TAG)) return null;
        String key = getKey(old);
        if (key != null) {
            var set = getItemSet(key);
            if (set != null) {
                var i = set.with(newColor);
                if (i != old) return i;
            }
        }
        return null;
    }

    @Nullable
    public static String getKey(Block block) {
        return OBJ_TO_TYPE.get(block);
    }

    @Nullable
    public static String getKey(Item item) {
        return OBJ_TO_TYPE.get(item);
    }

    @Nullable
    private static ColoredSet<Block> getBlockSet(String key) {
        key = ResourceLocation.parse(key).toString();
        return BLOCK_COLOR_SETS.get(key);
    }

    @Nullable
    private static ColoredSet<Item> getItemSet(String key) {
        key = ResourceLocation.parse(key).toString();
        return ITEM_COLOR_SETS.get(key);
    }

    @Nullable
    public static HolderSet<Block> getBlockHolderSet(String key) {
        var set = getBlockSet(key);
        if (set != null) {
            return set.makeHolderSet(BuiltInRegistries.BLOCK);
        }
        return null;
    }

    @Nullable
    public static HolderSet<Item> getItemHolderSet(String key) {
        var set = getItemSet(key);
        if (set != null) {
            return set.makeHolderSet(BuiltInRegistries.ITEM);
        }
        return null;
    }

    /**
     * A collection of blocks or items that come in all colors
     */
    private static class ColoredSet<T> {

        private final ResourceLocation id;
        private final Map<DyeColor, T> colorsToObj;
        private final T defaultObj;

        private ColoredSet(ResourceLocation id, EnumMap<DyeColor, T> map, Registry<T> registry) {
            this(id, map, registry, null);
        }

        private ColoredSet(ResourceLocation id, EnumMap<DyeColor, T> map, Registry<T> registry, @Nullable T defBlock) {
            this.colorsToObj = map;
            this.id = id;

            //fill optional
            List<String> newColorMods = List.of("tinted", "dye_depot", "dyenamics");
            colors:
            for (var c : MODDED_COLORS) {
                String namespace = id.getNamespace();
                String path = id.getPath();

                for (var mod : newColorMods) {
                    for (var s : new String[]{namespace + ":" + path + "_%s", namespace + ":%s_" + path, mod + ":" + path + "_%s", mod + ":%s_" + path}) {
                        var o = registry.getOptional(ResourceLocation.parse(String.format(s, c.getName())));
                        if (o.isPresent()) {
                            colorsToObj.put(c, o.get());
                            continue colors;
                        }
                    }
                }
            }

            //fill default
            this.defaultObj = defBlock == null ? computeDefault(id, registry) : defBlock;
        }

        private T computeDefault(ResourceLocation id, Registry<T> registry) {
            if (id.getNamespace().equals("minecraft") && id.getPath().contains("stained_glass")) {
                id = ResourceLocation.parse(id.getPath().replace("stained_", ""));
            } else if (id.getNamespace().equals("quark")) {
                if (id.getPath().equals("rune")) {
                    id = ResourceLocation.fromNamespaceAndPath("quark", "blank_rune");
                } else if (id.getPath().equals("shard")) {
                    id = ResourceLocation.fromNamespaceAndPath("quark", "clear_shard");
                }
            } else if (id.equals(ResourceLocation.parse("suppsquared:sack"))) {
                id = ResourceLocation.parse("supplementaries:sack");
            }
            ResourceLocation finalId = id;
            var o = registry.getOptional(id);
            if (o.isEmpty()) {
                return registry.getOptional(ResourceLocation.parse(finalId.getPath()))
                        .orElseGet(() -> colorsToObj.get(DyeColor.WHITE));
            } else {
                return o.get();
            }
        }

        /**
         * Kind of expensive. don't call too often
         */
        private HolderSet<T> makeHolderSet(Registry<T> registry) {
            //standard tag location
            var v = registry.getTag(TagKey.create(registry.key(),
                   id.withSuffix("s")));
            if (v.isEmpty()) {
                v = registry.getTag(TagKey.create(registry.key(),
                        ResourceLocation.fromNamespaceAndPath("c", id.getPath() + "s")));
            }
            if (v.isPresent()) {
                var tag = v.get();
                boolean success = true;
                for (var t : colorsToObj.values()) {
                    if (!tag.contains(registry.getHolderOrThrow(registry.getResourceKey(t).get()))) {
                        success = false;
                        break;
                    }
                }
                if (success) return tag;
            }
            return HolderSet.direct(t -> registry.getHolderOrThrow(registry.getResourceKey(t).get()),
                    new ArrayList<>(colorsToObj.values()));
        }

        /**
         * Null if no color is available.
         * If null dye is provided will give the default color
         */
        @Nullable
        private T with(@Nullable DyeColor newColor) {
            if (newColor != null && !colorsToObj.containsKey(newColor)) return null;
            return colorsToObj.getOrDefault(newColor, defaultObj);
        }

    }
}