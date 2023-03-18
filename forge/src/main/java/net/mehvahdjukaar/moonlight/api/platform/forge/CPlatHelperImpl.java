package net.mehvahdjukaar.moonlight.api.platform.forge;

import com.google.gson.JsonElement;
import net.mehvahdjukaar.moonlight.api.platform.CPlatHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.model.ExtendedBlockModelDeserializer;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.data.loading.DatagenModLoader;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.resource.PathPackResources;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Consumer;

public class CPlatHelperImpl {

    public static void registerRenderType(Block block, RenderType type) {
        //from 0.64 we should register render types in out model json
        ItemBlockRenderTypes.setRenderLayer(block, type);
    }

    public static void registerFluidRenderType(Fluid fluid, RenderType type) {
        ItemBlockRenderTypes.setRenderLayer(fluid, type);
    }

    public static void registerItemProperty(Item item, ResourceLocation name, ClampedItemPropertyFunction property) {
        ItemProperties.register(item, name, property);
    }

    public static void addParticleRegistration(Consumer<CPlatHelper.ParticleEvent> eventListener) {
        Consumer<RegisterParticleProvidersEvent> eventConsumer = event -> {
            W w = new W(event);
            eventListener.accept(w::register);
        };
        FMLJavaModLoadingContext.get().getModEventBus().addListener(eventConsumer);
    }

    private record W(RegisterParticleProvidersEvent event) {
        public <T extends ParticleOptions> void register(ParticleType<T> type, CPlatHelper.ParticleFactory<T> provider) {
            this.event.register(type, provider::create);
        }
    }

    public static void addEntityRenderersRegistration(Consumer<CPlatHelper.EntityRendererEvent> eventListener) {
        Consumer<EntityRenderersEvent.RegisterRenderers> eventConsumer = event ->
                eventListener.accept(event::registerEntityRenderer);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(eventConsumer);
    }

    public static void addBlockEntityRenderersRegistration(Consumer<CPlatHelper.BlockEntityRendererEvent> eventListener) {
        Consumer<EntityRenderersEvent.RegisterRenderers> eventConsumer = event ->
                eventListener.accept(event::registerBlockEntityRenderer);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(eventConsumer);
    }

    public static void addBlockColorsRegistration(Consumer<CPlatHelper.BlockColorEvent> eventListener) {
        Consumer<RegisterColorHandlersEvent.Block> eventConsumer = event -> {
            eventListener.accept(new CPlatHelper.BlockColorEvent() {
                @Override
                public void register(BlockColor color, Block... block) {
                    event.register(color, block);
                }

                @Override
                public int getColor(BlockState block, BlockAndTintGetter level, BlockPos pos, int tint) {
                    return event.getBlockColors().getColor(block, level, pos, tint);
                }
            });
        };
        FMLJavaModLoadingContext.get().getModEventBus().addListener(eventConsumer);
    }

    public static void addItemColorsRegistration(Consumer<CPlatHelper.ItemColorEvent> eventListener) {
        Consumer<RegisterColorHandlersEvent.Item> eventConsumer = event -> {
            eventListener.accept(new CPlatHelper.ItemColorEvent() {
                @Override
                public void register(ItemColor color, ItemLike... items) {
                    event.register(color, items);
                }

                @Override
                public int getColor(ItemStack stack, int tint) {
                    return event.getItemColors().getColor(stack, tint);
                }
            });
        };
        FMLJavaModLoadingContext.get().getModEventBus().addListener(eventConsumer);
    }

    public static void addAtlasTextureCallback(ResourceLocation atlasLocation, Consumer<CPlatHelper.AtlasTextureEvent> eventListener) {
        Consumer<TextureStitchEvent.Pre> eventConsumer = event -> {
            if (event.getAtlas().location().equals(atlasLocation)) {
                eventListener.accept(event::addSprite);
            }
        };
        FMLJavaModLoadingContext.get().getModEventBus().addListener(eventConsumer);
    }

