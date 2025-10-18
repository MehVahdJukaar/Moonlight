package net.mehvahdjukaar.moonlight.api.platform.configs.neoforge;

import com.electronwill.nightconfig.core.concurrent.ConcurrentCommentedConfig;
import com.electronwill.nightconfig.core.io.ParsingMode;
import com.electronwill.nightconfig.toml.TomlFormat;
import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.moonlight.api.platform.configs.ModConfigHolder;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PathPackResources;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ConfigTracker;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("all")
public final class ForgeConfigHolder extends ModConfigHolder {

    private static final Map<ModConfig, ForgeConfigHolder> BY_FORGE_CONFIG = new HashMap<>();

    public static ForgeConfigHolder getFromForgeConfig(ModConfig config) {
        return BY_FORGE_CONFIG.get(config);
    }

    private static final Method LOAD_CONFIG = ObfuscationReflectionHelper.findMethod(
            ConfigTracker.class, "loadConfig",
            ModConfig.class, Path.class, Function.class);

    private final ModConfigSpec spec;
    private final ModConfig modConfig;

    private final List<ConfigBuilderImpl.ValueWrapper<?, ?>> specialValues;

    ForgeConfigHolder(ResourceLocation name, ModConfigSpec spec, ConfigType type,
                      @Nullable Runnable onChange,
                      List<ConfigBuilderImpl.ValueWrapper<?, ?>> specialValues) {
        super(name, "toml", FMLPaths.CONFIGDIR.get(), type, onChange);
        this.spec = spec;
        this.specialValues = specialValues;

        ModConfig.Type forgeType = this.getConfigType() == ConfigType.CLIENT ? ModConfig.Type.CLIENT : ModConfig.Type.COMMON;

        ModContainer modContainer = ModList.get().getModContainerById(this.getModId()).orElseThrow();
        // create config and registers immediately
        this.modConfig = ConfigTracker.INSTANCE.registerConfig(forgeType, spec, modContainer, this.getFileName());

        var bus = modContainer.getEventBus();
        if (onChange != null || this.isSynced() || !specialValues.isEmpty()) bus.addListener(this::onConfigChange);
        if (this.isSynced()) {
            NeoForge.EVENT_BUS.addListener(this::onPlayerLoggedIn);
            NeoForge.EVENT_BUS.addListener(this::onPlayerLoggedOut);
        }
        //for event

        BY_FORGE_CONFIG.put(this.modConfig, this);
    }

    @Override
    public Path getFullPath() {
        return FMLPaths.CONFIGDIR.get().resolve(this.getFileName());
        // return modConfig.getFullPath();
    }

    @Override
    public void forceLoad() {
        if (this.isLoaded()) return;
        try {
            LOAD_CONFIG.invoke(ConfigTracker.INSTANCE, this.modConfig, this.getFullPath(),
                    (Function<ModConfig, ModConfigEvent>) ModConfigEvent.Loading::new);
        } catch (Exception e) {
            throw new ConfigLoadingException(this, e);
        }
    }

    public ModConfigSpec getSpec() {
        return spec;
    }

    @Nullable
    public ModConfig getModConfig() {
        return modConfig;
    }

    @Override
    public boolean isLoaded() {
        return spec.isLoaded();
    }

    @Nullable
    @Override
    @OnlyIn(Dist.CLIENT)
    public Screen makeScreen(Screen parent, @Nullable ResourceLocation background) {
        return ModList.get().getModContainerById(this.getModId())
                .flatMap(container -> container.getCustomExtension(IConfigScreenFactory.class)
                        .map(factory -> factory.createScreen(container, parent)))
                .orElse(null);
    }

    @Override
    public boolean hasConfigScreen() {
        return ModList.get().getModContainerById(this.getModId())
                .map(container -> container.getCustomExtension(IConfigScreenFactory.class)
                        .isPresent()).orElse(false);
    }

    @ApiStatus.Internal
    @EventCalled
    private void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            //send this configuration to connected clients
            syncConfigsToPlayer(serverPlayer);
        }
    }

    @ApiStatus.Internal
    @EventCalled
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity().level().isClientSide) {
            onRefresh();
        }
    }

    @ApiStatus.Internal
    @EventCalled
    public void onConfigChange(ModConfigEvent event) {
        if (event.getConfig().getSpec() == this.getSpec()) {
            //send this configuration to connected clients if on server
            if (this.isSynced() && PlatHelper.getPhysicalSide().isServer()) sendSyncedConfigsToAllPlayers();
            onRefresh();
            specialValues.forEach(ConfigBuilderImpl.ValueWrapper::clearCache);
        }
    }

    @Override
    public void loadFromBytes(InputStream stream) {
        try {
            byte[] b = stream.readAllBytes();
            //this should work the same as below and internaly calls refresh
//            acceptConfig(this.modConfig, b);
            ConfigTracker.acceptSyncedConfig(this.modConfig, b);
        } catch (Exception e) {
            Moonlight.LOGGER.warn("Failed to sync config file {}:", this.getFileName(), e);
        }
        //using this isntead so we dont fire the config changes event otherwise this will loop
       // this.getSpec().setConfig(TomlFormat.instance().createParser().parse(stream));
       // this.onRefresh();
    }

    public static void acceptConfig(ModConfig modConfig, byte[] bytes) {
        if (modConfig.getLoadedConfig().config() instanceof ConcurrentCommentedConfig cc) {
            cc.bulkCommentedUpdate(view -> {
                TomlFormat.instance().createParser().parse(new ByteArrayInputStream(bytes), view, ParsingMode.REPLACE);
            });
        }
    }

}
