package net.mehvahdjukaar.moonlight.api.platform.platform;

import net.mehvahdjukaar.moonlight.api.client.model.platform.CustomUnbakedModelWrapper;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.platform.configs.platform.ForeignConfigBridge;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.server.packs.*;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.IItemDecorator;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.data.loading.DatagenModLoader;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.locating.IModFile;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static net.mehvahdjukaar.moonlight.platform.MoonlightForge.getCurrentBus;
import java.util.function.Function;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;

public class ClientHelperImpl {

    public static void addParticleRegistration(Consumer<ClientHelper.ParticleEvent> eventListener) {
        Moonlight.assertInitPhase();

        Consumer<RegisterParticleProvidersEvent> eventConsumer = event -> {
            eventListener.accept(new ParticleEventImpl(event));
        };
        getCurrentBus().addListener(eventConsumer);
    }

    private record ParticleEventImpl(RegisterParticleProvidersEvent event) implements ClientHelper.ParticleEvent {

        @Override
        public <P extends ParticleType<T>, T extends ParticleOptions> void register(P type, ClientHelper.ParticleFactory<T> provider) {
            this.event.registerSpriteSet(type, provider::create);

        }
    }

    public static void addEntityRenderersRegistration(Consumer<ClientHelper.EntityRendererEvent> eventListener) {
        Moonlight.assertInitPhase();

        Consumer<EntityRenderersEvent.RegisterRenderers> eventConsumer = event ->
                eventListener.accept(event::registerEntityRenderer);
        getCurrentBus().addListener(eventConsumer);
    }

    public static void addBlockEntityRenderersRegistration(Consumer<ClientHelper.BlockEntityRendererEvent> eventListener) {
        Moonlight.assertInitPhase();

        Consumer<EntityRenderersEvent.RegisterRenderers> eventConsumer = event ->
                eventListener.accept(event::registerBlockEntityRenderer);
        getCurrentBus().addListener(eventConsumer);
    }

    public static void addBlockColorsRegistration(Consumer<ClientHelper.BlockColorEvent> eventListener) {
        Moonlight.assertInitPhase();

        Consumer<RegisterColorHandlersEvent.BlockTintSources> eventConsumer = event -> {
            eventListener.accept(new ClientHelper.BlockColorEvent() {
                @Override
                public void register(List<BlockTintSource> tintSources, Block... blocks) {
                    event.register(tintSources, blocks);
                }

                @Override
                public int getColor(BlockState block, BlockAndTintGetter level, BlockPos pos, int tint) {
                    var source = event.getBlockColors().getTintSource(block, tint);
                    return source == null ? -1 : source.colorInWorld(block, level, pos);
                }

            });
        };
        getCurrentBus().addListener(eventConsumer);
    }

    @SuppressWarnings("ConstantConditions")
    public static void addClientReloadListener(Supplier<PreparableReloadListener> listener, Identifier location) {
        Moonlight.assertInitPhase();

        Consumer<AddClientReloadListenersEvent> eventConsumer = event -> event.addListener(location, listener.get());
        getCurrentBus().addListener(eventConsumer);
    }

    public static void addModelLayerRegistration(Consumer<ClientHelper.ModelLayerEvent> eventListener) {
        Moonlight.assertInitPhase();

        Consumer<EntityRenderersEvent.RegisterLayerDefinitions> eventConsumer = event -> {
            eventListener.accept(event::registerLayerDefinition);
        };
        getCurrentBus().addListener(eventConsumer);
    }

    public static void addTooltipComponentRegistration(Consumer<ClientHelper.TooltipComponentEvent> eventListener) {
        Moonlight.assertInitPhase();

        Consumer<RegisterClientTooltipComponentFactoriesEvent> eventConsumer = event -> {
            eventListener.accept(event::register);
        };
        getCurrentBus().addListener(eventConsumer);
    }

    public static void addFluidModelRegistration(Consumer<ClientHelper.FluidModelEvent> eventListener) {
        Moonlight.assertInitPhase();

        Consumer<RegisterFluidModelsEvent> eventConsumer = event -> {
            eventListener.accept(event::register);
        };
        getCurrentBus().addListener(eventConsumer);
    }

    public static void addBlockModelRegistration(Consumer<ClientHelper.BlockModelEvent> eventListener) {
        Moonlight.assertInitPhase();

        Consumer<RegisterBlockStateModels> eventConsumer = event -> {
            eventListener.accept((id, codec) -> event.registerModel(id, CustomUnbakedModelWrapper.wrap(codec)));
        };
        getCurrentBus().addListener(eventConsumer);
    }

    public static void collectModelParts(BlockStateModel model, @Nullable BlockAndTintGetter level,
                                         @Nullable BlockPos pos, @Nullable BlockState state,
                                         RandomSource random, List<BlockStateModelPart> parts) {
        if (level == null || pos == null || state == null) model.collectParts(random, parts);
        else model.collectParts(level, pos, state, random, parts);
    }

    public static void addItemDecoratorsRegistration(Consumer<ClientHelper.ItemDecoratorEvent> eventListener) {
        Moonlight.assertInitPhase();

        Consumer<RegisterItemDecorationsEvent> eventConsumer = event -> {
            eventListener.accept((i, l) -> {
                IItemDecorator deco = l::render;
                event.register(i, deco);
            });
        };
        getCurrentBus().addListener(eventConsumer);
    }

