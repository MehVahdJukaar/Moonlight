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
import net.mehvahdjukaar.moonlight.api.util.math.Vec2i;
import net.mehvahdjukaar.moonlight.core.client.config.schema.SchemaEditScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.resources.Identifier;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;
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

//Client side registry turning a server safe ConfigOption into an editing ConfigControl.
public final class ConfigControllers {
//TODO:  expose api
    private static final Map<Class<?>, Factory<?>> FACTORIES = new HashMap<>();

    @FunctionalInterface
    private interface Factory<O extends ConfigOption<?>> {
        ConfigControl<?> create(O option, ConfigEditSession session, Runnable onChange, int width);
    }

    public static <O extends ConfigOption<?>> void register(Class<O> type, ConfigControl.Provider<O> provider) {
        FACTORIES.put(type, (Factory<O>) (o, s, onChange, width) -> {
            ConfigControl<?> control = provider.create(o, s, onChange);
            control.widget().setWidth(width);
            return control;
        });
    }

    private static <O extends ConfigOption<?>> void register(Class<O> type, Factory<O> factory) {
        FACTORIES.put(type, factory);
    }

    public static ConfigControl<?> create(ConfigOption<?> option, ConfigEditSession session, Runnable onChange) {
        return create(option, session, onChange, CONTROL_WIDTH);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static ConfigControl<?> create(ConfigOption<?> option, ConfigEditSession session, Runnable onChange, int width) {
        Factory factory = FACTORIES.get(option.getClass());
        if (factory == null) return disabled(width);
        return factory.create(option, session, onChange, width);
    }


    static {
        register(ConfigOption.BooleanOption.class, (o, s, onChange, w) -> {
            CycleButton<Boolean> b = CycleButton.onOffBuilder(s.valueOrPendingValue(o))
                    .displayOnlyValue()
                    .create(0, 0, w, CONTROL_HEIGHT, Component.empty(), (btn, val) -> {
                        s.put(o, val);
                        onChange.run();
                    });
            return new ConfigControl<>(b, b::setValue);
        });

        @SuppressWarnings("unchecked")
        Class<ConfigOption.EnumOption<?>> enumClass =
                (Class<ConfigOption.EnumOption<?>>) (Class<?>) ConfigOption.EnumOption.class;
        register(enumClass, ConfigControllers::enumControl);

        register(ConfigOption.StringOption.class, (o, s, onChange, w) ->
                textField(w, s.valueOrPendingValue(o), String::valueOf, str -> {
                    if (!o.isValid(str)) throw new IllegalArgumentException();
                    s.put(o, str);
                    onChange.run();
                }));

        register(ConfigOption.RegexOption.class, (o, s, onChange, w) -> {
            ConfigControl<Object> control = textField(w, s.valueOrPendingValue(o), String::valueOf, str -> {
                if (!o.isValid(str)) throw new IllegalArgumentException();
                s.put(o, str);
                onChange.run();
            });
            EditBox box = (EditBox) control.widget();
            box.addFormatter(RegexHighlighter.INSTANCE.formatter(box)); // live regex syntax coloring
            return control;
        });

        register(ConfigOption.ColorOption.class, (o, s, onChange, w) -> {
            ColorFieldWidget field = new ColorFieldWidget(w, CONTROL_HEIGHT, s.valueOrPendingValue(o), o.hasAlpha,
                    c -> {
                        s.put(o, c);
                        onChange.run();
                    },
                    currentColor -> Minecraft.getInstance().setScreen(
                            new ColorPickerScreen(currentColor, o.hasAlpha, Minecraft.getInstance().screen, picked -> {
                                s.put(o, picked);
                                onChange.run();
                            })));
            return new ConfigControl<>(field, field::setColor);
        });

        register(ConfigOption.IntOption.class, (o, s, onChange, w) ->
                numberField(w, s.valueOrPendingValue(o), o.min, o.max, true, v -> {
                    s.put(o, (int) Math.round(v));
                    onChange.run();
                }));
        register(ConfigOption.IntSliderOption.class, (o, s, onChange, w) ->
                slider(w, o.min, o.max, s.valueOrPendingValue(o), true, v -> s.put(o, (int) Math.round(v)), onChange));

        register(ConfigOption.DoubleOption.class, (o, s, onChange, w) ->
                numberField(w, s.valueOrPendingValue(o), o.min, o.max, false, v -> {
                    s.put(o, v);
                    onChange.run();
                }));
        register(ConfigOption.DoubleSliderOption.class, (o, s, onChange, w) ->
                slider(w, o.min, o.max, s.valueOrPendingValue(o), false, v -> s.put(o, v), onChange));

        register(ConfigOption.PercentOption.class, (o, s, onChange, w) ->
                slider(w, 0, 1, s.valueOrPendingValue(o), false, true, v -> s.put(o, v), onChange));

        register(ConfigOption.FloatOption.class, (o, s, onChange, w) ->
                numberField(w, s.valueOrPendingValue(o), o.min, o.max, false, v -> {
                    s.put(o, (float) v);
                    onChange.run();
                }));
        register(ConfigOption.FloatSliderOption.class, (o, s, onChange, w) ->
                slider(w, o.min, o.max, s.valueOrPendingValue(o), false, v -> s.put(o, v.floatValue()), onChange));

        register(ConfigOption.RangeOption.class, (o, s, onChange, w) -> {
            Range current = s.valueOrPendingValue(o);
            RangeControlWidget field = new RangeControlWidget(w, CONTROL_HEIGHT, current, o.min, o.max, r -> {
                s.put(o, r);
                onChange.run();
            });
            return new ConfigControl<>(field, field::setRange);
        });

        register(ConfigOption.Vec2Option.class, (o, s, onChange, w) -> {
            Vec2 c = s.valueOrPendingValue(o);
            VecControlWidget field = new VecControlWidget(w, CONTROL_HEIGHT, new double[]{c.x, c.y}, o.min, o.max, false,
                    v -> {
                        s.put(o, new Vec2((float) v[0], (float) v[1]));
                        onChange.run();
                    });
            return new ConfigControl<Vec2>(field, vv -> field.setValues(vv.x, vv.y));
        });

        register(ConfigOption.Vec2iOption.class, (o, s, onChange, w) -> {
            Vec2i c = s.valueOrPendingValue(o);
            VecControlWidget field = new VecControlWidget(w, CONTROL_HEIGHT, new double[]{c.x(), c.y()}, o.min, o.max, true,
                    v -> {
                        s.put(o, new Vec2i((int) Math.round(v[0]), (int) Math.round(v[1])));
                        onChange.run();
                    });
            return new ConfigControl<Vec2i>(field, vv -> field.setValues(vv.x(), vv.y()));
        });

        register(ConfigOption.Vec3Option.class, (o, s, onChange, w) -> {
            Vec3 c = s.valueOrPendingValue(o);
            VecControlWidget field = new VecControlWidget(w, CONTROL_HEIGHT, new double[]{c.x, c.y, c.z}, o.min, o.max, false,
                    v -> {
                        s.put(o, new Vec3(v[0], v[1], v[2]));
                        onChange.run();
                    });
            return new ConfigControl<Vec3>(field, vv -> field.setValues(vv.x, vv.y, vv.z));
        });

        register(ConfigOption.Vec3iOption.class, (o, s, onChange, w) -> {
            Vec3i c = s.valueOrPendingValue(o);
            VecControlWidget field = new VecControlWidget(w, CONTROL_HEIGHT, new double[]{c.getX(), c.getY(), c.getZ()}, o.min, o.max, true,
                    v -> {
                        s.put(o, new Vec3i((int) Math.round(v[0]), (int) Math.round(v[1]), (int) Math.round(v[2])));
                        onChange.run();
                    });
            return new ConfigControl<Vec3i>(field, vv -> field.setValues(vv.getX(), vv.getY(), vv.getZ()));
        });

        register(ConfigOption.DateOption.class, (o, s, onChange, w) -> {
            MonthDay c = s.valueOrPendingValue(o);
            NumberPickerFieldWidget field = new NumberPickerFieldWidget(w, CONTROL_HEIGHT, "/", c.getMonthValue(), c.getDayOfMonth(),
                    MoonlightIcons.CALENDAR, DatePickerPopup::new, (m, d) -> {
                        s.put(o, MonthDay.of(m, d));
                        onChange.run();
                    });
            return new ConfigControl<MonthDay>(field, v -> field.setValues(v.getMonthValue(), v.getDayOfMonth()));
        });

        register(ConfigOption.TimeOption.class, (o, s, onChange, w) -> {
            LocalTime c = s.valueOrPendingValue(o);
            NumberPickerFieldWidget field = new NumberPickerFieldWidget(w, CONTROL_HEIGHT, ":", c.getHour(), c.getMinute(),
                    MoonlightIcons.CLOCK, ClockPickerPopup::new, (h, m) -> {
                        s.put(o, LocalTime.of(h, m));
                        onChange.run();
                    });
            return new ConfigControl<LocalTime>(field, v -> field.setValues(v.getHour(), v.getMinute()));
        });

        register(ConfigOption.DropdownOption.class, (o, s, onChange, w) -> {
            DropdownWidget field = new DropdownWidget(w, CONTROL_HEIGHT, o.options.get(), o.icon, s.valueOrPendingValue(o), val -> {
                s.put(o, val);
                onChange.run();
            });
            return new ConfigControl<>(field, field::setValue);
        });

        Factory<ConfigOption.ListOption> listFactory = (o, s, onChange, w) -> {
            IconButton button = new IconButton(0, 0, w, CONTROL_HEIGHT, listLabel(s.valueOrPendingValue(o)), MoonlightIcons.EDIT, b ->
                    Minecraft.getInstance().setScreen(SchemaEditScreen.create(o, s, onChange)));
            return new ConfigControl<List<String>>(button, list -> button.setMessage(listLabel(list)));
        };
        register(ConfigOption.ListOption.class, listFactory);
        register(ConfigOption.ColorListOption.class, listFactory::create);

        register(ConfigOption.JsonOption.class, (o, s, onChange, w) -> {
            Button button = new IconButton(0, 0, w, CONTROL_HEIGHT,
                    Component.translatable("gui.moonlight.config.edit"), MoonlightIcons.EDIT, b ->
                    Minecraft.getInstance().setScreen(new JsonEditScreen(o.title(), o.description(), s.valueOrPendingValue(o), Minecraft.getInstance().screen, edited -> {
                        s.put(o, edited);
                        onChange.run();
                    })));
            return new ConfigControl<>(button, v -> {
            });
        });

        @SuppressWarnings("unchecked")
        Class<ConfigOption.SchemaOption<?>> schemaClass =
                (Class<ConfigOption.SchemaOption<?>>) (Class<?>) ConfigOption.SchemaOption.class;
        register(schemaClass, (o, s, onChange, w) -> {
            Button button = new IconButton(0, 0, w, CONTROL_HEIGHT,
                    Component.translatable("gui.moonlight.config.edit"), MoonlightIcons.EDIT, b ->
                    Minecraft.getInstance().setScreen(SchemaEditScreen.create(o, s, onChange)));
            return new ConfigControl<>(button, v -> {
            });
        });

        register(ConfigOption.UnsupportedOption.class, (o, s, onChange, w) -> disabled(w));
    }


    public static ConfigControl<Boolean> featureToggle(ConfigOption.BooleanOption o, ConfigEditSession s, Runnable onChange) {
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

    private static <E extends Enum<E>> ConfigControl<E> enumControl(ConfigOption.EnumOption<E> o, ConfigEditSession s, Runnable onChange, int width) {
        CycleButton<E> w = CycleButton.builder(x -> Component.literal(x.name()), s.valueOrPendingValue(o))
                .withValues(o.options)
                .displayOnlyValue()
                .create(0, 0, width, CONTROL_HEIGHT, Component.empty(), (btn, val) -> {
                    s.put(o, val);
                    onChange.run();
                });
        return new ConfigControl<E>(w, w::setValue);
    }

    private static ConfigControl<Number> slider(int width, double min, double max, double current, boolean integer,
                                                Consumer<Double> store, Runnable onChange) {
        return slider(width, min, max, current, integer, false, store, onChange);
    }

    private static ConfigControl<Number> slider(int width, double min, double max, double current, boolean integer, boolean percent,
                                                Consumer<Double> store, Runnable onChange) {
        RangedSlider slider = new RangedSlider(width, CONTROL_HEIGHT, min, max, current, integer, percent, v -> {
            store.accept(v);
            onChange.run();
        });
        return new ConfigControl<>(slider, n -> slider.setActualValue(n.doubleValue()));
    }

    private static ConfigControl<Number> numberField(int width, Number initial, double min, double max, boolean integer,
                                                     DoubleConsumer store) {
        NumberFieldWidget w = new NumberFieldWidget(width, CONTROL_HEIGHT, initial.doubleValue(),
                min, max, integer, store);
        return new ConfigControl<>(w, n -> w.setValue(n.doubleValue()));
    }

    private static ConfigControl<Object> textField(int width, String initial, Function<Object, String> display, TextCommit commit) {
        EditBox box = new PanningEditBox(Minecraft.getInstance().font, 0, 0, width, CONTROL_HEIGHT, Component.empty());
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

    private static ConfigControl<Object> disabled(int width) {
        Button button = Button.builder(Component.translatable("gui.moonlight.config.edit_manually"), b -> {
        }).bounds(0, 0, width, CONTROL_HEIGHT).build();
        button.active = false;
        return new ConfigControl<>(button, v -> {
        });
    }

    @FunctionalInterface
    interface TextCommit {
        void accept(String value) throws Exception;
    }
}
