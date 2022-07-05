package net.mehvahdjukaar.moonlight.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ClientPlatformHelper {

    @FunctionalInterface
    @Environment(EnvType.CLIENT)
    public interface ParticleFactory<T extends ParticleOptions> {
        @NotNull ParticleProvider<T> create(SpriteSet spriteSet);
    }

    @FunctionalInterface
    public interface ParticleEvent{
        <P extends ParticleType<T>, T extends ParticleOptions> void register(Supplier<P> particleType, ParticleFactory<T> factory);
    }

    @ExpectPlatform
    public static void onRegisterParticles(Consumer<ParticleEvent> eventListener) {
        throw new AssertionError();
    }


    @FunctionalInterface
    public interface EntityRendererEvent {
        <E extends Entity> void register(EntityType<? extends E> entity, EntityRendererProvider<E> renderer);
    }

    @ExpectPlatform
    public static void onRegisterEntityRenderers(Consumer<EntityRendererEvent> eventListener) {
        throw new AssertionError();
    }

    @FunctionalInterface
    public interface BlockColorEvent {
        void register(BlockColor color, Block... block);
    }

    @ExpectPlatform
    public static void onRegisterBlockColors(Consumer<BlockColorEvent> eventListener) {
        throw new AssertionError();
    }

    @FunctionalInterface
    public interface ItemColorEvent {
        void register(ItemColor color, ItemLike... block);
    }

    @ExpectPlatform
    public static void onRegisterItemColors(Consumer<ItemColorEvent> eventListener) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void registerRenderType(Block block, RenderType type) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Path getModIcon(String modId) {
        throw new AssertionError();
    }


}
