package net.mehvahdjukaar.moonlight.api.platform.configs;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import net.mehvahdjukaar.candlelight.api.PlatformImpl;
import net.mehvahdjukaar.moonlight.api.events.AfterLanguageLoadEvent;
import net.mehvahdjukaar.moonlight.api.events.MoonlightEventsHelper;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigCategory;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigNode;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigOption;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigReloadType;
import net.mehvahdjukaar.moonlight.api.resources.assets.LangBuilder;
import net.mehvahdjukaar.moonlight.api.util.math.Range;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.misc.ConfigLangExporter;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public abstract class ConfigBuilder {

    protected final Map<String, String> translations = new LinkedHashMap<>();
    // keys whose name Moonlight made up rather than the mod (see ConfigLangExporter.SHARED_NAMES), mapped to that
    // name. Moonlight translates those itself, so no mod has to
    private final Map<String, String> sharedNames = new LinkedHashMap<>();
    protected Runnable changeCallback;
    protected boolean pendingDynamicPacks;

    // comment(...) may come before or after its define(...). It binds forward to the next define, falling back on the
    // last one if nothing claims it. Forward-first stops an un-commented define (a feature() toggle) from stealing
    // the before-comment of the value that follows it
    @Nullable
    private String pendingComment;
    // handed out once only, so a grouped define like defineRange doesn't repeat it for each hidden value it makes
    private boolean pendingCommentForwarded;
    @Nullable
    private CommentTarget lastCommentTarget;
    @Nullable
    private String lastCommentKey;

    private final ConfigCategory uiRoot = new ConfigCategory(Component.empty());
    private final Deque<ConfigCategory> uiStack = new ArrayDeque<>();
    private final Deque<Supplier<Boolean>> gateStack = new ArrayDeque<>();
    // while set, define(...) and push(...) add nothing to the screen, so defineRange and friends show a single row
    protected boolean suppressUi = false;

    // every feature()'s supplier, keyed by both short name and full dotted path
    private final Map<String, Supplier<Boolean>> featureToggles = new LinkedHashMap<>();
    // raw category names, root first, so a feature's full path can be built
    private final Deque<String> categoryPath = new ArrayDeque<>();

    public static final String FEATURE_TOGGLE_NAME = "enabled";

    // NeoForge only: when set, defineObject stores a json string instead of a native toml object
    protected boolean writeObjectsAsJson = false;

    protected ConfigReloadType pendingReload = ConfigReloadType.NONE;

    // set by icon(...), applied to and cleared by the next category push or defined option
    @Nullable
    private ResourceLocation pendingIcon;

    @FunctionalInterface
    protected interface CommentTarget {
        void applyComment(String rawComment);
    }

    @PlatformImpl
    public static ConfigBuilder create(ResourceLocation name, ConfigType type) {
        throw new AssertionError();
    }

    public static ConfigBuilder create(String modId, ConfigType type) {
        return create(ResourceLocation.fromNamespaceAndPath(modId, type.getDefaultName()), type);
    }

    private final ResourceLocation name;
    protected final ConfigType type;

    protected ConfigBuilder(ResourceLocation name, ConfigType type) {
        this.name = name;
        this.type = type;
        this.uiStack.push(this.uiRoot);
        this.gateStack.push(() -> true);
        Consumer<AfterLanguageLoadEvent> consumer = e -> {
            // shared words go in first so a common name like "chance" reads translated in every language even if the
            // mod ships no lang file. addEntry never overwrites, so a mod's own entry always wins over both
            definedNames.forEach((key, rawName) -> {
                String shared = e.getEntry(moonlightNamedKey(rawName));
                if (shared != null) e.addEntry(key, shared);
            });
            if (e.isDefault()) translations.forEach(e::addEntry);
        };
        MoonlightEventsHelper.addListener(consumer, AfterLanguageLoadEvent.class);
        Moonlight.addDependent(name.getNamespace());
    }

    public final ModConfigHolder build() {
        flushPendingComment(); // a trailing after-comment has no define to claim it
        ModConfigHolder holder = buildHolder();
        holder.setFeatureToggles(getFeatureToggles());
        ConfigLangExporter.exportInDev(name.getNamespace(), translations, definedNames);
        return holder;
    }

    private static String moonlightNamedKey(String name) {
        return "moonlight.config.common." + name;
    }

    protected abstract ModConfigHolder buildHolder();

    public ResourceLocation getName() {
        return name;
    }

    public abstract ConfigBuilder push(String category);

    public abstract ConfigBuilder pop();

    /** Stores defineObject values as a json string rather than a native toml object. NeoForge only. */
    public <T extends ConfigBuilder> T writeObjectsAsJson() {
        this.writeObjectsAsJson = true;
        return (T) this;
    }

    @Deprecated(forRemoval = true)
    public <T extends ConfigBuilder> T setWriteJsons() {
        return writeObjectsAsJson();
    }

    /** Marks the next defined value as one that affects dynamic resource/data packs. Sticky until then, like worldReload(). */
    public <T extends ConfigBuilder> T affectsDynamicPacks() {
        this.pendingDynamicPacks = true;
        return (T) this;
    }

    public abstract Supplier<Boolean> define(String name, boolean defaultValue);

    public abstract Supplier<Double> define(String name, double defaultValue, double min, double max);

    public abstract Supplier<Float> define(String name, float defaultValue, float min, float max);

    public abstract Supplier<Integer> define(String name, int defaultValue, int min, int max);

    public Supplier<Integer> defineColor(String name, int defaultValue) {
        return defineColor(name, defaultValue, true);
    }

    /**
     * An int color, edited as a hex field. With hasAlpha it's an ARGB color (#AARRGGBB), without it the alpha is
     * dropped and it's a plain RGB color (#2A77EA).
     */
    public abstract Supplier<Integer> defineColor(String name, int defaultValue, boolean hasAlpha);

    public abstract Supplier<Integer> defineSlider(String name, int defaultValue, int min, int max);

    public abstract Supplier<Double> defineSlider(String name, double defaultValue, double min, double max);

    public abstract Supplier<Float> defineSlider(String name, float defaultValue, float min, float max);

    public abstract Supplier<Double> definePercentage(String name, double defaultValue);

    public abstract Supplier<String> define(String name, String defaultValue, Predicate<Object> validator);

    public Supplier<String> define(String name, String defaultValue) {
        return define(name, defaultValue, STRING_CHECK);
    }

    public Supplier<Pattern> defineRegex(String name, String defaultValue) {
        return new RegexPatternValue(defineRegexInternal(name, defaultValue));
    }

    protected abstract Supplier<String> defineRegexInternal(String name, String defaultValue);

    protected abstract Supplier<String> defineChoiceInternal(String name, String defaultValue, Predicate<Object> validator,
                                                             Supplier<List<String>> options, @Nullable Function<String, ItemStack> icon);

    public Supplier<String> defineDropdown(String name, String defaultValue, List<String> options) {
        List<String> copy = List.copyOf(options);
        return defineChoiceInternal(name, defaultValue, o -> o instanceof String s && copy.contains(s), () -> copy, null);
    }

    public Supplier<ResourceLocation> defineRegistry(String name, ResourceLocation defaultValue, Registry<?> registry) {
        Supplier<String> handle = defineChoiceInternal(name, defaultValue.toString(), REGISTRY_ID_CHECK,
                () -> registryIds(registry), null);
        return () -> ResourceLocation.parse(handle.get());
    }

    public Supplier<Item> defineItem(String name, ResourceLocation defaultValue) {
        Supplier<String> handle = defineChoiceInternal(name, defaultValue.toString(), REGISTRY_ID_CHECK,
                () -> registryIds(BuiltInRegistries.ITEM),
                id -> new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(id))));
        return () -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(handle.get()));
    }

    public Supplier<Block> defineBlock(String name, ResourceLocation defaultValue) {
        Supplier<String> handle = defineChoiceInternal(name, defaultValue.toString(), REGISTRY_ID_CHECK,
                () -> registryIds(BuiltInRegistries.BLOCK),
                id -> new ItemStack(BuiltInRegistries.BLOCK.get(ResourceLocation.parse(id)).asItem()));
        return () -> BuiltInRegistries.BLOCK.get(ResourceLocation.parse(handle.get()));
    }

    private static List<String> registryIds(Registry<?> registry) {
        return registry.keySet().stream().map(ResourceLocation::toString).sorted().toList();
    }

    private static List<String> idStrings(List<ResourceLocation> ids) {
        return ids.stream().map(ResourceLocation::toString).toList();
    }

    public static final Predicate<Object> REGISTRY_ID_CHECK = o -> o instanceof String s && ResourceLocation.tryParse(s) != null;

    protected abstract Supplier<List<String>> defineListInternal(String name, List<String> defaultValue,
                                                                 Predicate<Object> entryValidator,
                                                                 Supplier<List<String>> options,
                                                                 @Nullable Function<String, ItemStack> icon);

    public Supplier<List<String>> defineList(String name, List<String> defaultValue, List<String> options) {
        List<String> copy = List.copyOf(options);
        return defineListInternal(name, defaultValue, o -> o instanceof String s && copy.contains(s), () -> copy, null);
    }

    /**
     * Like defineList, but the suggestions are read lazily, so options that only exist later (after registration for
     * instance) still show up. Entries aren't limited to them: anything entryValidator accepts is kept, so regex
     * patterns or ids that aren't loaded yet don't get dropped.
     */
    public Supplier<List<String>> defineSuggestionList(String name, List<String> defaultValue,
                                                       Supplier<List<String>> suggestions,
                                                       Predicate<Object> entryValidator,
                                                       @Nullable Function<String, ItemStack> icon) {
        return defineListInternal(name, defaultValue, entryValidator, suggestions, icon);
    }

    public Supplier<List<ResourceLocation>> defineRegistryList(String name, List<ResourceLocation> defaultValue, Registry<?> registry) {
        Supplier<List<String>> handle = defineListInternal(name, idStrings(defaultValue), REGISTRY_ID_CHECK,
                () -> registryIds(registry), null);
        return () -> handle.get().stream().map(ResourceLocation::parse).toList();
    }

    public Supplier<List<Item>> defineItemList(String name, List<ResourceLocation> defaultValue) {
        Supplier<List<String>> handle = defineListInternal(name, idStrings(defaultValue), REGISTRY_ID_CHECK,
                () -> registryIds(BuiltInRegistries.ITEM),
                id -> new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(id))));
        return () -> handle.get().stream().map(id -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(id))).toList();
    }

    public Supplier<List<Block>> defineBlockList(String name, List<ResourceLocation> defaultValue) {
        Supplier<List<String>> handle = defineListInternal(name, idStrings(defaultValue), REGISTRY_ID_CHECK,
                () -> registryIds(BuiltInRegistries.BLOCK),
                id -> new ItemStack(BuiltInRegistries.BLOCK.get(ResourceLocation.parse(id)).asItem()));
        return () -> handle.get().stream().map(id -> BuiltInRegistries.BLOCK.get(ResourceLocation.parse(id))).toList();
    }

    private static class RegexPatternValue implements Supplier<Pattern> {
        private final Supplier<String> source;
        private String cachedSource;
        private Pattern cached;

        RegexPatternValue(Supplier<String> source) {
            this.source = source;
        }

        @Override
        public Pattern get() {
            String s = source.get();
            if (cached == null || !s.equals(cachedSource)) {
                cachedSource = s;
                // validated on write, but a hand-edited file could still be invalid: fall back to a literal match
                try {
                    cached = Pattern.compile(s);
                } catch (Exception e) {
                    cached = Pattern.compile(Pattern.quote(s));
                }
            }
            return cached;
        }
    }

    public <T extends String> Supplier<List<String>> define(String name, List<? extends T> defaultValue) {
        return define(name, defaultValue, s -> true);
    }

    public abstract String currentCategory();

    public abstract String parentCategory();

    public abstract <T extends String> Supplier<List<String>> define(String name, List<? extends T> defaultValue, Predicate<Object> predicate);

    public abstract <V extends Enum<V>> Supplier<V> define(String name, V defaultValue);

    public abstract <T> Supplier<T> defineObject(String name, com.google.common.base.Supplier<T> defaultSupplier, Codec<T> codec);

    public <T> Supplier<List<T>> defineObjectList(String name, com.google.common.base.Supplier<List<T>> defaultSupplier, Codec<T> codec) {
        return defineObject(name, defaultSupplier, codec.listOf());
    }

    public Supplier<Map<String, String>> defineMap(String name, Map<String, String> def) {
        return defineObject(name, () -> def, Codec.unboundedMap(Codec.STRING, Codec.STRING));
    }

    public Supplier<Map<ResourceLocation, ResourceLocation>> defineIDMap(String name, Map<ResourceLocation, ResourceLocation> def) {
        return defineObject(name, () -> def, Codec.unboundedMap(ResourceLocation.CODEC, ResourceLocation.CODEC));
    }

    public abstract Supplier<JsonElement> defineJson(String name, JsonElement defaultValue);

    public abstract Supplier<JsonElement> defineJson(String name, Supplier<JsonElement> defaultValue);

    public <T> Supplier<T> defineBean(String name, T defaultValue) {
        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) defaultValue.getClass();
        this.push(name);
        try {
            return ConfigBeans.define(this, type, defaultValue);
        } finally {
            this.pop();
        }
    }


    public Supplier<ResourceLocation> define(String name, ResourceLocation defaultValue) {
        Supplier<String> handle = define(name, defaultValue.toString(), REGISTRY_ID_CHECK);
        return () -> ResourceLocation.parse(handle.get());
    }

    public Component description(String name) {
        return Component.translatable(translationKey(name));
    }

    public Component tooltip(String name) {
        return Component.translatable(tooltipKey(name));
    }

    public String tooltipKey(String name) {
        return this.name.getNamespace() + ".configuration." + currentCategory() + "." + name + ".description";
    }

    public String translationKey(String name) {
        return this.name.getNamespace() + ".configuration." + currentCategory() +
                (name.isEmpty() ? "" : "." + name);
    }

    public ConfigBuilder comment(String comment) {
        // a new comment means the previous one had no define of its own, so it was an "after" comment: flush it first
        if (this.pendingComment != null) applyComment(this.pendingComment);
        this.pendingComment = comment;
        this.pendingCommentForwarded = false;
        return this;
    }

    public ConfigBuilder comment(String... comment) {
        return comment(String.join("\n", comment));
    }

    public ConfigBuilder icon(ResourceLocation id) {
        this.pendingIcon = id;
        return this;
    }

    public ConfigBuilder icon(String id) {
        return icon(id.indexOf(':') >= 0
                ? ResourceLocation.parse(id)
                : ResourceLocation.fromNamespaceAndPath(this.name.getNamespace(), id));
    }

    // a still-pending comment at a section boundary (pop/build) was an "after" comment for the last value
    protected void flushPendingComment() {
        if (this.suppressUi) return;
        if (this.pendingComment != null) {
            applyComment(this.pendingComment);
            this.pendingComment = null;
        }
    }

    // Forge attaches a comment to the NEXT define, so hand out the pending before-comment once
    @Nullable
    protected String pollCommentToForward() {
        if (this.pendingComment != null && !this.pendingCommentForwarded) {
            this.pendingCommentForwarded = true;
            return this.pendingComment;
        }
        return null;
    }

    public ConfigBuilder pop(int count) {
        for (int i = 0; i < count; i++) pop();
        return this;
    }

    @Deprecated(forRemoval = true)
    public ConfigBuilder translation(String translationKey) {
        return this;
    }

    private void applyComment(String rawComment) {
        if (this.lastCommentKey != null) this.translations.put(this.lastCommentKey, rawComment);
        if (this.lastCommentTarget != null) this.lastCommentTarget.applyComment(rawComment);
        this.lastCommentTarget = null;
        this.lastCommentKey = null;
    }

    protected void noteDefined(String name, @Nullable ConfigNode uiNode, @Nullable Consumer<String> rawCommentSink) {
        if (this.suppressUi) return;
        String key = this.tooltipKey(name);
        Component description = Component.translatable(key);
        this.lastCommentKey = key;
        this.lastCommentTarget = raw -> {
            if (uiNode != null) uiNode.setDescription(description);
            if (rawCommentSink != null) rawCommentSink.accept(raw);
        };
        if (this.pendingComment != null) {
            applyComment(this.pendingComment);
            this.pendingComment = null;
        }
        if (this.pendingIcon != null && uiNode != null) {
            uiNode.setIcon(this.pendingIcon);
            this.pendingIcon = null;
        }
    }

    protected void uiPush(Component title) {
        // a comment(...) right before a push belongs to the category itself, not to its first value. Category
        // descriptions aren't shown yet, so drop it instead of letting the first child claim it
        this.pendingComment = null;
        this.pendingCommentForwarded = false;
        if (this.suppressUi) return;
        ConfigCategory cat = new ConfigCategory(title);
        if (this.pendingIcon != null) {
            cat.setIcon(this.pendingIcon);
            this.pendingIcon = null;
        }
        this.uiStack.peek().add(cat);
        this.uiStack.push(cat);
        this.gateStack.push(this.gateStack.peek()); // inherited until a feature() narrows it
        this.categoryPath.addLast(currentCategory());
    }

    protected void uiPop() {
        if (this.suppressUi) return;
        this.uiStack.pop();
        this.gateStack.pop();
        this.categoryPath.pollLast();
    }

    protected String currentCategoryPath() {
        return String.join(".", this.categoryPath);
    }

    /**
     * Adds the current category's on/off toggle. The returned supplier is true only when this toggle and every parent
     * one are on. Nothing is rewritten on disk: turning a parent off just makes the children read false.
     */
    public Supplier<Boolean> mainFeature(boolean defaultEnabled) {
        ConfigCategory cat = this.uiStack.peek();
        if (cat == this.uiRoot) {
            throw new IllegalStateException("mainFeature() must be called inside a category (use push/pushFeature first), not at the config root");
        }
        if (cat.gate() != null) {
            throw new IllegalStateException("category '" + currentCategory() + "' already has a mainFeature() toggle");
        }
        Supplier<Boolean> raw = define(FEATURE_TOGGLE_NAME, defaultEnabled);
        // define() just recorded the matching BooleanValue as this category's last entry: adopt it as the gate row
        List<ConfigNode> entries = cat.entries();
        if (!entries.isEmpty() && entries.get(entries.size() - 1) instanceof ConfigOption.BooleanValue bv) {
            cat.setGate(bv);
            // explicit icon(...) wins, else infer from the category name. Mirrored so the category button and the
            // gate row share one icon
            if (bv.icon() == null) bv.setIcon(cat.icon() != null ? cat.icon() : inferFeatureIcon(currentCategory()));
            if (cat.icon() == null) cat.setIcon(bv.icon());
        }
        Supplier<Boolean> ancestor = this.gateStack.peek();
        Supplier<Boolean> effective = () -> raw.get() && ancestor.get();
        this.gateStack.pop(); // replace the inherited gate with this category's own
        this.gateStack.push(effective);
        registerFeature(currentCategory(), currentCategoryPath(), effective);
        return effective;
    }

    public Supplier<Boolean> mainFeature() {
        return this.mainFeature(true);
    }


    /**
     * A named on/off feature. Draws as a check/cross switch instead of an ON/OFF button, and the supplier reads false if this
     * one or any parent feature is off. Pair it with icon(), like builder.icon("lever").feature("test_bool", true).
     */
    public Supplier<Boolean> feature(String name, boolean defaultEnabled) {
        Supplier<Boolean> raw = define(name, defaultEnabled);
        // adopt the just-recorded BooleanValue so the client draws it as a check/cross toggle
        List<ConfigNode> entries = this.uiStack.peek().entries();
        if (!entries.isEmpty() && entries.get(entries.size() - 1) instanceof ConfigOption.BooleanValue bv) {
            bv.setFeature(true);
            if (bv.icon() == null) bv.setIcon(inferFeatureIcon(name));
        }
        Supplier<Boolean> ancestor = this.gateStack.peek();
        Supplier<Boolean> effective = () -> raw.get() && ancestor.get();
        String path = this.categoryPath.isEmpty() ? name : currentCategoryPath() + "." + name;
        registerFeature(name, path, effective);
        return effective;
    }

    public Supplier<Boolean> feature(String name) {
        return feature(name, true);
    }

    public Supplier<Boolean> pushFeature(String name, boolean defaultEnabled) {
        push(name);
        return mainFeature(defaultEnabled);
    }


    // resolved lazily on the client, so a name that isn't a real item/block simply shows no icon
    @Nullable
    private ResourceLocation inferFeatureIcon(String name) {
        return ResourceLocation.tryBuild(this.name.getNamespace(), name);
    }

    // keyed by both short name and full dotted path, so a feature can be queried either way
    private void registerFeature(String name, String path, Supplier<Boolean> effective) {
        this.featureToggles.put(name, effective);
        this.featureToggles.put(path, effective);
    }

    protected Map<String, Supplier<Boolean>> getFeatureToggles() {
        return this.featureToggles;
    }

    public Supplier<Boolean> pushFeature(String name) {
        return pushFeature(name, true);
    }

    protected void recordOption(ConfigOption<?> option) {
        if (this.suppressUi) return;
        // the flags were already stamped onto each backing leaf as it was defined. Suppressed groups no-op here, so
        // every leaf gets stamped before we clear them
        this.pendingReload = ConfigReloadType.NONE;
        this.pendingDynamicPacks = false;
        this.uiStack.peek().add(option);
    }

    /** Root of the screen tree. Ready once build() has run. */
    public ConfigCategory getUiRoot() {
        return this.uiRoot;
    }

    public Supplier<Range> defineRange(String name, Range defaultValue, double min, double max) {
        return defineRange(name, defaultValue.min(), defaultValue.max(), min, max);
    }

    public Supplier<Range> defineRange(String name, double defaultMin, double defaultMax, double min, double max) {
        // two doubles nested under `name`, their rows suppressed so the range shows as one combined row
        this.suppressUi = true;
        push(name);
        Supplier<Double> minHandle = define("min", defaultMin, min, max);
        Supplier<Double> maxHandle = define("max", defaultMax, min, max);
        pop();
        this.suppressUi = false;

        putName(this.translationKey(name), name);
        ConfigOption.RangeValue node = new ConfigOption.RangeValue(
                description(name), null, minHandle, maxHandle,
                new Range(defaultMin, defaultMax), min, max);
        recordOption(node);
        noteDefined(name, node, null);
        return () -> new Range(minHandle.get(), maxHandle.get());
    }

    /** A Vec3 shown as one row of x/y/z fields, each clamped between min and max. */
    public Supplier<Vec3> defineVec3(String name, Vec3 defaultValue, double min, double max) {
        this.suppressUi = true;
        push(name);
        Supplier<Double> xHandle = define("x", defaultValue.x, min, max);
        Supplier<Double> yHandle = define("y", defaultValue.y, min, max);
        Supplier<Double> zHandle = define("z", defaultValue.z, min, max);
        pop();
        this.suppressUi = false;

        putName(this.translationKey(name), name);
        ConfigOption.Vec3Value node = new ConfigOption.Vec3Value(
                description(name), null, xHandle, yHandle, zHandle, defaultValue, min, max);
        recordOption(node);
        noteDefined(name, node, null);
        return () -> new Vec3(xHandle.get(), yHandle.get(), zHandle.get());
    }

    /** A Vec3i shown as one row of x/y/z fields, each clamped between min and max. */
    public Supplier<Vec3i> defineVec3i(String name, Vec3i defaultValue, int min, int max) {
        this.suppressUi = true;
        push(name);
        Supplier<Integer> xHandle = define("x", defaultValue.getX(), min, max);
        Supplier<Integer> yHandle = define("y", defaultValue.getY(), min, max);
        Supplier<Integer> zHandle = define("z", defaultValue.getZ(), min, max);
        pop();
        this.suppressUi = false;

        putName(this.translationKey(name), name);
        ConfigOption.Vec3iValue node = new ConfigOption.Vec3iValue(
                description(name), null, xHandle, yHandle, zHandle, defaultValue, min, max);
        recordOption(node);
        noteDefined(name, node, null);
        return () -> new Vec3i(xHandle.get(), yHandle.get(), zHandle.get());
    }

    public ConfigBuilder onChange(Runnable callback) {
        this.changeCallback = callback;
        return this;
    }

    protected Runnable buildChangeCallback() {
        return this.changeCallback;
    }

    public ConfigBuilder worldReload() {
        this.pendingReload = ConfigReloadType.WORLD_RELOAD;
        forwardReloadFlag(ConfigReloadType.WORLD_RELOAD);
        return this;
    }

    public ConfigBuilder gameRestart() {
        this.pendingReload = ConfigReloadType.GAME_RESTART;
        forwardReloadFlag(ConfigReloadType.GAME_RESTART);
        return this;
    }

    // Forge needs the flag before the next define. Fabric keeps it on its own value object and reads pendingReload
    // at record time, so it doesn't override this
    protected void forwardReloadFlag(ConfigReloadType type) {
    }

    protected void addTranslationsAndComments(String name) {
        putName(this.translationKey(name), name);
        if (this.currentCategory() == null && PlatHelper.isDev())
            throw new AssertionError("Current config category was null. How?");
    }

    // called by push() once the category is on the stack, so translationKey("") points at it
    protected void noteCategoryName(String category) {
        putName(this.translationKey(""), category);
    }

    private void putName(String key, String rawName) {
        this.translations.put(key, LangBuilder.getReadableName(rawName));
        this.definedNames.put(key, rawName);
    }

    public static final Predicate<Object> STRING_CHECK = o -> o instanceof String;

    public static final Predicate<Object> REGEX_CHECK = o -> o instanceof String s && ConfigOption.RegexValue.isValidRegex(s);

    public static final Predicate<Object> LIST_STRING_CHECK = o ->
            o instanceof List<?> l && l.stream().allMatch(e -> e instanceof String);

}
