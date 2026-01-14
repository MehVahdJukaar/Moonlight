package net.mehvahdjukaar.moonlight.core.set;

import com.google.common.base.Stopwatch;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.stream.Stream;

public class BlocksColorInternal extends SimplePreparableReloadListener<List<JsonElement>> {

    public static final BlocksColorInternal INSTANCE = new BlocksColorInternal();

    public static final List<DyeColor> VANILLA_COLORS = List.of(DyeColor.WHITE,
            DyeColor.ORANGE, DyeColor.MAGENTA, DyeColor.LIGHT_BLUE, DyeColor.YELLOW, DyeColor.LIME, DyeColor.PINK, DyeColor.GRAY,
            DyeColor.LIGHT_GRAY, DyeColor.CYAN, DyeColor.PURPLE, DyeColor.BLUE, DyeColor.BROWN, DyeColor.GREEN, DyeColor.RED, DyeColor.BLACK);
    public static final List<DyeColor> MODDED_COLORS = List.of(Arrays.stream(DyeColor.values()).filter(v -> !VANILLA_COLORS.contains(v)).toArray(DyeColor[]::new));

    private static final List<String> KNOWN_COLOR_MODS =
            Stream.of("tinted", "dye_depot", "dyenamics", "delicate_dyes", "mint")
                    .filter(PlatHelper::isModLoaded).toList();


    private State defaultState;
    private State state;

    private final Gson gson = new Gson();

    @Override
    protected List<JsonElement> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        List<JsonElement> output = new ArrayList<>();

        String directory = "color_sets";
        FileToIdConverter filetoidconverter = FileToIdConverter.json(directory);

