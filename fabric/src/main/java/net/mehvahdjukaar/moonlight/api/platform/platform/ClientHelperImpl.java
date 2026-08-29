package net.mehvahdjukaar.moonlight.api.platform.platform;

import com.google.common.base.Suppliers;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.mehvahdjukaar.moonlight.api.client.model.platform.CustomUnbakedModelWrapper;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.mehvahdjukaar.moonlight.api.integration.mod_menu.ModMenuCompat;
import net.mehvahdjukaar.moonlight.api.client.gui.IItemDecoratorRenderer;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.mixins.platform.RenderPipelinesAccessor;
import net.minecraft.client.Minecraft;
import net.mehvahdjukaar.moonlight.platform.MoonlightFabricClient;
import net.minecraft.client.color.block.BlockTintSource;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry;
import net.minecraft.client.renderer.special.SpecialModelRenderers;

public class ClientHelperImpl {

    public static void addParticleRegistration(Consumer<ClientHelper.ParticleEvent> eventListener) {
        Moonlight.assertInitPhase();
        MoonlightFabricClient.addClientTask(() -> {
            eventListener.accept(ClientHelperImpl::registerParticle);
        });
    }

    private static <P extends ParticleType<T>, T extends ParticleOptions> void registerParticle(P type, ClientHelper.ParticleFactory<T> registration) {
        ParticleProviderRegistry.getInstance().register(type, registration::create);
    }

    public static void addEntityRenderersRegistration(Consumer<ClientHelper.EntityRendererEvent> eventListener) {
        Moonlight.assertInitPhase();

        MoonlightFabricClient.addClientTask(() -> {
            eventListener.accept(EntityRendererRegistry::register);
        });
    }

    public static void addBlockEntityRenderersRegistration(Consumer<ClientHelper.BlockEntityRendererEvent> eventListener) {
        Moonlight.assertInitPhase();

        MoonlightFabricClient.addClientTask(() -> {
            eventListener.accept(BlockEntityRenderers::register);
        });
    }

    public static void addBlockColorsRegistration(Consumer<ClientHelper.BlockColorEvent> eventListener) {
        Moonlight.assertInitPhase();

        MoonlightFabricClient.addClientTask(() -> {
            eventListener.accept(new ClientHelper.BlockColorEvent() {
                @Override
                public void register(List<BlockTintSource> tintSources, Block... blocks) {
                    BlockColorRegistry.register(tintSources, blocks);
                }

                @Override
                public int getColor(BlockState block, BlockAndTintGetter level, BlockPos pos, int tint) {
                    var source = Minecraft.getInstance().getBlockColors().getTintSource(block, tint);
                    return source == null ? -1 : source.colorInWorld(block, level, pos);
                }
            });
        });
    }

    public static void addClientReloadListener(Supplier<PreparableReloadListener> listener, Identifier name) {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(
                new ReloadWrapper(listener, name));
    }

    private record ReloadWrapper(Supplier<PreparableReloadListener> inner,
                                 Identifier getFabricId) implements IdentifiableResourceReloadListener, PreparableReloadListener {
        private ReloadWrapper(Supplier<PreparableReloadListener> inner,
                              Identifier getFabricId) {
            this.inner = Suppliers.memoize(inner::get);
            this.getFabricId = getFabricId;
        }

        @Override
        public CompletableFuture<Void> reload(SharedState currentReload, Executor taskExecutor,
                                              PreparationBarrier preparationBarrier, Executor reloadExecutor) {
            return inner.get().reload(currentReload, taskExecutor, preparationBarrier, reloadExecutor);
        }
    }

    public static final Map<ItemLike, IItemDecoratorRenderer> ITEM_DECORATORS = new IdentityHashMap<>();

    public static void addItemDecoratorsRegistration(Consumer<ClientHelper.ItemDecoratorEvent> eventListener) {
        Moonlight.assertInitPhase();

        MoonlightFabricClient.addClientTask(() -> {
            eventListener.accept(ITEM_DECORATORS::put);
        });
    }


    public static void addModelLayerRegistration(Consumer<ClientHelper.ModelLayerEvent> eventListener) {
        Moonlight.assertInitPhase();

        MoonlightFabricClient.addClientTask(() -> {
            eventListener.accept((a, b) -> ModelLayerRegistry.registerModelLayer(a, b::get));
        });
    }

    public static void addTooltipComponentRegistration(Consumer<ClientHelper.TooltipComponentEvent> eventListener) {
        Moonlight.assertInitPhase();

        MoonlightFabricClient.addClientTask(() -> {
            eventListener.accept(ClientHelperImpl::tooltipReg);
        });
    }

    private static <T extends TooltipComponent> void tooltipReg(Class<T> tClass, Function<? super T, ? extends ClientTooltipComponent> factory) {
        ClientTooltipComponentCallback.EVENT.register(data -> tClass.isAssignableFrom(data.getClass()) ? factory.apply((T) data) : null);
    }


