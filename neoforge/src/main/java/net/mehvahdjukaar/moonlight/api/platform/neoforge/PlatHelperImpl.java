package net.mehvahdjukaar.moonlight.api.platform.neoforge;

import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.DynamicOps;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.neoforge.MoonlightForge;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.neoforged.neoforgespi.language.IModInfo;
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
        return EventHooks.canEntityGrief(level, entity);
    }

    public static boolean isAreaLoaded(LevelReader level, BlockPos pos, int maxRange) {
        return level.isAreaLoaded(pos, maxRange);
    }

    @Nullable
    public static FoodProperties getFoodProperties(ItemStack stack, Player player) {
        return stack.getFoodProperties(player);
    }

    public static int getBurnTime(ItemStack stack) {
        return stack.getBurnTime(RecipeType.SMELTING);
    }

    public static int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getFireSpreadSpeed(level, pos, direction);
    }

    public static int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getFlammability(level, pos, direction);
    }

    public static boolean isFireSource(BlockState blockState, Level level, BlockPos pos, Direction up) {
        return blockState.isFireSource(level, pos, up);
    }

    @Nullable
    public static MinecraftServer getCurrentServer() {
        return ServerLifecycleHooks.getCurrentServer();
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
        return new DeferredSpawnEggItem(entityType, color, outerColor, properties);
    }

    public static Path getModFilePath(String modId) {
        return ModList.get().getModFileById(modId).getFile().getFilePath();
    }

    public static FlowerPotBlock newFlowerPot(@Nullable Supplier<FlowerPotBlock> emptyPot, Supplier<? extends Block> supplier, BlockBehaviour.Properties properties) {
        return new FlowerPotBlock(emptyPot, supplier, properties);
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
        return !ModLoader.hasErrors();
    }

    public static void openCustomMenu(ServerPlayer player, MenuProvider menuProvider, Consumer<RegistryFriendlyByteBuf> extraDataProvider) {
        player.openMenu(menuProvider, extraDataProvider);
    }

    public static boolean evaluateRecipeCondition(DynamicOps<JsonElement> ops, JsonElement jo) {
        return ICondition.conditionsMatched(ops, jo);
    }

    public static List<String> getInstalledMods() {
        return ModList.get().getMods().stream().map(IModInfo::getModId).toList();
    }

    public static Player getFakeServerPlayer(GameProfile id, ServerLevel level) {
        return FakePlayerFactory.get(level, id);
    }

    public static boolean isInitializing() {
        return !ModLoadingContext.get().getActiveNamespace().equals("minecraft");
    }

    public static void addCommonSetup(Runnable commonSetup) {
        Moonlight.assertInitPhase();

        Consumer<FMLCommonSetupEvent> eventConsumer = event -> event.enqueueWork(commonSetup);
        MoonlightForge.getCurrentBus().addListener(eventConsumer);
    }


    public static void addCommonSetupAsync(Runnable commonSetup) {
        Moonlight.assertInitPhase();

        Consumer<FMLCommonSetupEvent> eventConsumer = event -> commonSetup.run();
        MoonlightForge.getCurrentBus().addListener(eventConsumer);
    }


    //maybe move these

    public static void addServerReloadListener(PreparableReloadListener listener, ResourceLocation location) {
        Moonlight.assertInitPhase();

        Consumer<AddReloadListenerEvent> eventConsumer = event -> event.addListener(listener);
        NeoForge.EVENT_BUS.addListener(eventConsumer);
    }

    public static void registerResourcePack(PackType packType, @Nullable Supplier<Pack> packSupplier) {
        Moonlight.assertInitPhase();

        if (packSupplier == null) return;
        var bus = MoonlightForge.getCurrentBus();
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

    public static String getModVersion(String modId) {
        return ModList.get().getModContainerById(modId).map(v->v.getModInfo().getVersion().toString()).orElse(null);
    }

    public static Packet<ClientGamePacketListener> getEntitySpawnPacket(Entity entity, ServerEntity serverEntity) {
        return new ClientboundAddEntityPacket(entity, serverEntity);
    }

    public static <A> void setComponent(DataComponentHolder to, DataComponentType<A> type, A componentValue) {
        if(to instanceof MutableDataComponentHolder mc){
            mc.set(type, componentValue);
        }
    }


}
