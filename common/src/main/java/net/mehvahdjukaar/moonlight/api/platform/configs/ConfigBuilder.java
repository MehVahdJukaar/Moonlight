package net.mehvahdjukaar.moonlight.api.platform.configs;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import net.mehvahdjukaar.candlelight.api.PlatformImpl;
import net.mehvahdjukaar.codecui.SchemaCodec;
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

/**
 * A loader independent config builder. Supports common config syncing.
 */
public abstract class ConfigBuilder {

    protected final Map<String, String> translations = new HashMap<>();
    protected Runnable changeCallback;
    protected boolean pendingDynamicPacks;

    // ===== lenient comment wiring =====
    // A comment(...) may come before or after its define(...). A comment always binds FORWARD to the next define
    // (pendingComment, consumed there). If no define claims it before another comment arrives or the section
    // closes, it was really an "after" comment for the last define (lastCommentTarget), so it's flushed there.
    // This forward-first rule stops an un-commented define (e.g. a feature() "enabled" toggle) from greedily
    // stealing the before-comment meant for the value that follows it. applyComment() writes the readable text
    // into the lang map and lets the platform stamp it onto the on-disk value and the screen row.
    @Nullable
    private String pendingComment;
    // whether pendingComment has already been handed to the backing store (Forge attaches to the next define, so
    // it must be forwarded once, before that define — see pollCommentToForward); avoids re-emitting it for each
    // suppressed backing value of a compound define.
    private boolean pendingCommentForwarded;
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

    // name -> effective enabled supplier for every feature() declared, so mods can query "is feature X on?" by name
    // (e.g. to gate content registration) without keeping their own map. Each feature is registered under BOTH its
    // short name and its full dotted path (see registerFeature). Handed to the built ModConfigHolder.
    private final Map<String, Supplier<Boolean>> featureToggles = new LinkedHashMap<>();
    // raw category-name stack (root first), kept parallel to uiStack so a feature's full path can be built
    private final Deque<String> categoryPath = new ArrayDeque<>();

    /**
     * Reserved child name for the boolean a {@link #feature} declares.
     */
    public static final String FEATURE_TOGGLE_NAME = "enabled";

    protected boolean usesDataBuddy = true; // on by default; setWriteJsons() disables it

    // set by worldReload()/gameRestart(), applied to (and cleared by) the next recorded option — see recordOption
    protected ConfigReloadType pendingReload = ConfigReloadType.NONE;

    // set by icon(...), applied to (and cleared by) the next category push or defined option — see uiPush/noteDefined
    @Nullable
    private ResourceLocation pendingIcon;

    /**
     * How a pending/late comment is applied to the value it belongs to (its on-disk comment and screen row).
     */
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
        Moonlight.addDependent(name.getNamespace());
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

