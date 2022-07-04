package net.mehvahdjukaar.moonlight.fabric;

import net.mehvahdjukaar.moonlight.Moonlight;
import net.mehvahdjukaar.moonlight.MoonlightClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.inventory.InventoryMenu;

import java.util.function.Function;

public class MoonlightFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        MoonlightClient.initClient();
        MoonlightClient.onRegisterEntityRenderTypes(EntityRendererRegistry::register);
        MoonlightClient.onRegisterBlockColors(ColorProviderRegistry.BLOCK::register);
        MoonlightClient.onRegisterItemColors(ColorProviderRegistry.ITEM::register);
        MoonlightClient.onRegisterParticles(MoonlightFabricClient::registerParticle);
    }

    private static <T extends ParticleOptions> void registerParticle(ParticleType<T> type, Function<SpriteSet,
            ParticleProvider<T>> registration) {
        ParticleFactoryRegistry.getInstance().register(type,registration::apply);
    }
}