    @SuppressWarnings("ConstantConditions")
    public static void addClientReloadListener(PreparableReloadListener listener, ResourceLocation location) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) { // datagens
            ((ReloadableResourceManager) mc.getResourceManager())
                    .registerReloadListener(listener);
        }
    }

    public static void addModelLayerRegistration(Consumer<CPlatHelper.ModelLayerEvent> eventListener) {
        Consumer<EntityRenderersEvent.RegisterLayerDefinitions> eventConsumer = event -> {
            eventListener.accept(event::registerLayerDefinition);
        };
        FMLJavaModLoadingContext.get().getModEventBus().addListener(eventConsumer);
    }

    public static void addSpecialModelRegistration(Consumer<CPlatHelper.SpecialModelEvent> eventListener) {
        Consumer<ModelEvent.RegisterAdditional> eventConsumer = event -> {
            eventListener.accept(event::register);
        };
        FMLJavaModLoadingContext.get().getModEventBus().addListener(eventConsumer);
    }

    public static void addTooltipComponentRegistration(Consumer<CPlatHelper.TooltipComponentEvent> eventListener) {
        Consumer<RegisterClientTooltipComponentFactoriesEvent> eventConsumer = event -> {
            eventListener.accept(event::register);
        };
        FMLJavaModLoadingContext.get().getModEventBus().addListener(eventConsumer);
    }

    public static void addModelLoaderRegistration(Consumer<CPlatHelper.ModelLoaderEvent> eventListener) {
        Consumer<ModelEvent.RegisterGeometryLoaders> eventConsumer = event -> {
            eventListener.accept((i, l) -> event.register(i.getPath(), (IGeometryLoader<?>) l));
        };
        FMLJavaModLoadingContext.get().getModEventBus().addListener(eventConsumer);
    }

    public static void addItemDecoratorsRegistration(Consumer<CPlatHelper.ItemDecoratorEvent> eventListener) {
        Consumer<RegisterItemDecorationsEvent> eventConsumer = event -> {
            eventListener.accept((i, l) -> event.register(i, l::render));
        };
        FMLJavaModLoadingContext.get().getModEventBus().addListener(eventConsumer);
    }

    public static void addKeyBindRegistration(Consumer<CPlatHelper.KeyBindEvent> eventListener) {
        Consumer<RegisterKeyMappingsEvent> eventConsumer = event -> {
            eventListener.accept(event::register);
        };
        FMLJavaModLoadingContext.get().getModEventBus().addListener(eventConsumer);
    }


    public static int getPixelRGBA(TextureAtlasSprite sprite, int frameIndex, int x, int y) {
        return sprite.getPixelRGBA(frameIndex, x, y);
    }

    public static BakedModel getModel(ModelManager modelManager, ResourceLocation modelLocation) {
        return modelManager.getModel(modelLocation);
    }

    @Nullable
    public static Path getModIcon(String modId) {
        var m = ModList.get().getModContainerById(modId);
        if (m.isPresent()) {
            IModInfo mod = m.get().getModInfo();
            IModFile file = mod.getOwningFile().getFile();

            var logo = mod.getLogoFile().orElse(null);
            if (logo != null && file != null) {
                Path logoPath = file.findResource(logo);
                if (Files.exists(logoPath)) {
                    return logoPath;
                }
            }
        }
        return null;
    }

    public static BlockModel parseBlockModel(JsonElement json) {
        return ExtendedBlockModelDeserializer.INSTANCE.getAdapter(BlockModel.class).fromJsonTree(json);
    }

    public static void addClientSetup(Runnable clientSetup) {
        Consumer<FMLClientSetupEvent> eventConsumer = event -> event.enqueueWork(clientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(eventConsumer);
    }

    public static void registerOptionalTexturePack(ResourceLocation folderName, String displayName, boolean defaultEnabled) {
        Consumer<AddPackFindersEvent> eventConsumer = e -> {
            if (e.getPackType() == PackType.CLIENT_RESOURCES) {
                e.addRepositorySource((consumer, constructor) -> {
                    IModFile file = ModList.get().getModFileById(folderName.getNamespace()).getFile();
                    try (PathPackResources pack = new PathPackResources(
                            folderName.toString(),
                            file.findResource("resourcepacks/" + folderName.getPath()));) {

                        consumer.accept(constructor.create(
                                folderName.toString(),
                                Component.literal(displayName),
                                defaultEnabled,
                                () -> pack,
                                Objects.requireNonNull(pack.getMetadataSection(PackMetadataSection.SERIALIZER)),
                                Pack.Position.TOP,
                                PackSource.BUILT_IN,
                                false));

                    } catch (IOException ee) {
                        if (!DatagenModLoader.isRunningDataGen()) ee.printStackTrace();
                    }
                });
            }
        };
        FMLJavaModLoadingContext.get().getModEventBus().addListener(eventConsumer);
    }


}