        //like tags, we match resource stacks instead of all resources to avoid overriding since these work more like tags
        for (var entry : filetoidconverter.listMatchingResourceStacks(resourceManager).entrySet()) {
            ResourceLocation resourcelocation = entry.getKey();
            ResourceLocation resourcelocation1 = filetoidconverter.fileToId(resourcelocation);
            var value = entry.getValue();
            for (Resource r : value) {
                try {
                    Reader reader = r.openAsReader();
                    try {
                        JsonElement jsonelement = GsonHelper.fromJson(gson, reader, JsonElement.class);
                        output.add(jsonelement);
                    } catch (Throwable var13) {
                        try {
                            reader.close();
                        } catch (Throwable var12) {
                            var13.addSuppressed(var12);
                        }
                        throw var13;
                    }

                    reader.close();
                } catch (IOException | JsonParseException | IllegalArgumentException var14) {
                    Moonlight.LOGGER.error("Couldn't parse data file {} from {}", resourcelocation1, resourcelocation, var14);
                }
            }
        }
        return output;
    }

    @Override
    protected void apply(List<JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        List<ColorSetModification> colorSets = new ArrayList<>();
        //cant be bothered to make a conditional ops for this. we are using builtin registries anyways
        // JsonOps ops = ForgeHelper.conditionalOps(JsonOps.INSTANCE, registryAccess);
        for (var json : object) {
            try {
                ColorSetModification cs = ColorSetModification.CODEC.decode(JsonOps.INSTANCE, json)
                        .getOrThrow().getFirst();
                colorSets.add(cs);
            } catch (Exception ex) {
                //we fail like this for mod compat stuff. TODO: make these support conditions
                Moonlight.LOGGER.info("Failed to load custom color set definition {}. Ignoring", json, ex);
            }
        }

        this.state = this.defaultState.cloneModified(colorSets);
    }


    public void setup() {
        Stopwatch sw = Stopwatch.createStarted();

        Map<String, DyeColor> colors = new HashMap<>();
        VANILLA_COLORS.forEach(d -> colors.put(d.getName(), d));
        List<String> colorPriority = new ArrayList<>(colors.keySet().stream().toList());

        var blockSets = scanRegistryAndDetectSets(colors, colorPriority, BuiltInRegistries.BLOCK);
        var itemSets = scanRegistryAndDetectSets(colors, colorPriority, BuiltInRegistries.ITEM);

        this.defaultState = new State(blockSets, itemSets);
        this.state = this.defaultState;

        Moonlight.LOGGER.info("Initialized color sets in {}ms", sw.elapsed().toMillis());
    }

    private <T> Map<String, ColoredSet<T>> scanRegistryAndDetectSets(Map<String, DyeColor> colors, List<String> colorPriority,
                                                                     Registry<T> registry) {
        Map<ResourceLocation, ColorSetBuilder<T>> groupedByType = new HashMap<>();
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
                    groupedByType.computeIfAbsent(newId, a -> new ColorSetBuilder<>()).setColor(dyeColor, e.getValue());
                    continue loop1;
                }
            }
        }
        //create sets
        Map<String, ColoredSet<T>> result = new HashMap<>();

        //to qualify all vanilla colors must be found
        for (var j : groupedByType.entrySet()) {
            ColorSetBuilder <T> set = j.getValue();
            ResourceLocation id = j.getKey();
            if (isHardcodedBlacklisted(id)) continue;
            if (set.hasAllVanilla()) {
                addExtraEntries(id, registry, set);
                result.put(id.toString(), set.build());
            }
        }
        return result;
    }

    private <T> void addExtraEntries(ResourceLocation id, Registry<T> registry, ColorSetBuilder<T> colorsToObj) {
        //fill optional
        //we dont know the namespace of these
        colors:
        for (var c : MODDED_COLORS) {
            String namespace = id.getNamespace();
            String path = id.getPath();

            for (var mod : KNOWN_COLOR_MODS) {
                for (var s : new String[]{namespace + ":" + path + "_%s", namespace + ":%s_" + path, mod + ":" + path + "_%s", mod + ":%s_" + path}) {
                    var o = registry.getOptional(ResourceLocation.parse(String.format(s, c.getName())));
                    if (o.isPresent()) {
                        colorsToObj.setColor(c, o.get());
                        continue colors;
                    }
                }
            }
        }

        //fill default
        var o = registry.getOptional(id);
        T def = o.orElseGet(() -> registry.getOptional(ResourceLocation.parse(id.getPath()))
                .orElseGet(() -> colorsToObj.getColor(DyeColor.WHITE)));
        colorsToObj.setColor(null, def);
    }

    private boolean isHardcodedBlacklisted(ResourceLocation id) {
        String modId = id.getNamespace();
        return modId.equals("energeticsheep") || modId.equals("xycraft_world") || modId.equals("botania") || modId.equals("spectrum");
    }

    @Nullable
    public DyeColor getColor(Block block) {
        return state.obj2Colors.get(block);
    }

    @Nullable
    public DyeColor getColor(Item item) {
        return state.obj2Colors.get(item);
    }

    @Nullable
    public Item getColoredItem(String key, @Nullable DyeColor color) {
        var set = getItemSet(key);
        if (set != null) {
            return set.with(color);
        }
        return null;
    }

    @Nullable
    public Block getColoredBlock(String key, @Nullable DyeColor color) {
        var set = getBlockSet(key);
        if (set != null) {
            return set.with(color);
        }
        return null;
    }

    public Set<String> getBlockKeys() {
        return state.blockColorSets.keySet();
    }

    public Set<String> getItemKeys() {
        return state.itemColorSets.keySet();
    }

    /**
     * Changes this block color
     * If the given color is null it will yield the default colored block, usually uncolored or white
     * Will return null if no block can be found using that color
     */
    @Nullable
    public Block changeColor(Block old, @Nullable DyeColor newColor) {
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
    public Item changeColor(Item old, @Nullable DyeColor newColor) {
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
    public String getKey(Block block) {
        return state.obj2Type.get(block);
    }

    @Nullable
    public String getKey(Item item) {
        return state.obj2Type.get(item);
    }

    @Nullable
    private ColoredSet<Block> getBlockSet(String key) {
        key = ResourceLocation.parse(key).toString();
        return state.blockColorSets.get(key);
    }

    @Nullable
    private ColoredSet<Item> getItemSet(String key) {
        key = ResourceLocation.parse(key).toString();
        return state.itemColorSets.get(key);
    }

    @Nullable
    public HolderSet<Block> getBlockHolderSet(String key) {
        var set = getBlockSet(key);
        if (set != null) {
            return set.makeHolderSet(BuiltInRegistries.BLOCK);
        }
        return null;
    }

    @Nullable
    public HolderSet<Item> getItemHolderSet(String key) {
        var set = getItemSet(key);
        if (set != null) {
            return set.makeHolderSet(BuiltInRegistries.ITEM);
        }
        return null;
    }

    private static class ColorSetBuilder<T> {
        private final Map<DyeColor, T> colorsToObj = new HashMap<>();

        private static <T> ColorSetBuilder<T> from(ColoredSet<T> other) {
            ColorSetBuilder<T> b = new ColorSetBuilder<T>();
            b.colorsToObj.putAll(other.colorsToObj);
            return b;
        }

        private void setColor(@Nullable DyeColor color, T b) {
            colorsToObj.put(color, b);
        }

        private boolean isEmpty() {
            return colorsToObj.isEmpty();
        }

        private ColoredSet<T> build() {
            return new ColoredSet<>(colorsToObj);
        }

        public boolean hasAllVanilla() {
            return VANILLA_COLORS.stream().allMatch(colorsToObj::containsKey);
        }

        @Nullable
        public T getColor(DyeColor dyeColor) {
            return colorsToObj.get(dyeColor);
        }
    }

    /**
     * A collection of blocks or items that come in all colors
     */
    private record ColoredSet<T>(Map<DyeColor, T> colorsToObj) { //map is supposed to be immutable, dont touch

        //make truly immutable constructor
        private ColoredSet(Map<DyeColor, T> colorsToObj) {
            this.colorsToObj = new HashMap<>(colorsToObj); //hashmap since enum maps do not allow null keys
        }

        /**
         * Kind of expensive. don't call too often
         */
        private HolderSet<T> makeHolderSet(Registry<T> registry) {
            return HolderSet.direct(registry::wrapAsHolder, new ArrayList<>(colorsToObj.values()));
        }

        /**
         * Null if no color is available.
         * If null dye is provided will give the default color
         */
        @Nullable
        private T with(@Nullable DyeColor newColor) {
            if (newColor != null && !colorsToObj.containsKey(newColor)) return null;
            return colorsToObj.getOrDefault(newColor, colorsToObj.get(null));
        }
    }


    static class State {
        private final Map<String, ColoredSet<Block>> blockColorSets;
        private final Map<String, ColoredSet<Item>> itemColorSets;

        private final Object2ObjectOpenHashMap<Object, DyeColor> obj2Colors = new Object2ObjectOpenHashMap<>();
        private final Object2ObjectOpenHashMap<Object, String> obj2Type = new Object2ObjectOpenHashMap<>();

        private State(Map<String, ColoredSet<Block>> blockColorSets,
                      Map<String, ColoredSet<Item>> itemColorSets) {
            this.blockColorSets = blockColorSets;
            this.itemColorSets = itemColorSets;

            for (var e : blockColorSets.entrySet()) {
                String id = e.getKey();
                var set = e.getValue();
                for (var v : set.colorsToObj.entrySet()) {
                    obj2Colors.put(v.getValue(), v.getKey());
                    obj2Type.put(v.getValue(), id);
                }
            }
            for (var e : itemColorSets.entrySet()) {
                String id = e.getKey();
                var set = e.getValue();
                for (var v : set.colorsToObj.entrySet()) {
                    obj2Colors.put(v.getValue(), v.getKey());
                    obj2Type.put(v.getValue(), id);
                }
            }
        }

        private State cloneModified(List<ColorSetModification> mods) {
            Map<String, ColorSetBuilder<Block>> blockBuilder = this.blockColorSets.entrySet().stream()
                    .map(e -> Map.entry(e.getKey(), ColorSetBuilder.from(e.getValue())))
                    .collect(HashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), HashMap::putAll);
            Map<String, ColorSetBuilder<Item>> itemBuilder = this.itemColorSets.entrySet().stream()
                    .map(e -> Map.entry(e.getKey(), ColorSetBuilder.from(e.getValue())))
                    .collect(HashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), HashMap::putAll);


            //order with ones that have replace = true are last
            mods.sort(Comparator.comparingInt(m -> m.replace() ? 1 : 0));
            for (var mod : mods) {
                String id = mod.getId().toString();
                ColorSetBuilder<Block> blockSet = null;
                ColorSetBuilder<Item> itemSet = null;
                if (mod.hasBlocks()) {
                    if (mod.replace()) {
                        blockBuilder.put(id, new ColorSetBuilder<>());
                    }
                    blockSet = blockBuilder.get(id);
                }
                if (mod.hasItems()) {
                    if (mod.replace()) {
                        itemBuilder.put(id, new ColorSetBuilder<>());
                    }
                    itemSet = itemBuilder.get(id);
                }
                for (var e : mod.entrySet()) {
                    @Nullable
                    DyeColor color = e.getKey();
                    @Nullable
                    Block b = e.getValue().block();
                    @Nullable
                    Item i = e.getValue().item();
                    if (b != null && blockSet != null) {
                        blockSet.setColor(color, b);
                    }
                    if (i != null && itemSet != null) {
                        itemSet.setColor(color, i);
                    }
                }
            }
            Map<String, ColoredSet<Block>> newBlockSets = blockBuilder.entrySet().stream()
                    .filter(e -> !e.getValue().isEmpty())
                    .collect(HashMap::new, (m, e) -> m.put(e.getKey(), e.getValue().build()), HashMap::putAll);
            Map<String, ColoredSet<Item>> newItemSets = itemBuilder.entrySet().stream()
                    .filter(e -> !e.getValue().isEmpty())
                    .collect(HashMap::new, (m, e) -> m.put(e.getKey(), e.getValue().build()), HashMap::putAll);
            return new State(newBlockSets, newItemSets);
        }

    }

}