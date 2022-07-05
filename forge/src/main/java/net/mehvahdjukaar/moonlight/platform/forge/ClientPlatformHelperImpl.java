package net.mehvahdjukaar.moonlight.platform.forge;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.platform.ClientPlatformHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.locating.IModFile;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ClientPlatformHelperImpl {

    public static void registerRenderType(Block block, RenderType type) {
        ItemBlockRenderTypes.setRenderLayer(block, type);
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


    public static void onRegisterParticles(Consumer<ClientPlatformHelper.ParticleEvent> eventListener) {
        Consumer<ParticleFactoryRegisterEvent> eventConsumer = event->
                eventListener.accept(ClientPlatformHelperImpl::registerParticle);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(eventConsumer);
    }

    private static <P extends ParticleType<T>, T extends ParticleOptions> void registerParticle(Supplier<P> type, ClientPlatformHelper.ParticleFactory<T> registration) {
        ParticleEngine particleEngine = Minecraft.getInstance().particleEngine;
        particleEngine.register(type.get(), registration::create);
    }

    public static void onRegisterEntityRenderers(Consumer<ClientPlatformHelper.EntityRendererEvent> eventListener) {
        Consumer<EntityRenderersEvent.RegisterRenderers> eventConsumer = event->
                eventListener.accept(event::registerEntityRenderer);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(eventConsumer);
    }

    public static void onRegisterBlockColors(Consumer<ClientPlatformHelper.BlockColorEvent> eventListener) {
        Consumer<ColorHandlerEvent.Block> eventConsumer = event->{
            var colors = event.getBlockColors();
            eventListener.accept(colors::register);
        };
        FMLJavaModLoadingContext.get().getModEventBus().addListener(eventConsumer);
    }

    public static void onRegisterItemColors(Consumer<ClientPlatformHelper.ItemColorEvent> eventListener) {
        Consumer<ColorHandlerEvent.Item> eventConsumer = event->{
            var colors = event.getItemColors();
            eventListener.accept(colors::register);
        };
        FMLJavaModLoadingContext.get().getModEventBus().addListener(eventConsumer);
    }


    public static void renderBlock(long seed, PoseStack matrixStack, MultiBufferSource buffer, BlockState blockstate, Level world, BlockPos blockpos, BlockRenderDispatcher modelRenderer) {
        for (RenderType type : RenderType.chunkBufferLayers()) {
            if (ItemBlockRenderTypes.canRenderInLayer(blockstate, type)) {
                ForgeHooksClient.setRenderType(type);
                modelRenderer.getModelRenderer().tesselateBlock(world, modelRenderer.getBlockModel(blockstate), blockstate, blockpos, matrixStack,
                        buffer.getBuffer(type), false, RandomSource.create(), seed, OverlayTexture.NO_OVERLAY);
            }
        }
        ForgeHooksClient.setRenderType(null);
    }

}
