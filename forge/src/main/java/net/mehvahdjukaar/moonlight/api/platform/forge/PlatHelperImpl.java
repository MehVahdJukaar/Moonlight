package net.mehvahdjukaar.moonlight.api.platform.forge;

import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfile;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class PlatHelperImpl {

    public static boolean isDev() {
        return !FMLLoader.isProduction();
    }

    public static boolean isData() {
        return FMLLoader.getLaunchHandler().isData();
    }

    public static PlatHelper.Side getPhysicalSide() {
        return FMLEnvironment.dist == Dist.CLIENT ? PlatHelper.Side.CLIENT : PlatHelper.Side.SERVER;
    }

    public static PlatHelper.Platform getPlatform() {
        return PlatHelper.Platform.FORGE;
    }

    public static boolean isModLoaded(String name) {
        return ModList.get().isLoaded(name);
    }

    @Nullable
    public static <T> Field findField(Class<? super T> clazz, String fieldName) {
        try {
            return ObfuscationReflectionHelper.findField(clazz, fieldName);
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    public static Method findMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            return ObfuscationReflectionHelper.findMethod(clazz, methodName, parameterTypes);
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isMobGriefingOn(Level level, Entity entity) {
        return ForgeEventFactory.getMobGriefingEvent(level, entity);
    }

    public static boolean isAreaLoaded(LevelReader level, BlockPos pos, int maxRange) {
        return level.isAreaLoaded(pos, maxRange);
    }

    public static int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        return state.getFlammability(level, pos, face);
    }

    @Nullable
    public static FoodProperties getFoodProperties(Item food, ItemStack stack, Player player) {
        return food.getFoodProperties(stack, player);
    }




    public static int getBurnTime(ItemStack stack) {
        return ForgeHooks.getBurnTime(stack, null);
    }

    @Nullable
    public static MinecraftServer getCurrentServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }

    public static Packet<ClientGamePacketListener> getEntitySpawnPacket(Entity entity) {
        return NetworkHooks.getEntitySpawningPacket(entity);
    }

    public static Path getGamePath() {
        return FMLPaths.GAMEDIR.get();
    }

    public static String getModPageUrl(String modId) {
        return ModList.get().getModContainerById(modId).get().getModInfo().getModURL().map(URL::toString).orElse(null);
    }

    public static String getModName(String modId) {
        return ModList.get().getModContainerById(modId).get().getModInfo().getDisplayName();
    }

    public static SpawnEggItem newSpawnEgg(Supplier<? extends EntityType<? extends Mob>> entityType, int color, int outerColor, Item.Properties properties) {
        return new ForgeSpawnEggItem(entityType, color, outerColor, properties);
    }

    public static Path getModFilePath(String modId) {
        return ModList.get().getModFileById(modId).getFile().getFilePath();
    }

    public static FlowerPotBlock newFlowerPot(@Nullable Supplier<FlowerPotBlock> emptyPot, Supplier<? extends Block> supplier, BlockBehaviour.Properties properties) {
        return new FlowerPotBlock(emptyPot, supplier, properties);
    }

    public static RecordItem newMusicDisc(int power, Supplier<SoundEvent> soundSupplier, Item.Properties properties, int duration) {
        return new RecordItem(power, soundSupplier, properties, duration);
    }

    public static SimpleParticleType newParticle() {
        return new SimpleParticleType(true);
    }

    public static <T extends BlockEntity> BlockEntityType<T> newBlockEntityType(PlatHelper.BlockEntitySupplier<T> blockEntitySupplier, Block... validBlocks) {
        return BlockEntityType.Builder.of(blockEntitySupplier::create, validBlocks).build(null);
    }

    public static <E extends Entity> EntityType<E> newEntityType(String name,
                                                                 EntityType.EntityFactory<E> factory, MobCategory category, float width, float height,
                                                                 int clientTrackingRange, boolean velocityUpdates, int updateInterval) {
        return EntityType.Builder.of(factory, category)
                .sized(width, height).clientTrackingRange(clientTrackingRange)
                .setShouldReceiveVelocityUpdates(velocityUpdates).updateInterval(updateInterval).build(name);
    }



    public static boolean isModLoadingValid() {
        return ModLoader.isLoadingStateValid();
    }

    public static void openCustomMenu(ServerPlayer player, MenuProvider menuProvider, Consumer<FriendlyByteBuf> extraDataProvider) {
        NetworkHooks.openScreen(player, menuProvider, extraDataProvider);
    }

    public static boolean evaluateRecipeCondition(JsonElement jo) {
        return CraftingHelper.getCondition(jo.getAsJsonObject()).test(ICondition.IContext.EMPTY);
    }

    public static List<String> getInstalledMods() {
        return ModList.get().getMods().stream().map(IModInfo::getModId).toList();
    }

    public static Player getFakeServerPlayer(GameProfile id, ServerLevel level) {
        return FakePlayerFactory.get(level, id);
    }

    public static boolean isInitializing() {
        return ModLoadingContext.get().getActiveNamespace().equals("minecraft");
    }

    public static void addCommonSetup(Runnable commonSetup) {
        Moonlight.assertInitPhase();

        Consumer<FMLCommonSetupEvent> eventConsumer = event -> event.enqueueWork(commonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(eventConsumer);
    }


    //maybe move these

    public static void addServerReloadListener(PreparableReloadListener listener, ResourceLocation location) {
        Moonlight.assertInitPhase();

        Consumer<AddReloadListenerEvent> eventConsumer = event -> event.addListener(listener);
        MinecraftForge.EVENT_BUS.addListener(eventConsumer);
    }

    public static void registerResourcePack(PackType packType, @Nullable Supplier<Pack> packSupplier) {
        Moonlight.assertInitPhase();

        if (packSupplier == null) return;
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        Consumer<AddPackFindersEvent> consumer = event -> {
            if (event.getPackType() == packType) {
                var p = packSupplier.get();
                if (p != null) {
                    event.addRepositorySource(infoConsumer -> infoConsumer.accept(packSupplier.get()));
                }
            }
        };
        bus.addListener(consumer);
    }

}
