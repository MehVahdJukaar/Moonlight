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
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * A loader independent config builder
 * Support common config syncing
 */
public abstract class ConfigBuilder {

    protected final Map<String, String> translations = new HashMap<>();
    protected Runnable changeCallback;
    protected boolean pendingDynamicPacks;

    // ===== lenient comment wiring =====
    // A comment(...) may come before or after its define(...). We remember a comment given before as
    // pendingComment (consumed by the next define), and remember the last define's target so a comment given
    // after can still attach to it. applyComment() writes the readable text into the lang map and lets the
    // platform stamp it onto the on-disk value and the screen row.
    @Nullable
    private String pendingComment;
    @Nullable
    private CommentTarget lastCommentTarget;
    @Nullable
    private String lastCommentKey;

    // parallel, loader independent UI tree consumed by the native config screen. Built as values are defined so
    // both loaders end up with the exact same tree regardless of how they store the actual config.
    private final ConfigCategory uiRoot = new ConfigCategory(Component.empty());
    private final Deque<ConfigCategory> uiStack = new ArrayDeque<>();
    // Effective "enabled" supplier of the current category, kept parallel to uiStack. A feature() replaces the top
    // with its own (ANDed with the ancestor beneath it) so nested feature() suppliers compose at read time without
    // ever touching the stored child values.
    private final Deque<Supplier<Boolean>> gateStack = new ArrayDeque<>();
    // while set, define(...)/push(...) skip UI emission: used by compound values (e.g. defineRange) that own
    // several backing values but want a single combined row instead of one row per backing value.
    protected boolean suppressUi = false;

    /** Reserved child name for the boolean a {@link #feature} declares. */
    public static final String FEATURE_TOGGLE_NAME = "enabled";

    //always on. can be called to disable
    protected boolean usesDataBuddy = true;

    // set by worldReload()/gameRestart(), applied to (and cleared by) the next recorded option — see recordOption
    protected ConfigReloadType pendingReload = ConfigReloadType.NONE;

