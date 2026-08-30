package net.mehvahdjukaar.moonlight.api.platform;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.serialization.MapCodec;
import net.mehvahdjukaar.candlelight.api.PlatformImpl;

import net.mehvahdjukaar.moonlight.api.client.model.CustomUnbakedModel;
import net.mehvahdjukaar.moonlight.api.client.gui.IItemDecoratorRenderer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.mehvahdjukaar.moonlight.api.platform.configs.ModConfigHolder;
import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigCategory;
import net.mehvahdjukaar.moonlight.core.client.config.MoonlightConfigScreen;
import net.mehvahdjukaar.moonlight.core.client.config.MoonlightConfigSelectScreen;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.mehvahdjukaar.moonlight.api.util.TextHelper;
import net.mehvahdjukaar.moonlight.core.ClientConfigs;
import net.mehvahdjukaar.moonlight.core.client.config.ModsTilesScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.client.resources.model.*;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;
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

    /** Null until the local player joins a world. */
    @Nullable
    public static GameType getLocalGameMode() {
        var gameMode = Minecraft.getInstance().gameMode;
        return gameMode == null ? null : gameMode.getPlayerMode();
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
    public static void addClientLoginCallback(Runnable callback) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static void addClientReloadListener(Supplier<PreparableReloadListener> listener, Identifier location) {
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

    @FunctionalInterface
    public interface RenderPipelineEvent {
        void register(RenderPipeline pipeline);
    }

    @PlatformImpl
    public static void addRenderPipelineRegistration(Consumer<RenderPipelineEvent> eventListener) {
        throw new AssertionError();
    }

    @FunctionalInterface
    public interface PictureInPictureEvent {
        <T extends PictureInPictureRenderState> void register(
                Class<T> stateClass, Function<MultiBufferSource.BufferSource, PictureInPictureRenderer<T>> factory);
    }

    @PlatformImpl
    public static void addPictureInPictureRendererRegistration(Consumer<PictureInPictureEvent> eventListener) {
        throw new AssertionError();
    }

    @FunctionalInterface
    public interface SpecialModelEvent {
        void register(Identifier id, MapCodec<? extends SpecialModelRenderer.Unbaked<?>> codec);
    }

    /** Items opt in from their items/<id>.json definition with a minecraft:special model of the registered type. */
    @PlatformImpl
    public static void addSpecialModelRegistration(Consumer<SpecialModelEvent> eventListener) {
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
        <E extends BlockEntity, S extends BlockEntityRenderState> void register(BlockEntityType<? extends E> blockEntity, BlockEntityRendererProvider<E, S> renderer);
    }

    @PlatformImpl
    public static void addBlockEntityRenderersRegistration(Consumer<BlockEntityRendererEvent> eventListener) {
        throw new AssertionError();
    }

    public interface BlockColorEvent {
        /** A quad's tint index picks the source at that position. */
        void register(List<BlockTintSource> tintSources, Block... blocks);

        int getColor(BlockState block, BlockAndTintGetter level, BlockPos pos, int tint);

    }

    @PlatformImpl
    public static void addBlockColorsRegistration(Consumer<BlockColorEvent> eventListener) {
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

    @FunctionalInterface
    public interface FluidModelEvent {
        void register(FluidModel.Unbaked model, Fluid still, Fluid flowing);
    }

    @PlatformImpl
    public static void addFluidModelRegistration(Consumer<FluidModelEvent> eventListener) {
        throw new AssertionError();
    }

    @FunctionalInterface
    public interface BlockModelEvent {
        void register(Identifier id, MapCodec<? extends CustomUnbakedModel> codec);
    }

    /** Custom model types usable in blockstates files, see CustomUnbakedModel. */
    @PlatformImpl
    public static void addBlockModelRegistration(Consumer<BlockModelEvent> eventListener) {
        throw new AssertionError();
    }

    /** Like BlockStateModel.collectParts but passes the level along so nested level aware models resolve. */
    @PlatformImpl
    public static void collectModelParts(BlockStateModel model, @Nullable BlockAndTintGetter level,
                                         @Nullable BlockPos pos, @Nullable BlockState state,
                                         RandomSource random, List<BlockStateModelPart> parts) {
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

    /** 0xAABBGGRR. Frames are stacked vertically. */
    public static int getPixelABGR(TextureAtlasSprite sprite, int frameIndex, int x, int y) {
        SpriteContents contents = sprite.contents();
        return contents.originalImage.getPixelABGR(x, y + frameIndex * contents.height());
    }

    @PlatformImpl
    public static Path getModIcon(String modId) {
        throw new AssertionError();
    }

    /**
     * Builds the Moonlight native screen for a single config file. When the player turned Moonlight's config screens
     * off this hands over to whatever screen the loader registered for that mod (NeoForge's extension point, or Mod
     * Menu on Fabric). Null when neither is available or the config isn't loaded yet.
     */
    @Nullable
    public static Screen makeConfigScreen(ModConfigHolder holder, Screen parent, @Nullable Identifier background) {
        if (!holder.isLoaded()) return null;
        if (!ClientConfigs.CUSTOM_CONFIG_SCREEN.get()) {
            return getModConfigScreen(holder.getModId(), parent);
        }
        ConfigCategory root = holder.getConfigRoot();
        return root == null ? null : MoonlightConfigScreen.create(holder, root, parent, background);
    }

    @Nullable
    public static Screen getMoonlightConfigScreen(String modId, Screen parent, @Nullable Identifier background) {
        return MoonlightConfigSelectScreen.create(modId, parent, background);
    }

    /**
     * Moonlight's mod hub: a grid with a tile for every installed mod that has a config screen. Returns null when the
     * player turned Moonlight's own config screens off, in which case there is no hub to show.
     */
    @Nullable
    public static Screen getModsListScreen(@Nullable Screen parent, @Nullable Identifier background) {
        if (!ClientConfigs.CUSTOM_CONFIG_SCREEN.get()) return null;
        return new ModsTilesScreen(parent, background);
    }

    /**
     * Opens the config screen a mod registered with the loader itself (NeoForge's screen extension, or Mod Menu
     * on Fabric) rather than through Moonlight's config system. Returns null when that mod exposes no such screen
     * (which on Fabric includes the case where Mod Menu is not installed).
     */
    @Contract
    @PlatformImpl
    @Nullable
    public static Screen getModConfigScreen(String modId, Screen parent) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static boolean hasModConfigScreen(String modId) {
        throw new AssertionError();
    }

    /**
     * Builds a Moonlight-native config screen for a mod that does <em>not</em> use Moonlight's config system, by
     * reading the config the mod registered with the loader directly. Only NeoForge can do this (its configs share one
     * ModConfigSpec format); Fabric always returns null. Also returns null when the mod has no readable config,
     * so callers should fall back to getModConfigScreen.
     */
    @PlatformImpl
    @Nullable
    public static Screen getNativeForeignConfigScreen(String modId, Screen parent, @Nullable Identifier background) {
        throw new AssertionError();
    }

    /**
     * Whether getNativeForeignConfigScreen would produce a screen for this mod (a readable config it didn't
     * register through Moonlight). Cheaper than building the screen; used to decide whether to show the mod a tile.
     * Always false on Fabric.
     */
    @PlatformImpl
    public static boolean hasNativeForeignConfig(String modId) {
        throw new AssertionError();
    }

    /**
     * Whether the screen this mod registered is a stock one it got for free (NeoForge's ConfigurationScreen, or
     * Configured's), or none at all. Either way the mod wrote no screen of its own, so converting its config costs
     * nothing. Always false on Fabric.
     */
    @PlatformImpl
    public static boolean hasOnlyGenericConfigScreen(String modId) {
        throw new AssertionError();
    }

    /**
     * Pack in /resources/resourcepacks
     */
    @PlatformImpl
    public static void registerOptionalTexturePack(Identifier folderName, Component displayName, boolean defaultEnabled) {
        throw new AssertionError();
    }

    public static void registerOptionalTexturePack(Identifier folderName, boolean defaultEnabled) {
        registerOptionalTexturePack(folderName, Component.literal(TextHelper.getReadableName(folderName.getPath())), defaultEnabled);
    }


    public static Material getBlockMaterial(Identifier blockTexture) {
        return new Material(blockTexture);
    }
}