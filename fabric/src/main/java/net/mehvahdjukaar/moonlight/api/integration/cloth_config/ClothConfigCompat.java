package net.mehvahdjukaar.moonlight.api.integration.cloth_config;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.gui.entries.EnumListEntry;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.platform.JsonConfigEntry;
import net.mehvahdjukaar.moonlight.api.platform.configs.platform.JsonConfigCategory;
import net.mehvahdjukaar.moonlight.api.platform.configs.platform.FabricConfigHolder;
import net.mehvahdjukaar.moonlight.api.platform.configs.platform.values.*;
import net.mehvahdjukaar.moonlight.api.util.TextHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClothConfigCompat {
    //call FabricConfigListScreen.makeScreen instead
    @ApiStatus.Internal
    public static Screen makeScreen(Screen parent, FabricConfigHolder spec) {
        return makeScreen(parent, spec, null);
    }
    @ApiStatus.Internal
    public static Screen makeScreen(Screen parent, FabricConfigHolder spec, @Nullable ResourceLocation background) {
        spec.forceLoad();

        ConfigBuilder builder = ConfigBuilder.create();

        builder.setParentScreen(parent);
        builder.setTitle(spec.getReadableName());
        builder.setSavingRunnable(spec::saveConfig);

        if (background != null) builder.setDefaultBackgroundTexture(background);


        for (var en : spec.getMainEntry().getEntries()) {
            //skips stray config values
            if (!(en instanceof JsonConfigCategory c)) continue;
            ConfigCategory mainCat = builder.getOrCreateCategory(Component.translatable(TextHelper.getReadableName(c.getName())));
            for (var entry : c.getEntries()) {
                if (entry instanceof JsonConfigCategory subCat) {
                    var subBuilder = builder.entryBuilder().startSubCategory(Component.translatable(subCat.getName()));
                    addEntriesRecursive(builder, subBuilder, subCat);

                    mainCat.addEntry(subBuilder.build());
                } else {
                    mainCat.addEntry(buildEntry(builder, entry));
                }
            }

        }
        return builder.build();
    }

    private static void addEntriesRecursive(ConfigBuilder builder, SubCategoryBuilder subCategoryBuilder, JsonConfigCategory c) {

        for (var entry : c.getEntries()) {
            if (entry instanceof JsonConfigCategory cc) {
                var scb = builder.entryBuilder().startSubCategory(Component.translatable(entry.getName()));
                addEntriesRecursive(builder, scb, cc);
                subCategoryBuilder.add(scb.build());
            } else subCategoryBuilder.add(buildEntry(builder, entry));
        }
    }

    private static AbstractConfigListEntry<?> buildEntry(ConfigBuilder builder, JsonConfigEntry entry) {

        if (entry instanceof ColorConfigValue col) {
            var eb = builder.entryBuilder();
            var e = (col.hasAlpha()
                    ? eb.startAlphaColorField(col.getTranslation(), col.get())
                    : eb.startColorField(col.getTranslation(), col.get()))
                    .setDefaultValue(col.getDefaultValue()) // Recommended: Used when user click "Reset"
                    .setSaveConsumer(col::set);// Recommended: Called when user save the config
            var description = col.getCommentComponent();
            if (description != null) e.setTooltip(description);// Shown when the user hover over this option
            return e.build(); // Builds the option entry for cloth config
        } else if (entry instanceof IntConfigValue ic) {
            var e = builder.entryBuilder()
                    .startIntField(ic.getTranslation(), ic.get())
                    .setMax(ic.getMax())
                    .setMin(ic.getMin())
                    .setDefaultValue(ic.getDefaultValue()) // Recommended: Used when user click "Reset"
                    .setSaveConsumer(ic::set); // Recommended: Called when user save the config
            var description = ic.getCommentComponent();
            if (description != null) e.setTooltip(description);// Shown when the user hover over this option
            return e.build(); // Builds the option entry for cloth config
        } else if (entry instanceof DoubleConfigValue dc) {
            var e = builder.entryBuilder()
                    .startDoubleField(dc.getTranslation(), dc.get())
                    .setMax(dc.getMax())
                    .setMin(dc.getMin())
                    .setDefaultValue(dc.getDefaultValue()) // Recommended: Used when user click "Reset"
                    .setSaveConsumer(dc::set); // Recommended: Called when user save the config
            var description = dc.getCommentComponent();
            if (description != null) e.setTooltip(description);// Shown when the user hover over this option
            return e.build(); // Builds the option entry for cloth config
        } else if ( entry instanceof FloatConfigValue fc) {
            var e = builder.entryBuilder()
                    .startFloatField(fc.getTranslation(), fc.get())
                    .setMax(fc.getMax())
                    .setMin(fc.getMin())
                    .setDefaultValue(fc.getDefaultValue()) // Recommended: Used when user click "Reset"
                    .setSaveConsumer(fc::set); // Recommended: Called when user save the config
            var description = fc.getCommentComponent();
            if (description != null) e.setTooltip(description);// Shown when the user hover over this option
            return e.build(); // Builds the option entry for cloth config
        }
        else if (entry instanceof StringConfigValue sc) {
            var e = builder.entryBuilder()
                    .startStrField(sc.getTranslation(), sc.get())
                    .setDefaultValue(sc.getDefaultValue()) // Recommended: Used when user click "Reset"
                    .setSaveConsumer(sc::set); // Recommended: Called when user save the config
            var description = sc.getCommentComponent();
            if (description != null) e.setTooltip(description);// Shown when the user hover over this option
            return e.build(); // Builds the option entry for cloth config
        } else if (entry instanceof BoolConfigValue bc) {
            var e = builder.entryBuilder()
                    .startBooleanToggle(bc.getTranslation(), bc.get())
                    .setDefaultValue(bc.getDefaultValue()) // Recommended: Used when user click "Reset"
                    .setSaveConsumer(bc::set); // Recommended: Called when user save the config
            var description = bc.getCommentComponent();
            if (description != null) e.setTooltip(description);// Shown when the user hover over this option
            return e.build(); // Builds the option entry for cloth config
        } else if (entry instanceof EnumConfigValue<?> ec) {
            return addEnum(builder, ec);
        } else if (entry instanceof ListStringConfigValue<?> lc) {
            var e = builder.entryBuilder()
                    .startStrList(lc.getTranslation(), lc.get())
                    .setDefaultValue(lc.getDefaultValue()) // Recommended: Used when user click "Reset"
                    .setSaveConsumer(lc::set); // Recommended: Called when user save the config
            var description = lc.getCommentComponent();
            if (description != null) e.setTooltip(description);// Shown when the user hover over this option
            return e.build(); // Builds the option entry for cloth config
        }
        else if(entry instanceof JsonConfigValue || entry instanceof ObjectConfigValue<?>){
          var e =  builder.entryBuilder().startTextDescription(Component.literal("Unsupported entry. Edit config manually"));
            return e.build();
        }
        throw new UnsupportedOperationException("unknown entry: " + entry.getClass().getName());
    }

    private static @NotNull <T extends Enum<T>> EnumListEntry<T> addEnum(ConfigBuilder builder, EnumConfigValue<T> ec) {
        var e = builder.entryBuilder()
                .startEnumSelector(ec.getTranslation(), ec.getEnumClass(), ec.get())
                .setDefaultValue(ec.getDefaultValue()) // Recommended: Used when user click "Reset"
                .setSaveConsumer(ec::set); // Recommended: Called when user save the config
        var description = ec.getCommentComponent();
        if (description != null) e.setTooltip(description);// Shown when the user hover over this option
        return e.build(); // Builds the option entry for cloth config
    }

}