    /**
     * Marks the <em>next</em> defined value as affecting dynamic resource/data packs (so changing it invalidates the
     * matching pack cache). Fluent and sticky until the next value is recorded, like {@link #worldReload()}.
     */
    public <T extends ConfigBuilder> T affectsDynamicPacks() {
        this.pendingDynamicPacks = true;
        return (T) this;
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

    /**
     * Platform hook: stores the regex as a string and records the {@link ConfigOption.RegexValue} screen row.
     */
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

    /**
     * A string list whose entries are each picked from a fixed set of {@code options} via a dropdown.
     */
    public Supplier<List<String>> defineList(String name, List<String> defaultValue, List<String> options) {
        List<String> copy = List.copyOf(options);
        return defineListSource(name, defaultValue, o -> o instanceof String s && copy.contains(s), () -> copy, null);
    }

    /**
     * A list of registry ids, each picked from {@code registry} via a dropdown. Stored as resource location strings.
     */
    public Supplier<List<ResourceLocation>> defineRegistryList(String name, List<ResourceLocation> defaultValue, Registry<?> registry) {
        Supplier<List<String>> handle = defineListSource(name, idStrings(defaultValue), REGISTRY_ID_CHECK,
                () -> registryIds(registry), null);
        return () -> handle.get().stream().map(ResourceLocation::parse).toList();
    }

    /**
     * Like {@link #defineRegistryList} but preset to the item registry, previewing each item's icon.
     */
    public Supplier<List<Item>> defineItemList(String name, List<ResourceLocation> defaultValue) {
        Supplier<List<String>> handle = defineListSource(name, idStrings(defaultValue), REGISTRY_ID_CHECK,
                () -> registryIds(BuiltInRegistries.ITEM),
                id -> new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(id))));
        return () -> handle.get().stream().map(id -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(id))).toList();
    }

    /**
     * Like {@link #defineRegistryList} but preset to the block registry, previewing each block's item icon.
     */
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

    /**
     * Defines a value stored via {@code codec}, edited on the native config screen as a schema-driven form: the codec
     * is wrapped into a {@link SchemaCodec} (one that already is keeps its declared schema; a plain codec degrades to
     * a raw-JSON editor). Be careful with defaults referencing objects that aren't registered this early.
     */
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
     * Defines a config value from a plain Java bean (POJO or record), for when you'd rather not write a {@link Codec}.
     * Each field becomes its own native config value grouped under a sub-category named {@code name}, and the returned
     * supplier reconstructs the bean from those live values. Supported field types: boolean, int, double, float, String
     * and enums; the bean must be a record or have a no-arg constructor.
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
        if (type == int.class || type == Integer.class)
            return define(name, (Integer) current, Integer.MIN_VALUE, Integer.MAX_VALUE);
        if (type == double.class || type == Double.class)
            return define(name, (Double) current, -Double.MAX_VALUE, Double.MAX_VALUE);
        if (type == float.class || type == Float.class)
            return define(name, (Float) current, -Float.MAX_VALUE, Float.MAX_VALUE);
        // must reject null: a null-accepting validator (o -> true) makes NeoForge treat a MISSING string field as
        // valid, so it never writes the default. The key stays absent while the spec still carries its comment,
        // which NeoForge tries to re-apply every load -> endless "config is not correct. Correcting" loop.
        if (type == String.class) return define(name, (String) current);
        if (type.isEnum()) return define(name, (Enum) current);
        throw new IllegalArgumentException("defineBean: unsupported field type " + type.getName() + " for field '" + name + "'");
    }


    public Supplier<ResourceLocation> define(String name, ResourceLocation defaultValue) {
        // stored (and screen-edited) as a validated string; the returned supplier just parses it, exactly like
        // defineRegistry/defineItem below. The dynamic-pack flag rides on the backing string handle.
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


    /**
     * Adds a comment/description to a value. Lenient: it may be called either before or after that value's
     * {@code define(...)}. A comment always binds forward to the next define; if none follows before another
     * comment arrives or the section closes, it falls back onto the most recently defined value (an "after"
     * comment). Ends up in the english language file and as the hover description of the value's screen row.
     */
    public ConfigBuilder comment(String comment) {
        // a new comment means the previous one had no define of its own -> it was an "after" comment; flush it first
        if (this.pendingComment != null) applyComment(this.pendingComment);
        this.pendingComment = comment;
        this.pendingCommentForwarded = false;
        return this;
    }

    /**
     * Multi line variant of {@link #comment(String)}, mirroring Forge's {@code comment(String...)}.
     */
    public ConfigBuilder comment(String... comment) {
        return comment(String.join("\n", comment));
    }

    /**
     * Attaches a decorative icon to the NEXT category {@code push(...)}/{@code pushFeature(...)} or defined value.
     * Eye candy for the native config screen, resolved to an item/block (or GUI sprite) lazily on the client, so the
     * {@code id} may reference things not registered yet at build time. Chainable, like {@link #comment}.
     */
    public ConfigBuilder icon(ResourceLocation id) {
        this.pendingIcon = id;
        return this;
    }

    /**
     * Convenience {@link #icon(ResourceLocation)}: a bare path (no {@code :}) is namespaced to this config's mod id,
     * so {@code icon("faucet")} means {@code <modid>:faucet}.
     */
    public ConfigBuilder icon(String id) {
        return icon(id.indexOf(':') >= 0
                ? ResourceLocation.parse(id)
                : ResourceLocation.fromNamespaceAndPath(this.name.getNamespace(), id));
    }

    /**
     * A still-pending comment at a section boundary ({@link #pop()}, {@link #build()}) had no following define, so
     * it was an "after" comment for the last defined value; attach it there. Skipped while {@link #suppressUi} is
     * set, because a compound value's internal push/pop (see {@link #defineRange}) must not consume the comment
     * before the compound's own combined row does.
     */
    protected void flushPendingComment() {
        if (this.suppressUi) return;
        if (this.pendingComment != null) {
            applyComment(this.pendingComment);
            this.pendingComment = null;
        }
    }

    /**
     * For platforms whose backing store attaches a comment to the NEXT define (Forge's {@code ModConfigSpec}):
     * returns the pending before-comment exactly once, to be forwarded right before that define runs, then marks
     * it forwarded so the suppressed backing values of a compound value don't each re-emit it. Returns
     * {@code null} when there is nothing new to forward.
     */
    @Nullable
    protected String pollCommentToForward() {
        if (this.pendingComment != null && !this.pendingCommentForwarded) {
            this.pendingCommentForwarded = true;
            return this.pendingComment;
        }
        return null;
    }

    /**
     * Forge parity alias for {@link #worldReload()} (Forge calls it {@code worldRestart()}).
     */
    public ConfigBuilder worldRestart() {
        return worldReload();
    }

    public ConfigBuilder pop(int count) {
        for (int i = 0; i < count; i++) pop();
        return this;
    }

    /**
     * Accepted for Forge parity but a no-op: Moonlight derives translation keys from the category/value names.
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
        if (this.pendingIcon != null && uiNode != null) {
            uiNode.setIcon(this.pendingIcon);
            this.pendingIcon = null;
        }
    }

    // ===== loader independent UI tree (consumed by the native config screen) =====

    /**
     * Pushes a UI sub category mirroring a {@code push(...)}. No-op while UI emission is suppressed.
     */
    protected void uiPush(Component title) {
        if (this.suppressUi) return;
        ConfigCategory cat = new ConfigCategory(title);
        if (this.pendingIcon != null) { // an icon(...) right before this push decorates the category row
            cat.setIcon(this.pendingIcon);
            this.pendingIcon = null;
        }
        this.uiStack.peek().add(cat);
        this.uiStack.push(cat);
        this.gateStack.push(this.gateStack.peek()); // inherit the parent's gate until a feature() narrows it
        this.categoryPath.addLast(currentCategory()); // raw-name path, so features can be looked up by full path
    }

    /**
     * Pops the current UI sub category. No-op while UI emission is suppressed.
     */
    protected void uiPop() {
        if (this.suppressUi) return;
        this.uiStack.pop();
        this.gateStack.pop();
        this.categoryPath.pollLast();
    }

    /** The current category as a dotted raw-name path (e.g. {@code redstone.speaker_block}), root first. */
    protected String currentCategoryPath() {
        return String.join(".", this.categoryPath);
    }

    /**
     * Declares the current category's single "feature" boolean — the switch that enables the whole category — and
     * returns its <em>effective</em> supplier: {@code ownValue && everyAncestorFeature}. The composition is
     * read-time only, so a parent turning off makes this (and any nested feature) read {@code false} without ever
     * rewriting the stored child values; turning the parent back on restores them. Only one feature per category.
     */
    public Supplier<Boolean> mainFeature(boolean defaultEnabled) {
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
            // icon priority: an explicit icon(...) on the value or the category wins; otherwise infer one from the
            // category's own name (a feature named after its block shows that block). Then mirror it onto the category
            // button so it and the enable-gate row share the same icon.
            if (bv.icon() == null) bv.setIcon(cat.icon() != null ? cat.icon() : inferFeatureIcon(currentCategory()));
            if (cat.icon() == null) cat.setIcon(bv.icon());
        }
        Supplier<Boolean> ancestor = this.gateStack.peek();
        Supplier<Boolean> effective = () -> raw.get() && ancestor.get();
        this.gateStack.pop();            // replace the inherited gate with this category's own effective gate
        this.gateStack.push(effective);
        // a category gate is referenced by the category's own name, so its short key IS the category name
        registerFeature(currentCategory(), currentCategoryPath(), effective);
        return effective;
    }

    /** A category gate enabled by default, mirroring {@link #pushFeature(String)}. */
    public Supplier<Boolean> mainFeature() {
        return this.mainFeature(true);
    }


    /**
     * Declares a named boolean "feature" leaf: a standalone toggle that, like a category's {@link #mainFeature(boolean)}
     * gate, draws as the ✓/✗ switch (with its {@link #icon} shown next to the symbol) rather than a plain ON/OFF
     * button. The returned supplier is <em>effective</em>: {@code ownValue && everyAncestorFeature}, so it reads
     * {@code false} whenever an enclosing feature category is off, without ever rewriting the stored value. Combine
     * with {@link #icon}: {@code builder.icon("lever").feature("test_bool", true)}.
     */
    public Supplier<Boolean> feature(String name, boolean defaultEnabled) {
        Supplier<Boolean> raw = define(name, defaultEnabled);
        // adopt the just-recorded BooleanValue so the client draws it as a ✓/✗ toggle instead of an ON/OFF button
        List<ConfigNode> entries = this.uiStack.peek().entries();
        if (!entries.isEmpty() && entries.get(entries.size() - 1) instanceof ConfigOption.BooleanValue bv) {
            bv.setFeature(true);
            // infer the icon from the feature's own name unless an explicit icon(...) already set one
            if (bv.icon() == null) bv.setIcon(inferFeatureIcon(name));
        }
        Supplier<Boolean> ancestor = this.gateStack.peek();
        Supplier<Boolean> effective = () -> raw.get() && ancestor.get();
        // a leaf feature lives under the current category, so its full path is that category path plus its own name
        String path = this.categoryPath.isEmpty() ? name : currentCategoryPath() + "." + name;
        registerFeature(name, path, effective);
        return effective;
    }

    /**
     * Infers a feature's decorative icon from its name: {@code <this config's mod id>:<name>}. Resolved lazily on the
     * client (see {@code ConfigScreenIcons}), so a name that isn't a real item/block simply shows no icon. An explicit
     * {@link #icon} always takes precedence. Returns {@code null} for a name that isn't a valid resource path.
     */
    @Nullable
    private ResourceLocation inferFeatureIcon(String name) {
        return ResourceLocation.tryBuild(this.name.getNamespace(), name);
    }

    /**
     * Registers a feature's effective supplier under both its short {@code name} and its full dotted {@code path}, so
     * it can be queried either way via {@link ModConfigHolder#isFeatureEnabled}. The short name is the convenient
     * shorthand; the full path disambiguates when two features in different categories share a short name (the last
     * short-name registration wins, but the full path is always unique).
     */
    private void registerFeature(String name, String path, Supplier<Boolean> effective) {
        this.featureToggles.put(name, effective);
        this.featureToggles.put(path, effective);
    }

    /** The feature registry (short name and full path -> effective enabled supplier), handed to the built holder. */
    protected Map<String, Supplier<Boolean>> getFeatureToggles() {
        return this.featureToggles;
    }

    /** A named leaf feature enabled by default. */
    public Supplier<Boolean> feature(String name) {
        return feature(name, true);
    }

    public Supplier<Boolean> pushFeature(String name, boolean defaultEnabled) {
        push(name);
        return mainFeature(defaultEnabled);
    }

    public Supplier<Boolean> pushFeature(String name) {
        return pushFeature(name, true);
    }

    /**
     * Adds a value row to the current UI category, stamping (and clearing) any pending reload/restart flag onto it.
     * No-op while UI emission is suppressed, so a compound value's backing rows don't consume the flag before it.
     */
    protected void recordOption(ConfigOption<?> option) {
        if (this.suppressUi) return;
        // Both change-effect flags (reload + dynamic packs) were already stamped onto each backing leaf value as it
        // was defined (see the platform builders); the option derives what it shows from those leaves. Here we just
        // clear the pending flags at this single, compound-safe boundary so they don't leak onto the next value. A
        // grouped value keeps them set across its suppressed inner defines (recordOption no-ops while suppressed),
        // so every leaf of the group is stamped, not just the first.
        this.pendingReload = ConfigReloadType.NONE;
        this.pendingDynamicPacks = false;
        this.uiStack.peek().add(option);
    }

    /**
     * The root of the loader independent screen model, ready once {@link #build()} has run.
     */
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

    /**
     * A {@link Vec3} (three doubles) shown as one row of x/y/z number fields, each bounded by {@code [min, max]}.
     * Stored as three doubles nested under a {@code name} section (their individual rows suppressed, like
     * {@link #defineRange}), written back together.
     */
    public Supplier<Vec3> defineVec3(String name, Vec3 defaultValue, double min, double max) {
        this.suppressUi = true;
        push(name);
        Supplier<Double> xHandle = define("x", defaultValue.x, min, max);
        Supplier<Double> yHandle = define("y", defaultValue.y, min, max);
        Supplier<Double> zHandle = define("z", defaultValue.z, min, max);
        pop();
        this.suppressUi = false;

        this.translations.put(this.translationKey(name), LangBuilder.getReadableName(name));
        ConfigOption.Vec3Value node = new ConfigOption.Vec3Value(
                description(name), null, xHandle, yHandle, zHandle, defaultValue, min, max);
        recordOption(node);
        noteDefined(name, node, null);
        return () -> new Vec3(xHandle.get(), yHandle.get(), zHandle.get());
    }

    /**
     * A {@link Vec3i} (three ints) shown as one row of x/y/z number fields, each bounded by {@code [min, max]}.
     */
    public Supplier<Vec3i> defineVec3i(String name, Vec3i defaultValue, int min, int max) {
        this.suppressUi = true;
        push(name);
        Supplier<Integer> xHandle = define("x", defaultValue.getX(), min, max);
        Supplier<Integer> yHandle = define("y", defaultValue.getY(), min, max);
        Supplier<Integer> zHandle = define("z", defaultValue.getZ(), min, max);
        pop();
        this.suppressUi = false;

        this.translations.put(this.translationKey(name), LangBuilder.getReadableName(name));
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
