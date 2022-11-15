package net.mehvahdjukaar.moonlight.core.set;

import com.google.common.base.Stopwatch;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.*;

public class BlocksColorInternal {
    public static final List<DyeColor> VANILLA_COLORS = List.of(Arrays.copyOfRange(DyeColor.values(), 0, 16));
    public static final List<DyeColor> MODDED_COLORS = Arrays.stream(DyeColor.values()).filter(v -> !VANILLA_COLORS.contains(v)).toList();

    private static final Map<String, ColorSet<Block>> BLOCK_COLOR_SETS = new HashMap<>();
    private static final Map<String, ColorSet<Item>> ITEM_COLOR_SETS = new HashMap<>();

    private static final Object2ObjectOpenHashMap<Object, DyeColor> BLOCK_TO_COLORS = new Object2ObjectOpenHashMap<>();
    private static final Object2ObjectOpenHashMap<Object, String> BLOCK_TO_TYPE = new Object2ObjectOpenHashMap<>();


    public static void setup() {
        Stopwatch sw = Stopwatch.createStarted();

        Map<String, DyeColor> colors = new HashMap<>();
        VANILLA_COLORS.forEach(d -> colors.put(d.getName(), d));
        List<String> colorPriority = new ArrayList<>(colors.keySet().stream().toList());

        scanRegistry(colors, colorPriority, Registry.BLOCK, BLOCK_COLOR_SETS);
        scanRegistry(colors, colorPriority, Registry.ITEM, ITEM_COLOR_SETS);

        Moonlight.LOGGER.info("Initialized color sets in {}ms", sw.elapsed().toMillis());
    }

    private static <T> void scanRegistry(Map<String, DyeColor> colors, List<String> colorPriority,
                                         Registry<T> registry, Map<String, ColorSet<T>> colorSetMap) {
        Map<ResourceLocation, Map<DyeColor, T>> groupedByType = new HashMap<>();
        colorPriority.sort(Comparator.comparingInt(String::length));
        Collections.reverse(colorPriority);
        //group by color
        loop1:
        for (var b : registry) {
            ResourceLocation id = Utils.getID(b);
            String name = id.getPath();
            if (!name.contains("_")) continue;

            for (var c : colorPriority) {
                ResourceLocation newId = null;
                if (name.startsWith(c)) {
                    newId = new ResourceLocation(id.getNamespace(), name.substring((c + "_").length()));
                }
                if (name.endsWith(c)) {
                    newId = new ResourceLocation(id.getNamespace(), name.substring(0, name.length() - ("_" + c).length()));
                }
                if (newId != null) {
                    DyeColor dyeColor = colors.get(c);
                    groupedByType.computeIfAbsent(newId, a -> new EnumMap<>(DyeColor.class)).put(dyeColor, b);
                    continue loop1;
                }
            }
        }

        //to qualify all vanilla colors must be found
        for (var j : groupedByType.entrySet()) {
            var map = j.getValue();
            ResourceLocation id = j.getKey();
            if (map.keySet().containsAll(VANILLA_COLORS)) {
                var set = new ColorSet<>(id, map, registry);
                colorSetMap.put(id.toString(), set);

                for (var v : set.colorsToBlock.entrySet()) {
                    BLOCK_TO_COLORS.put(v.getValue(), v.getKey());
                    BLOCK_TO_TYPE.put(v.getValue(), id.toString());
                }
            }
        }
    }

    @Nullable
    public static DyeColor getColor(Block block) {
        return BLOCK_TO_COLORS.get(block);
    }

    @Nullable
    public static DyeColor getColor(Item item) {
        return BLOCK_TO_COLORS.get(item);
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

    /**
     * Changes this block color
     * If the given color is null it will yield the default colored block, usually uncolored or white
     * Will return null if no block can be found using that color
     */
    @Nullable
    public static Block changeColor(Block old, @Nullable DyeColor newColor) {
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
        return BLOCK_TO_TYPE.get(block);
    }

    @Nullable
    public static String getKey(Item item) {
        return BLOCK_TO_TYPE.get(item);
    }

    @Nullable
    private static ColorSet<Block> getBlockSet(String key) {
        key = new ResourceLocation(key).toString();
        return BLOCK_COLOR_SETS.get(key);
    }

    @Nullable
    private static ColorSet<Item> getItemSet(String key) {
        key = new ResourceLocation(key).toString();
        return ITEM_COLOR_SETS.get(key);
    }

    private static class ColorSet<T> {

        private final Map<DyeColor, T> colorsToBlock;
        private final T defaultBlock;

        public ColorSet(ResourceLocation id, Map<DyeColor, T> map, Registry<T> registry) {
            this.colorsToBlock = map;

            //fill default
            this.defaultBlock = registry.getOptional(id).orElseGet(() -> colorsToBlock.get(DyeColor.WHITE));
            //fill optional
            //only supports tinted mod
            colors:
            for (var c : MODDED_COLORS) {
                String namespace = id.getNamespace();
                String path = id.getPath();
                String mod = "tinted";
                for (var s : new String[]{namespace + ":" + path + "_%s", namespace + ":%s_" + path, mod + ":" + path + "_%s", mod + ":%s_" + path}) {
                    var o = registry.getOptional(new ResourceLocation(String.format(s, c.getName())));
                    if (o.isPresent()) {
                        colorsToBlock.put(c, o.get());
                        continue colors;
                    }
                }
            }
        }

        /**
         * Null if no color is available.
         * If null dye is provided will give the default color
         */
        @Nullable
        public T with(@Nullable DyeColor newColor) {
            if (newColor != null && !colorsToBlock.containsKey(newColor)) return null;
            return colorsToBlock.getOrDefault(newColor, defaultBlock);
        }

    }
}