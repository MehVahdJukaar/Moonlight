package net.mehvahdjukaar.moonlight.api.util;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.BaseMapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.util.internal.UnstableApi;
import net.mehvahdjukaar.moonlight.api.block.IOneUserInteractable;
import net.mehvahdjukaar.moonlight.api.client.IScreenProvider;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.map.decoration.MLMapDecorationType;
import net.mehvahdjukaar.moonlight.api.misc.InvPlacer;
import net.mehvahdjukaar.moonlight.api.misc.TileOrEntityTarget;
import net.mehvahdjukaar.moonlight.api.misc.TriFunction;
import net.mehvahdjukaar.moonlight.api.misc.Triplet;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.util.codec.CodecUtils;
import net.mehvahdjukaar.moonlight.api.util.codec.LenientListCodec;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.MoonlightClient;
import net.mehvahdjukaar.moonlight.core.map.MapDataInternal;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.StatType;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;


public class Utils {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static boolean openGuiIfPossible(BlockEntity be, ServerPlayer player, ItemStack stack, Direction hitFace, Vec3 hitPos) {
        return openGuiIfPossible(TileOrEntityTarget.of(be), player, stack, hitFace, hitPos);
    }

    /// Prefer using this over the above one.
    public static boolean openGuiIfPossible(TileOrEntityTarget target, ServerPlayer player, ItemStack stack, Direction hitFace, Vec3 hitPos) {
        //this is likely not needed
        Object targetObj = target.getTargetOrThrow(player.level());
        BlockPos targetPos = targetObj instanceof Entity e ? e.blockPosition() : ((BlockEntity) targetObj).getBlockPos();
        if (!Utils.mayPerformBlockAction(player, targetPos, stack)) {
            return false;
        }
        switch (targetObj) {
            case IOneUserInteractable ci when !ci.canBeUsedBy(targetPos, player) -> {
                return false;
            }
            case IScreenProvider sp -> {
                if (targetObj instanceof IOneUserInteractable ci) {
                    ci.setCurrentUser(player.getUUID());
                }
                sp.sendOpenGuiPacket(player, hitFace, hitPos);
                return false;
            }
            case MenuProvider mp -> {
                if (targetObj instanceof IOneUserInteractable ci) {
                    ci.setCurrentUser(player.getUUID());
                }
                PlatHelper.openCustomMenu(player, mp, target::write);
                return true;
            }
            default -> {
            }
        }
        return false;
    }

