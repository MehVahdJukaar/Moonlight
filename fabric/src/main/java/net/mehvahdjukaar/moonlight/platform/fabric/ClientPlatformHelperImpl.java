package net.mehvahdjukaar.moonlight.platform.fabric;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.mehvahdjukaar.moonlight.platform.ClientPlatformHelper;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.level.block.Block;

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
}
