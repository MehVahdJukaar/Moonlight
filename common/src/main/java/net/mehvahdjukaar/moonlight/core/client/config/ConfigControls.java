package net.mehvahdjukaar.moonlight.core.client.config;

import net.mehvahdjukaar.moonlight.api.client.gui.ConfigVisuals;
import net.mehvahdjukaar.moonlight.api.client.gui.ConfigEditSession;
import net.mehvahdjukaar.moonlight.api.client.gui.misc.RegexHighlighter;
import net.mehvahdjukaar.moonlight.api.client.gui.screen.ColorPickerScreen;
import net.mehvahdjukaar.moonlight.api.client.gui.screen.JsonEditScreen;
import net.mehvahdjukaar.moonlight.api.client.gui.widget.*;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigOption;
import net.mehvahdjukaar.moonlight.api.util.math.Range;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static net.mehvahdjukaar.moonlight.core.client.config.ConfigScreenLayout.*;
import static net.mehvahdjukaar.moonlight.api.client.gui.misc.ConfigGuiColors.*;

/**
 * Client side registry that turns a server safe {@link ConfigOption} into an editing {@link ConfigVisuals}. This
 * is the one place that knows about widgets: the screen just asks {@link #create} and never branches on value
 * type itself, so adding a new control means registering one provider here (or, for add-ons,
 * {@link #register} from their own client init) rather than touching the screen.
 */
public final class ConfigControls {

    private static final Map<Class<?>, ConfigVisuals. Provider<?>> PROVIDERS = new HashMap<>();