    public static void addFluidModelRegistration(Consumer<ClientHelper.FluidModelEvent> eventListener) {
        Moonlight.assertInitPhase();

        MoonlightFabricClient.addClientTask(() -> {
            eventListener.accept((model, still, flowing) -> FluidRenderingRegistry.register(still, flowing, model));
        });
    }

    public static void addBlockModelRegistration(Consumer<ClientHelper.BlockModelEvent> eventListener) {
        Moonlight.assertInitPhase();

        MoonlightFabricClient.addClientTask(() -> {
            eventListener.accept((id, codec) ->
                    CustomUnbakedBlockStateModel.register(id, CustomUnbakedModelWrapper.wrap(codec)));
        });
    }

    // fabric has no level aware collectParts, this is only the fallback for plain quad access
    public static void collectModelParts(BlockStateModel model, @Nullable BlockAndTintGetter level,
                                         @Nullable BlockPos pos, @Nullable BlockState state,
                                         RandomSource random, List<BlockStateModelPart> parts) {
        model.collectParts(random, parts);
    }

    public static void addKeyBindRegistration(Consumer<ClientHelper.KeyBindEvent> eventListener) {
        Moonlight.assertInitPhase();

        MoonlightFabricClient.addClientTask(() -> {
            eventListener.accept(KeyMappingHelper::registerKeyMapping);
        });
    }



    public static Path getModIcon(String modId) {
        var container = FabricLoader.getInstance().getModContainer(modId).orElseThrow();
        return container.getMetadata().getIconPath(512).flatMap(container::findPath).orElse(null);
    }

    public static Screen getModConfigScreen(String modId, Screen parent) {
        // Mod Menu is optional: only touch its classes when it is loaded
        if (PlatHelper.isModLoaded("modmenu")) {
            return ModMenuCompat.getModConfigScreen(modId, parent);
        }
        return null;
    }

    public static boolean hasModConfigScreen(String modId) {
        return PlatHelper.isModLoaded("modmenu") && ModMenuCompat.hasModConfigScreen(modId);
    }

    @Nullable
    public static Screen getNativeForeignConfigScreen(String modId, Screen parent, @Nullable Identifier background) {
        // no universal config format on Fabric to convert; callers fall back to the mod's own (Mod Menu) screen
        return null;
    }

    public static boolean hasNativeForeignConfig(String modId) {
        return false;
    }

    public static boolean hasOnlyGenericConfigScreen(String modId) {
        return false;
    }

    public static void addClientSetup(Runnable clientSetup) {
        Moonlight.assertInitPhase();

        MoonlightFabricClient.addClientTaskLate(clientSetup);
    }


    public static void addClientSetupAsync(Runnable clientSetup) {
        addClientSetup(clientSetup);
    }

    public static void addClientLoginCallback(Runnable callback) {
        Moonlight.assertInitPhase();

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> callback.run());
    }


    public static void registerOptionalTexturePack(Identifier folderName, Component displayName, boolean defaultEnabled) {
        Moonlight.assertInitPhase();
        if (!PlatHelper.isDev()) {
            FabricLoader.getInstance().getModContainer(folderName.getNamespace()).ifPresent(c -> {
                ResourceManagerHelper.registerBuiltinResourcePack(folderName, c, displayName,
                        defaultEnabled ? ResourcePackActivationType.DEFAULT_ENABLED : ResourcePackActivationType.NORMAL);
            });
        }
    }

    public static void addRenderPipelineRegistration(Consumer<ClientHelper.RenderPipelineEvent> eventListener) {
        Moonlight.assertInitPhase();
        //no fabric event for this. registering just precompiles the pipeline on reload
        MoonlightFabricClient.addClientTask(() -> eventListener.accept(pipeline ->
                RenderPipelinesAccessor.getPIPELINES_BY_LOCATION().putIfAbsent(pipeline.getLocation(), pipeline)));
    }

    public static void addPictureInPictureRendererRegistration(Consumer<ClientHelper.PictureInPictureEvent> eventListener) {
        Moonlight.assertInitPhase();
        //fabric keys the renderer off getRenderStateClass, the class here is redundant
        MoonlightFabricClient.addClientTask(() -> eventListener.accept(new ClientHelper.PictureInPictureEvent() {
            @Override
            public <T extends PictureInPictureRenderState> void register(
                    Class<T> stateClass, Function<MultiBufferSource.BufferSource, PictureInPictureRenderer<T>> factory) {
                PictureInPictureRendererRegistry.register(context -> factory.apply(context.bufferSource()));
            }
        }));
    }

    public static void addSpecialModelRegistration(Consumer<ClientHelper.SpecialModelEvent> eventListener) {
        MoonlightFabricClient.addClientTask(() -> eventListener.accept(SpecialModelRenderers.ID_MAPPER::put));
    }

    public static void addMenuScreensRegistration(Consumer<ClientHelper.MenuScreenEvent> eventListener) {
        MoonlightFabricClient.addClientTask(() -> {
            eventListener.accept(MenuScreens::register);
        });
    }

}