    public static void spawnItemWithTileData(Player player, RandomizableContainerBlockEntity tile) {
        Level level = player.level();
        if (!level.isClientSide() && player.isCreative() && !tile.isEmpty()) {
            BlockPos pos = tile.getBlockPos();
            ItemStack itemstack = saveTileToItem(tile);

            ItemEntity itementity = new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, itemstack);
            itementity.setDefaultPickUpDelay();
            level.addFreshEntity(itementity);
        } else {
            tile.unpackLootTable(player);
        }
    }

    /** False until a world loads. */
    public static boolean areItemComponentsBound() {
        return Items.AIR.builtInRegistryHolder().areComponentsBound();
    }

    /** Default stack of an item, also usable before components are bound. */
    public static ItemStack displayStack(ItemLike item) {
        Item i = item.asItem();
        if (i == Items.AIR) return ItemStack.EMPTY;
        if (i.builtInRegistryHolder().areComponentsBound()) return i.getDefaultInstance();
        // items with a custom Properties.model won't match this and get dropped by model lookups
        Identifier model = BuiltInRegistries.ITEM.getKey(i);
        if (model == null) return ItemStack.EMPTY;
        DataComponentMap components = DataComponentMap.builder()
                .set(DataComponents.ITEM_MODEL, model)
                .set(DataComponents.ITEM_NAME, Component.translatable(i.getDescriptionId()))
                .build();
        return new ItemStack(Holder.direct(i, components));
    }

    public static ItemStack saveTileToItem(BlockEntity tile) {
        Block block = tile.getBlockState().getBlock();
        ItemStack stack = new ItemStack(block.asItem());
        try (var reporter = new ProblemReporter.ScopedCollector(tile.problemPath(), LOGGER)) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, tile.getLevel().registryAccess());
            tile.saveCustomOnly(output);
            tile.removeComponentsFromTag(output);
            BlockItem.setBlockEntityData(stack, tile.getType(), output);
            stack.applyComponents(tile.collectComponents());
        }
        return stack;
    }

    public static void loadTileFromItem(BlockEntity tile, ItemStack stack) {
        var comp = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (comp != null) {
            comp.loadInto(tile, tile.getLevel().registryAccess());
        }
    }

    public static void swapItem(Player player, InteractionHand hand, ItemStack oldItem, ItemStack newItem, boolean bothSides) {
        if (!player.level().isClientSide() || bothSides)
            player.setItemInHand(hand, ItemUtils.createFilledResult(oldItem.copy(), player, newItem, player.isCreative()));
    }

    public static void swapItem(Player player, InteractionHand hand, ItemStack oldItem, ItemStack newItem) {
        swapItem(player, hand, oldItem, newItem, false);
    }

    public static void swapItemNBT(Player player, InteractionHand hand, ItemStack oldItem, ItemStack newItem) {
        if (!player.level().isClientSide())
            player.setItemInHand(hand, ItemUtils.createFilledResult(oldItem.copy(), player, newItem, false));
    }

    public static void swapItem(Player player, InteractionHand hand, ItemStack newItem) {
        swapItem(player, hand, player.getItemInHand(hand), newItem);
    }

    //TODO: add more stuff from item utils

    /**
     * Adds an item to the player's inventory, uses the given strategy to determine where to place it
     */
    public static void addItemOrDrop(Player player, ItemStack stack, InvPlacer placer) {
        Inventory inv = player.getInventory();
        placer.or(InvPlacer.DROP).place(stack, inv, player);
    }

    public static void addItemOrDrop(Player player, ItemStack stack) {
        addItemOrDrop(player, stack, InvPlacer.existingOrAny()); //default impl
    }


    //xp bottle logic
    public static int getXPinaBottle(int bottleCount, RandomSource rand) {
        int xp = 0;
        for (int i = 0; i < bottleCount; i++) xp += (3 + rand.nextInt(5) + rand.nextInt(5));
        return xp;
    }

    public static Identifier getId(Holder<?> object) {
        return object.unwrapKey().orElseThrow().identifier();
    }

    //BlockState, ItemStack, Entity, BlockEntity, FluidState
    public static Identifier getID(@NotNull TypedInstance<?> object) {
        return getId(object.typeHolder());
    }

    public static Identifier getID(@NotNull Block object) {
        return object.properties().id.identifier();
    }

    public static Identifier getID(@NotNull EntityType<?> object) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(object);
    }

    public static Identifier getID(@NotNull Biome object) {
        return hackyGetRegistry(Registries.BIOME).getKey(object);
    }

    public static Identifier getID(@NotNull DamageType type) {
        return hackyGetRegistry(Registries.DAMAGE_TYPE).getKey(type);
    }

    public static Identifier getID(@NotNull ConfiguredFeature<?, ?> object) {
        return hackyGetRegistry(Registries.CONFIGURED_FEATURE).getKey(object);
    }

    //items don't keep their properties around, but the holder they make at construction has the key
    public static Identifier getID(@NotNull Item object) {
        return object.builtInRegistryHolder().key().identifier();
    }

    public static Identifier getID(@NotNull Fluid object) {
        return BuiltInRegistries.FLUID.getKey(object);
    }

    public static Identifier getID(@NotNull BlockEntityType<?> object) {
        return BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(object);
    }

    public static Identifier getID(@NotNull RecipeSerializer<?> object) {
        return BuiltInRegistries.RECIPE_SERIALIZER.getKey(object);
    }

    public static Identifier getID(@NotNull Potion object) {
        return BuiltInRegistries.POTION.getKey(object);
    }

    public static Identifier getID(@NotNull MobEffect object) {
        return BuiltInRegistries.MOB_EFFECT.getKey(object);
    }

    public static Identifier getID(@NotNull CreativeModeTab object) {
        return BuiltInRegistries.CREATIVE_MODE_TAB.getKey(object);
    }

    public static Identifier getID(@NotNull StatType<?> object) {
        return BuiltInRegistries.STAT_TYPE.getKey(object);
    }

    public static Identifier getID(@NotNull Object object) {
        //classes first, most common ones on top. interfaces go last as those checks are the slow ones
        return switch (object) {
            case Block b -> getID(b);
            case Item b -> getID(b);
            case Fluid b -> getID(b);
            case EntityType<?> b -> getID(b);
            case BlockEntityType<?> b -> getID(b);
            case SoftFluid s -> getID(s);
            case MobEffect c -> getID(c);
            case Potion c -> getID(c);
            case RecipeSerializer<?> b -> getID(b);
            case CreativeModeTab t -> getID(t);
            case StatType<?> t -> getID(t);
            case Biome b -> getID(b);
            case DamageType t -> getID(t);
            case ConfiguredFeature<?, ?> c -> getID(c);
            case MLMapDecorationType<?, ?> s -> getID(s);
            //RegSupplier is both a supplier and a holder, so this one has to stay above
            case Supplier<?> s -> getID(s.get());
            case Holder<?> h -> getId(h);
            case TypedInstance<?> t -> getID(t);
            default -> throw new UnsupportedOperationException("Unsupported class type " +
                    object.getClass() + ". Expected a registry entry for a call to Utils.getID()");
        };
    }

    // finds the right registry access to which a data pack entry belongs to
    public static <T> HolderLookup.RegistryLookup<T> hackyFindRegistryOf(
            Holder<T> holder, ResourceKey<Registry<T>> registryKey) {
        if (holder instanceof Holder.Reference<T> ref) {
            if (ref.owner instanceof HolderLookup.RegistryLookup<T> ra) {
                return ra;
            }
        }
        if (PlatHelper.getPhysicalSide().isClient()) {
            var level = ClientHelper.getLocalLevel();
            if (level != null) {
                var clientRa = level.registryAccess();
                Registry<T> r = clientRa.lookupOrThrow(registryKey);
                if (holder.canSerializeIn(r)) {
                    return clientRa.lookupOrThrow(registryKey);
                }
            }
        }
        var s = PlatHelper.getCurrentServer();
        if (s != null) {
            var serverRa = s.registryAccess();
            Registry<T> r = serverRa.lookupOrThrow(registryKey);
            if (holder.canSerializeIn(r)) {
                return serverRa.lookupOrThrow(registryKey);
            }
        }
        throw new UnsupportedOperationException("Failed to find registry access for " + holder);
    }

    //very very hacky
    //attempts to grab the right registry access for the current thread, hopefully matching the logical side one
    @UnstableApi
    public static RegistryAccess hackyGetRegistryAccess() {
        var s = PlatHelper.getCurrentServer();
        if (PlatHelper.getPhysicalSide().isClient()) {
            if (s != null && (s.isSameThread() || !MoonlightClient.isClientThread())) return s.registryAccess();
            var level = ClientHelper.getLocalLevel();
            if (level != null) return level.registryAccess();
            var hack2 = Moonlight.EARLY_REGISTRY_ACCESS.get();
            if (hack2 != null) return hack2.get();
            throw new UnsupportedOperationException("Failed to get registry access: level was null");
        }
        if (s != null) return s.registryAccess();
        var hack2 = Moonlight.EARLY_REGISTRY_ACCESS.get();
        if (hack2 != null) return hack2.get();
        throw new UnsupportedOperationException("Failed to get registry access. This is a bug");
    }

    @UnstableApi
    public static <T> Registry<T> hackyGetRegistry(ResourceKey<Registry<T>> key) {
        return hackyGetRegistryAccess().lookupOrThrow(key);
    }


    /**
     * Copies block properties without keeping stupid lambdas that could include references to the wrong blockstate properties
     */
    public static BlockBehaviour.Properties copyPropertySafe(Block blockBehaviour) {
        var p = BlockBehaviour.Properties.ofFullCopy(blockBehaviour);
        BlockState state = blockBehaviour.defaultBlockState();
        p.lightLevel(s -> state.getLightEmission());
        p.offsetType(BlockBehaviour.OffsetType.NONE);
        p.isValidSpawn((blockState, blockGetter, pos, entityType) ->
                blockState.isFaceSturdy(blockGetter, pos, Direction.UP) && blockState.getLightEmission() < 14);
        p.mapColor(blockBehaviour.defaultMapColor());
        p.emissiveRendering((blockState, blockGetter, blockPos) -> false);
        //TODO: this isnt safe anymore... in 1.21
        return p;
    }

    public static void awardAdvancement(ServerPlayer sp, Identifier name) {
        awardAdvancement(sp, name, "unlock");
    }

    public static void awardAdvancement(ServerPlayer sp, Identifier name, String unlockProp) {
        AdvancementHolder advancement = sp.level().getServer().getAdvancements().get(name);
        if (advancement != null) {
            PlayerAdvancements advancements = sp.getAdvancements();
            if (!advancements.getOrStartProgress(advancement).isDone()) {
                advancements.award(advancement, unlockProp);
            }
        }
    }

    @Nullable
    public static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> getTicker(BlockEntityType<A> type, BlockEntityType<E> targetType, BlockEntityTicker<? super E> ticker) {
        return targetType == type ? (BlockEntityTicker<A>) ticker : null;
    }

    @Nullable
    public static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> getTicker(
            BlockEntityType<A> type, BlockEntityType<E> targetType,
            Consumer<A> tickFunc) {
        return targetType == type ?
                (level, blockPos, blockState, blockEntity) -> tickFunc.accept(blockEntity) : null;
    }


    public static BlockState readBlockState(CompoundTag compound, @Nullable Level level) {
        HolderGetter<Block> holderGetter = level != null ? level.holderLookup(Registries.BLOCK) : BuiltInRegistries.BLOCK;
        return NbtUtils.readBlockState(holderGetter, compound);
    }

    public static <T extends Comparable<T>, A extends Property<T>> BlockState replaceProperty(BlockState from, BlockState to, A property) {
        if (from.hasProperty(property)) {
            return to.setValue(property, from.getValue(property));
        }
        return to;
    }

    /**
     * Call this instead of player.abilities.mayBuild. Mainly used for adventure mode ege case
     * <p>
     * This is needed as vanilla handles most of its block altering actions from the item class which calls this.
     * In a Block class this should be called instead to allow adventure mode to work properly
     * <p>
     * Call when placing or modifying a block. Checks edge cases like spectator
     * Needed also in BLOCK use methods. or other places. not item I believe
     */
    public static boolean mayPerformBlockAction(Player player, BlockPos pos, ItemStack stack) {
        GameType gameMode;
        if (player instanceof ServerPlayer sp) {
            gameMode = sp.gameMode.getGameModeForPlayer();
        } else {
            //client player, or a fake player on a dedicated server. the latter has no game mode to query
            GameType local = PlatHelper.getPhysicalSide().isClient() ? ClientHelper.getLocalGameMode() : null;
            gameMode = local == null ? GameType.SURVIVAL : local;
        }
        //we don't have context so we check both can break and can place. this below already check canBreak
        //this only checks the adventure mode canDestroyTag tag
        boolean result = !player.blockActionRestricted(player.level(), pos, gameMode);
        if (!result) {
            //also checks this because vanilla doesn't as it does not place blocks in block use method
            //also vanilla tends to allow a bunch of unpreventable adventure interactions

            if (gameMode == GameType.ADVENTURE && !stack.isEmpty()) {
                if (stack.canPlaceOnBlockInAdventureMode(new BlockInWorld(player.level(), pos, false))) {
                    return true;
                }
            }
        }
        return result;
    }

    public static Optional<BlockState> getRotatedDirectionalBlock(BlockState state, Direction axis, boolean ccw) {
        Vec3 targetNormal = state.getValue(BlockStateProperties.FACING).getUnitVec3();
        Vec3 myNormal = axis.getUnitVec3();
        if (!ccw) targetNormal = targetNormal.scale(-1);

        Vec3 rotated = myNormal.cross(targetNormal);
        // not on same axis, can rotate
        if (!rotated.equals(Vec3.ZERO)) {
            Direction newDir = Direction.getApproximateNearest(rotated.x(), rotated.y(), rotated.z());
            return Optional.of(state.setValue(BlockStateProperties.FACING, newDir));
        }
        return Optional.empty();
    }

    public static boolean isMethodImplemented(Class<?> original, Class<?> subclass, String name) {
        Method declaredMethod = findMethodWithMatchingName(subclass, name);
        Method modMethod = findMethodWithMatchingName(original, name);
        return declaredMethod != null && modMethod != null && Arrays.equals(declaredMethod.getParameterTypes(), modMethod.getParameterTypes());
    }

    private static Method findMethodWithMatchingName(Class<?> clazz, String name) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(name)) {
                return method;
            }
        }
        return null;
    }

    public static Identifier idWithOptionalNamespace(String id, String namespace) {
        if (id.contains(":")) {
            return Identifier.parse(id);
        } else {
            return Identifier.fromNamespaceAndPath(namespace, id);
        }
    }

    @Nullable
    public static <T> T findFirstInRegistry(Registry<T> registry, Identifier... ids) {
        for (Identifier r : ids) {
            var optional = registry.getOptional(r);
            if (optional.isPresent()) return optional.get();
        }
        return null;
    }

    @Nullable
    public static <T> T findFirstInRegistry(Registry<T> registry, Iterable<Identifier> ids) {
        for (Identifier r : ids) {
            var optional = registry.getOptional(r);
            if (optional.isPresent()) return optional.get();
        }
        return null;
    }

    public static <T, U, D, R> TriFunction<T, U, D, R> memoize(final TriFunction<T, U, D, R> memoBiFunction) {
        return new TriFunction<>() {
            private final Map<Triplet<T, U, D>, R> cache = new ConcurrentHashMap<>();

            public R apply(T object, U object2, D object3) {
                return this.cache.computeIfAbsent(Triplet.of(object, object2, object3), (pair) ->
                        memoBiFunction.apply(pair.left(), pair.middle(), pair.right()));
            }

            @Override
            public String toString() {
                String var10000 = String.valueOf(memoBiFunction);
                return "memoize/3[function=" + var10000 + ", size=" + this.cache.size() + "]";
            }
        };
    }


}