package net.mehvahdjukaar.moonlight.api.platform.configs.forge;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.moonlight.api.platform.configs.IConfigSpec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import org.jetbrains.annotations.Nullable;

public class ConfigSpecWrapper implements IConfigSpec {

    private final ForgeConfigSpec spec;
    private final ResourceLocation name;
    private final ConfigType type;

    @Nullable
    private ModConfig modConfig;


    public ConfigSpecWrapper(ResourceLocation name, ForgeConfigSpec spec, ConfigType type) {
        this.spec = spec;
        this.name = name;
        this.type = type;
    }

    @Override
    public ConfigType getConfigType() {
        return this.type;
    }

    @Override
    public void register() {
        ModConfig.Type t = this.type == ConfigType.COMMON ? ModConfig.Type.COMMON : ModConfig.Type.CLIENT;
        ModLoadingContext.get().registerConfig(t, spec);

        ModContainer modContainer = ModLoadingContext.get().getActiveContainer();
        this.modConfig = new ModConfig( t, spec, modContainer);
        modContainer.addConfig(this.modConfig);
    }

    @Override
    public void loadFromFile() {
        CommentedFileConfig replacementConfig = CommentedFileConfig
                .builder(FMLPaths.CONFIGDIR.get().resolve(name.getPath()))
                .sync()
                .preserveInsertionOrder()
                .writingMode(WritingMode.REPLACE)
                .build();
        replacementConfig.load();
        replacementConfig.save();

        spec.setConfig(replacementConfig);
    }

    public ForgeConfigSpec getSpec() {
        return spec;
    }

    @Nullable
    public ModConfig getModConfig() {
        return modConfig;
    }

    @Nullable
    @Override
    @OnlyIn(Dist.CLIENT)
    public Screen makeScreen(Screen parent, @Nullable ResourceLocation background) {
        var container = ModList.get().getModContainerById(this.name.getNamespace());
        if (container.isPresent()) {
            var factory = container.get().getCustomExtension(ConfigGuiHandler.ConfigGuiFactory.class);
            if (factory.isPresent()) return factory.get().screenFunction().apply(Minecraft.getInstance(), parent);
        }
        return null;
    }
}
