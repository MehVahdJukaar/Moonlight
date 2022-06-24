package net.mehvahdjukaar.moonlight.configs;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.mehvahdjukaar.moonlight.Moonlight;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class ConfigHelper {

    /**
     * Loads a config file from the specified directory and assigns it to the given spec
     *
     * @param targetSpec target config spec
     * @param fileName   name of the config file
     * @param addToMod   if true it will add the config file to the mod container aswell
     * @return created mod config. Used for tracking and events
     */
    public static ModConfig addAndLoadConfigFile(ForgeConfigSpec targetSpec, String fileName, boolean addToMod) {
        loadConfigFile(fileName, targetSpec);

        ModContainer modContainer = ModLoadingContext.get().getActiveContainer();
        ModConfig config = new ModConfig(ModConfig.Type.COMMON, targetSpec, modContainer, fileName);

        if (addToMod) modContainer.addConfig(config);

        return config;
    }

    public static void loadConfigFile(String fileName, ForgeConfigSpec targetSpec) {
        CommentedFileConfig replacementConfig = CommentedFileConfig
                .builder(FMLPaths.CONFIGDIR.get().resolve(fileName))
                .sync()
                .preserveInsertionOrder()
                .writingMode(WritingMode.REPLACE)
                .build();
        replacementConfig.load();
        replacementConfig.save();

        targetSpec.setConfig(replacementConfig);
    }


    public static final Predicate<Object> STRING_CHECK = o -> o instanceof String;

    public static final Predicate<Object> LIST_STRING_CHECK = (s) -> {
        if (s instanceof List<?>) {
            return ((Collection<?>) s).stream().allMatch(o -> o instanceof String);
        }
        return false;
    };

    public static final Predicate<Object> COLOR_CHECK = s -> {
        try {
            Integer.parseUnsignedInt(((String) s).replace("0x", ""), 16);
            return true;
        } catch (Exception e) {
            return false;
        }
    };
}
