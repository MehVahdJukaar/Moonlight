package net.mehvahdjukaar.moonlight.api.platform.configs.forge;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigSpec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public class ConfigSpecWrapper extends ConfigSpec {

    private final ForgeConfigSpec spec;

    @Nullable
    private ModConfig modConfig;

    public ConfigSpecWrapper(ResourceLocation name, ForgeConfigSpec spec, ConfigType type) {
        this(name, spec, type, false);
    }
    public ConfigSpecWrapper(ResourceLocation name, ForgeConfigSpec spec, ConfigType type, boolean synced) {
        super(name,FMLPaths.CONFIGDIR.get(), type, synced);
        this.spec = spec;

        if(this.isSynced()) {
            var bus = FMLJavaModLoadingContext.get().getModEventBus();
            bus.addListener(this::onConfigChange);
            MinecraftForge.EVENT_BUS.addListener(this::onPlayerLoggedIn);
            MinecraftForge.EVENT_BUS.addListener(this::onPlayerLoggedOut);
        }
    }

    @Override
    public String getFileName() {
        assert modConfig != null;
        return modConfig.getFileName();
    }

    @Override
    public Path getFullPath() {
        assert modConfig != null : "This config must be registered";
        return modConfig.getFullPath();
    }

    @Override
    public void register() {
        ModConfig.Type t = this.getConfigType() == ConfigType.COMMON ? ModConfig.Type.COMMON : ModConfig.Type.CLIENT;
        ModLoadingContext.get().registerConfig(t, spec);

        ModContainer modContainer = ModLoadingContext.get().getActiveContainer();
        this.modConfig = new ModConfig( t, spec, modContainer);
        modContainer.addConfig(this.modConfig);
    }

    @Override
    public void loadFromFile() {
        CommentedFileConfig replacementConfig = CommentedFileConfig
                .builder(this.getFullPath())
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
        var container = ModList.get().getModContainerById(this.getModId());
        if (container.isPresent()) {
            var factory = container.get().getCustomExtension(ConfigGuiHandler.ConfigGuiFactory.class);
            if (factory.isPresent()) return factory.get().screenFunction().apply(Minecraft.getInstance(), parent);
        }
        return null;
    }



    @EventCalled
    protected void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer() instanceof ServerPlayer serverPlayer) {
            //send this configuration to connected clients
            syncConfigsToPlayer(serverPlayer);
        }
    }

    @EventCalled
    protected void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getPlayer().level.isClientSide) {
            onRefresh();
        }
    }

    @EventCalled
    protected void onConfigChange(ModConfigEvent event) {
        if (event.getConfig().getSpec() == this.getSpec()) {
            //send this configuration to connected clients
            sendSyncedConfigsToAllPlayers();
            onRefresh();
        }
    }

    private void onRefresh() {
    }

    //called on server. sync server -> all clients
    public void sendSyncedConfigsToAllPlayers() {
        MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
        if (currentServer != null) {
            PlayerList playerList = currentServer.getPlayerList();
            for (ServerPlayer player : playerList.getPlayers()) {
                syncConfigsToPlayer(player);
            }
        }
    }



}
