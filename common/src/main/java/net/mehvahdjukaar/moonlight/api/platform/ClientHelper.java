package net.mehvahdjukaar.moonlight.api.platform;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonElement;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.mehvahdjukaar.candlelight.api.PlatformImpl;
import net.mehvahdjukaar.moonlight.api.client.CoreShaderContainer;
import net.mehvahdjukaar.moonlight.api.client.ItemRenderExtension;
import net.mehvahdjukaar.moonlight.api.client.ItemStackRenderer;
import net.mehvahdjukaar.moonlight.api.client.model.CustomBakedModel;
import net.mehvahdjukaar.moonlight.api.client.model.CustomModelLoader;
import net.mehvahdjukaar.moonlight.api.item.IItemDecoratorRenderer;
import net.mehvahdjukaar.moonlight.api.resources.assets.LangBuilder;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
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

    @SuppressWarnings("all")
    public static Player getLocalPlayer() {
        //dont inline otherwise verified will shit itself
        var player = Minecraft.getInstance().player;
        return (Player) (Object) player;
    }

    @SuppressWarnings("all")
    public static Level getLocalLevel() {
        var level = Minecraft.getInstance().level;
        return (Level) (Object) level;
    }

    @FunctionalInterface
    public interface MenuScreenEvent {

        <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void register(
                MenuType<? extends M> type, MenuScreens.ScreenConstructor<M, U> factory);
    }

    @PlatformImpl
    public static void addMenuScreensRegistration(Consumer<MenuScreenEvent> eventListener) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static void addClientSetup(Runnable clientSetup) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static void addClientSetupAsync(Runnable clientSetup) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static void registerRenderType(Block block, RenderType... types) {
        throw new AssertionError();
    }

    public static void registerRenderType(Block block, RenderType type) {
        registerRenderType(block, new RenderType[]{type});
    }

    @PlatformImpl
    public static void registerFluidRenderType(Fluid fluid, RenderType type) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static void addClientReloadListener(Supplier<PreparableReloadListener> listener, ResourceLocation location) {
        throw new AssertionError();
    }

    @FunctionalInterface
    public interface ParticleFactory<T extends ParticleOptions> {
        @NotNull ParticleProvider<T> create(SpriteSet spriteSet);
    }

    @FunctionalInterface
    public interface ParticleEvent {
        <P extends ParticleType<T>, T extends ParticleOptions> void register(P particleType, ParticleFactory<T> factory);
    }

    @PlatformImpl
    public static void addParticleRegistration(Consumer<ParticleEvent> eventListener) {
        throw new AssertionError();
    }

    @FunctionalInterface
    public interface ItemDecoratorEvent {
        void register(ItemLike itemLike, IItemDecoratorRenderer renderer);
    }

    public interface ShaderEvent {
        void register(ResourceLocation id, VertexFormat vertexFormat, Consumer<ShaderInstance> setter);

        default void register(ResourceLocation id, VertexFormat vertexFormat, CoreShaderContainer container) {
            register(id, vertexFormat, container::assign);
        }
    }

    @PlatformImpl
    public static void addShaderRegistration(Consumer<ShaderEvent> eventListener) {
        throw new AssertionError();
    }

    @FunctionalInterface
    public interface ItemRendererEvent {
        default void register(ItemLike item, ItemStackRenderer renderer) {
            register(item, new ItemRenderExtension() {
                @Override
                public @Nullable ItemStackRenderer getItemRenderer() {
                    return renderer;
                }
            });
        }

        void register(ItemLike item, ItemRenderExtension extension);
    }

    @PlatformImpl
    public static void addItemRenderersRegistration(Consumer<ItemRendererEvent> eventListener) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static void addItemDecoratorsRegistration(Consumer<ItemDecoratorEvent> eventListener) {
        throw new AssertionError();
    }

    @FunctionalInterface
    public interface EntityRendererEvent {
        <E extends Entity> void register(EntityType<? extends E> entity, EntityRendererProvider<E> renderer);
    }

    @PlatformImpl
    public static void addEntityRenderersRegistration(Consumer<EntityRendererEvent> eventListener) {
        throw new AssertionError();
    }

    @FunctionalInterface
    public interface BlockEntityRendererEvent {
        <E extends BlockEntity> void register(BlockEntityType<? extends E> blockEntity, BlockEntityRendererProvider<E> renderer);
    }

    @PlatformImpl
    public static void addBlockEntityRenderersRegistration(Consumer<BlockEntityRendererEvent> eventListener) {
        throw new AssertionError();
    }

    public interface BlockColorEvent {
        void register(BlockColor color, Block... block);

        int getColor(BlockState block, BlockAndTintGetter level, BlockPos pos, int tint);

    }

    @PlatformImpl
    public static void addBlockColorsRegistration(Consumer<BlockColorEvent> eventListener) {
        throw new AssertionError();
    }

    public interface ItemColorEvent {
        void register(ItemColor color, ItemLike... items);

        int getColor(ItemStack stack, int tint);

    }

    @PlatformImpl
    public static void addItemColorsRegistration(Consumer<ItemColorEvent> eventListener) {
        throw new AssertionError();
    }

    @FunctionalInterface
    public interface ModelLayerEvent {
        void register(ModelLayerLocation modelLayer, Supplier<LayerDefinition> provider);
    }

    @PlatformImpl
    public static void addModelLayerRegistration(Consumer<ModelLayerEvent> eventListener) {
        throw new AssertionError();
    }

    public interface SpecialModelEvent {
        void register(ModelResourceLocation modelLocation);

        void register(ResourceLocation id);
    }

    //Use the "special_models" folder instead since that's auto loaded
    @Deprecated
    @PlatformImpl
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

    @PlatformImpl
    public static void addModelLoaderRegistration(Consumer<ModelLoaderEvent> eventListener) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static BakedModel getModel(ModelManager modelManager, ModelResourceLocation modelLocation) {
        throw new AssertionError();
    }


    @FunctionalInterface
    public interface TooltipComponentEvent {
        <T extends TooltipComponent> void register(Class<T> type, Function<? super T, ? extends ClientTooltipComponent> factory);
    }

    @PlatformImpl
    public static void addTooltipComponentRegistration(Consumer<TooltipComponentEvent> eventListener) {
        throw new AssertionError();
    }

    @FunctionalInterface
    public interface KeyBindEvent {
        void register(KeyMapping keyMapping);
    }

    @PlatformImpl
    public static void addKeyBindRegistration(Consumer<KeyBindEvent> eventListener) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static int getPixelRGBA(TextureAtlasSprite sprite, int frameIndex, int x, int y) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static BlockModel parseBlockModel(JsonElement json) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static Path getModIcon(String modId) {
        throw new AssertionError();
    }

    /**
     * Opens the config screen a mod registered with the loader itself (NeoForge's screen extension, or Mod Menu
     * on Fabric) rather than through Moonlight's config system. Returns null when that mod exposes no such screen
     * (which on Fabric includes the case where Mod Menu is not installed).
     */
    @PlatformImpl
    @Nullable
    public static Screen getModConfigScreen(String modId, Screen parent) {
        throw new AssertionError();
    }

    /** Whether {@link #getModConfigScreen} would return a screen for this mod. */
    @PlatformImpl
    public static boolean hasModConfigScreen(String modId) {
        throw new AssertionError();
    }

    /**
     * Builds a Moonlight-native config screen for a mod that does <em>not</em> use Moonlight's config system, by
     * reading the config the mod registered with the loader directly. Only NeoForge can do this (its configs share one
     * {@code ModConfigSpec} format); Fabric always returns null. Also returns null when the mod has no readable config,
     * so callers should fall back to {@link #getModConfigScreen}.
     */
    @PlatformImpl
    @Nullable
    public static Screen getNativeForeignConfigScreen(String modId, Screen parent, @Nullable ResourceLocation background) {
        throw new AssertionError();
    }

    /**
     * Whether {@link #getNativeForeignConfigScreen} would produce a screen for this mod (a readable config it didn't
     * register through Moonlight). Cheaper than building the screen; used to decide whether to show the mod a tile.
     * Always false on Fabric.
     */
    @PlatformImpl
    public static boolean hasNativeForeignConfig(String modId) {
        throw new AssertionError();
    }

    /**
     * Pack in /resources/resourcepacks
     */
    @PlatformImpl
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