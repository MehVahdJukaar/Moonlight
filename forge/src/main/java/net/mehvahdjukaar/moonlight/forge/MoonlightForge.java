package net.mehvahdjukaar.moonlight.forge;

import com.google.gson.JsonParser;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynClientResourcesGenerator;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicTexturePack;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.MoonlightClient;
import net.mehvahdjukaar.moonlight.core.fake_player.FPClientAccess;
import net.mehvahdjukaar.moonlight.core.misc.forge.ModLootConditions;
import net.mehvahdjukaar.moonlight.core.misc.forge.ModLootModifiers;
import net.mehvahdjukaar.moonlight.core.network.ClientBoundSendLoginPacket;
import net.mehvahdjukaar.moonlight.core.network.ModMessages;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.StairBlock;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

/**
 * Author: MehVahdJukaar
 */
@Mod(Moonlight.MOD_ID)
public class MoonlightForge {
    public static final String MOD_ID = Moonlight.MOD_ID;

    public MoonlightForge() {

        Moonlight.commonInit();
        MinecraftForge.EVENT_BUS.register(this);
        ModLootModifiers.register();
        ModLootConditions.register();
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        if (PlatHelper.getPhysicalSide().isClient()) {
            modEventBus.addListener(MoonlightForgeClient::registerShader);
            modEventBus.addListener(MoonlightForgeClient::clientSetup);
            MoonlightClient.initClient();

            //ClientHelper.addModelLoaderRegistration(modelLoaderEvent -> {
            //    modelLoaderEvent.register(Moonlight.res("lazy_copy"), new RetexturedModelLoader());
            //});

            //new aa(new DynamicTexturePack(Moonlight.res("test"))).register();

        }

    }


    public static class aa extends DynClientResourcesGenerator {

        protected aa(DynamicTexturePack pack) {
            super(pack);
            pack.addNamespaces("minecraft");
        }

        @Override
        public Logger getLogger() {
            return Moonlight.LOGGER;
        }

        @Override
        public boolean dependsOnLoadedPacks() {
            return true;
        }

        @Override
        public void regenerateDynamicAssets(ResourceManager manager) {
            for (var b : BuiltInRegistries.BLOCK) {
                if (b instanceof StairBlock) {
                    ResourceLocation id = Utils.getID(b);
                    if (id.getPath().equals("oak_stairs")) continue;
                    ResourceLocation location = new ResourceLocation(id.getNamespace(), id.getPath());
                    dynamicPack.addBlockModel(location, JsonParser.parseString(
                            """ 
                                    {
                                      "loader": "moonlight:lazy_copy",
                                      "parent_block": "minecraft:oak_stairs",
                                      "parent_model": "minecraft:block/stairs",
                                      "textures": {
                                        "bottom": "minecraft:block/dark_oak_planks",
                                        "side": "minecraft:block/dark_oak_planks",
                                        "top": "minecraft:block/dark_oak_planks"
                                      }
                                    }
                                    """.replace("dark_oak", id.getPath().replace("_stairs", ""))));
                    location = new ResourceLocation(id.getNamespace(), id.getPath() + "_inner");

                    dynamicPack.addBlockModel(location, JsonParser.parseString(
                            """ 
                                    {
                                      "loader": "moonlight:lazy_copy",
                                      "parent_block": "minecraft:oak_stairs",
                                      "parent_model": "minecraft:block/inner_stairs",
                                      "textures": {
                                        "bottom": "minecraft:block/dark_oak_planks",
                                        "side": "minecraft:block/dark_oak_planks",
                                        "top": "minecraft:block/dark_oak_planks"
                                      }
                                    }
                                    """.replace("dark_oak", id.getPath().replace("_stairs", ""))));
                    location = new ResourceLocation(id.getNamespace(), id.getPath() + "_outer");

                    dynamicPack.addBlockModel(location, JsonParser.parseString(
                            """ 
                                    {
                                      "loader": "moonlight:lazy_copy",
                                      "parent_block": "minecraft:oak_stairs",
                                      "parent_model": "minecraft:block/outer_stairs",
                                      "textures": {
                                        "bottom": "minecraft:block/dark_oak_planks",
                                        "side": "minecraft:block/dark_oak_planks",
                                        "top": "minecraft:block/dark_oak_planks"
                                      }
                                    }
                                    """.replace("dark_oak", id.getPath().replace("_stairs", ""))));
                }
            }
        }
    }

    //hacky but eh
    @SubscribeEvent
    public void onTagUpdated(TagsUpdatedEvent event) {
        Moonlight.afterDataReload(event.getRegistryAccess());
    }

    @Nullable
    private static WeakReference<ICondition.IContext> context = null;

    @Nullable
    public static ICondition.IContext getConditionContext() {
        if (context == null) return null;
        return context.get();
    }

    @SubscribeEvent
    public void onResourceReload(AddReloadListenerEvent event) {
        context = new WeakReference<>(event.getConditionContext());
    }

    @SubscribeEvent
    public void onDataSync(OnDatapackSyncEvent event) {
        SoftFluidRegistry.onDataLoad();
        //send syncing packets
        if (event.getPlayer() != null) {
            SoftFluidRegistry.onDataSyncToPlayer(event.getPlayer(), true);
        } else {
            for (var p : event.getPlayerList().getPlayers()) {
                SoftFluidRegistry.onDataSyncToPlayer(p, true);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            try {
                ModMessages.CHANNEL.sendToClientPlayer(player,
                        new ClientBoundSendLoginPacket());
            } catch (Exception ignored) {
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onDimensionUnload(LevelEvent.Unload event) {
        var level = event.getLevel();
        try {
            if (level.isClientSide()) {
                //got to be careful with classloading
                FPClientAccess.unloadLevel(level);
            }
        } catch (Exception ignored) {
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        Moonlight.onPlayerCloned(event.getOriginal(), event.getEntity(), event.isWasDeath());
    }
}