    public static void addKeyBindRegistration(Consumer<ClientHelper.KeyBindEvent> eventListener) {
        Moonlight.assertInitPhase();

        Consumer<RegisterKeyMappingsEvent> eventConsumer = event -> {
            eventListener.accept(event::register);
        };
        getCurrentBus().addListener(eventConsumer);
    }



    @Nullable
    public static Path getModIcon(String modId) {
        return PlatHelperImpl.getModIcon(modId);
    }

    @Nullable
    public static Screen getModConfigScreen(String modId, Screen parent) {
        return ModList.get().getModContainerById(modId)
                .flatMap(container -> container.getCustomExtension(IConfigScreenFactory.class)
                        .map(factory -> factory.createScreen(container, parent)))
                .orElse(null);
    }

    public static boolean hasModConfigScreen(String modId) {
        return ModList.get().getModContainerById(modId)
                .map(container -> container.getCustomExtension(IConfigScreenFactory.class).isPresent())
                .orElse(false);
    }

    @Nullable
    public static Screen getNativeForeignConfigScreen(String modId, Screen parent, @Nullable Identifier background) {
        return ForeignConfigBridge.createScreen(modId, parent, background);
    }

    public static boolean hasNativeForeignConfig(String modId) {
        return ForeignConfigBridge.hasConfig(modId);
    }

    public static boolean hasOnlyGenericConfigScreen(String modId) {
        return ForeignConfigBridge.hasOnlyGenericScreen(modId);
    }

    @ApiStatus.Internal
    public static boolean hasHiddenPerWorldConfig(String modId) {
        return ForeignConfigBridge.hasHiddenPerWorldConfig(modId);
    }

    public static void addClientSetup(Runnable clientSetup) {
        Moonlight.assertInitPhase();

        Consumer<FMLClientSetupEvent> eventConsumer = event -> event.enqueueWork(clientSetup);
        getCurrentBus().addListener(eventConsumer);
    }

    public static void addClientSetupAsync(Runnable clientSetup) {
        Moonlight.assertInitPhase();

        Consumer<FMLClientSetupEvent> eventConsumer = event -> clientSetup.run();
        getCurrentBus().addListener(eventConsumer);
    }

    public static void registerOptionalTexturePack(Identifier folderName, Component displayName, boolean defaultEnabled) {
        Moonlight.assertInitPhase();

        RegHelper.registerResourcePack(PackType.CLIENT_RESOURCES,
                () -> {
                    PackLocationInfo locationInfo = new PackLocationInfo(
                            folderName.toString(),
                            displayName,
                            defaultEnabled ? PackSource.BUILT_IN : PackSource.FEATURE,
                            Optional.empty()
                    );
                    try (PathPackResources pack = new PathPackResources(
                            locationInfo,
                            PlatHelperImpl.findModResource(folderName.getNamespace(),
                                    "resourcepacks/" + folderName.getPath()))) {
                        return Pack.readMetaAndCreate(
                                locationInfo,
                                new Pack.ResourcesSupplier() {
                                    @Override
                                    public PackResources openPrimary(PackLocationInfo location) {
                                        return pack;
                                    }

                                    @Override
                                    public PackResources openFull(PackLocationInfo location, Pack.Metadata metadata) {
                                        return pack;
                                    }
                                },
                                PackType.CLIENT_RESOURCES,
                                new PackSelectionConfig(
                                        false,
                                        Pack.Position.TOP,
                                        false
                                ));
                    } catch (Exception ee) {
                        if (!DatagenModLoader.isRunningDataGen()) {
                            Moonlight.LOGGER.error("Failed to load optional texture pack: {}", folderName, ee);
                        }
                    }
                    return null;
                }
        );
    }

    public static void addRenderPipelineRegistration(Consumer<ClientHelper.RenderPipelineEvent> eventListener) {
        Moonlight.assertInitPhase();

        Consumer<RegisterRenderPipelinesEvent> eventConsumer = event ->
                eventListener.accept(event::registerPipeline);
        getCurrentBus().addListener(eventConsumer);
    }

    public static void addPictureInPictureRendererRegistration(Consumer<ClientHelper.PictureInPictureEvent> eventListener) {
        Moonlight.assertInitPhase();

        Consumer<RegisterPictureInPictureRenderersEvent> eventConsumer = event ->
                eventListener.accept(new ClientHelper.PictureInPictureEvent() {
                    @Override
                    public <T extends PictureInPictureRenderState> void register(
                            Class<T> stateClass, Function<MultiBufferSource.BufferSource, PictureInPictureRenderer<T>> factory) {
                        event.register(stateClass, factory);
                    }
                });
        getCurrentBus().addListener(eventConsumer);
    }

    public static void addSpecialModelRegistration(Consumer<ClientHelper.SpecialModelEvent> eventListener) {
        Moonlight.assertInitPhase();

        Consumer<RegisterSpecialModelRendererEvent> eventConsumer = event -> eventListener.accept(event::register);
        getCurrentBus().addListener(eventConsumer);
    }

    public static void addMenuScreensRegistration(Consumer<ClientHelper.MenuScreenEvent> eventListener) {
        Moonlight.assertInitPhase();
        Consumer<RegisterMenuScreensEvent> eventConsumer = event -> {
            eventListener.accept(event::register);
        };
        getCurrentBus().addListener(eventConsumer);
    }

    public static void addClientLoginCallback(Runnable callback) {
        Moonlight.assertInitPhase();
        // game bus, not the mod bus. LoggingIn is the client side counterpart of PlayerLoggedInEvent
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingIn event) -> callback.run());
    }

}
