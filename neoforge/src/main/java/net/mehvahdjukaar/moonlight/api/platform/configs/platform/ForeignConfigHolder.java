package net.mehvahdjukaar.moonlight.api.platform.configs.platform;

import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.moonlight.api.platform.configs.ModConfigHolder;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigCategory;
import net.mehvahdjukaar.moonlight.core.client.config.MoonlightConfigScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;

public class ForeignConfigHolder extends ModConfigHolder {

    private final ModConfigSpec spec;
    private final ConfigCategory root;
    private final Component readableName;

    ForeignConfigHolder(ResourceLocation id, ConfigType type, ModConfigSpec spec, ConfigCategory root, Component readableName) {
        super(id, "toml", FMLPaths.CONFIGDIR.get(), type, null, false);
        this.spec = spec;
        this.root = root;
        this.readableName = readableName;
    }

    @Override
    public Component getReadableName() {
        return readableName;
    }

    @Override
    public void forceLoad() {
        // the owning mod already loaded it; the bridge only ever mirrors an already-loaded spec
    }

    @Override
    protected void saveToDisk() {
        spec.save();
    }

    @Override
    public ConfigCategory getConfigRoot() {
        return root;
    }

    @Override
    public void loadFromBytes(InputStream stream, boolean readOnly) {
        // foreign holders are never synced, so nothing ever pushes bytes at them
    }

    @Nullable
    @Override
    @OnlyIn(Dist.CLIENT)
    public Screen makeScreen(Screen parent, @Nullable ResourceLocation background) {
        return new MoonlightConfigScreen(this, root, parent, background);
    }
}
