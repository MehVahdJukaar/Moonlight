package net.mehvahdjukaar.moonlight.api.platform.forge;

import com.google.gson.JsonElement;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.platform.ClientPlatformHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.model.ExtendedBlockModelDeserializer;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.locating.IModFile;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class ClientPlatformHelperImpl {

    public static void registerRenderType(Block block, RenderType type) {
        //from 0.64 we should register render types in out model jsons
        //TODO: remove
        ItemBlockRenderTypes.setRenderLayer(block, type);
    }

    public static void registerItemProperty(Item item, ResourceLocation name, ClampedItemPropertyFunction property) {
        ItemProperties.register(item, name, property);
    }

    public static void addParticleRegistration(Consumer<ClientPlatformHelper.ParticleEvent> eventListener) {
        Consumer<RegisterParticleProvidersEvent> eventConsumer = event -> {
            W w = new W(event);
            eventListener.accept(w::register);
        };
        FMLJavaModLoadingContext.get().getModEventBus().addListener(eventConsumer);
    }

    private record W(RegisterParticleProvidersEvent event) {
        public <T extends ParticleOptions> void register(ParticleType<T> type, ClientPlatformHelper.ParticleFactory<T> provider) {
            this.event.register(type, provider::create);
        }
    }

    public static void addEntityRenderersRegistration(Consumer<ClientPlatformHelper.EntityRendererEvent> eventListener) {
        Consumer<EntityRenderersEvent.RegisterRenderers> eventConsumer = event ->
                eventListener.accept(event::registerEntityRenderer);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(eventConsumer);
    }

    public static void addBlockEntityRenderersRegistration(Consumer<ClientPlatformHelper.BlockEntityRendererEvent> eventListener) {
        Consumer<EntityRenderersEvent.RegisterRenderers> eventConsumer = event ->
                eventListener.accept(event::registerBlockEntityRenderer);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(eventConsumer);
    }

    public static void addBlockColorsRegistration(Consumer<ClientPlatformHelper.BlockColorEvent> eventListener) {
        Consumer<RegisterColorHandlersEvent.Block> eventConsumer = event -> {
            eventListener.accept(new ClientPlatformHelper.BlockColorEvent() {
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

    public static void addItemColorsRegistration(Consumer<ClientPlatformHelper.ItemColorEvent> eventListener) {
        Consumer<RegisterColorHandlersEvent.Item> eventConsumer = event -> {
            eventListener.accept(new ClientPlatformHelper.ItemColorEvent() {
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

    public static void addAtlasTextureCallback(ResourceLocation atlasLocation, Consumer<ClientPlatformHelper.AtlasTextureEvent> eventListener) {
        Consumer<TextureStitchEvent.Pre> eventConsumer = event -> {
            if (event.getAtlas().location().equals(atlasLocation)) {
                eventListener.accept(event::addSprite);
            }
        };
        FMLJavaModLoadingContext.get().getModEventBus().addListener(eventConsumer);
    }

    public static void addClientReloadListener(PreparableReloadListener listener, ResourceLocation location) {
        ((ReloadableResourceManager) Minecraft.getInstance().getResourceManager())
                .registerReloadListener(listener);
    }

    public static void addModelLayerRegistration(Consumer<ClientPlatformHelper.ModelLayerEvent> eventListener) {
        Consumer<EntityRenderersEvent.RegisterLayerDefinitions> eventConsumer = event -> {
            eventListener.accept(event::registerLayerDefinition);
        };
        FMLJavaModLoadingContext.get().getModEventBus().addListener(eventConsumer);
    }

    public static void addSpecialModelRegistration(Consumer<ClientPlatformHelper.SpecialModelEvent> eventListener) {
        Consumer<ModelEvent.RegisterAdditional> eventConsumer = event -> {
            eventListener.accept(event::register);
        };
        FMLJavaModLoadingContext.get().getModEventBus().addListener(eventConsumer);
    }

    public static void addTooltipComponentRegistration(Consumer<ClientPlatformHelper.TooltipComponentEvent> eventListener) {
        Consumer<RegisterClientTooltipComponentFactoriesEvent> eventConsumer = event -> {
            eventListener.accept(event::register);
        };
        FMLJavaModLoadingContext.get().getModEventBus().addListener(eventConsumer);
    }

    public static void addModelLoaderRegistration(Consumer<ClientPlatformHelper.ModelLoaderEvent> eventListener) {
        Consumer<ModelEvent.RegisterGeometryLoaders> eventConsumer = event -> {
            eventListener.accept((i, l) -> event.register(i.getPath(), (IGeometryLoader<?>) l));
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


}
