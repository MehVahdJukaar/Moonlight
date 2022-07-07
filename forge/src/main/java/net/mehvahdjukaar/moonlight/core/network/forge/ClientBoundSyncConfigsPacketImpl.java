package net.mehvahdjukaar.moonlight.core.network.forge;

import com.electronwill.nightconfig.toml.TomlFormat;
import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.network.ClientBoundSyncConfigsPacket;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.io.ByteArrayInputStream;
import java.util.EnumMap;
import java.util.Optional;
import java.util.Set;

public class ClientBoundSyncConfigsPacketImpl {

    private static final EnumMap<ModConfig.Type, Set<ModConfig>> CONFIG_SETS = ObfuscationReflectionHelper
            .getPrivateValue(ConfigTracker.class, ConfigTracker.INSTANCE, "configSets");

    //TODO: implement
    public static void acceptConfigs(ClientBoundSyncConfigsPacket packet, ChannelHandler.Context context) {
        /*
        Optional<ModConfig> config = CONFIG_SETS.get(ModConfig.Type.COMMON).stream()
                .filter(c -> c.getFileName().equals(this.fineName)).findFirst();

        if (config.orElse(null) instanceof SyncedModConfigs cfg && cfg.getSpec() instanceof ForgeConfigSpec spec) {
            spec.setConfig(TomlFormat.instance().createParser().parse(new ByteArrayInputStream(this.configData)));
            cfg.onRefresh();
            Moonlight.LOGGER.info("Synced Common configs");
        } else {
            Moonlight.LOGGER.error("Failed to find config file with name {}", this.fineName);
        }*/
    }
}
