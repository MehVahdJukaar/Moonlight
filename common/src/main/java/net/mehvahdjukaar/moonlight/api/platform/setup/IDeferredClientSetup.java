package net.mehvahdjukaar.moonlight.api.platform.setup;

import net.mehvahdjukaar.moonlight.api.client.model.CustomBakedModel;
import net.mehvahdjukaar.moonlight.api.client.model.CustomModelLoader;
import net.mehvahdjukaar.moonlight.api.item.IItemDecoratorRenderer;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.resources.assets.LangBuilder;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
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

import java.util.function.Function;
import java.util.function.Supplier;

//experimental
public interface IDeferredClientSetup {

    default void setup() {
    }

    default void asyncSetup(){
    }

    default void registerRenderTypes(RenderTypeEvent event) {
    }

    @FunctionalInterface
    interface RenderTypeEvent {
        void register(Block block, RenderType... types);
    }

    default void registerFluidRenderTypes(FluidRenderTypeEvent event) {
    }

    @FunctionalInterface
    interface FluidRenderTypeEvent {
        void register(Fluid block, RenderType types);
    }


    default void registerParticlesRenderers(ParticleEvent event) {
    }

    @FunctionalInterface
    interface ParticleEvent {
        <P extends ParticleType<T>, T extends ParticleOptions> void register(P particleType, ClientHelper.ParticleFactory<T> factory);
    }

    default void registerItemDecorators(ItemDecoratorEvent event) {
    }

    @FunctionalInterface
    interface ItemDecoratorEvent {
        void register(ItemLike itemLike, IItemDecoratorRenderer renderer);
    }

    default void registerEntityRenderers(EntityRendererEvent event) {
    }

    @FunctionalInterface
    interface EntityRendererEvent {
        <E extends Entity> void register(EntityType<? extends E> entity, EntityRendererProvider<E> renderer);
    }

    default void registerBlockEntityRenderers(BlockEntityRendererEvent event) {
    }

    @FunctionalInterface
    interface BlockEntityRendererEvent {
        <E extends BlockEntity> void register(BlockEntityType<? extends E> blockEntity, BlockEntityRendererProvider<E> renderer);
    }

    default void registerBlockColors(BlockColorEvent event) {
    }

    interface BlockColorEvent {
        void register(BlockColor color, Block... block);

        int getColor(BlockState block, BlockAndTintGetter level, BlockPos pos, int tint);
    }

    default void registerItemColors(ItemColorEvent event) {
    }

    interface ItemColorEvent {
        void register(ItemColor color, ItemLike... items);

        int getColor(ItemStack stack, int tint);
    }

    default void registerModelLayers(ModelLayerEvent event) {
    }

    @FunctionalInterface
    interface ModelLayerEvent {
        void register(ModelLayerLocation modelLayer, Supplier<LayerDefinition> provider);
    }

    default void registerModelLoaders(ModelLoaderEvent event) {
    }

    @FunctionalInterface
    interface ModelLoaderEvent {
        void register(ResourceLocation id, CustomModelLoader loader);

        default void register(ResourceLocation id, Supplier<CustomBakedModel> bakedModelFactory) {
            register(id, (json, context) -> (modelBaker, spriteGetter, transform, location) -> bakedModelFactory.get());
        }
    }

    default void registerExtraModels(ExtraModelEvent event) {
    }

    @FunctionalInterface
    interface ExtraModelEvent {
        void register(ResourceLocation modelLocation);
    }

    default void registerTooltipComponents(TooltipComponentEvent event) {
    }

    @FunctionalInterface
    interface TooltipComponentEvent {
        <T extends TooltipComponent> void register(Class<T> type, Function<? super T, ? extends ClientTooltipComponent> factory);
    }

    default void registerKeyBind(KeyBindEvent event) {
    }

    @FunctionalInterface
    interface KeyBindEvent {
        void register(KeyMapping keyMapping);
    }

    default void registerClientReloadListener(ReloadListenerEvent event) {
    }

    @FunctionalInterface
    interface ReloadListenerEvent {
        void register(Supplier<PreparableReloadListener> listener, ResourceLocation location);
    }

    default void registerOptionalTexturePack(OptionalTexturePackEvent event) {
    }

    @FunctionalInterface
    interface OptionalTexturePackEvent {
        void register(ResourceLocation folderName, Component displayName, boolean defaultEnabled);

        default void register(ResourceLocation folderName) {
            register(folderName, Component.literal(LangBuilder.getReadableName(folderName.getPath())), false);
        }
    }


}
