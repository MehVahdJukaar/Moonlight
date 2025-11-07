package net.mehvahdjukaar.moonlight.fabric;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.mehvahdjukaar.moonlight.api.block.IFlammable;
import net.mehvahdjukaar.moonlight.api.misc.fake_level.FakeLevelManager;
import net.mehvahdjukaar.moonlight.api.platform.configs.fabric.FabricConfigHolder;
import net.mehvahdjukaar.moonlight.api.platform.fabric.RegHelperImpl;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.moonlight.api.resources.recipe.fabric.BlockTypeSwapIngredientImpl;
import net.mehvahdjukaar.moonlight.api.resources.recipe.fabric.ResourceConditionsBridge;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.fake_player.FPClientAccess;
import net.mehvahdjukaar.moonlight.core.fake_player.FakeGenericPlayer;
import net.mehvahdjukaar.moonlight.core.network.ClientBoundSendLoginMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

public class MoonlightFabric implements ModInitializer, DedicatedServerModInitializer {

    private static boolean isInit = true;
    private static MinecraftServer currentServer;

    @Override
    public void onInitialize() {

        Moonlight.commonInit();


        ServerPlayConnectionEvents.JOIN.register((l, s, m) -> NetworkHelper.sendToClientPlayer(l.player,
                new ClientBoundSendLoginMessage()));
        ServerLifecycleEvents.SERVER_STARTING.register(s -> {
            currentServer = s;
            Moonlight.beforeServerStart(s.registryAccess());
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(s -> {
            currentServer = null;
            FakeLevelManager.invalidateAll();
        });
        ServerWorldEvents.UNLOAD.register((server, world) -> {
            try {
                FakeGenericPlayer.unloadLevel(world);
                if (PlatHelper.getPhysicalSide().isClient()) {
                    //got to be careful with classloading
                    FPClientAccess.unloadLevel(world);
                }
            } catch (Exception e) {
                Moonlight.LOGGER.error("Failed to unload fake players for level {}", world, e);
            }
        });
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(Moonlight::onDataSyncToPlayer);
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(DataMapBridge::onDataSyncToPlayer);
        ServerPlayerEvents.COPY_FROM.register(Moonlight::onPlayerCloned);

        ResourceConditionsBridge.init();
        DataMapBridge.init();
        BlockTypeSwapIngredientImpl.register();

    }

    //called after all other mod initialize have been called.
    // we can register extra stuff here that depends on those before client and server common setup is fired
    static void commonSetup() {

        registerFlammableBlocks();

        RegHelperImpl.lateRegisterEntries();
        FabricConfigHolder.loadAllConfigs();

        isInit = false;

        PRE_SETUP_WORK.forEach(Runnable::run);
        COMMON_SETUP_WORK.forEach(Runnable::run);
        AFTER_SETUP_WORK.forEach(Runnable::run);
        PRE_SETUP_WORK.clear();
        COMMON_SETUP_WORK.clear();
        AFTER_SETUP_WORK.clear();
    }

    private static void registerFlammableBlocks() {
        EmptyBlockGetter level = EmptyBlockGetter.INSTANCE;
        var reg = FlammableBlockRegistry.getDefaultInstance();
        for (Block b : BuiltInRegistries.BLOCK) {
            if (b instanceof IFlammable f) {
                int flammability = f.getFlammability(b.defaultBlockState(), level, BlockPos.ZERO, Direction.UP);
                int spreadSpeed = f.getFireSpreadSpeed(b.defaultBlockState(), level, BlockPos.ZERO, Direction.UP);

                reg.add(b, flammability, spreadSpeed);
            }
        }
    }

    @Override
    public void onInitializeServer() {
        commonSetup();
    }


    public static MinecraftServer getCurrentServer() {
        return currentServer;
    }

    public static boolean isInitializing() {
        return isInit;
    }

    public static final Queue<Runnable> COMMON_SETUP_WORK = new ConcurrentLinkedQueue<>();
    public static final Queue<Runnable> PRE_SETUP_WORK = new ConcurrentLinkedQueue<>();
    public static final Queue<Runnable> AFTER_SETUP_WORK = new ConcurrentLinkedQueue<>();


    public static final Map<PackType, List<Supplier<Pack>>> EXTRA_RESOURCE_PACKS = new EnumMap<>(PackType.class);


    public static Collection<Supplier<Pack>> getAdditionalPacks(@Nullable PackType packType) {
        List<Supplier<Pack>> list = new ArrayList<>();
        if (packType == null) {
            List<Supplier<Pack>> p = EXTRA_RESOURCE_PACKS.get(PackType.CLIENT_RESOURCES);
            if (p != null) list.addAll(p);
            packType = PackType.SERVER_DATA;
        }
        var suppliers = EXTRA_RESOURCE_PACKS.get(packType);
        if (suppliers != null) {
            list.addAll(suppliers);
        }
        return list;
    }
}
