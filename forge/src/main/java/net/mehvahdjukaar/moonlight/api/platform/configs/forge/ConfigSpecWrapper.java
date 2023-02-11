package net.mehvahdjukaar.moonlight.api.platform.configs.forge;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigSpec;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.IConfigEvent;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigSpecWrapper extends ConfigSpec {

    private static final Method SET_CONFIG_DATA = ObfuscationReflectionHelper.findMethod(ModConfig.class, "setConfigData", CommentedConfig.class);

    private final ForgeConfigSpec spec;

    private final ModConfig modConfig;
    private final ModContainer modContainer;

    private final Map<ForgeConfigSpec.ConfigValue<?>, Object> requireRestartValues;

    public ConfigSpecWrapper(ResourceLocation name, ForgeConfigSpec spec, ConfigType type, boolean synced,
                             @Nullable Runnable onChange, List<ForgeConfigSpec.ConfigValue<?>> requireRestart) {
        super(name, FMLPaths.CONFIGDIR.get(), type, synced, onChange);
        this.spec = spec;

        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        if (onChange != null || this.isSynced()) bus.addListener(this::onConfigChange);
        if (this.isSynced()) {

            MinecraftForge.EVENT_BUS.addListener(this::onPlayerLoggedIn);
            MinecraftForge.EVENT_BUS.addListener(this::onPlayerLoggedOut);
        }

        ModConfig.Type t = this.getConfigType() == ConfigType.COMMON ? ModConfig.Type.COMMON : ModConfig.Type.CLIENT;

        this.modContainer = ModLoadingContext.get().getActiveContainer();

        this.modConfig = new ModConfig(t, spec, modContainer, name.getNamespace() + "-" + name.getPath() + ".toml");
        //for event
        ConfigSpec.addTrackedSpec(this);

        if(!requireRestart.isEmpty()){
            loadFromFile(); //early load if this has world reload ones
        }
        this.requireRestartValues = requireRestart.stream().collect(Collectors.toMap(e -> e, ForgeConfigSpec.ConfigValue::get));
    }

    @Override
    public String getFileName() {
        return modConfig.getFileName();
    }

    @Override
    public Path getFullPath() {
        return FMLPaths.CONFIGDIR.get().resolve(this.getFileName());
        // return modConfig.getFullPath();
    }

    @Override
    public void register() {
        ModContainer modContainer = ModLoadingContext.get().getActiveContainer();
        modContainer.addConfig(this.modConfig);
    }

    @Override
    public void loadFromFile() {
        //same stuff that forge config tracker does
        try {
            final CommentedFileConfig configData = modConfig.getHandler().reader(FMLPaths.CONFIGDIR.get()).apply(modConfig);
            SET_CONFIG_DATA.setAccessible(true);
            SET_CONFIG_DATA.invoke(modConfig, configData);
            modContainer.dispatchConfigEvent(IConfigEvent.loading(modConfig));
            modConfig.save();
        } catch (Exception e) {
            throw new RuntimeException(
                    new IOException("Failed to load " + this.getFileName() + " config. Try deleting it: " + e));
        }
    }

    public ForgeConfigSpec getSpec() {
        return spec;
    }

    @Nullable
    public ModConfig getModConfig() {
        return modConfig;
    }

    public ModConfig.Type getModConfigType() {
        return this.getConfigType() == ConfigType.CLIENT ? ModConfig.Type.CLIENT : ModConfig.Type.COMMON;
    }

    @Override
    public boolean isLoaded() {
        return spec.isLoaded();
    }

    @Nullable
    @Override
    @OnlyIn(Dist.CLIENT)
    public Screen makeScreen(Screen parent, @Nullable ResourceLocation background) {
        var container = ModList.get().getModContainerById(this.getModId());
        if (container.isPresent()) {
            var factory = container.get().getCustomExtension(ConfigScreenHandler.ConfigScreenFactory.class);
            if (factory.isPresent()) return factory.get().screenFunction().apply(Minecraft.getInstance(), parent);
        }
        return null;
    }

    @Override
    public boolean hasConfigScreen() {
        return ModList.get().getModContainerById(this.getModId())
                .map(container -> container.getCustomExtension(ConfigScreenHandler.ConfigScreenFactory.class)
                        .isPresent()).orElse(false);
    }

    @EventCalled
    protected void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            //send this configuration to connected clients
            syncConfigsToPlayer(serverPlayer);
        }
    }

    @EventCalled
    protected void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity().level.isClientSide) {
            onRefresh();
        }
    }

    @EventCalled
    protected void onConfigChange(ModConfigEvent event) {
        if (event.getConfig().getSpec() == this.getSpec()) {
            //send this configuration to connected clients if on server
            if (this.isSynced() && PlatformHelper.getEnv().isServer()) sendSyncedConfigsToAllPlayers();
            onRefresh();
        }
    }

    @Override
    public void loadFromBytes(InputStream stream) {
        try { //this should work the same as below and internaly calls refresh
            var b = stream.readAllBytes();
            this.modConfig.acceptSyncedConfig(b);
        } catch (Exception e) {
            Moonlight.LOGGER.warn("Failed to sync config file {}:", this.getFileName(), e);
        }

        //using this isntead so we dont fire the config changes event otherwise this will loop
        //this.getSpec().setConfig(TomlFormat.instance().createParser().parse(stream));
        //this.onRefresh();
    }


    public boolean requiresGameRestart(ForgeConfigSpec.ConfigValue<?> value) {
        var v = requireRestartValues.get(value);
        if (v == null) return false;
        else return v != value.get();
    }


}
