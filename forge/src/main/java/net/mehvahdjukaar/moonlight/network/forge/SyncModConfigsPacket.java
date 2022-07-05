package net.mehvahdjukaar.moonlight.network.forge;

import com.electronwill.nightconfig.toml.TomlFormat;
import net.mehvahdjukaar.moonlight.Moonlight;
import net.mehvahdjukaar.moonlight.platform.configs.forge.SyncedModConfigs;
import net.mehvahdjukaar.moonlight.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.platform.network.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.io.ByteArrayInputStream;
import java.util.EnumMap;
import java.util.Optional;
import java.util.Set;

public class SyncModConfigsPacket implements Message {

    private static final EnumMap<ModConfig.Type, Set<ModConfig>> CONFIG_SETS = ObfuscationReflectionHelper
            .getPrivateValue(ConfigTracker.class, ConfigTracker.INSTANCE, "configSets");

    private final String fineName;
    private final byte[] configData;

    public SyncModConfigsPacket(FriendlyByteBuf buf) {
        this.fineName = buf.readUtf();
        this.configData = buf.readByteArray();
    }

    public SyncModConfigsPacket(final byte[] configFileData, final String fileName) {
        this.fineName = fileName;
        this.configData = configFileData;
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeUtf(this.fineName);
        buf.writeByteArray(this.configData);
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        Optional<ModConfig> config = CONFIG_SETS.get(ModConfig.Type.COMMON).stream()
                .filter(c -> c.getFileName().equals(this.fineName)).findFirst();

        if (config.orElse(null) instanceof SyncedModConfigs cfg && cfg.getSpec() instanceof ForgeConfigSpec spec) {
            spec.setConfig(TomlFormat.instance().createParser().parse(new ByteArrayInputStream(this.configData)));
            cfg.onRefresh();
            Moonlight.LOGGER.info("Synced Common configs");
        } else {
            Moonlight.LOGGER.error("Failed to find config file with name {}", this.fineName);
        }
    }


}
