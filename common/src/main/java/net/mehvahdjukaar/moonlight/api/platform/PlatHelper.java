package net.mehvahdjukaar.moonlight.api.platform;

import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.misc.TileOrEntityTarget;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicResourcePack;
import net.mehvahdjukaar.moonlight.core.misc.LoaderCondition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Helper class dedicated to platform independent common utility methods that require a different implementation for each loader
 * Forge specific methods that dont have a fabric equivalent are located in ForgeHelper
 */
public class PlatHelper {

    @ExpectPlatform
    public static void addCommonSetup(Runnable commonSetup) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void addCommonSetupAsync(Runnable commonSetup) {
        throw new AssertionError();
    }

    /**
     * A setup step that runs similar to resource reloads. RegistryAccess and tags are available here as this runs on tag load
     * Boolean given is if this is run on the client logical side
     */
    @ExpectPlatform
    public static void addReloadableCommonSetup(BiConsumer<RegistryAccess, Boolean> setup) {
        throw new AssertionError();
    }

    @Contract
    @ExpectPlatform
    public static boolean isDev() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isModLoadingValid() {
        throw new AssertionError();
    }

    /**
     * If loaders are during standard mod init phase
     */
    @ExpectPlatform
    public static boolean isInitializing() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean evaluateRecipeCondition(DynamicOps<JsonElement> ops, JsonElement jo) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <A> void setComponent(DataComponentHolder to, DataComponentType<A> type, A componentValue) {
        throw new AssertionError();
    }

    @Contract
    @ExpectPlatform
    public static void invokeLevelUnload(Level l) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isFakePlayer(ServerPlayer instance) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static MapCodec<LoaderCondition> getConditionCodec() {
        throw new AssertionError();
    }

    public enum Platform {
        FORGE, FABRIC;
        private static boolean quilt = false;

        static {
            try {
                Class.forName("org.quiltmc.loader.api.QuiltLoader");
                quilt = true;
            } catch (ClassNotFoundException ignored) {
            }
        }

        public boolean isForge() {
            return this == FORGE;
        }

        public boolean isFabric() {
            return this == FABRIC;
        }

        public boolean isQuilt() {
            return isFabric() && quilt;
        }

        public void ifForge(Runnable runnable) {
            if (isForge()) runnable.run();
        }

        public void ifFabric(Runnable runnable) {
            if (isFabric()) runnable.run();
        }
    }

    @Contract
    @ExpectPlatform
    public static Platform getPlatform() {
        throw new AssertionError();
    }

    public enum Side {
        CLIENT, SERVER;

        public boolean isClient() {
            return this == CLIENT;
        }

        public boolean isServer() {
            return this == SERVER;
        }

        public void ifClient(Runnable runnable) {
            if (isClient()) runnable.run();
        }

        public void ifServer(Runnable runnable) {
            if (isServer()) runnable.run();
        }
    }

    @Contract
    @ExpectPlatform
    public static Side getPhysicalSide() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Path getGamePath() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Path getModFilePath(String modId) {
        throw new AssertionError();
    }

    @Nullable
    @ExpectPlatform
    public static String getModPageUrl(String modId) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static String getModName(String modId) {
        throw new AssertionError();
    }

    @Nullable
    @ExpectPlatform
    public static <T> Field findField(Class<? super T> clazz, String fieldName) {
        throw new AssertionError();
    }

    @Nullable
    @ExpectPlatform
    public static Method findMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        throw new AssertionError();
    }

    @Nullable
    @ExpectPlatform
    public static MinecraftServer getCurrentServer() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isModLoaded(String modId) {
        throw new AssertionError();
    }

    @Nullable
    @ExpectPlatform
    public static String getModVersion(String modId) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static List<String> getInstalledMods() {
        throw new AssertionError();
    }

    @Deprecated(forRemoval = true)
    public static void registerResourcePack(PackType packType, Supplier<Pack> packSupplier) {
        RegHelper.registerResourcePack(packType, packSupplier);
    }

    @Contract
    @ExpectPlatform
    public static boolean isMobGriefingOn(Level level, Entity entity) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isAreaLoaded(LevelReader level, BlockPos pos, int maxRange) {
        throw new AssertionError();
    }

    @ExpectPlatform
    @Nullable
    public static FoodProperties getFoodProperties(ItemStack stack, Player player) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static int getBurnTime(ItemStack stack) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isFireSource(BlockState blockState, Level level, BlockPos pos, Direction up) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void onCaughtFire(BlockState state, Level level, BlockPos pos, Direction direction, @Nullable LivingEntity igniter) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Packet<ClientGamePacketListener> getEntitySpawnPacket(Entity entity, ServerEntity serverEntity) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static SpawnEggItem newSpawnEgg(Supplier<? extends EntityType<? extends Mob>> entityType, int color, int outerColor, Item.Properties properties) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static FlowerPotBlock newFlowerPot(@Nullable Supplier<FlowerPotBlock> emptyPot, Supplier<? extends Block> supplier, BlockBehaviour.Properties properties) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static SimpleParticleType newParticle() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <T extends ParticleOptions> ParticleType<T> newParticle(MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <T extends BlockEntity> BlockEntityType<T> newBlockEntityType(BlockEntitySupplier<T> blockEntitySupplier, Block... validBlocks) {
        throw new AssertionError();
    }

    @FunctionalInterface
    public interface BlockEntitySupplier<T extends BlockEntity> {
        @NotNull T create(BlockPos pos, BlockState state);
    }

    @ExpectPlatform
    public static <E extends Entity> EntityType<E> newEntityType(String name,
                                                                 EntityType.EntityFactory<E> factory, MobCategory category, float width, float height,
                                                                 int clientTrackingRange, boolean velocityUpdates, int updateInterval) {
        throw new AssertionError();
    }

    @Deprecated(forRemoval = true)
    public static void addServerReloadListener(PreparableReloadListener listener, ResourceLocation location) {
        addServerReloadListener(provider -> listener, location);
    }

    @ExpectPlatform
    public static void addServerReloadListener(Function<HolderLookup.Provider, PreparableReloadListener> listener, ResourceLocation location) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void openCustomMenu(ServerPlayer player, MenuProvider menuProvider, Consumer<RegistryFriendlyByteBuf> extraDataProvider) {
        throw new AssertionError();
    }

    @Deprecated(forRemoval = true)
    public static void openCustomMenu(ServerPlayer player, MenuProvider menuProvider, BlockPos pos) {
        openCustomMenu(player, menuProvider, buf -> buf.writeBlockPos(pos));
    }

    public static <T extends Entity & MenuProvider> void openCustomMenu(ServerPlayer player, T menuProvider) {
        TileOrEntityTarget target = TileOrEntityTarget.of(menuProvider);
        openCustomMenu(player, menuProvider, target::write);
    }

    public static <T extends BlockEntity & MenuProvider> void openCustomMenu(ServerPlayer player, T menuProvider) {
        TileOrEntityTarget target = TileOrEntityTarget.of(menuProvider);
        openCustomMenu(player, menuProvider, target::write);
    }

    @ExpectPlatform
    public static Player getFakeServerPlayer(GameProfile id, ServerLevel level) {
        throw new AssertionError();
    }

}
