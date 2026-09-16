package net.mehvahdjukaar.moonlight.core.client.config;

import net.mehvahdjukaar.moonlight.api.client.gui.ConfigControl;
import net.mehvahdjukaar.moonlight.api.client.gui.ConfigEditSession;
import net.mehvahdjukaar.moonlight.api.client.gui.misc.RegexHighlighter;
import net.mehvahdjukaar.moonlight.api.client.gui.screen.ColorPickerScreen;
import net.mehvahdjukaar.moonlight.api.client.gui.screen.JsonEditScreen;
import net.mehvahdjukaar.moonlight.api.client.gui.widget.*;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigOption;
import net.mehvahdjukaar.moonlight.api.util.math.Range;
import net.mehvahdjukaar.moonlight.api.client.gui.MoonlightIcons;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.resources.Identifier;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import java.time.LocalTime;
import java.time.MonthDay;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Function;

import static net.mehvahdjukaar.moonlight.core.client.config.ConfigScreenLayout.*;
import static net.mehvahdjukaar.moonlight.api.client.gui.misc.ConfigGuiColors.*;

// Client side registry turning a server safe ConfigOption into an editing ConfigControl. The screen just calls
// create() and never branches on value type, so a new control means registering a provider here instead
public final class ConfigControllers {
//TODO:  expose api
    private static final Map<Class<?>, ConfigControl.Provider<?>> PROVIDERS = new HashMap<>();

