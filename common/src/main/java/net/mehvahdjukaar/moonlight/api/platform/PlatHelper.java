package net.mehvahdjukaar.moonlight.api.platform;

import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import net.mehvahdjukaar.candlelight.api.PlatformImpl;
import net.mehvahdjukaar.moonlight.api.misc.TileOrEntityTarget;
import net.mehvahdjukaar.moonlight.api.util.TextHelper;
import net.mehvahdjukaar.moonlight.core.fake_player.FakeGenericPlayer;
import net.mehvahdjukaar.moonlight.core.fake_player.FakeLocalPlayer;
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

    @PlatformImpl
    public static void addCommonSetup(Runnable commonSetup) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static void addCommonSetupAsync(Runnable commonSetup) {
        throw new AssertionError();
    }

    /**
     * A setup step that runs similar to resource reloads. RegistryAccess and tags are available here as this runs on tag load
     * Boolean given is if this is run on the client logical side
     */
    @PlatformImpl
    public static void addReloadableCommonSetup(BiConsumer<RegistryAccess, Boolean> setup) {
        throw new AssertionError();
    }

    @Contract
    @PlatformImpl
    public static boolean isDev() {
        throw new AssertionError();
    }

    @PlatformImpl
    public static boolean isModLoadingValid() {
        throw new AssertionError();
    }

    /**
     * If loaders are during standard mod init phase
     */
    @PlatformImpl
    public static boolean isInitializing() {
        throw new AssertionError();
    }

    @PlatformImpl
    public static boolean evaluateRecipeCondition(DynamicOps<JsonElement> ops, JsonElement jo) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static <A> void setComponent(DataComponentHolder to, DataComponentType<A> type, A componentValue) {
        throw new AssertionError();
    }

    @Contract
    @PlatformImpl
    public static void invokeLevelUnload(Level l) {
        throw new AssertionError();
    }

    @Contract
    @PlatformImpl
    public static boolean isFakePlayer(ServerPlayer instance) {
        throw new AssertionError();
    }

    public static boolean isAFakePlayer(Player player) {
        if (player instanceof FakeGenericPlayer) return true;
        if (PlatHelper.getPhysicalSide().isClient() && player instanceof FakeLocalPlayer) return true;
        return player instanceof ServerPlayer sp && isFakePlayer(sp);
    }

    @PlatformImpl
    public static MapCodec<LoaderCondition> getConditionCodec() {
        throw new AssertionError();
    }

    public static boolean isIntegratedServer() {
        return PlatHelper.getPhysicalSide().isClient() && getCurrentServer() != null;
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
    @PlatformImpl
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
    @PlatformImpl
    public static Side getPhysicalSide() {
        throw new AssertionError();
    }

    @PlatformImpl
    public static Path getGamePath() {
        throw new AssertionError();
    }

    @PlatformImpl
    public static Path getModFilePath(String modId) {
        throw new AssertionError();
    }

    @Nullable
    @PlatformImpl
    @Contract
    public static String getModPageUrl(String modId) {
        throw new AssertionError();
    }

    /** Mod's CurseForge page url, declared in {@code fabric.mod.json#contact.curseforge}
     *  or {@code neoforge.mods.toml [[mods]] curseforge}. {@code null} when unset. */
    @Nullable
    @PlatformImpl
    @Contract
    public static String getModCurseforgeUrl(String modId) {
        throw new AssertionError();
    }

    /** Mod's Modrinth page url, declared in {@code fabric.mod.json#contact.modrinth}
     *  or {@code neoforge.mods.toml [[mods]] modrinth}. {@code null} when unset. */
    @Nullable
    @PlatformImpl
    @Contract
    public static String getModModrinthUrl(String modId) {
        throw new AssertionError();
    }

    /** Mod's source-code url, declared in {@code fabric.mod.json#contact.sources}
     *  or {@code neoforge.mods.toml [[mods]] sources}. {@code null} when unset. */
    @Nullable
    @PlatformImpl
    @Contract
    public static String getModSourcesUrl(String modId) {
        throw new AssertionError();
    }

    /**
     * Every http url the mod declared in its metadata, in no particular order. Neither loader says what any of them
     * point at, so callers work that out from the host.
     */
    @PlatformImpl
    public static List<String> getModLinks(String modId) {
        throw new AssertionError();
    }

    /**
     * @deprecated moved to TextHelper
     */
    @Deprecated(forRemoval = true)
    @Nullable
    public static String urlHost(String url) {
        return TextHelper.urlHost(url);
    }

    /** The mod's display name, or a readable form of the id when the loader doesn't know that mod. */
    @PlatformImpl
    public static String getModName(String modId) {
        throw new AssertionError();
    }

    /** The mod's icon image file (inside its jar), or null if it declares none. Read on the client to build a texture. */
    @Nullable
    @PlatformImpl
    public static Path getModIcon(String modId) {
        throw new AssertionError();
    }

    /**
     * A path inside the mod's jar (or its classes directory in dev), or null when the mod has nothing there. Works for
     * directories too, so it can answer "does this mod ship the {@code net/mehvahdjukaar} package".
     */
    @Nullable
    @PlatformImpl
    public static Path findModResource(String modId, String path) {
        throw new AssertionError();
    }

    /**
     * The mod's declared authors, empty when it declares none. Fabric returns one entry per author; NeoForge has a
     * single free-form {@code authors} string, which is returned as is (usually already comma separated).
     */
    @PlatformImpl
    public static List<String> getModAuthors(String modId) {
        throw new AssertionError();
    }

    /** The mod's declared license, or null when unset. */
    @Nullable
    @PlatformImpl
    public static String getModLicense(String modId) {
        throw new AssertionError();
    }

    @Nullable
    @PlatformImpl
    public static <T> Field findField(Class<? super T> clazz, String fieldName) {
        throw new AssertionError();
    }

    @Nullable
    @PlatformImpl
    public static Method findMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        throw new AssertionError();
    }

    @Contract
    @Nullable
    @PlatformImpl
    public static MinecraftServer getCurrentServer() {
        throw new AssertionError();
    }

    @PlatformImpl
    public static boolean isModLoaded(String modId) {
        throw new AssertionError();
    }

    @Nullable
    @PlatformImpl
    public static String getModVersion(String modId) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static List<String> getInstalledMods() {
        throw new AssertionError();
    }

    @Deprecated(forRemoval = true)
    public static void registerResourcePack(PackType packType, Supplier<Pack> packSupplier) {
        RegHelper.registerResourcePack(packType, packSupplier);
    }

    @Contract
    @PlatformImpl
    public static boolean isMobGriefingOn(Level level, Entity entity) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static boolean isAreaLoaded(LevelReader level, BlockPos pos, int maxRange) {
        throw new AssertionError();
    }

    @PlatformImpl
    @Nullable
    public static FoodProperties getFoodProperties(ItemStack stack, Player player) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static int getBurnTime(ItemStack stack) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static boolean isFireSource(BlockState blockState, Level level, BlockPos pos, Direction up) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static void onCaughtFire(BlockState state, Level level, BlockPos pos, Direction direction, @Nullable LivingEntity igniter) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static Packet<ClientGamePacketListener> getEntitySpawnPacket(Entity entity, ServerEntity serverEntity) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static SpawnEggItem newSpawnEgg(Supplier<? extends EntityType<? extends Mob>> entityType, int color, int outerColor, Item.Properties properties) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static FlowerPotBlock newFlowerPot(@Nullable Supplier<FlowerPotBlock> emptyPot, Supplier<? extends Block> supplier, BlockBehaviour.Properties properties) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static SimpleParticleType newSimpleParticle() {
        throw new AssertionError();
    }

    public static <T extends ParticleOptions> ParticleType<T> newParticle(MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec, boolean overrideLimiter) {
        return newParticle(c -> codec, c -> streamCodec, overrideLimiter);
    }

    @PlatformImpl
    public static <T extends ParticleOptions> ParticleType<T> newParticle(Function<ParticleType<T>, MapCodec<T>> codec, Function<ParticleType<T>, StreamCodec<? super RegistryFriendlyByteBuf, T>> streamCodec, boolean overrideLimiter) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static <T extends BlockEntity> BlockEntityType<T> newBlockEntityType(BlockEntitySupplier<T> blockEntitySupplier, Block... validBlocks) {
        throw new AssertionError();
    }

    @FunctionalInterface
    public interface BlockEntitySupplier<T extends BlockEntity> {
        @NotNull T create(BlockPos pos, BlockState state);
    }

    @PlatformImpl
    public static <E extends Entity> EntityType<E> newEntityType(String name,
                                                                 EntityType.EntityFactory<E> factory, MobCategory category, float width, float height,
                                                                 int clientTrackingRange, boolean velocityUpdates, int updateInterval) {
        throw new AssertionError();
    }

    //TODO: move to reg
    @Deprecated(forRemoval = true)
    public static void addServerReloadListener(PreparableReloadListener listener, ResourceLocation location) {
        addServerReloadListener(provider -> listener, location);
    }

    @PlatformImpl
    public static void addServerReloadListener(Function<HolderLookup.Provider, PreparableReloadListener> listener, ResourceLocation location) {
        throw new AssertionError();
    }

    @PlatformImpl
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


    @PlatformImpl
    public static Player getFakeServerPlayer(GameProfile id, ServerLevel level) {
        throw new AssertionError();
    }

}
