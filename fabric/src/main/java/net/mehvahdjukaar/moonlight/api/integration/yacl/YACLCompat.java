package net.mehvahdjukaar.moonlight.api.integration.yacl;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import dev.isxander.yacl3.gui.controllers.LabelController;
import net.mehvahdjukaar.moonlight.api.platform.configs.fabric.ConfigEntry;
import net.mehvahdjukaar.moonlight.api.platform.configs.fabric.ConfigSubCategory;
import net.mehvahdjukaar.moonlight.api.platform.configs.fabric.FabricConfigHolder;
import net.mehvahdjukaar.moonlight.api.platform.configs.fabric.values.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class YACLCompat {

    public static Screen makeScreen(Screen parent, FabricConfigHolder spec) {
        return makeScreen(parent, spec, null);
    }

    public static Screen makeScreen(Screen parent, FabricConfigHolder spec, @Nullable ResourceLocation background) {

        spec.forceLoad();

        YetAnotherConfigLib.Builder builder = YetAnotherConfigLib.createBuilder();

        builder.title(spec.getReadableName());
        builder.save(spec::saveConfig);


        for (var en : spec.getMainEntry().getEntries()) {
            //skips stray config values
            if (!(en instanceof ConfigSubCategory c)) continue;
            var mainCat = ConfigCategory.createBuilder()
                    .name(Component.translatable(c.getName()));


            for (var entry : c.getEntries()) {
                if (entry instanceof ConfigSubCategory subCat) {
                    var subBuilder = OptionGroup.createBuilder()
                            .name(Component.translatable(subCat.getName()))
                            .collapsed(true);

                    addEntriesRecursive(mainCat, subBuilder, subCat);

                    mainCat.group(subBuilder.build());
                } else {
                    mainCat.option(buildEntry(entry));
                }
            }

            builder.category(mainCat.build());

        }
        return builder.build().generateScreen(parent);
    }

    private static void addEntriesRecursive(ConfigCategory.Builder builder, OptionGroup.Builder subCategoryBuilder, ConfigSubCategory c) {

        for (var entry : c.getEntries()) {
            if (entry instanceof ConfigSubCategory cc) {
                //not nested subcat not supported. merging
                var scb = OptionGroup.createBuilder()
                        .name(Component.translatable(entry.getName()))
                        .description(OptionDescription.of(Component.literal("Unsupported")));
                // optional
                addEntriesRecursive(builder, subCategoryBuilder, cc);
                //subCategoryBuilder.group(scb.build());
            } else subCategoryBuilder.option(buildEntry(entry));
        }
    }

    private static Option<?> buildEntry(ConfigEntry entry) {

        if (entry instanceof ColorConfigValue col) {
            var e = Option.<Color>createBuilder()
                    .name(col.getTranslation())
                    .binding(new Color(col.getDefaultValue()), () -> new Color(col.get()), v -> col.set(v.getRGB()))
                    .controller(ColorControllerBuilder::create);
            var description = col.getDescription();
            if (description != null)
                e.description(OptionDescription.of(description));// Shown when the user hover over this option
            return e.build(); // Builds the option entry for cloth config
        } else if (entry instanceof IntConfigValue ic) {
            var e = Option.<Integer>createBuilder()
                    .name(ic.getTranslation())
                    .binding(ic.getDefaultValue(), ic, ic::set)
                    .controller(o -> IntegerSliderControllerBuilder.create(o)
                            .range(ic.getMin(), ic.getMax())
                            .step(1)
                    );
            var description = ic.getDescription();
            if (description != null)
                e.description(OptionDescription.of(description));// Shown when the user hover over this option
            return e.build(); // Builds the option entry for cloth config
        } else if (entry instanceof DoubleConfigValue dc) {
            var e = Option.<Double>createBuilder()
                    .name(dc.getTranslation())
                    .binding(dc.getDefaultValue(), dc, dc::set)
                    .controller(o -> DoubleSliderControllerBuilder.create(o)
                            .range(dc.getMin(), dc.getMax())
                            .step(0.0001d)
                            .formatValue(DOUBLE_FORMATTER)
                    );
            var description = dc.getDescription();
            if (description != null)
                e.description(OptionDescription.of(description));// Shown when the user hover over this option
            return e.build(); // Builds the option entry for cloth config
        } else if (entry instanceof FloatConfigValue fc) {
            var e = Option.<Float>createBuilder()
                    .name(fc.getTranslation())
                    .binding(fc.getDefaultValue(), fc, fc::set)
                    .controller(o -> FloatSliderControllerBuilder.create(o)
                            .range(fc.getMin(), fc.getMax())
                            .step(0.0001f)
                            .formatValue(FLOAT_FORMATTER)
                    );
            var description = fc.getDescription();
            if (description != null)
                e.description(OptionDescription.of(description));// Shown when the user hover over this option
            return e.build(); // Builds the option entry for cloth config
        } else if (entry instanceof StringConfigValue sc) {
            var e = Option.createBuilder(String.class)
                    .name(sc.getTranslation())
                    .binding(sc.getDefaultValue(), sc, sc::set)
                    .controller(StringControllerBuilder::create);
            var description = sc.getDescription();
            if (description != null)
                e.description(OptionDescription.of(description));// Shown when the user hover over this option
            return e.build(); // Builds the option entry for cloth config
        } else if (entry instanceof BoolConfigValue bc) {
            var e = Option.<Boolean>createBuilder()
                    .name(bc.getTranslation())
                    .binding(bc.getDefaultValue(), bc, bc::set)
                    .controller(TickBoxControllerBuilder::create);
            var description = bc.getDescription();
            if (description != null)
                e.description(OptionDescription.of(description));// Shown when the user hover over this option
            return e.build(); // Builds the option entry for cloth config
        } else if (entry instanceof EnumConfigValue<?> ec) {
            return addEnum(ec);
        } else if (entry instanceof ListStringConfigValue<?> lc) {
            var e = Option.<Component>createBuilder()
                    .name(lc.getTranslation())
                    .binding(Binding.immutable(Component.literal("String Lists are not supported")))
                    .customController(LabelController::new);
            var description = lc.getDescription();
            if (description != null)
                e.description(OptionDescription.of(description));// Shown when the user hover over this option
            return e.build(); // Builds the option entry for cloth config
        } else if (entry instanceof JsonConfigValue || entry instanceof ObjectConfigValue<?>) {
            var lc = (ConfigValue) entry;
            var e = Option.<Component>createBuilder()
                    .name(lc.getTranslation())
                    .binding(Binding.immutable(Component.literal("Object fields are not supported. Edit the config manually instead")))
                    .customController(LabelController::new);
            var description = lc.getDescription();
            if (description != null) e.description(OptionDescription.of(description));// Shown when the user hover over this option
            return e.build(); // Builds the option entry for cloth config
        }
        throw new UnsupportedOperationException("unknown entry: " + entry.getClass().getName());
    }

    private static final ValueFormatter<Double> DOUBLE_FORMATTER = value -> Component.nullToEmpty(String.format("%,.4f", value).replaceAll("[  ]", " "));
    private static final ValueFormatter<Float> FLOAT_FORMATTER = value -> Component.nullToEmpty(String.format("%,.4f", value).replaceAll("[  ]", " "));

    private static <T extends Enum<T>> Option<T> addEnum(EnumConfigValue<T> ec) {
        var e = Option.createBuilder(ec.getEnumClass())
                .name(ec.getTranslation())
                .binding(ec.getDefaultValue(), ec, ec::set)
                .controller(EnumControllerBuilder::create);
        var description = ec.getDescription();
        if (description != null)
            e.description(OptionDescription.of(description));// Shown when the user hover over this option
        return e.build(); // Builds the option entry for cloth config
    }

}