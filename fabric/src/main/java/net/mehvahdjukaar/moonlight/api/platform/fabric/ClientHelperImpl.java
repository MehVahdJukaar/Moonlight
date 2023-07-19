package net.mehvahdjukaar.moonlight.api.platform.fabric;

import com.google.common.base.Suppliers;
import com.google.gson.JsonElement;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.mehvahdjukaar.moonlight.api.client.model.fabric.MLFabricModelLoaderRegistry;
import net.mehvahdjukaar.moonlight.api.item.IItemDecoratorRenderer;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.misc.fabric.ITextureAtlasSpriteExtension;
import net.mehvahdjukaar.moonlight.core.mixins.fabric.ModelManagerAccessor;
import net.mehvahdjukaar.moonlight.fabric.MoonlightFabricClient;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

import java.nio.file.Path;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ClientHelperImpl {

    public static void registerRenderType(Block block, RenderType... type) {
        BlockRenderLayerMap.INSTANCE.putBlock(block, type[0]);
    }

    public static void addParticleRegistration(Consumer<ClientHelper.ParticleEvent> eventListener) {
        Moonlight.assertInitPhase();
        MoonlightFabricClient.PRE_CLIENT_SETUP_WORK.add(() -> {
            eventListener.accept(ClientHelperImpl::registerParticle);
        });
    }

    private static <P extends ParticleType<T>, T extends ParticleOptions> void registerParticle(P type, ClientHelper.ParticleFactory<T> registration) {
        ParticleFactoryRegistry.getInstance().register(type, registration::create);
    }

    public static void addEntityRenderersRegistration(Consumer<ClientHelper.EntityRendererEvent> eventListener) {
        Moonlight.assertInitPhase();

        MoonlightFabricClient.PRE_CLIENT_SETUP_WORK.add(() -> {
            eventListener.accept(EntityRendererRegistry::register);
        });
    }

    public static void addBlockEntityRenderersRegistration(Consumer<ClientHelper.BlockEntityRendererEvent> eventListener) {
        Moonlight.assertInitPhase();

        MoonlightFabricClient.PRE_CLIENT_SETUP_WORK.add(() -> {
            eventListener.accept(BlockEntityRenderers::register);
        });
    }

    public static void addBlockColorsRegistration(Consumer<ClientHelper.BlockColorEvent> eventListener) {
        Moonlight.assertInitPhase();

        MoonlightFabricClient.PRE_CLIENT_SETUP_WORK.add(() -> {
            eventListener.accept(new ClientHelper.BlockColorEvent() {
                @Override
                public void register(BlockColor color, Block... block) {
                    ColorProviderRegistry.BLOCK.register(color, block);
                }

                @Override
                public int getColor(BlockState block, BlockAndTintGetter level, BlockPos pos, int tint) {
                    var c = ColorProviderRegistry.BLOCK.get(block.getBlock());
                    return c == null ? -1 : c.getColor(block, level, pos, tint);
                }
            });
        });
    }

    public static void addItemColorsRegistration(Consumer<ClientHelper.ItemColorEvent> eventListener) {
        Moonlight.assertInitPhase();

        MoonlightFabricClient.PRE_CLIENT_SETUP_WORK.add(() -> {
            eventListener.accept(new ClientHelper.ItemColorEvent() {
                @Override
                public void register(ItemColor color, ItemLike... items) {
                    ColorProviderRegistry.ITEM.register(color, items);
                }

                @Override
                public int getColor(ItemStack stack, int tint) {
                    var c = ColorProviderRegistry.ITEM.get(stack.getItem());
                    return c == null ? -1 : c.getColor(stack, tint);
                }
            });
        });
    }

    public static void addClientReloadListener(Supplier<PreparableReloadListener> listener, ResourceLocation name) {
        Moonlight.assertInitPhase();

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new IdentifiableResourceReloadListener() {
            private final Supplier<PreparableReloadListener> inner = Suppliers.memoize(listener::get);

            @Override
            public ResourceLocation getFabricId() {
                return name;
            }

            @Override
            public CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
                return inner.get().reload(preparationBarrier, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor);
            }
        });
    }

    public static final Map<ItemLike, IItemDecoratorRenderer> ITEM_DECORATORS = new IdentityHashMap<>();

    public static void addItemDecoratorsRegistration(Consumer<ClientHelper.ItemDecoratorEvent> eventListener) {
        Moonlight.assertInitPhase();

        MoonlightFabricClient.PRE_CLIENT_SETUP_WORK.add(() -> {
            eventListener.accept(ITEM_DECORATORS::put);
        });
    }


    public static void addModelLayerRegistration(Consumer<ClientHelper.ModelLayerEvent> eventListener) {
        Moonlight.assertInitPhase();

        MoonlightFabricClient.PRE_CLIENT_SETUP_WORK.add(() -> {
            eventListener.accept((a, b) -> EntityModelLayerRegistry.registerModelLayer(a, b::get));
        });
    }

    public static void addSpecialModelRegistration(Consumer<ClientHelper.SpecialModelEvent> eventListener) {
        Moonlight.assertInitPhase();

        MoonlightFabricClient.PRE_CLIENT_SETUP_WORK.add(() -> {
            ModelLoadingRegistry.INSTANCE.registerModelProvider((m, loader) -> eventListener.accept(loader::accept));
        });
    }

    public static void addTooltipComponentRegistration(Consumer<ClientHelper.TooltipComponentEvent> eventListener) {
        Moonlight.assertInitPhase();

        MoonlightFabricClient.PRE_CLIENT_SETUP_WORK.add(() -> {
            eventListener.accept(ClientHelperImpl::tooltipReg);
        });
    }

    private static <T extends TooltipComponent> void tooltipReg(Class<T> tClass, Function<? super T, ? extends ClientTooltipComponent> factory) {
        TooltipComponentCallback.EVENT.register(data -> tClass.isAssignableFrom(data.getClass()) ? factory.apply((T) data) : null);
    }


    public static void addModelLoaderRegistration(Consumer<ClientHelper.ModelLoaderEvent> eventListener) {
        Moonlight.assertInitPhase();

        MoonlightFabricClient.PRE_CLIENT_SETUP_WORK.add(() -> {
            eventListener.accept(MLFabricModelLoaderRegistry::registerLoader);
        });
    }

    public static void addKeyBindRegistration(Consumer<ClientHelper.KeyBindEvent> eventListener) {
        Moonlight.assertInitPhase();

        MoonlightFabricClient.PRE_CLIENT_SETUP_WORK.add(() -> {
            eventListener.accept(KeyBindingHelper::registerKeyBinding);
        });
    }


    public static int getPixelRGBA(TextureAtlasSprite sprite, int frameIndex, int x, int y) {
        return ((ITextureAtlasSpriteExtension) sprite).getPixelRGBA(frameIndex, x, y);
    }

    public static BakedModel getModel(ModelManager modelManager, ResourceLocation modelLocation) {
        return ((ModelManagerAccessor) modelManager).getBakedRegistry().getOrDefault(modelLocation, modelManager.getMissingModel());
    }


    public static Path getModIcon(String modId) {
        var container = FabricLoader.getInstance().getModContainer(modId).get();
        return container.getMetadata().getIconPath(512).flatMap(container::findPath).orElse(null);
    }

    public static BlockModel parseBlockModel(JsonElement json) {
        return BlockModel.fromString(json.toString()); //sub optimal... too bad
    }

    public static void addClientSetup(Runnable clientSetup) {
        Moonlight.assertInitPhase();

        MoonlightFabricClient.CLIENT_SETUP_WORK.add(clientSetup);
    }

    public static void registerFluidRenderType(Fluid fluid, RenderType type) {
        BlockRenderLayerMap.INSTANCE.putFluid(fluid, type);
    }

    public static void registerOptionalTexturePack(ResourceLocation folderName, Component displayName, boolean defaultEnabled) {
        Moonlight.assertInitPhase();

        FabricLoader.getInstance().getModContainer(folderName.getNamespace()).ifPresent(c -> {
            ResourceManagerHelper.registerBuiltinResourcePack(folderName, c, displayName,
                    defaultEnabled ? ResourcePackActivationType.DEFAULT_ENABLED : ResourcePackActivationType.NORMAL);
        });
    }

    public static UnbakedModel getUnbakedModel(ModelManager modelManager, ResourceLocation modelLocation) {
        return null;
    }

}