    public static <O extends ConfigOption<?>> void register(Class<O> type, ConfigVisuals.Provider<O> provider) {
        PROVIDERS.put(type, provider);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static ConfigVisuals<?> create(ConfigOption<?> option, ConfigEditSession session, Runnable onChange) {
        ConfigVisuals. Provider provider = PROVIDERS.get(option.getClass());
        if (provider == null) return disabled();
        return provider.create(option, session, onChange);
    }


    // ===== built-in providers =====
    static {
        // normal booleans use a plain ON/OFF text button; the yes/no (✓/✗) sprite toggle is reserved for category
        // feature() switches (see CategoryRow and ConfigControls#featureToggle)
        register(ConfigOption.BooleanValue.class, (o, s, onChange) -> {
            CycleButton<Boolean> w = CycleButton.onOffBuilder(s.current(o))
                    .displayOnlyValue()
                    .create(0, 0, CONTROL_WIDTH, CONTROL_HEIGHT, Component.empty(), (btn, val) -> {
                        s.put(o, val);
                        onChange.run();
                    });
            return new ConfigVisuals<Boolean>(w, w::setValue);
        });

        @SuppressWarnings("unchecked")
        Class<ConfigOption.EnumValue<?>> enumClass =
                (Class<ConfigOption.EnumValue<?>>) (Class<?>) ConfigOption.EnumValue.class;
        register(enumClass, (o, s, onChange) -> enumControl(o, s, onChange));

        register(ConfigOption.StringValue.class, (o, s, onChange) ->
                textField(s.current(o), String::valueOf, str -> {
                    if (!o.isValid(str)) throw new IllegalArgumentException();
                    s.put(o, str);
                    onChange.run();
                }));

        register(ConfigOption.RegexValue.class, (o, s, onChange) -> {
            ConfigVisuals control = textField(s.current(o), String::valueOf, str -> {
                if (!o.isValid(str)) throw new IllegalArgumentException();
                s.put(o, str);
                onChange.run();
            });
            EditBox box = (EditBox) control.widget();
            box.setFormatter(RegexHighlighter.INSTANCE.formatter(box)); // live regex syntax coloring
            return control;
        });

        register(ConfigOption.ColorValue.class, (o, s, onChange) -> {
            ColorFieldWidget w = new ColorFieldWidget(CONTROL_WIDTH, CONTROL_HEIGHT, s.current(o),
                    c -> {
                        s.put(o, c);
                        onChange.run();
                    },
                    // clicking the swatch opens the color picker page; on Done it writes the picked color back
                    currentColor -> Minecraft.getInstance().setScreen(
                            new ColorPickerScreen(currentColor, Minecraft.getInstance().screen, picked -> {
                                s.put(o, picked);
                                onChange.run();
                            })));
            return new ConfigVisuals<Integer>(w, w::setColor);
        });

        // plain numbers -> validated text field; slider subtypes -> slider. The value's own class is the
        // "draw me as X" signal, so there is no style flag to branch on.
        register(ConfigOption.IntValue.class, (o, s, onChange) ->
                textField(String.valueOf(s.current(o)), String::valueOf, str -> {
                    int parsed = Integer.parseInt(str.trim());
                    if (parsed < o.min || parsed > o.max) throw new NumberFormatException();
                    s.put(o, parsed);
                    onChange.run();
                }));
        register(ConfigOption.IntSliderValue.class, (o, s, onChange) ->
                slider(o.min, o.max, s.current(o), true, v -> s.put(o, (int) Math.round(v)), onChange));

        register(ConfigOption.DoubleValue.class, (o, s, onChange) ->
                textField(String.valueOf(s.current(o)), String::valueOf, str -> {
                    double parsed = Double.parseDouble(str.trim());
                    if (parsed < o.min || parsed > o.max) throw new NumberFormatException();
                    s.put(o, parsed);
                    onChange.run();
                }));
        register(ConfigOption.DoubleSliderValue.class, (o, s, onChange) ->
                slider(o.min, o.max, s.current(o), false, v -> s.put(o, v), onChange));

        register(ConfigOption.PercentValue.class, (o, s, onChange) ->
                slider(0, 1, s.current(o), false, true, v -> s.put(o, v), onChange));

        register(ConfigOption.FloatValue.class, (o, s, onChange) ->
                textField(String.valueOf(s.current(o)), String::valueOf, str -> {
                    float parsed = Float.parseFloat(str.trim());
                    if (parsed < o.min || parsed > o.max) throw new NumberFormatException();
                    s.put(o, parsed);
                    onChange.run();
                }));
        register(ConfigOption.FloatSliderValue.class, (o, s, onChange) ->
                slider(o.min, o.max, s.current(o), false, v -> s.put(o, v.floatValue()), onChange));

        register(ConfigOption.RangeValue.class, (o, s, onChange) -> {
            Range current = s.current(o);
            RangeControlWidget w = new RangeControlWidget(CONTROL_WIDTH, CONTROL_HEIGHT, current, o.min, o.max, r -> {
                s.put(o, r);
                onChange.run();
            });
            return new ConfigVisuals<Range>(w, w::setRange);
        });

        register(ConfigOption.Vec3Value.class, (o, s, onChange) -> {
            Vec3 c = s.current(o);
            Vec3ControlWidget w = new Vec3ControlWidget(CONTROL_WIDTH, CONTROL_HEIGHT, c.x, c.y, c.z, o.min, o.max, false,
                    (x, y, z) -> {
                        s.put(o, new Vec3(x, y, z));
                        onChange.run();
                    });
            return new ConfigVisuals<Vec3>(w, vv -> w.setValues(vv.x, vv.y, vv.z));
        });

        register(ConfigOption.Vec3iValue.class, (o, s, onChange) -> {
            Vec3i c = s.current(o);
            Vec3ControlWidget w = new Vec3ControlWidget(CONTROL_WIDTH, CONTROL_HEIGHT, c.getX(), c.getY(), c.getZ(), o.min, o.max, true,
                    (x, y, z) -> {
                        s.put(o, new Vec3i((int) Math.round(x), (int) Math.round(y), (int) Math.round(z)));
                        onChange.run();
                    });
            return new ConfigVisuals<Vec3i>(w, vv -> w.setValues(vv.getX(), vv.getY(), vv.getZ()));
        });

        register(ConfigOption.DropdownValue.class, (o, s, onChange) -> {
            DropdownWidget w = new DropdownWidget(CONTROL_WIDTH, CONTROL_HEIGHT, o.options.get(), o.icon, s.current(o), val -> {
                s.put(o, val);
                onChange.run();
            });
            return new ConfigVisuals<String>(w, w::setValue);
        });

        register(ConfigOption.ListValue.class, (o, s, onChange) -> {
            IconButton button = new IconButton(0, 0, CONTROL_WIDTH, CONTROL_HEIGHT, listLabel(s.current(o)), EDIT_ICON, 12, 12, b ->
                    Minecraft.getInstance().setScreen(new ListEditScreen(o, s.current(o), Minecraft.getInstance().screen, edited -> {
                        s.put(o, edited);
                        onChange.run();
                    })));
            return new ConfigVisuals<List<String>>(button, list -> button.setMessage(listLabel(list)));
        });

        register(ConfigOption.JsonValue.class, (o, s, onChange) -> {
            Button button = new IconButton(0, 0, CONTROL_WIDTH, CONTROL_HEIGHT,
                    Component.translatable("gui.moonlight.config.edit"), EDIT_ICON, 12, 12, b ->
                    Minecraft.getInstance().setScreen(new JsonEditScreen(o.title(), o.description(), s.current(o), Minecraft.getInstance().screen, edited -> {
                        s.put(o, edited);
                        onChange.run();
                    })));
            return new ConfigVisuals<Object>(button, v -> {
            });
        });

        // codec objects that declare a CodecUI schema get a real, schema-generated form instead of the placeholder
        @SuppressWarnings("unchecked")
        Class<ConfigOption.SchemaValue<?>> schemaClass =
                (Class<ConfigOption.SchemaValue<?>>) (Class<?>) ConfigOption.SchemaValue.class;
        register(schemaClass, (o, s, onChange) -> {
            Button button = new IconButton(0, 0, CONTROL_WIDTH, CONTROL_HEIGHT,
                    Component.translatable("gui.moonlight.config.edit"), EDIT_ICON, 12, 12, b ->
                    Minecraft.getInstance().setScreen(SchemaEditScreen.create(o, s, onChange)));
            return new ConfigVisuals<Object>(button, v -> {
            });
        });

        register(ConfigOption.UnsupportedValue.class, (o, s, onChange) -> disabled());
    }


    /**
     * The control for a category's {@code feature()} gate when shown as a row inside its own category: a full-width
     * button styled like the plain boolean control but drawing the yes/no (✓/✗) sprites instead of ON/OFF text,
     * matching the small inline toggle the parent screen shows next to the category button.
     */
    static ConfigVisuals<Boolean> featureToggle(ConfigOption.BooleanValue o, ConfigEditSession s, Runnable onChange) {
        ResourceLocation icon = o.icon();
        // draw the feature's decorative item just left of the ✓/✗ symbol, when it resolves to something
        BooleanToggleWidget.IconRenderer iconRenderer = icon == null ? null : new BooleanToggleWidget.IconRenderer() {
            private final ConfigScreenIcons.Anim anim = new ConfigScreenIcons.Anim();

            @Override
            public boolean available() {
                return ConfigScreenIcons.has(icon);
            }

            @Override
            public void render(GuiGraphics graphics, int x, int y, int size, boolean hovered, boolean lit) {
                anim.update(hovered && lit);
                ConfigScreenIcons.renderAnimated(graphics, icon, x, y, anim.phase(), lit);
            }
        };
        BooleanToggleWidget w = new BooleanToggleWidget(CONTROL_WIDTH, CONTROL_HEIGHT, ON_ICON, OFF_ICON,
                Boolean.TRUE.equals(s.current(o)), val -> {
            s.put(o, val);
            onChange.run();
        }, iconRenderer);
        return new ConfigVisuals<Boolean>(w, w::set);
    }
    // ===== widget builders =====

    private static <E extends Enum<E>> ConfigVisuals<E> enumControl(ConfigOption.EnumValue<E> o, ConfigEditSession s, Runnable onChange) {
        CycleButton<E> w = CycleButton.<E>builder(x -> Component.literal(x.name()))
                .withValues(o.options)
                .withInitialValue(s.current(o))
                .displayOnlyValue()
                .create(0, 0, CONTROL_WIDTH, CONTROL_HEIGHT, Component.empty(), (btn, val) -> {
                    s.put(o, val);
                    onChange.run();
                });
        return new ConfigVisuals<E>(w, w::setValue);
    }

    private static ConfigVisuals<Number> slider(double min, double max, double current, boolean integer,
                                                java.util.function.Consumer<Double> store, Runnable onChange) {
        return slider(min, max, current, integer, false, store, onChange);
    }

    private static ConfigVisuals<Number> slider(double min, double max, double current, boolean integer, boolean percent,
                                                java.util.function.Consumer<Double> store, Runnable onChange) {
        RangedSlider slider = new RangedSlider(CONTROL_WIDTH, CONTROL_HEIGHT, min, max, current, integer, percent, v -> {
            store.accept(v);
            onChange.run();
        });
        return new ConfigVisuals<Number>(slider, n -> slider.setActualValue(n.doubleValue()));
    }

    private static ConfigVisuals<Object> textField(String initial, Function<Object, String> display, TextCommit commit) {
        EditBox box = new PanningEditBox(Minecraft.getInstance().font, 0, 0, CONTROL_WIDTH, CONTROL_HEIGHT, Component.empty());
        box.setMaxLength(Short.MAX_VALUE);
        box.setValue(initial);
        box.setResponder(str -> {
            try {
                commit.accept(str);
                box.setTextColor(TEXT);
            } catch (Exception ex) {
                box.setTextColor(ERROR);
            }
        });
        return new ConfigVisuals<Object>(box, v -> box.setValue(display.apply(v)));
    }

    private static Component listLabel(java.util.List<String> list) {
        return Component.translatable("gui.moonlight.config.list_entries", list.size());
    }

    private static ConfigVisuals<Object> disabled() {
        Button button = Button.builder(Component.translatable("gui.moonlight.config.edit_manually"), b -> {
        }).bounds(0, 0, CONTROL_WIDTH, CONTROL_HEIGHT).build();
        button.active = false;
        return new ConfigVisuals<Object>(button, v -> {
        });
    }

    @FunctionalInterface
    interface TextCommit {
        void accept(String value) throws Exception;
    }
}
