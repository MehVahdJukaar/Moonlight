package net.mehvahdjukaar.moonlight.api.integration;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.gui.entries.EnumListEntry;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.fabric.ConfigEntry;
import net.mehvahdjukaar.moonlight.api.platform.configs.fabric.ConfigSpec;
import net.mehvahdjukaar.moonlight.api.platform.configs.fabric.values.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClothConfigCompat {

    public static Screen makeScreen(Screen parent, ConfigSpec spec) {
        return makeScreen(parent, spec, null);
    }

    public static Screen makeScreen(Screen parent, ConfigSpec spec, @Nullable ResourceLocation background) {
        spec.loadFromFile();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable(spec.getTitleKey()));

        if (background != null) builder.setDefaultBackgroundTexture(background);

        builder.setSavingRunnable(spec::saveConfig);


        for (var en : spec.getMainEntry().getEntries()) {
            //skips stray config values
            if(!(en instanceof net.mehvahdjukaar.moonlight.api.platform.configs.fabric.ConfigCategory c))continue;
            ConfigCategory mainCat = builder.getOrCreateCategory(Component.translatable(c.getName()));
            for (var entry : c.getEntries()) {
                if(entry instanceof net.mehvahdjukaar.moonlight.api.platform.configs.fabric.ConfigCategory subCat){
                    var subBuilder = builder.entryBuilder().startSubCategory(Component.translatable(subCat.getName()));
                    addEntriesRecursive(builder, subBuilder, subCat);

                    mainCat.addEntry(subBuilder.build());
                }else{
                    mainCat.addEntry(buildEntry(builder, entry));
                }
            }

        }
        return builder.build();
    }

    private static void addEntriesRecursive(ConfigBuilder builder, SubCategoryBuilder subCategoryBuilder, net.mehvahdjukaar.moonlight.api.platform.configs.fabric.ConfigCategory c) {

        for (var entry : c.getEntries()) {
            if(entry instanceof net.mehvahdjukaar.moonlight.api.platform.configs.fabric.ConfigCategory cc){
                var scb = builder.entryBuilder().startSubCategory(Component.translatable(entry.getName()));
                addEntriesRecursive(builder,scb, cc);
                subCategoryBuilder.add(scb.build());
            }
            else subCategoryBuilder.add(buildEntry(builder, entry));
        }
    }

    @javax.annotation.Nullable
    private static AbstractConfigListEntry<?> buildEntry(ConfigBuilder builder, ConfigEntry entry) {

        if(entry instanceof ColorConfigValue col){
            return builder.entryBuilder()
                    .startColorField(col.getTranslation(), col.get())
                    .setDefaultValue(col.getDefaultValue()) // Recommended: Used when user click "Reset"
                    .setTooltip(col.getDescription()) // Optional: Shown when the user hover over this option
                    .setSaveConsumer(col::set) // Recommended: Called when user save the config
                    .build(); // Builds the option entry for cloth config
        }
        else if (entry instanceof IntConfigValue ic) {
            return builder.entryBuilder()
                    .startIntField(ic.getTranslation(), ic.get())
                    .setMax(ic.getMax())
                    .setMin(ic.getMin())
                    .setDefaultValue(ic.getDefaultValue()) // Recommended: Used when user click "Reset"
                    .setTooltip(ic.getDescription()) // Optional: Shown when the user hover over this option
                    .setSaveConsumer(ic::set) // Recommended: Called when user save the config
                    .build(); // Builds the option entry for cloth config
        } else if (entry instanceof DoubleConfigValue dc) {
            return builder.entryBuilder()
                    .startDoubleField(dc.getTranslation(), dc.get())
                    .setMax(dc.getMax())
                    .setMin(dc.getMin())
                    .setDefaultValue(dc.getDefaultValue()) // Recommended: Used when user click "Reset"
                    .setTooltip(dc.getDescription()) // Optional: Shown when the user hover over this option
                    .setSaveConsumer(dc::set) // Recommended: Called when user save the config
                    .build(); // Builds the option entry for cloth config
        } else if (entry instanceof StringConfigValue sc) {
            return builder.entryBuilder()
                    .startStrField(sc.getTranslation(), sc.get())
                    .setDefaultValue(sc.getDefaultValue()) // Recommended: Used when user click "Reset"
                    .setTooltip(sc.getDescription()) // Optional: Shown when the user hover over this option
                    .setSaveConsumer(sc::set) // Recommended: Called when user save the config
                    .build(); // Builds the option entry for cloth config
        } else if (entry instanceof BoolConfigValue bc) {
            return builder.entryBuilder()
                    .startBooleanToggle(bc.getTranslation(), bc.get())
                    .setDefaultValue(bc.getDefaultValue()) // Recommended: Used when user click "Reset"
                    .setTooltip(bc.getDescription()) // Optional: Shown when the user hover over this option
                    .setSaveConsumer(bc::set) // Recommended: Called when user save the config
                    .build(); // Builds the option entry for cloth config else if (entry instanceof EnumConfigValue<?> ec) {
        } else if (entry instanceof EnumConfigValue<?> ec) {
            return addEnum(builder, ec);
        }
        return null;
    }

    private static @NotNull <T extends Enum<T>> EnumListEntry<T> addEnum(ConfigBuilder builder, EnumConfigValue<T> ec) {
        return builder.entryBuilder()
                .startEnumSelector(ec.getTranslation(), ec.getEnum(), ec.get())
                .setDefaultValue(ec.getDefaultValue()) // Recommended: Used when user click "Reset"
                .setTooltip(ec.getDescription()) // Optional: Shown when the user hover over this option
                .setSaveConsumer(ec::set) // Recommended: Called when user save the config
                .build(); // Builds the option entry for cloth config
    }

}
