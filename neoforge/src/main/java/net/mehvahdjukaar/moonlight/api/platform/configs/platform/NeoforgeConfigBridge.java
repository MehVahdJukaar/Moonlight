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
import net.neoforged.fml.ModContainer;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.ModList;
import net.neoforged.fml.config.ConfigTracker;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Predicate;
import java.util.function.Supplier;

//Adapter for neoforge configs
@SuppressWarnings({"unchecked", "rawtypes"})
public final class NeoforgeConfigBridge {

    private static final Map<ModConfig, ForeignConfigHolder> CACHE = new WeakHashMap<>();
    private static final Map<String, List<ModConfig>> CONFIGS_BY_MOD = configsByModField();
    private static final Map<String, Boolean> GENERIC_SCREEN_CACHE = new HashMap<>();
    private static final String CONFIGURED_PACKAGE = "com.mrcrayfish.configured.";

    @Nullable
    public static Screen createScreen(String modId, Screen parent, @Nullable Identifier background) {
        List<ModConfigHolder> holders = getHoldersFor(modId);
        if (holders.isEmpty()) return null;
        return MoonlightConfigSelectScreen.create(modId, holders, parent, background);
    }

    /**
     * The mod either registered no config screen at all, or registered one of the stock ones anybody gets for free:
     * NeoForge's ConfigurationScreen, or Configured's. Either way there is no hand made screen to override.
     */
    public static boolean hasOnlyGenericScreen(String modId) {
        return GENERIC_SCREEN_CACHE.computeIfAbsent(modId, NeoforgeConfigBridge::doesConfigLookLikeGenericOne);
    }

