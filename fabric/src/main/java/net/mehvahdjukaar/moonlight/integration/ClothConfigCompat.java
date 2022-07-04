package net.mehvahdjukaar.moonlight.integration;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import net.mehvahdjukaar.moonlight.platform.configs.fabric.ConfigSpec;
import net.mehvahdjukaar.moonlight.platform.configs.fabric.ConfigBuilderImpl;
import net.mehvahdjukaar.moonlight.platform.configs.fabric.values.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class ClothConfigCompat {

    public static Screen makeScreen(Screen parent, ConfigSpec spec, @Nullable ResourceLocation background) {
        spec.loadConfig();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable(spec.getTitleKey()));

        if (background != null) builder.setDefaultBackgroundTexture(background);

        builder.setSavingRunnable(spec::saveConfig);

        for (var c : spec.getCategories()) {
            ConfigCategory category = builder.getOrCreateCategory(Component.translatable(c.getName()));
            for (var entry : c.getValues()) {
                String name = entry.getName();
                if (entry instanceof IntConfigValue ic) {
                    category.addEntry(builder.entryBuilder()
                            .startIntField(ic.getTranslation(), ic.get())
                            .setMax(ic.getMax())
                            .setMin(ic.getMin())
                            .setDefaultValue(ic.getDefaultValue()) // Recommended: Used when user click "Reset"
                            .setTooltip(ic.getDescription()) // Optional: Shown when the user hover over this option
                            .setSaveConsumer(ic::set) // Recommended: Called when user save the config
                            .build()); // Builds the option entry for cloth config
                } else if (entry instanceof DoubleConfigValue dc) {
                    category.addEntry(builder.entryBuilder()
                            .startDoubleField(dc.getTranslation(), dc.get())
                            .setMax(dc.getMax())
                            .setMin(dc.getMin())
                            .setDefaultValue(dc.getDefaultValue()) // Recommended: Used when user click "Reset"
                            .setTooltip(dc.getDescription()) // Optional: Shown when the user hover over this option
                            .setSaveConsumer(dc::set) // Recommended: Called when user save the config
                            .build()); // Builds the option entry for cloth config
                } else if (entry instanceof StringConfigValue sc) {
                    category.addEntry(builder.entryBuilder()
                            .startStrField(sc.getTranslation(), sc.get())
                            .setDefaultValue(sc.getDefaultValue()) // Recommended: Used when user click "Reset"
                            .setTooltip(sc.getDescription()) // Optional: Shown when the user hover over this option
                            .setSaveConsumer(sc::set) // Recommended: Called when user save the config
                            .build()); // Builds the option entry for cloth config
                } else if (entry instanceof BoolConfigValue bc) {
                    category.addEntry(builder.entryBuilder()
                            .startBooleanToggle(bc.getTranslation(), bc.get())
                            .setDefaultValue(bc.getDefaultValue()) // Recommended: Used when user click "Reset"
                            .setTooltip(bc.getDescription()) // Optional: Shown when the user hover over this option
                            .setSaveConsumer(bc::set) // Recommended: Called when user save the config
                            .build()); // Builds the option entry for cloth config else if (entry instanceof EnumConfigValue<?> ec) {
                } else if (entry instanceof EnumConfigValue<?> ec) {
                    addEnum(builder, category, name, ec);
                }
            }
        }
        return builder.build();
    }

    private static <T extends Enum<T>> void addEnum(ConfigBuilder builder, ConfigCategory category, String name, EnumConfigValue<T> ec) {
        category.addEntry(builder.entryBuilder()
                .startEnumSelector(ec.getTranslation(), ec.getEnum(), ec.get())
                .setDefaultValue(ec.getDefaultValue()) // Recommended: Used when user click "Reset"
                .setTooltip(ec.getDescription()) // Optional: Shown when the user hover over this option
                .setSaveConsumer(ec::set) // Recommended: Called when user save the config
                .build()); // Builds the option entry for cloth config
    }

}
