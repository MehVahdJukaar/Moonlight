package net.mehvahdjukaar.moonlight.api.platform.configs.forge;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.NetworkHooks;

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
       // loadConfigFile(fileName, targetSpec);

        ModContainer modContainer = ModLoadingContext.get().getActiveContainer();
        ModConfig config = new ModConfig(ModConfig.Type.COMMON, targetSpec, modContainer, fileName);

        if (addToMod) modContainer.addConfig(config);

        return config;
    }



    public static void reloadConfigFile(ModConfig config) {
       // loadConfigFile(config.getFileName(), (ForgeConfigSpec) config.getSpec());
    }


}
