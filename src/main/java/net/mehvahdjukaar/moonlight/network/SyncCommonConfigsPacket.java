package net.mehvahdjukaar.moonlight.network;

import com.electronwill.nightconfig.toml.TomlFormat;
import net.mehvahdjukaar.moonlight.Moonlight;
import net.mehvahdjukaar.moonlight.configs.SyncedCommonConfigs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.io.ByteArrayInputStream;
import java.util.EnumMap;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public class SyncCommonConfigsPacket {

    private static final EnumMap<ModConfig.Type, Set<ModConfig>> CONFIG_SETS = ObfuscationReflectionHelper
            .getPrivateValue(ConfigTracker.class, ConfigTracker.INSTANCE, "configSets");

    private final String fineName;
    private final byte[] configData;

    public SyncCommonConfigsPacket(FriendlyByteBuf buf) {
        this.configData = buf.readByteArray();
        this.fineName = buf.readUtf();
    }

    public SyncCommonConfigsPacket(final byte[] configFileData, final String fileName) {
        this.configData = configFileData;
        this.fineName = fileName;
    }

    public static void buffer(SyncCommonConfigsPacket message, FriendlyByteBuf buf) {
        buf.writeByteArray(message.configData);
    }

    //client
    public static void handler(SyncCommonConfigsPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getDirection() != NetworkDirection.PLAY_TO_CLIENT) {
                return;
            }
            Optional<ModConfig> config = CONFIG_SETS.get(ModConfig.Type.COMMON).stream()
                    .filter(c -> c.getFileName().equals(message.fineName)).findFirst();

            if (config.orElse(null) instanceof SyncedCommonConfigs cfg && cfg.getSpec() instanceof ForgeConfigSpec spec) {
                spec.setConfig(TomlFormat.instance().createParser().parse(new ByteArrayInputStream(message.configData)));
                cfg.onRefresh();
                //ServerConfigs.cached.refresh();
            } else {
                Moonlight.LOGGER.error("Failed to find config file with name {}", message.fineName);
            }


            Moonlight.LOGGER.info("Synced Common configs");
        });
        context.setPacketHandled(true);
    }


}
