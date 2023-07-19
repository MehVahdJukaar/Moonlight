package net.mehvahdjukaar.moonlight.api.platform.setup.forge;

import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.platform.setup.IDeferredClientSetup;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.List;
import java.util.function.Consumer;

public class ClientSetupHelperImpl {

    public static void deferClientSetup(IDeferredClientSetup mod) {
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        //setup
        Consumer<FMLCommonSetupEvent> setup = event -> {
            event.enqueueWork(mod::setup);
            mod.asyncSetup();
            mod.registerRenderTypes((block, types) -> {
                //from 0.64 we should register render types in out model json
                if (types.length == 1) {
                    ItemBlockRenderTypes.setRenderLayer(block, types[0]);
                } else {
                    var l = List.of(types);
                    ItemBlockRenderTypes.setRenderLayer(block, l::contains);
                }
            });
            mod.registerFluidRenderTypes(ItemBlockRenderTypes::setRenderLayer);
        };
        bus.addListener(setup);

        //particle
        Consumer<RegisterParticleProvidersEvent> particle = event -> {
            mod.registerParticlesRenderers(new ParticleEventImpl(event));
        };
        bus.addListener(particle);

        //decorators
        Consumer<RegisterItemDecorationsEvent> decorator = event -> {
            mod.registerItemDecorators((i, l) -> event.register(i, l::render));
        };
        bus.addListener(decorator);

        //entity renderers
        Consumer<EntityRenderersEvent.RegisterRenderers> entityRenderers = event -> {
            mod.registerEntityRenderers(event::registerEntityRenderer);
            mod.registerBlockEntityRenderers(event::registerBlockEntityRenderer);
        };
        bus.addListener(entityRenderers);

        //block colors
        Consumer<RegisterColorHandlersEvent.Block> colors = event -> {
            mod.registerBlockColors(new IDeferredClientSetup.BlockColorEvent() {
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
        bus.addListener(colors);

        //item colors
        Consumer<RegisterColorHandlersEvent.Item> itemColors = event -> {
            mod.registerItemColors(new IDeferredClientSetup.ItemColorEvent() {
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
        bus.addListener(itemColors);

        //model layers
        Consumer<EntityRenderersEvent.RegisterLayerDefinitions> modelLayers = event -> {
            mod.registerModelLayers(event::registerLayerDefinition);
        };
        bus.addListener(modelLayers);

        //extra models
        Consumer<ModelEvent.RegisterAdditional> extraModels = event -> {
            mod.registerExtraModels(event::register);
        };
        bus.addListener(extraModels);

        //model loaders
        Consumer<ModelEvent.RegisterGeometryLoaders> modelLoaders = event -> {
            mod.registerModelLoaders((i, l) -> event.register(i.getPath(), (IGeometryLoader<?>) l));
        };
        FMLJavaModLoadingContext.get().getModEventBus().addListener(modelLoaders);

        //tooltip
        Consumer<RegisterClientTooltipComponentFactoriesEvent> tooltip = event -> {
            mod.registerTooltipComponents(event::register);
        };
        FMLJavaModLoadingContext.get().getModEventBus().addListener(tooltip);

        //keybind
        Consumer<RegisterKeyMappingsEvent> keybind = event -> {
            mod.registerKeyBind(event::register);
        };
        FMLJavaModLoadingContext.get().getModEventBus().addListener(keybind);

        //reload
        Consumer<RegisterClientReloadListenersEvent> reload = event -> {
         mod.registerClientReloadListener((listener, location) -> event.registerReloadListener(listener.get()));
        };
        bus.addListener(reload);

        //pack
        //TODO:
        /*
        PlatHelper.registerResourcePack(PackType.CLIENT_RESOURCES,
                () -> {
                    IModFile file = ModList.get().getModFileById(folderName.getNamespace()).getFile();
                    try (PathPackResources pack = new PathPackResources(
                            folderName.toString(),
                            true,
                            file.findResource("resourcepacks/" + folderName.getPath()))) {
                        var metadata = Objects.requireNonNull(pack.getMetadataSection(PackMetadataSection.TYPE));
                        return Pack.create(
                                folderName.toString(),
                                displayName,
                                defaultEnabled,
                                (s) -> pack,
                                new Pack.Info(metadata.getDescription(), metadata.getPackFormat(), FeatureFlagSet.of()),
                                PackType.CLIENT_RESOURCES,
                                Pack.Position.TOP,
                                false,
                                PackSource.BUILT_IN);
                    } catch (Exception ee) {
                        if (!DatagenModLoader.isRunningDataGen()) ee.printStackTrace();
                    }
                    return null;
                }
        );*/
    }

    private record ParticleEventImpl(RegisterParticleProvidersEvent event) implements IDeferredClientSetup.ParticleEvent {
        @Override
        public <P extends ParticleType<T>, T extends ParticleOptions> void register(P type, ClientHelper.ParticleFactory<T> factory) {
            this.event.registerSpriteSet(type, factory::create);

        }
    }
}