    /** How a pending/late comment is applied to the value it belongs to (its on-disk comment and screen row). */
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
        this.gateStack.push(() -> true); // root is always enabled
        Consumer<AfterLanguageLoadEvent> consumer = e -> {
            if (e.isDefault()) translations.forEach(e::addEntry);
        };
        MoonlightEventsHelper.addListener(consumer, AfterLanguageLoadEvent.class);
        Moonlight.addDependent(name.getNamespace()); //add config mod id
    }

    public abstract ModConfigHolder build();

    public ResourceLocation getName() {
        return name;
    }

    public abstract ConfigBuilder push(String category);

    public abstract ConfigBuilder pop();

    public <T extends ConfigBuilder> T setWriteJsons() {
        this.usesDataBuddy = false;
        return (T) this;
    }

    public <T extends ConfigBuilder> T affectsDynamicPacks() {
        this.pendingDynamicPacks = true;
        return (T) this;
    }

    public <T> T affectsDynamicPacks(T config) {
        if (config instanceof IConfigWrapper dynamicPackAffecting) {
            dynamicPackAffecting.setAffectsDynamicPacks(true);
        }
        return config;
    }

    public abstract Supplier<Boolean> define(String name, boolean defaultValue);

    public abstract Supplier<Double> define(String name, double defaultValue, double min, double max);

    public abstract Supplier<Float> define(String name, float defaultValue, float min, float max);

    public abstract Supplier<Integer> define(String name, int defaultValue, int min, int max);

    public abstract Supplier<Integer> defineColor(String name, int defaultValue);

    public abstract Supplier<Integer> defineSlider(String name, int defaultValue, int min, int max);

    public abstract Supplier<Double> defineSlider(String name, double defaultValue, double min, double max);

    public abstract Supplier<Float> defineSlider(String name, float defaultValue, float min, float max);

    public abstract Supplier<Double> definePercentage(String name, double defaultValue);

    public abstract Supplier<String> define(String name, String defaultValue, Predicate<Object> validator);

    public Supplier<String> define(String name, String defaultValue) {
        return define(name, defaultValue, STRING_CHECK);
    }

    public Supplier<Pattern> defineRegex(String name, String defaultValue) {
        return new RegexPatternValue(defineRegexSource(name, defaultValue));
    }

    /** Platform hook: stores the regex as a string and records the {@link ConfigOption.RegexValue} screen row. */
    protected abstract Supplier<String> defineRegexSource(String name, String defaultValue);

    /**
     * Platform hook backing every dropdown/picker: stores a string (validated by {@code validator}) and records a
     * {@link ConfigOption.DropdownValue} screen row with the given lazy {@code options} and optional {@code icon}.
     */
    protected abstract Supplier<String> defineChoiceSource(String name, String defaultValue, Predicate<Object> validator,
                                                           Supplier<List<String>> options, @Nullable Function<String, ItemStack> icon);

    public Supplier<String> defineDropdown(String name, String defaultValue, List<String> options) {
        List<String> copy = List.copyOf(options);
        return defineChoiceSource(name, defaultValue, o -> o instanceof String s && copy.contains(s), () -> copy, null);
    }

    public Supplier<ResourceLocation> defineRegistry(String name, ResourceLocation defaultValue, Registry<?> registry) {
        Supplier<String> handle = defineChoiceSource(name, defaultValue.toString(), REGISTRY_ID_CHECK,
                () -> registryIds(registry), null);
        return () -> ResourceLocation.parse(handle.get());
    }

    public Supplier<Item> defineItem(String name, ResourceLocation defaultValue) {
        Supplier<String> handle = defineChoiceSource(name, defaultValue.toString(), REGISTRY_ID_CHECK,
                () -> registryIds(BuiltInRegistries.ITEM),
                id -> new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(id))));
        return () -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(handle.get()));
    }

    public Supplier<Block> defineBlock(String name, ResourceLocation defaultValue) {
        Supplier<String> handle = defineChoiceSource(name, defaultValue.toString(), REGISTRY_ID_CHECK,
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

    /**
     * Platform hook backing the option-backed lists below: stores a string list (entries validated by
     * {@code entryValidator}) and records a {@link ConfigOption.ListValue} whose editor rows are dropdowns fed by
     * {@code options} (with an optional {@code icon}).
     */
    protected abstract Supplier<List<String>> defineListSource(String name, List<String> defaultValue,
                                                               Predicate<Object> entryValidator,
                                                               Supplier<List<String>> options,
                                                               @Nullable Function<String, ItemStack> icon);

    /** A string list whose entries are each picked from a fixed set of {@code options} via a dropdown. */
    public Supplier<List<String>> defineList(String name, List<String> defaultValue, List<String> options) {
        List<String> copy = List.copyOf(options);
        return defineListSource(name, defaultValue, o -> o instanceof String s && copy.contains(s), () -> copy, null);
    }

    /** A list of registry ids, each picked from {@code registry} via a dropdown. Stored as resource location strings. */
    public Supplier<List<ResourceLocation>> defineRegistryList(String name, List<ResourceLocation> defaultValue, Registry<?> registry) {
        Supplier<List<String>> handle = defineListSource(name, idStrings(defaultValue), REGISTRY_ID_CHECK,
                () -> registryIds(registry), null);
        return () -> handle.get().stream().map(ResourceLocation::parse).toList();
    }

    /** Like {@link #defineRegistryList} but preset to the item registry, previewing each item's icon. */
    public Supplier<List<Item>> defineItemList(String name, List<ResourceLocation> defaultValue) {
        Supplier<List<String>> handle = defineListSource(name, idStrings(defaultValue), REGISTRY_ID_CHECK,
                () -> registryIds(BuiltInRegistries.ITEM),
                id -> new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(id))));
        return () -> handle.get().stream().map(id -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(id))).toList();
    }

    /** Like {@link #defineRegistryList} but preset to the block registry, previewing each block's item icon. */
    public Supplier<List<Block>> defineBlockList(String name, List<ResourceLocation> defaultValue) {
        Supplier<List<String>> handle = defineListSource(name, idStrings(defaultValue), REGISTRY_ID_CHECK,
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

    //be very careful with these as you might use some objects that aren't registered yet and things will break
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

    /**
     * Defines a config value from a plain Java bean (POJO or record) — for when you'd rather not write a
     * {@link Codec}. Instead of storing a JSON blob, each field is reflectively turned into its own native config
     * value (a boolean toggle / string / number / enum widget) grouped under a sub-category named {@code name}, and
     * the returned supplier reconstructs the bean from those live values. Supported field types: boolean, int,
     * double, float, String and enums; the bean must be either a record or have a no-arg constructor.
     */
    public <T> Supplier<T> defineBean(String name, T defaultValue) {
        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) defaultValue.getClass();
        this.push(name);
        try {
            return type.isRecord() ? defineRecordBean(type, defaultValue) : definePojoBean(type, defaultValue);
        } finally {
            this.pop();
        }
    }

    private <T> Supplier<T> definePojoBean(Class<T> type, T defaultValue) {
        List<java.lang.reflect.Field> fields = new java.util.ArrayList<>();
        List<Supplier<?>> readers = new java.util.ArrayList<>();
        try {
            for (java.lang.reflect.Field f : type.getDeclaredFields()) {
                int mods = f.getModifiers();
                if (java.lang.reflect.Modifier.isStatic(mods) || java.lang.reflect.Modifier.isTransient(mods)) continue;
                f.setAccessible(true);
                fields.add(f);
                readers.add(defineBeanField(f.getName(), f.getType(), f.get(defaultValue)));
            }
            var ctor = type.getDeclaredConstructor();
            ctor.setAccessible(true);
            return () -> {
                try {
                    T instance = ctor.newInstance();
                    for (int i = 0; i < fields.size(); i++) fields.get(i).set(instance, readers.get(i).get());
                    return instance;
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException("Failed to build bean " + type.getName(), e);
                }
            };
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("defineBean: " + type.getName() + " needs a no-arg constructor and readable fields", e);
        }
    }

    private <T> Supplier<T> defineRecordBean(Class<T> type, T defaultValue) {
        java.lang.reflect.RecordComponent[] comps = type.getRecordComponents();
        List<Supplier<?>> readers = new java.util.ArrayList<>();
        Class<?>[] paramTypes = new Class<?>[comps.length];
        try {
            for (int i = 0; i < comps.length; i++) {
                paramTypes[i] = comps[i].getType();
                readers.add(defineBeanField(comps[i].getName(), comps[i].getType(), comps[i].getAccessor().invoke(defaultValue)));
            }
            var ctor = type.getDeclaredConstructor(paramTypes);
            ctor.setAccessible(true);
            return () -> {
                try {
                    Object[] args = new Object[readers.size()];
                    for (int i = 0; i < args.length; i++) args[i] = readers.get(i).get();
                    return ctor.newInstance(args);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException("Failed to build record " + type.getName(), e);
                }
            };
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("defineBean: failed to read record " + type.getName(), e);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Supplier<?> defineBeanField(String name, Class<?> type, Object current) {
        if (type == boolean.class || type == Boolean.class) return define(name, (Boolean) current);
        if (type == int.class || type == Integer.class) return define(name, (Integer) current, Integer.MIN_VALUE, Integer.MAX_VALUE);
        if (type == double.class || type == Double.class) return define(name, (Double) current, -Double.MAX_VALUE, Double.MAX_VALUE);
        if (type == float.class || type == Float.class) return define(name, (Float) current, -Float.MAX_VALUE, Float.MAX_VALUE);
        if (type == String.class) return define(name, (String) current, o -> true);
        if (type.isEnum()) return define(name, (Enum) current);
        throw new IllegalArgumentException("defineBean: unsupported field type " + type.getName() + " for field '" + name + "'");
    }


    public Supplier<ResourceLocation> define(String name, ResourceLocation defaultValue) {
        var value = new ResourceLocationConfigValue(this, name, defaultValue);
        return applyPendingDynamicPacks(value);
    }

    protected <T> T applyPendingDynamicPacks(T value) {
        if (this.pendingDynamicPacks && value instanceof IConfigWrapper dynamicPackAffecting) {
            dynamicPackAffecting.setAffectsDynamicPacks(true);
        }
        this.pendingDynamicPacks = false;
        return value;
    }

    private static class ResourceLocationConfigValue implements Supplier<ResourceLocation>, IConfigWrapper {

        private final Supplier<String> inner;
        private ResourceLocation cache;
        private String oldString;
        private boolean affectsDynamicPacks;

        public ResourceLocationConfigValue(ConfigBuilder builder, String path, ResourceLocation defaultValue) {
            this.inner = builder.define(path, defaultValue.toString(), s -> s != null && ResourceLocation.tryParse((String) s) != null);
            if (this.inner instanceof IConfigWrapper dynamicPackAffecting) {
                this.affectsDynamicPacks = dynamicPackAffecting.affectsDynamicPacks();
            }
        }

        @Override
        public ResourceLocation get() {
            String s = inner.get();
            if (!s.equals(oldString)) cache = null;
            oldString = s;
            if (cache == null) cache = ResourceLocation.parse(s);
            return cache;
        }

        @Override
        public boolean affectsDynamicPacks() {
            return affectsDynamicPacks;
        }

        @Override
        public void setAffectsDynamicPacks(boolean affectsDynamicPacks) {
            this.affectsDynamicPacks = affectsDynamicPacks;
            if (inner instanceof IConfigWrapper dynamicPackAffecting) {
                dynamicPackAffecting.setAffectsDynamicPacks(affectsDynamicPacks);
            }
        }
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


    /**
     * Adds a comment/description to a value. Lenient: it may be called either before or after that value's
     * {@code define(...)}. A comment given before is held and attached to the next defined value; a comment
     * given after attaches to the most recently defined value. Ends up in the english language file and as the
     * hover description of the value's screen row.
     */
    public ConfigBuilder comment(String comment) {
        if (this.lastCommentTarget != null) {
            // a value was just defined and is still waiting: this is an "after" comment for it
            applyComment(comment);
        } else {
            // no value waiting: hold it for the next define ("before" comment)
            this.pendingComment = comment;
        }
        return this;
    }

    /**
     * Multi line comment, mirroring Forge's {@code ModConfigSpec.Builder.comment(String...)}. Like the single
     * line version it may come before or after the value's {@code define(...)}.
     */
    public ConfigBuilder comment(String... comment) {
        return comment(String.join("\n", comment));
    }

    /** Whether the last defined value is still waiting to (maybe) receive a comment that follows it. */
    protected boolean isAwaitingAfterComment() {
        return this.lastCommentTarget != null;
    }

    /**
     * Forge parity alias for {@link #worldReload()} (Forge calls it {@code worldRestart()}), so configs can be
     * ported over with fewer edits.
     */
    public ConfigBuilder worldRestart() {
        return worldReload();
    }

    public ConfigBuilder pop(int count) {
        for (int i = 0; i < count; i++) pop();
        return this;
    }

    /**
     * Accepted for Forge parity ({@code ModConfigSpec.Builder.translation}), but a no-op: Moonlight derives the
     * translation keys automatically from the category/value names, so an explicit key isn't needed.
     */
    public ConfigBuilder translation(String translationKey) {
        return this;
    }

    private void applyComment(String rawComment) {
        if (this.lastCommentKey != null) this.translations.put(this.lastCommentKey, rawComment);
        if (this.lastCommentTarget != null) this.lastCommentTarget.applyComment(rawComment);
        this.lastCommentTarget = null;
        this.lastCommentKey = null;
    }

    /**
     * Called by each define once its value and screen row exist. Wires the comment target so a comment given
     * before (held as {@link #pendingComment}) or after (via the retained target) reaches this value. Skipped
     * while {@link #suppressUi} is set, so the backing values of a compound value don't steal its comment.
     */
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
    }

    // ===== loader independent UI tree (consumed by the native config screen) =====

    /** Pushes a UI sub category mirroring a {@code push(...)}. No-op while UI emission is suppressed. */
    protected void uiPush(Component title) {
        if (this.suppressUi) return;
        ConfigCategory cat = new ConfigCategory(title);
        this.uiStack.peek().add(cat);
        this.uiStack.push(cat);
        this.gateStack.push(this.gateStack.peek()); // inherit the parent's gate until a feature() narrows it
    }

    /** Pops the current UI sub category. No-op while UI emission is suppressed. */
    protected void uiPop() {
        if (this.suppressUi) return;
        this.uiStack.pop();
        this.gateStack.pop();
    }

    /**
     * Declares the current category's single "feature" boolean — the switch that enables the whole category — and
     * returns its <em>effective</em> supplier: {@code ownValue && everyAncestorFeature}. The composition is
     * read-time only, so a parent turning off makes this (and any nested feature) read {@code false} without ever
     * rewriting the stored child values; turning the parent back on restores them. Only one feature per category.
     */
    public Supplier<Boolean> feature(boolean defaultEnabled) {
        ConfigCategory cat = this.uiStack.peek();
        if (cat == this.uiRoot) {
            throw new IllegalStateException("feature() must be called inside a category (use push/pushFeature first), not at the config root");
        }
        if (cat.gate() != null) {
            throw new IllegalStateException("category '" + currentCategory() + "' already has a feature() toggle");
        }
        Supplier<Boolean> raw = define(FEATURE_TOGGLE_NAME, defaultEnabled);
        // define() just recorded the matching BooleanValue as this category's last entry; adopt it as the gate row
        List<ConfigNode> entries = cat.entries();
        if (!entries.isEmpty() && entries.get(entries.size() - 1) instanceof ConfigOption.BooleanValue bv) {
            cat.setGate(bv);
        }
        Supplier<Boolean> ancestor = this.gateStack.peek();
        Supplier<Boolean> effective = () -> raw.get() && ancestor.get();
        this.gateStack.pop();            // replace the inherited gate with this category's own effective gate
        this.gateStack.push(effective);
        return effective;
    }

    /** Sugar for {@code push(name)} followed by {@link #feature(boolean)}. Pop it like any other category. */
    public Supplier<Boolean> pushFeature(String name, boolean defaultEnabled) {
        push(name);
        return feature(defaultEnabled);
    }

    /** Adds a value row to the current UI category, stamping (and clearing) any pending reload/restart flag onto it.
     *  No-op while UI emission is suppressed, so a compound value's backing rows don't consume the flag before it. */
    protected void recordOption(ConfigOption<?> option) {
        if (this.suppressUi) return;
        option.setReloadType(this.pendingReload);
        this.pendingReload = ConfigReloadType.NONE;
        this.uiStack.peek().add(option);
    }

    /** The root of the loader independent screen model, ready once {@link #build()} has run. */
    public ConfigCategory getUiRoot() {
        return this.uiRoot;
    }

    public Supplier<Range> defineRange(String name, Range defaultValue, double min, double max) {
        return defineRange(name, defaultValue.min(), defaultValue.max(), min, max);
    }

    public Supplier<Range> defineRange(String name, double defaultMin, double defaultMax, double min, double max) {
        // Storage: two doubles nested under a `name` section. Their individual rows are suppressed so the whole
        // range shows as a single combined row instead.
        this.suppressUi = true;
        push(name);
        Supplier<Double> minHandle = define("min", defaultMin, min, max);
        Supplier<Double> maxHandle = define("max", defaultMax, min, max);
        pop();
        this.suppressUi = false;

        this.translations.put(this.translationKey(name), LangBuilder.getReadableName(name));
        ConfigOption.RangeValue node = new ConfigOption.RangeValue(
                description(name), null, minHandle, maxHandle,
                new Range(defaultMin, defaultMax), min, max);
        recordOption(node);
        noteDefined(name, node, null);
        return () -> new Range(minHandle.get(), maxHandle.get());
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

    /**
     * Platform hook: forward the flag to a backing store that needs it set <em>before</em> the next define (Forge's
     * {@code ModConfigSpec}). The screen-side enum is instead read from {@link #pendingReload} at record time, so
     * loaders that keep the flag on their own value object (Fabric) don't need to override this.
     */
    protected void forwardReloadFlag(ConfigReloadType type) {
    }

    protected void addTranslationsAndComments(String name) {
        //name translation (comments are wired separately, see noteDefined/comment, so they can come before or after)
        this.translations.put(this.translationKey(name), LangBuilder.getReadableName(name));
        if (this.currentCategory() == null && PlatHelper.isDev())
            throw new AssertionError("Current config category was null. How?");
    }

    public static final Predicate<Object> STRING_CHECK = o -> o instanceof String;

    public static final Predicate<Object> REGEX_CHECK = o -> o instanceof String s && ConfigOption.RegexValue.isValidRegex(s);

    public static final Predicate<Object> LIST_STRING_CHECK = (s) -> {
        if (s instanceof List<?>) {
            return ((Collection<?>) s).stream().allMatch(o -> o instanceof String);
        }
        return false;
    };

}
