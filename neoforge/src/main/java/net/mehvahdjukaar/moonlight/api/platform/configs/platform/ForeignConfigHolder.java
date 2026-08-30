package net.mehvahdjukaar.moonlight.api.platform.configs.platform;

import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.moonlight.api.platform.configs.ModConfigHolder;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;

public class ForeignConfigHolder extends ModConfigHolder {

    private final ModConfigSpec spec;
    private final ConfigCategory root;
    private final Component readableName;

    ForeignConfigHolder(Identifier id, ConfigType type, ModConfigSpec spec, ConfigCategory root, Component readableName) {
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
    public boolean isLoaded() {
        return spec.isLoaded();
    }

    @Override
    public void forceLoad() {
        // the owning mod owns the loading. A world bound spec only shows up as a disabled row until it does
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
    }
}
