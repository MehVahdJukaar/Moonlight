package net.mehvahdjukaar.moonlight.api.platform;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonElement;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mehvahdjukaar.moonlight.api.client.model.CustomBakedModel;
import net.mehvahdjukaar.moonlight.api.client.model.CustomModelLoader;
import net.mehvahdjukaar.moonlight.api.item.IItemDecoratorRenderer;
import net.mehvahdjukaar.moonlight.api.resources.assets.LangBuilder;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Helper class dedicated to platform independent client utility methods
 */
public class ClientHelper {

    @ExpectPlatform
    public static void addClientSetup(Runnable clientSetup) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void addClientSetupAsync(Runnable clientSetup) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void registerRenderType(Block block, RenderType... types) {
        throw new AssertionError();
    }

    public static void registerRenderType(Block block, RenderType type) {
        registerRenderType(block, new RenderType[]{type});
    }

    @ExpectPlatform
    public static void registerFluidRenderType(Fluid fluid, RenderType type) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void addClientReloadListener(Supplier<PreparableReloadListener> listener, ResourceLocation location) {
        throw new AssertionError();
    }

    @FunctionalInterface
    @Environment(EnvType.CLIENT)
    public interface ParticleFactory<T extends ParticleOptions> {
        @NotNull ParticleProvider<T> create(SpriteSet spriteSet);
    }

    @FunctionalInterface
    public interface ParticleEvent {
        <P extends ParticleType<T>, T extends ParticleOptions> void register(P particleType, ParticleFactory<T> factory);
    }

    @ExpectPlatform
    public static void addParticleRegistration(Consumer<ParticleEvent> eventListener) {
        throw new AssertionError();
    }

    @FunctionalInterface
    public interface ItemDecoratorEvent {
        void register(ItemLike itemLike, IItemDecoratorRenderer renderer);
    }

    @ExpectPlatform
    public static void addItemDecoratorsRegistration(Consumer<ItemDecoratorEvent> eventListener) {
        throw new AssertionError();
    }

    @FunctionalInterface
    public interface EntityRendererEvent {
        <E extends Entity> void register(EntityType<? extends E> entity, EntityRendererProvider<E> renderer);
    }

    @ExpectPlatform
    public static void addEntityRenderersRegistration(Consumer<EntityRendererEvent> eventListener) {
        throw new AssertionError();
    }

    @FunctionalInterface
    public interface BlockEntityRendererEvent {
        <E extends BlockEntity> void register(BlockEntityType<? extends E> blockEntity, BlockEntityRendererProvider<E> renderer);
    }

    @ExpectPlatform
    public static void addBlockEntityRenderersRegistration(Consumer<BlockEntityRendererEvent> eventListener) {
        throw new AssertionError();
    }

    public interface BlockColorEvent {
        void register(BlockColor color, Block... block);

        int getColor(BlockState block, BlockAndTintGetter level, BlockPos pos, int tint);

    }

    @ExpectPlatform
    public static void addBlockColorsRegistration(Consumer<BlockColorEvent> eventListener) {
        throw new AssertionError();
    }

    public interface ItemColorEvent {
        void register(ItemColor color, ItemLike... items);

        int getColor(ItemStack stack, int tint);

    }

    @ExpectPlatform
    public static void addItemColorsRegistration(Consumer<ItemColorEvent> eventListener) {
        throw new AssertionError();
    }

    @FunctionalInterface
    public interface ModelLayerEvent {
        void register(ModelLayerLocation modelLayer, Supplier<LayerDefinition> provider);
    }

    @ExpectPlatform
    public static void addModelLayerRegistration(Consumer<ModelLayerEvent> eventListener) {
        throw new AssertionError();
    }

    public interface SpecialModelEvent {
        void register(ModelResourceLocation modelLocation);

        void register(ResourceLocation id);
    }

    @ExpectPlatform
    public static void addSpecialModelRegistration(Consumer<SpecialModelEvent> eventListener) {
        throw new AssertionError();
    }

    @FunctionalInterface
    public interface ModelLoaderEvent {
        void register(ResourceLocation id, CustomModelLoader loader);

        default void register(ResourceLocation id, Supplier<CustomBakedModel> bakedModelFactory) {
            register(id, (CustomModelLoader) (json, context) -> (modelBaker, spriteGetter, transform) -> bakedModelFactory.get());
        }

        default void register(ResourceLocation id, BiFunction<ModelState, Function<Material, TextureAtlasSprite>, CustomBakedModel> bakedModelFactory) {
            register(id, (CustomModelLoader) (json, context) -> (modelBaker, spriteGetter, transform) -> bakedModelFactory.apply(transform, spriteGetter));
        }
    }

    @ExpectPlatform
    public static void addModelLoaderRegistration(Consumer<ModelLoaderEvent> eventListener) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static BakedModel getModel(ModelManager modelManager, ModelResourceLocation modelLocation) {
        throw new AssertionError();
    }


    @FunctionalInterface
    public interface TooltipComponentEvent {
        <T extends TooltipComponent> void register(Class<T> type, Function<? super T, ? extends ClientTooltipComponent> factory);
    }

    @ExpectPlatform
    public static void addTooltipComponentRegistration(Consumer<TooltipComponentEvent> eventListener) {
        throw new AssertionError();
    }

    @FunctionalInterface
    public interface KeyBindEvent {
        void register(KeyMapping keyMapping);
    }

    @ExpectPlatform
    public static void addKeyBindRegistration(Consumer<KeyBindEvent> eventListener) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static int getPixelRGBA(TextureAtlasSprite sprite, int frameIndex, int x, int y) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static BlockModel parseBlockModel(JsonElement json) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Path getModIcon(String modId) {
        throw new AssertionError();
    }

    /**
     * Pack in /resources/resourcepacks
     */
    @ExpectPlatform
    public static void registerOptionalTexturePack(ResourceLocation folderName, Component displayName, boolean defaultEnabled) {
        throw new AssertionError();
    }

    public static void registerOptionalTexturePack(ResourceLocation folderName, boolean defaultEnabled) {
        registerOptionalTexturePack(folderName, Component.literal(LangBuilder.getReadableName(folderName.getPath())), defaultEnabled);
    }


    private static final Cache<ResourceLocation, Material> CACHED_MATERIALS = CacheBuilder.newBuilder()
            .expireAfterAccess(2, TimeUnit.MINUTES)
            .build();

    //cached materials
    public static Material getBlockMaterial(ResourceLocation bockTexture) {
        try {
            return CACHED_MATERIALS.get(bockTexture, () -> new Material(TextureAtlas.LOCATION_BLOCKS, bockTexture));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}