    public static <O extends ConfigOption<?>> void register(Class<O> type, ConfigControl.Provider<O> provider) {
        PROVIDERS.put(type, provider);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static ConfigControl<?> create(ConfigOption<?> option, ConfigEditSession session, Runnable onChange) {
        ConfigControl.Provider provider = PROVIDERS.get(option.getClass());
        if (provider == null) return disabled();
        return provider.create(option, session, onChange);
    }


    static {
        // the check/cross sprite toggle is reserved for feature() switches, plain booleans get an ON/OFF text button
        register(ConfigOption.BooleanValue.class, (o, s, onChange) -> {
            CycleButton<Boolean> w = CycleButton.onOffBuilder(s.valueOrPendingValue(o))
                    .displayOnlyValue()
                    .create(0, 0, CONTROL_WIDTH, CONTROL_HEIGHT, Component.empty(), (btn, val) -> {
                        s.put(o, val);
                        onChange.run();
                    });
            return new ConfigControl<>(w, w::setValue);
        });

        @SuppressWarnings("unchecked")
        Class<ConfigOption.EnumValue<?>> enumClass =
                (Class<ConfigOption.EnumValue<?>>) (Class<?>) ConfigOption.EnumValue.class;
        register(enumClass, ConfigControllers::enumControl);

        register(ConfigOption.StringValue.class, (o, s, onChange) ->
                textField(s.valueOrPendingValue(o), String::valueOf, str -> {
                    if (!o.isValid(str)) throw new IllegalArgumentException();
                    s.put(o, str);
                    onChange.run();
                }));

        register(ConfigOption.RegexValue.class, (o, s, onChange) -> {
            ConfigControl<Object> control = textField(s.valueOrPendingValue(o), String::valueOf, str -> {
                if (!o.isValid(str)) throw new IllegalArgumentException();
                s.put(o, str);
                onChange.run();
            });
            EditBox box = (EditBox) control.widget();
            box.addFormatter(RegexHighlighter.INSTANCE.formatter(box)); // live regex syntax coloring
            return control;
        });

        register(ConfigOption.ColorValue.class, (o, s, onChange) -> {
            ColorFieldWidget w = new ColorFieldWidget(CONTROL_WIDTH, CONTROL_HEIGHT, s.valueOrPendingValue(o), o.hasAlpha,
                    c -> {
                        s.put(o, c);
                        onChange.run();
                    },
                    currentColor -> Minecraft.getInstance().setScreen(
                            new ColorPickerScreen(currentColor, o.hasAlpha, Minecraft.getInstance().screen, picked -> {
                                s.put(o, picked);
                                onChange.run();
                            })));
            return new ConfigControl<>(w, w::setColor);
        });

        // plain numbers get a stepper field, slider subtypes get a slider
        register(ConfigOption.IntValue.class, (o, s, onChange) ->
                numberField(s.valueOrPendingValue(o), o.min, o.max, true, v -> {
                    s.put(o, (int) Math.round(v));
                    onChange.run();
                }));
        register(ConfigOption.IntSliderValue.class, (o, s, onChange) ->
                slider(o.min, o.max, s.valueOrPendingValue(o), true, v -> s.put(o, (int) Math.round(v)), onChange));

        register(ConfigOption.DoubleValue.class, (o, s, onChange) ->
                numberField(s.valueOrPendingValue(o), o.min, o.max, false, v -> {
                    s.put(o, v);
                    onChange.run();
                }));
        register(ConfigOption.DoubleSliderValue.class, (o, s, onChange) ->
                slider(o.min, o.max, s.valueOrPendingValue(o), false, v -> s.put(o, v), onChange));

        register(ConfigOption.PercentValue.class, (o, s, onChange) ->
                slider(0, 1, s.valueOrPendingValue(o), false, true, v -> s.put(o, v), onChange));

        register(ConfigOption.FloatValue.class, (o, s, onChange) ->
                numberField(s.valueOrPendingValue(o), o.min, o.max, false, v -> {
                    s.put(o, (float) v);
                    onChange.run();
                }));
        register(ConfigOption.FloatSliderValue.class, (o, s, onChange) ->
                slider(o.min, o.max, s.valueOrPendingValue(o), false, v -> s.put(o, v.floatValue()), onChange));

        register(ConfigOption.RangeValue.class, (o, s, onChange) -> {
            Range current = s.valueOrPendingValue(o);
            RangeControlWidget w = new RangeControlWidget(CONTROL_WIDTH, CONTROL_HEIGHT, current, o.min, o.max, r -> {
                s.put(o, r);
                onChange.run();
            });
            return new ConfigControl<>(w, w::setRange);
        });

        register(ConfigOption.Vec3Value.class, (o, s, onChange) -> {
            Vec3 c = s.valueOrPendingValue(o);
            Vec3ControlWidget w = new Vec3ControlWidget(CONTROL_WIDTH, CONTROL_HEIGHT, c.x, c.y, c.z, o.min, o.max, false,
                    (x, y, z) -> {
                        s.put(o, new Vec3(x, y, z));
                        onChange.run();
                    });
            return new ConfigControl<Vec3>(w, vv -> w.setValues(vv.x, vv.y, vv.z));
        });

        register(ConfigOption.Vec3iValue.class, (o, s, onChange) -> {
            Vec3i c = s.valueOrPendingValue(o);
            Vec3ControlWidget w = new Vec3ControlWidget(CONTROL_WIDTH, CONTROL_HEIGHT, c.getX(), c.getY(), c.getZ(), o.min, o.max, true,
                    (x, y, z) -> {
                        s.put(o, new Vec3i((int) Math.round(x), (int) Math.round(y), (int) Math.round(z)));
                        onChange.run();
                    });
            return new ConfigControl<Vec3i>(w, vv -> w.setValues(vv.getX(), vv.getY(), vv.getZ()));
        });

        register(ConfigOption.DateValue.class, (o, s, onChange) -> {
            MonthDay c = s.valueOrPendingValue(o);
            NumberPickerFieldWidget w = new NumberPickerFieldWidget(CONTROL_WIDTH, CONTROL_HEIGHT, "/", c.getMonthValue(), c.getDayOfMonth(),
                    MoonlightIcons.CALENDAR, DatePickerPopup::new, (m, d) -> {
                        s.put(o, MonthDay.of(m, d));
                        onChange.run();
                    });
            return new ConfigControl<MonthDay>(w, v -> w.setValues(v.getMonthValue(), v.getDayOfMonth()));
        });

        register(ConfigOption.TimeValue.class, (o, s, onChange) -> {
            LocalTime c = s.valueOrPendingValue(o);
            NumberPickerFieldWidget w = new NumberPickerFieldWidget(CONTROL_WIDTH, CONTROL_HEIGHT, ":", c.getHour(), c.getMinute(),
                    MoonlightIcons.CLOCK, ClockPickerPopup::new, (h, m) -> {
                        s.put(o, LocalTime.of(h, m));
                        onChange.run();
                    });
            return new ConfigControl<LocalTime>(w, v -> w.setValues(v.getHour(), v.getMinute()));
        });

        register(ConfigOption.DropdownValue.class, (o, s, onChange) -> {
            DropdownWidget w = new DropdownWidget(CONTROL_WIDTH, CONTROL_HEIGHT, o.options.get(), o.icon, s.valueOrPendingValue(o), val -> {
                s.put(o, val);
                onChange.run();
            });
            return new ConfigControl<>(w, w::setValue);
        });

        register(ConfigOption.ListValue.class, (o, s, onChange) -> {
            IconButton button = new IconButton(0, 0, CONTROL_WIDTH, CONTROL_HEIGHT, listLabel(s.valueOrPendingValue(o)), MoonlightIcons.EDIT, b ->
                    Minecraft.getInstance().setScreen(new ListEditScreen(o, s.valueOrPendingValue(o), Minecraft.getInstance().screen, edited -> {
                        s.put(o, edited);
                        onChange.run();
                    })));
            return new ConfigControl<List<String>>(button, list -> button.setMessage(listLabel(list)));
        });

        register(ConfigOption.JsonValue.class, (o, s, onChange) -> {
            Button button = new IconButton(0, 0, CONTROL_WIDTH, CONTROL_HEIGHT,
                    Component.translatable("gui.moonlight.config.edit"), MoonlightIcons.EDIT, b ->
                    Minecraft.getInstance().setScreen(new JsonEditScreen(o.title(), o.description(), s.valueOrPendingValue(o), Minecraft.getInstance().screen, edited -> {
                        s.put(o, edited);
                        onChange.run();
                    })));
            return new ConfigControl<>(button, v -> {
            });
        });

        @SuppressWarnings("unchecked")
        Class<ConfigOption.SchemaValue<?>> schemaClass =
                (Class<ConfigOption.SchemaValue<?>>) (Class<?>) ConfigOption.SchemaValue.class;
        register(schemaClass, (o, s, onChange) -> {
            Button button = new IconButton(0, 0, CONTROL_WIDTH, CONTROL_HEIGHT,
                    Component.translatable("gui.moonlight.config.edit"), MoonlightIcons.EDIT, b ->
                    Minecraft.getInstance().setScreen(SchemaEditScreen.create(o, s, onChange)));
            return new ConfigControl<>(button, v -> {
            });
        });

        register(ConfigOption.UnsupportedValue.class, (o, s, onChange) -> disabled());
    }


    static ConfigControl<Boolean> featureToggle(ConfigOption.BooleanValue o, ConfigEditSession s, Runnable onChange) {
        Identifier icon = o.icon();
        BooleanToggleWidget.ExtraIcon iconRenderer = icon == null ? null : new BooleanToggleWidget.ExtraIcon() {
            private final ConfigScreenIcons.Anim anim = new ConfigScreenIcons.Anim();

            @Override
            public boolean available() {
                return ConfigScreenIcons.has(icon);
            }

            @Override
            public void render(GuiGraphicsExtractor graphics, int x, int y, int size, boolean hovered, boolean lit) {
                anim.update(hovered && lit);
                ConfigScreenIcons.renderAnimated(graphics, icon, x, y, anim.phase(), lit);
            }
        };
        BooleanToggleWidget w = new BooleanToggleWidget(CONTROL_WIDTH, CONTROL_HEIGHT, MoonlightIcons.YES, MoonlightIcons.NO,
                Boolean.TRUE.equals(s.valueOrPendingValue(o)), val -> {
            s.put(o, val);
            onChange.run();
        }, iconRenderer);
        return new ConfigControl<>(w, w::set);
    }

    private static <E extends Enum<E>> ConfigControl<E> enumControl(ConfigOption.EnumValue<E> o, ConfigEditSession s, Runnable onChange) {
        CycleButton<E> w = CycleButton.builder(x -> Component.literal(x.name()), s.valueOrPendingValue(o))
                .withValues(o.options)
                .displayOnlyValue()
                .create(0, 0, CONTROL_WIDTH, CONTROL_HEIGHT, Component.empty(), (btn, val) -> {
                    s.put(o, val);
                    onChange.run();
                });
        return new ConfigControl<E>(w, w::setValue);
    }

    private static ConfigControl<Number> slider(double min, double max, double current, boolean integer,
                                                Consumer<Double> store, Runnable onChange) {
        return slider(min, max, current, integer, false, store, onChange);
    }

    private static ConfigControl<Number> slider(double min, double max, double current, boolean integer, boolean percent,
                                                Consumer<Double> store, Runnable onChange) {
        RangedSlider slider = new RangedSlider(CONTROL_WIDTH, CONTROL_HEIGHT, min, max, current, integer, percent, v -> {
            store.accept(v);
            onChange.run();
        });
        return new ConfigControl<>(slider, n -> slider.setActualValue(n.doubleValue()));
    }

    private static ConfigControl<Number> numberField(Number initial, double min, double max, boolean integer,
                                                     DoubleConsumer store) {
        NumberFieldWidget w = new NumberFieldWidget(CONTROL_WIDTH, CONTROL_HEIGHT, initial.doubleValue(),
                min, max, integer, store);
        return new ConfigControl<>(w, n -> w.setValue(n.doubleValue()));
    }

    private static ConfigControl<Object> textField(String initial, Function<Object, String> display, TextCommit commit) {
        EditBox box = new PanningEditBox(Minecraft.getInstance().font, 0, 0, CONTROL_WIDTH, CONTROL_HEIGHT, Component.empty());
        box.setMaxLength(Short.MAX_VALUE);
        box.setValue(initial);
        box.setResponder(str -> {
            try {
                commit.accept(str);
                box.setTextColor(FIELD_TEXT);
            } catch (Exception ex) {
                box.setTextColor(ERROR);
            }
        });
        return new ConfigControl<>(box, v -> box.setValue(display.apply(v)));
    }

    private static Component listLabel(List<String> list) {
        return Component.translatable("gui.moonlight.config.list_entries", list.size());
    }

    private static ConfigControl<Object> disabled() {
        Button button = Button.builder(Component.translatable("gui.moonlight.config.edit_manually"), b -> {
        }).bounds(0, 0, CONTROL_WIDTH, CONTROL_HEIGHT).build();
        button.active = false;
        return new ConfigControl<>(button, v -> {
        });
    }

    @FunctionalInterface
    interface TextCommit {
        void accept(String value) throws Exception;
    }
}
