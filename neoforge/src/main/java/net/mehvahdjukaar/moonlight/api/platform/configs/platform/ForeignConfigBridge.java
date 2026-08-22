package net.mehvahdjukaar.moonlight.api.platform.configs.platform;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigMetadata;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.moonlight.api.platform.configs.IConfigValue;
import net.mehvahdjukaar.moonlight.api.platform.configs.ModConfigHolder;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigCategory;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigOption;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigReloadType;
import net.mehvahdjukaar.moonlight.api.util.TextHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.client.config.MoonlightConfigSelectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.config.ConfigTracker;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class ForeignConfigBridge {

    // one holder per foreign ModConfig, so re-opening the screen reuses it (and keeps reading current values live)
    private static final Map<ModConfig, ForeignConfigHolder> CACHE = new WeakHashMap<>();

    // ConfigTracker exposes no public "configs of mod X" accessor, so read its live registry field once
    private static final Map<String, List<ModConfig>> CONFIGS_BY_MOD = configsByModField();

    // building a screen just to look at its class is not free, so each mod is asked once
    private static final Map<String, Boolean> GENERIC_SCREEN_CACHE = new HashMap<>();

    private static final String CONFIGURED_PACKAGE = "com.mrcrayfish.configured.";

    @Nullable
    public static Screen createScreen(String modId, Screen parent, @Nullable ResourceLocation background) {
        List<ModConfigHolder> holders = holdersFor(modId);
        if (holders.isEmpty()) return null;
        return MoonlightConfigSelectScreen.create(modId, holders, parent, background);
    }

    /**
     * The mod either registered no config screen at all, or registered one of the stock ones anybody gets for free:
     * NeoForge's ConfigurationScreen, or Configured's. Either way there is no hand made screen to override.
     */
    public static boolean hasOnlyGenericScreen(String modId) {
        return GENERIC_SCREEN_CACHE.computeIfAbsent(modId, ForeignConfigBridge::readIsGenericScreen);
    }

    private static boolean readIsGenericScreen(String modId) {
        ModContainer container = ModList.get().getModContainerById(modId).orElse(null);
        if (container == null) return false;
        IConfigScreenFactory factory = container.getCustomExtension(IConfigScreenFactory.class).orElse(null);
        if (factory == null) return true;
        try {
            // the factory is a lambda in the registering mod's class, so the only way to tell them apart is the
            // screen it hands back. Building one is harmless, it's the init() call that does the work
            Screen screen = factory.createScreen(container, null);
            return screen instanceof ConfigurationScreen
                    || screen.getClass().getName().startsWith(CONFIGURED_PACKAGE);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean hasHiddenPerWorldConfig(String modId) {
        for (ModConfig mc : CONFIGS_BY_MOD.getOrDefault(modId, List.of())) {
            if (mc.getType() != ModConfig.Type.SERVER) continue;
            if (!(mc.getSpec() instanceof ModConfigSpec spec) || !spec.isLoaded()) return true;
        }
        return false;
    }

    // cheap check, no tree building: does this mod expose a loaded, non-Moonlight spec?
    public static boolean hasConfig(String modId) {
        for (ModConfig mc : CONFIGS_BY_MOD.getOrDefault(modId, List.of())) {
            if (ForgeConfigHolder.getFromForgeConfig(mc) != null) continue;
            if (mc.getSpec() instanceof ModConfigSpec spec && spec.isLoaded() && !spec.isEmpty()) return true;
        }
        return false;
    }

    private static List<ModConfigHolder> holdersFor(String modId) {
        List<ModConfig> configs = CONFIGS_BY_MOD.getOrDefault(modId, List.of());
        List<ModConfigHolder> out = new ArrayList<>();
        for (ModConfig mc : configs) {
            // skip anything Moonlight itself created: those already have a real holder and native screen
            if (ForgeConfigHolder.getFromForgeConfig(mc) != null) continue;
            if (!(mc.getSpec() instanceof ModConfigSpec spec)) continue;
            // can't safely read/write an unloaded spec (e.g. a server config with no world open)
            if (!spec.isLoaded()) continue;
            try {
                ForeignConfigHolder holder = CACHE.get(mc);
                if (holder == null) {
                    holder = build(modId, mc, spec);
                    CACHE.put(mc, holder);
                }
                if (holder.getConfigRoot() != null && !holder.getConfigRoot().isEmpty()) out.add(holder);
            } catch (Exception e) {
                Moonlight.LOGGER.warn("Failed to adapt config {} of mod {}", mc.getFileName(), modId, e);
            }
        }
        return out;
    }

    private static ForeignConfigHolder build(String modId, ModConfig mc, ModConfigSpec spec) {
        ConfigType type = switch (mc.getType()) {
            case CLIENT -> ConfigType.CLIENT;
            case SERVER -> ConfigType.COMMON_SYNCED; // world bound, so it gets the server paper icon
            default -> ConfigType.COMMON;
        };
        String typeName = mc.getType().name().toLowerCase(Locale.ROOT);
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(modId, typeName);

        ConfigCategory root = new ConfigCategory(Component.empty());
        walk(spec, spec.getValues(), List.of(), root);

        Component name = Component.literal(PlatHelper.getModName(modId) + " - " + TextHelper.getReadableName(typeName));
        return new ForeignConfigHolder(id, type, spec, root, name);
    }

    private static void walk(ModConfigSpec spec, UnmodifiableConfig config, List<String> path, ConfigCategory parent) {
        for (UnmodifiableConfig.Entry entry : config.entrySet()) {
            String key = entry.getKey();
            List<String> childPath = append(path, key);
            Object raw = entry.getRawValue();
            if (raw instanceof UnmodifiableConfig sub) {
                ConfigCategory cat = new ConfigCategory(categoryTitle(spec, childPath, key));
                String comment = spec.getLevelComment(childPath);
                if (comment != null) cat.setDescription(Component.literal(comment));
                walk(spec, sub, childPath, cat);
                if (!cat.isEmpty()) parent.add(cat); // drop categories that produced no rows
            } else if (raw instanceof ModConfigSpec.ConfigValue<?> cv) {
                ConfigOption<?> option = leaf(spec, cv);
                if (option != null) parent.add(option);
            }
        }
    }

    @Nullable
    private static ConfigOption<?> leaf(ModConfigSpec spec, ModConfigSpec.ConfigValue<?> cv) {
        List<String> path = cv.getPath();
        Object specEntry = spec.getSpec().get(path);
        if (!(specEntry instanceof ModConfigSpec.ValueSpec vs)) return null;

        String key = path.isEmpty() ? "" : path.get(path.size() - 1);
        Component title = leafTitle(vs, key);
        Component desc = vs.getComment() != null ? Component.literal(vs.getComment()) : null;
        ConfigMetadata meta = new ConfigMetadata(reloadType(vs.restartType()), false);

        Object sample = vs.getDefault() != null ? vs.getDefault() : cv.get();

        if (sample instanceof Boolean b) {
            return new ConfigOption.BooleanValue(title, desc, wrap(cv, meta), b);
        }
        if (sample instanceof Enum<?> e) {
            Enum<?>[] options = (Enum<?>[]) e.getDeclaringClass().getEnumConstants();
            return new ConfigOption.EnumValue(title, desc, wrap(cv, meta), e, options);
        }
        if (sample instanceof Integer i) {
            int[] r = intRange(vs);
            return new ConfigOption.IntValue(title, desc, wrap(cv, meta), i, r[0], r[1]);
        }
        if (sample instanceof Long l) {
            // no long control: present it as an int when the range fits, else leave it uneditable
            long[] r = longRange(vs);
            if (r[0] >= Integer.MIN_VALUE && r[1] <= Integer.MAX_VALUE) {
                return new ConfigOption.IntValue(title, desc, longAsInt(cv, meta), l.intValue(), (int) r[0], (int) r[1]);
            }
            return new ConfigOption.UnsupportedValue(title, desc, (Supplier<Object>) (Supplier<?>) cv);
        }
        if (sample instanceof Double d) {
            double[] r = doubleRange(vs);
            return new ConfigOption.DoubleValue(title, desc, wrap(cv, meta), d, r[0], r[1]);
        }
        if (sample instanceof String s) {
            return new ConfigOption.StringValue(title, desc, wrap(cv, meta), s, vs::test);
        }
        if (sample instanceof List<?> list && list.stream().allMatch(o -> o instanceof String)) {
            List<String> def = list.stream().map(o -> (String) o).toList();
            return new ConfigOption.ListValue(title, desc, wrap(cv, meta), def, null);
        }
        return new ConfigOption.UnsupportedValue(title, desc, (Supplier<Object>) (Supplier<?>) cv);
    }

    private static IConfigValue wrap(ModConfigSpec.ConfigValue<?> cv, ConfigMetadata meta) {
        return ForgeConfigValue.simple((ModConfigSpec.ConfigValue) cv, meta);
    }

    // adapts a long-backed value to the int control; the range was already checked to fit
    private static IConfigValue<Integer> longAsInt(ModConfigSpec.ConfigValue<?> cvRaw, ConfigMetadata meta) {
        ModConfigSpec.ConfigValue<Long> cv = (ModConfigSpec.ConfigValue<Long>) cvRaw;
        return new IConfigValue<>() {
            @Override
            public Integer get() {
                return cv.get().intValue();
            }

            @Override
            public boolean setValue(Integer value) {
                boolean changed = cv.get().longValue() != value.longValue();
                cv.set(value.longValue());
                cv.clearCache();
                return changed;
            }

            @Override
            public ConfigReloadType reloadType() {
                return meta.reloadType();
            }

            @Override
            public boolean affectsDynamicPacks() {
                return meta.affectsDynamicPacks();
            }
        };
    }

    private static int[] intRange(ModConfigSpec.ValueSpec vs) {
        ModConfigSpec.Range<?> r = vs.getRange();
        if (r != null && r.getMin() instanceof Number min && r.getMax() instanceof Number max) {
            return new int[]{min.intValue(), max.intValue()};
        }
        return new int[]{Integer.MIN_VALUE, Integer.MAX_VALUE};
    }

    private static long[] longRange(ModConfigSpec.ValueSpec vs) {
        ModConfigSpec.Range<?> r = vs.getRange();
        if (r != null && r.getMin() instanceof Number min && r.getMax() instanceof Number max) {
            return new long[]{min.longValue(), max.longValue()};
        }
        return new long[]{Long.MIN_VALUE, Long.MAX_VALUE};
    }

    private static double[] doubleRange(ModConfigSpec.ValueSpec vs) {
        ModConfigSpec.Range<?> r = vs.getRange();
        if (r != null && r.getMin() instanceof Number min && r.getMax() instanceof Number max) {
            return new double[]{min.doubleValue(), max.doubleValue()};
        }
        return new double[]{-Double.MAX_VALUE, Double.MAX_VALUE};
    }

    private static Component leafTitle(ModConfigSpec.ValueSpec vs, String key) {
        String tk = vs.getTranslationKey();
        if (tk != null && I18n.exists(tk)) return Component.translatable(tk);
        return Component.literal(TextHelper.getReadableName(key));
    }

    private static Component categoryTitle(ModConfigSpec spec, List<String> path, String key) {
        String tk = spec.getLevelTranslationKey(path);
        if (I18n.exists(tk)) return Component.translatable(tk);
        return Component.literal(TextHelper.getReadableName(key));
    }

    private static ConfigReloadType reloadType(ModConfigSpec.RestartType rt) {
        return switch (rt) {
            case WORLD -> ConfigReloadType.WORLD_RELOAD;
            case GAME -> ConfigReloadType.GAME_RESTART;
            default -> ConfigReloadType.NONE;
        };
    }

    private static List<String> append(List<String> path, String key) {
        List<String> out = new ArrayList<>(path.size() + 1);
        out.addAll(path);
        out.add(key);
        return out;
    }

    private static Map<String, List<ModConfig>> configsByModField() {
        try {
            Field f = ConfigTracker.class.getDeclaredField("configsByMod");
            f.setAccessible(true);
            return (Map<String, List<ModConfig>>) f.get(ConfigTracker.INSTANCE);
        } catch (Exception e) {
            Moonlight.LOGGER.error("Could not access NeoForge config registry; foreign config conversion disabled", e);
            return Map.of();
        }
    }
}
