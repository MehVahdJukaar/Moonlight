package net.mehvahdjukaar.moonlight.api.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.BaseMapCodec;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.map.decoration.MLMapDecorationType;
import net.mehvahdjukaar.moonlight.api.map.type.MapDecorationType;
import net.mehvahdjukaar.moonlight.api.misc.InvPlacer;
import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.MoonlightClient;
import net.mehvahdjukaar.moonlight.core.map.MapDataInternal;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.StatType;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;


public class Utils {

    public static void spawnItemWithTileData(Player player, RandomizableContainerBlockEntity tile) {
        Level level = player.level();
        if (!level.isClientSide && player.isCreative() && !tile.isEmpty()) {
            BlockPos pos = tile.getBlockPos();
            ItemStack itemstack = saveTileToItem(tile);

            ItemEntity itementity = new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, itemstack);
            itementity.setDefaultPickUpDelay();
            level.addFreshEntity(itementity);
        } else {
            tile.unpackLootTable(player);
        }
    }

    public static ItemStack saveTileToItem(BlockEntity tile) {
        Block block = tile.getBlockState().getBlock();
        ItemStack stack = new ItemStack(block.asItem());
        tile.saveToItem(stack, tile.getLevel().registryAccess());
        return stack;
    }

    public static void loadTileFromItem(BlockEntity tile, ItemStack stack) {
        var comp = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (comp != null) {
            tile.loadWithComponents(comp.copyTag(), tile.getLevel().registryAccess());
        }
    }

    public static void swapItem(Player player, InteractionHand hand, ItemStack oldItem, ItemStack newItem, boolean bothSides) {
        if (!player.level().isClientSide || bothSides)
            player.setItemInHand(hand, ItemUtils.createFilledResult(oldItem.copy(), player, newItem, player.isCreative()));
    }

    public static void swapItem(Player player, InteractionHand hand, ItemStack oldItem, ItemStack newItem) {
        swapItem(player, hand, oldItem, newItem, false);
    }

    public static void swapItemNBT(Player player, InteractionHand hand, ItemStack oldItem, ItemStack newItem) {
        if (!player.level().isClientSide)
            player.setItemInHand(hand, ItemUtils.createFilledResult(oldItem.copy(), player, newItem, false));
    }

    public static void swapItem(Player player, InteractionHand hand, ItemStack newItem) {
        swapItem(player, hand, player.getItemInHand(hand), newItem);
    }

    //TODO: add more stuff from item utils

    @Deprecated(forRemoval = true)
    public static void addStackToExisting(Player player, ItemStack stack, boolean avoidEmptyHands) {
        addItemOrDrop(player, stack, avoidEmptyHands ? InvPlacer.handOrExistingOrAnyAvoidEmptyHand(InteractionHand.MAIN_HAND) : InvPlacer.handOrExistingOrAny(InteractionHand.MAIN_HAND));
    }

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

    public static ResourceLocation getId(Holder<?> object) {
        return object.unwrapKey().get().location();
    }

    public static ResourceLocation getID(@NotNull Block object) {
        return BuiltInRegistries.BLOCK.getKey(object);
    }

    public static ResourceLocation getID(@NotNull EntityType<?> object) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(object);
    }

    public static ResourceLocation getID(@NotNull Biome object) {
        return hackyGetRegistry(Registries.BIOME).getKey(object);
    }

    public static ResourceLocation getID(@NotNull DamageType type) {
        return hackyGetRegistry(Registries.DAMAGE_TYPE).getKey(type);
    }

    public static ResourceLocation getID(@NotNull ConfiguredFeature<?, ?> object) {
        return hackyGetRegistry(Registries.CONFIGURED_FEATURE).getKey(object);
    }

    public static ResourceLocation getID(@NotNull Item object) {
        return BuiltInRegistries.ITEM.getKey(object);
    }

    public static ResourceLocation getID(@NotNull Fluid object) {
        return BuiltInRegistries.FLUID.getKey(object);
    }

    public static ResourceLocation getID(@NotNull BlockEntityType<?> object) {
        return BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(object);
    }

    public static ResourceLocation getID(@NotNull RecipeSerializer<?> object) {
        return BuiltInRegistries.RECIPE_SERIALIZER.getKey(object);
    }

    @Deprecated(forRemoval = true)
    public static ResourceLocation getID(@NotNull SoftFluid object) {
        return SoftFluidRegistry.hackyGetRegistry().getKey(object);
    }

    @Deprecated(forRemoval = true)
    public static ResourceLocation getID(@NotNull MapDecorationType<?, ?> object) {
        return MapDataInternal.hackyGetRegistry().getKey(object);
    }

    public static ResourceLocation getID(@NotNull Potion object) {
        return BuiltInRegistries.POTION.getKey(object);
    }

    public static ResourceLocation getID(@NotNull MobEffect object) {
        return BuiltInRegistries.MOB_EFFECT.getKey(object);
    }

    public static ResourceLocation getID(@NotNull CreativeModeTab object) {
        return BuiltInRegistries.CREATIVE_MODE_TAB.getKey(object);
    }

    public static ResourceLocation getID(@NotNull StatType<?> object) {
        return BuiltInRegistries.STAT_TYPE.getKey(object);
    }

    public static ResourceLocation getID(@NotNull Object object) {
        return switch (object) {
            case Block b -> getID(b);
            case Item b -> getID(b);
            case EntityType<?> b -> getID(b);
            case BlockEntityType<?> b -> getID(b);
            case Biome b -> getID(b);
            case Fluid b -> getID(b);
            case RecipeSerializer<?> b -> getID(b);
            case ConfiguredFeature<?, ?> c -> getID(c);
            case Potion c -> getID(c);
            case MobEffect c -> getID(c);
            case Supplier<?> s -> getID(s.get());
            case SoftFluid s -> getID(s);
            case MLMapDecorationType<?, ?> s -> getID(s);
            case CreativeModeTab t -> getID(t);
            case DamageType t -> getID(t);
            case StatType<?> t -> getID(t);
            case Holder<?> h -> getId(h);
            default -> throw new UnsupportedOperationException("Unsupported class type " +
                    object.getClass() + ". Expected a registry entry for a call to Utils.getID()");
        };
    }

    @Deprecated(forRemoval = true)
    public static <T> boolean isTagged(T entry, Registry<T> registry, TagKey<T> tag) {
        return registry.wrapAsHolder(entry).is(tag);
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
            var level = Minecraft.getInstance().level;
            if (level != null) {
                var clientRa = level.registryAccess();
                Registry<T> r = clientRa.registryOrThrow(registryKey);
                if (holder.canSerializeIn(r.holderOwner())) {
                    return clientRa.lookupOrThrow(registryKey);
                }
            }
        }
        var s = PlatHelper.getCurrentServer();
        if (s != null) {
            var serverRa = s.registryAccess();
            Registry<T> r = serverRa.registryOrThrow(registryKey);
            if (holder.canSerializeIn(r.holderOwner())) {
                return serverRa.lookupOrThrow(registryKey);
            }
        }
        throw new UnsupportedOperationException("Failed to find registry access for " + holder);
    }

    //very very hacky
    //attempts to grab the right registry access for the current thread, hopefully matching the logical side one
    public static RegistryAccess hackyGetRegistryAccess() {
        var s = PlatHelper.getCurrentServer();
        if (PlatHelper.getPhysicalSide().isClient()) {
            if (s != null && (s.isSameThread() || !MoonlightClient.isClientThread())) return s.registryAccess();
            var level = Minecraft.getInstance().level;
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

    public static <T> Registry<T> hackyGetRegistry(ResourceKey<Registry<T>> key) {
        return hackyGetRegistryAccess().registryOrThrow(key);
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

    public static void awardAdvancement(ServerPlayer sp, ResourceLocation name) {
        awardAdvancement(sp, name, "unlock");
    }

    public static void awardAdvancement(ServerPlayer sp, ResourceLocation name, String unlockProp) {
        AdvancementHolder advancement = sp.getServer().getAdvancements().get(name);
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

    public static BlockState readBlockState(CompoundTag compound, @Nullable Level level) {
        HolderGetter<Block> holderGetter = level != null ? level.holderLookup(Registries.BLOCK) : BuiltInRegistries.BLOCK.asLookup();
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
            gameMode = Minecraft.getInstance().gameMode.getPlayerMode();
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


    @ExpectPlatform
    public static <K, V, C extends BaseMapCodec<K, V> & Codec<Map<K, V>>> C optionalMapCodec(final Codec<K> keyCodec, final Codec<V> elementCodec) {
        throw new AssertionError();
    }

    public static <T> Codec<T> optionalRegistryCodec(Registry<T> reg, T defaultValue) {
        return ResourceLocation.CODEC.xmap(
                rl -> {
                    T value = reg.get(rl);
                    return value == null ? defaultValue : value;
                },
                reg::getKey);
    }


    /**
     * Like Registry::byNameCodec::listOf but won't fail for missing entries.
     * No reason to use this really, use HolderSet codec instead
     */
    public static <T> Codec<List<T>> optionalRegistryListCodec(Registry<T> reg) {
        return ResourceLocation.CODEC.listOf().xmap(
                l -> l.stream().filter(reg::containsKey).map(reg::get).toList(),
                a -> a.stream().map(reg::getKey).toList());
    }


    /**
     * Like listOf but won't fail for missing entries.
     */
    public static <A> LenientListCodec<A> lenientListCodec(final Codec<A> elementCodec) {
        return new LenientListCodec<>(elementCodec);
    }

    /**
     * Lenient holder set
     */
    public static <E> Codec<HolderSet<E>> lenientHomogeneousList(ResourceKey<? extends Registry<E>> registryKey) {
        return LenientHolderSetCodec.create(registryKey, RegistryFixedCodec.create(registryKey), false);
    }

    public static <T extends Enum<T>> StreamCodec<FriendlyByteBuf, T> enumStreamCodec(Class<T> enumClass) {
        return new EnumStreamCodec<>(enumClass);
    }

    private record EnumStreamCodec<T extends Enum<T>>(Class<T> enumClass) implements StreamCodec<FriendlyByteBuf, T> {

        @Override
        public T decode(FriendlyByteBuf buf) {
            return buf.readEnum(this.enumClass);
        }

        @Override
        public void encode(FriendlyByteBuf buf, T e) {
            buf.writeEnum(e);
        }
    }


    public static ResourceLocation idWithOptionalNamespace(String id, String namespace) {
        if (id.contains(":")) {
            return ResourceLocation.parse(id);
        } else {
            return ResourceLocation.fromNamespaceAndPath(namespace, id);
        }
    }

    @Nullable
    public static <T> T findFirstInRegistry(Registry<T> registry, ResourceLocation... id) {
        for (ResourceLocation r : id) {
            var optional = registry.getOptional(r);
            if (optional.isPresent()) return optional.get();
        }
        return null;
    }


}