    private static boolean doesConfigLookLikeGenericOne(String modId) {
        ModContainer container = ModList.get().getModContainerById(modId).orElse(null);
        if (container == null) return false;
        IConfigScreenFactory factory = container.getCustomExtension(IConfigScreenFactory.class).orElse(null);
        if (factory == null) return true;
        try {
            Screen screen = factory.createScreen(container, null);
            return screen instanceof ConfigurationScreen || screen.getClass().getName().startsWith(CONFIGURED_PACKAGE);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean hasCustomConfigScreen(String modId) {
        for (ModConfig mc : CONFIGS_BY_MOD.getOrDefault(modId, List.of())) {
            if (ForgeConfigHolder.getFromForgeConfig(mc) != null) continue;
            if (!(mc.getSpec() instanceof ModConfigSpec spec)) continue;
            if (!spec.isLoaded() || !spec.isEmpty()) return true;
        }
        return false;
    }

    private static List<ModConfigHolder> getHoldersFor(String modId) {
        List<ModConfig> configs = CONFIGS_BY_MOD.getOrDefault(modId, List.of());
        List<ModConfigHolder> out = new ArrayList<>();
        for (ModConfig mc : configs) {
            if (ForgeConfigHolder.getFromForgeConfig(mc) != null) continue;
            if (!(mc.getSpec() instanceof ModConfigSpec spec)) continue;
            if (!spec.isLoaded()) {
                out.add(new ForeignConfigHolder(idFor(modId, mc), typeFor(mc), spec,
                        new ConfigCategory(Component.empty()), nameFor(modId, mc)));
                continue;
            }
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
        ConfigCategory root = new ConfigCategory(Component.empty());
        walkSpec(mc, spec, spec.getValues(), List.of(), root);
        return new ForeignConfigHolder(idFor(modId, mc), typeFor(mc), spec, root, nameFor(modId, mc));
    }

    private static ConfigType typeFor(ModConfig mc) {
        return switch (mc.getType()) {
            case CLIENT -> ConfigType.CLIENT;
            case SERVER -> ConfigType.COMMON_SYNCED; // world bound, so it gets the server icon
            default -> ConfigType.COMMON;
        };
    }

    private static Identifier idFor(String modId, ModConfig mc) {
        return Identifier.fromNamespaceAndPath(modId, modConfigTypeName(mc));
    }

    //same logic as ConfigurationScreen.translatableConfig
    private static Component nameFor(String modId, ModConfig mc) {
        String fileKey = mc.getFileName().replaceAll("[^a-zA-Z0-9]+", ".").replaceFirst("^\\.", "").replaceFirst("\\.$", "").toLowerCase(Locale.ROOT);
        String sectionTitleKey = modId + ".configuration.section." + fileKey + ".title";
        String key = I18n.exists(sectionTitleKey) ? sectionTitleKey : "neoforge.configuration.uitext.title." + modConfigTypeName(mc);
        return Component.translatable(key, PlatHelper.getModName(modId));
    }

    private static String modConfigTypeName(ModConfig mc) {
        return mc.getType().name().toLowerCase(Locale.ROOT);
    }

    private static void walkSpec(ModConfig mc, ModConfigSpec spec, UnmodifiableConfig config, List<String> path, ConfigCategory parent) {
        for (UnmodifiableConfig.Entry entry : config.entrySet()) {
            String key = entry.getKey();
            List<String> childPath = append(path, key);
            Object raw = entry.getRawValue();
            if (raw instanceof UnmodifiableConfig sub) {
                String translationKey = translationKey(mc, spec.getLevelTranslationKey(childPath), key);
                ConfigCategory cat = new ConfigCategory(title(translationKey, key));
                Component desc = description(translationKey, spec.getLevelComment(childPath));
                if (desc != null) {
                    cat.setDescription(desc);
                }
                walkSpec(mc, spec, sub, childPath, cat);
                if (!cat.isEmpty()){
                    parent.add(cat);
                }
            } else if (raw instanceof ModConfigSpec.ConfigValue<?> cv) {
                ConfigOption<?> option = leaf(mc, spec, cv);
                if (option != null) parent.add(option);
            }
        }
    }

    @Nullable
    private static ConfigOption<?> leaf(ModConfig mc, ModConfigSpec spec, ModConfigSpec.ConfigValue<?> cv) {
        List<String> path = cv.getPath();
        Object specEntry = spec.getSpec().get(path);
        if (!(specEntry instanceof ModConfigSpec.ValueSpec vs)) {
            return null;
        }

        String key = path.isEmpty() ? "" : path.getLast();
        String translationKey = translationKey(mc, vs.getTranslationKey(), key);
        Component title = title(translationKey, key);
        Component desc = description(translationKey, vs.getComment());
        ConfigReloadType reload = mc.getType() == ModConfig.Type.STARTUP ? ConfigReloadType.GAME_RESTART : reloadType(vs.restartType());
        ConfigMetadata meta = new ConfigMetadata(reload, false);

        vs.getDefault();
        Object sample = vs.getDefault();

        switch (sample) {
            case Boolean b -> {
                return new ConfigOption.BooleanValue(title, desc, wrap(cv, meta), b);
            }
            case Enum<?> e -> {
                Enum<?>[] options = Arrays.stream(e.getDeclaringClass().getEnumConstants()).filter(vs::test).toArray(Enum[]::new);
                return new ConfigOption.EnumValue(title, desc, wrap(cv, meta), e, options);
            }
            case Integer i -> {
                int[] r = intRange(vs);
                return new ConfigOption.IntValue(title, desc, wrap(cv, meta), i, r[0], r[1]);
            }
            case Long l -> {
                // no long control: present it as an int when the range fits, else leave it uneditable
                long[] r = longRange(vs);
                if (r[0] >= Integer.MIN_VALUE && r[1] <= Integer.MAX_VALUE) {
                    return new ConfigOption.IntValue(title, desc, longAsInt(cv, meta), l.intValue(), (int) r[0], (int) r[1]);
                }
                return new ConfigOption.UnsupportedValue(title, desc, (Supplier<Object>) cv);
            }
            case Double d -> {
                double[] r = doubleRange(vs);
                return new ConfigOption.DoubleValue(title, desc, wrap(cv, meta), d, r[0], r[1]);
            }
            case String s -> {
                return new ConfigOption.StringValue(title, desc, wrap(cv, meta), s, vs::test);
            }
            case List<?> list when list.stream().allMatch(o -> o instanceof String) -> {
                List<String> def = list.stream().map(o -> (String) o).toList();
                Predicate<String> entryValidator = vs instanceof ModConfigSpec.ListValueSpec lvs ? lvs::testElement : null;
                return new ConfigOption.ListValue(title, desc, wrap(cv, meta), def, entryValidator);
            }
            default -> {
            }
        }
        return new ConfigOption.UnsupportedValue(title, desc, (Supplier<Object>) cv);
    }

    private static IConfigValue wrap(ModConfigSpec.ConfigValue<?> cv, ConfigMetadata meta) {
        return ForgeConfigValue.simple((ModConfigSpec.ConfigValue) cv, meta);
    }

    private static IConfigValue<Integer> longAsInt(ModConfigSpec.ConfigValue<?> cvRaw, ConfigMetadata meta) {
        ModConfigSpec.ConfigValue<Long> cv = (ModConfigSpec.ConfigValue<Long>) cvRaw;
        return new IConfigValue<>() {
            @Override
            public Integer get() {
                return cv.get().intValue();
            }

            @Override
            public boolean setValue(Integer value) {
                boolean changed = cv.get() != value.longValue();
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

    // neo's fallback when the builder never called translation()
    private static String translationKey(ModConfig mc, @Nullable String specKey, String key) {
        return specKey != null ? specKey : mc.getModId() + ".configuration." + key;
    }

    private static Component title(String translationKey, String key) {
        return Component.translatableWithFallback(translationKey, TextHelper.getReadableName(key));
    }

    //same as neo config screen
    @Nullable
    private static Component description(String translationKey, @Nullable String comment) {
        String tooltipKey = translationKey + ".tooltip";
        boolean hasComment = comment != null && !comment.isBlank();
        if (!hasComment && !I18n.exists(tooltipKey)) return null;
        return Component.translatableWithFallback(tooltipKey, comment);
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

    //hacks cuz cant mixin into fml.
    @SuppressWarnings("unchecked")
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
