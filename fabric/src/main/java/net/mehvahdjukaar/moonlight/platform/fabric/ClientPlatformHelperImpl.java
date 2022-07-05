package net.mehvahdjukaar.moonlight.platform.fabric;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.mehvahdjukaar.moonlight.platform.ClientPlatformHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ClientPlatformHelperImpl {

    public static void registerRenderType(Block block, RenderType type) {
        BlockRenderLayerMap.INSTANCE.putBlock(block, type);
    }

    public static Path getModIcon(String modId) {
        return null;
    }


    public static void onRegisterParticles(Consumer<ClientPlatformHelper.ParticleEvent> eventListener) {
        eventListener.accept(ClientPlatformHelperImpl::registerParticle);
    }

    private static <P extends ParticleType<T>, T extends ParticleOptions> void registerParticle(Supplier<P> type, ClientPlatformHelper.ParticleFactory<T> registration) {
        ParticleFactoryRegistry.getInstance().register(type.get(), registration::create);
    }

    public static void onRegisterEntityRenderers(Consumer<ClientPlatformHelper.EntityRendererEvent> eventListener) {
        eventListener.accept(EntityRendererRegistry::register);
    }



    public static void onRegisterBlockColors(Consumer<ClientPlatformHelper.BlockColorEvent> eventListener) {
        eventListener.accept(ColorProviderRegistry.BLOCK::register);
    }

    public static void onRegisterItemColors(Consumer<ClientPlatformHelper.ItemColorEvent> eventListener) {
        eventListener.accept(ColorProviderRegistry.ITEM::register);
    }


    public static void renderBlock(long seed, PoseStack poseStack, MultiBufferSource buffer, BlockState state, Level level, BlockPos pos, BlockRenderDispatcher blockRenderer) {
        blockRenderer.getModelRenderer().tesselateBlock(level, blockRenderer.getBlockModel(state), state, pos, poseStack, buffer.getBuffer(ItemBlockRenderTypes.getMovingBlockRenderType(state)),
                false, RandomSource.create(), seed, OverlayTexture.NO_OVERLAY);
    }
}
