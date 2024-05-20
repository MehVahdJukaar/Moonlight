package net.mehvahdjukaar.moonlight.api.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.BaseMapCodec;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.map.type.MapDecorationType;
import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.core.map.MapDataInternal;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;


public class Utils {

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
    public static void addStackToExisting(Player player, ItemStack stack, boolean avoidHands) {
        var inv = player.getInventory();
        boolean added = false;
        for (int j = 0; j < inv.items.size(); j++) {
            if (inv.getItem(j).is(stack.getItem()) && inv.add(j, stack)) {
                added = true;
                break;
            }
        }
        if(avoidHands && !added){
            for (int j = 0; j < inv.items.size(); j++) {
                if (inv.getItem(j).isEmpty() && j != inv.selected && inv.add(j, stack)) {
                    added = true;
                    break;
                }
            }
        }
        if (!added && inv.add(stack)) {
            player.drop(stack, false);
        }
    }

    //xp bottle logic
    public static int getXPinaBottle(int bottleCount, RandomSource rand) {
        int xp = 0;
        for (int i = 0; i < bottleCount; i++) xp += (3 + rand.nextInt(5) + rand.nextInt(5));
        return xp;
    }


    public static ResourceLocation getID(Block object) {
        return BuiltInRegistries.BLOCK.getKey(object);
    }

    public static ResourceLocation getID(EntityType<?> object) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(object);
    }

    public static ResourceLocation getID(Biome object) {
        return hackyGetRegistry(Registries.BIOME).getKey(object);
    }

    public static ResourceLocation getID(DamageType type){
        return hackyGetRegistry(Registries.DAMAGE_TYPE).getKey(type);
    }

    public static ResourceLocation getID(ConfiguredFeature<?, ?> object) {
        return hackyGetRegistry(Registries.CONFIGURED_FEATURE).getKey(object);
    }

    public static ResourceLocation getID(Item object) {
        return BuiltInRegistries.ITEM.getKey(object);
    }

    public static ResourceLocation getID(Fluid object) {
        return BuiltInRegistries.FLUID.getKey(object);
    }

    public static ResourceLocation getID(BlockEntityType<?> object) {
        return BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(object);
    }

    public static ResourceLocation getID(RecipeSerializer<?> object) {
        return BuiltInRegistries.RECIPE_SERIALIZER.getKey(object);
    }

    public static ResourceLocation getID(SoftFluid object) {
        return SoftFluidRegistry.hackyGetRegistry().getKey(object);
    }

    public static ResourceLocation getID(MapDecorationType<?, ?> object) {
        return MapDataInternal.hackyGetRegistry().getKey(object);
    }

    public static ResourceLocation getID(Potion object) {
        return BuiltInRegistries.POTION.getKey(object);
    }

    public static ResourceLocation getID(MobEffect object) {
        return BuiltInRegistries.MOB_EFFECT.getKey(object);
    }

    public static ResourceLocation getID(CreativeModeTab object) {
        return BuiltInRegistries.CREATIVE_MODE_TAB.getKey(object);
    }

    public static ResourceLocation getID(StatType<?> object) {
        return BuiltInRegistries.STAT_TYPE.getKey(object);
    }

    public static ResourceLocation getID(Object object) {
        if (object instanceof Block b) return getID(b);
        if (object instanceof Item b) return getID(b);
        if (object instanceof EntityType<?> b) return getID(b);
        if (object instanceof BlockEntityType<?> b) return getID(b);
        if (object instanceof Biome b) return getID(b);
        if (object instanceof Fluid b) return getID(b);
        if (object instanceof RecipeSerializer<?> b) return getID(b);
        if (object instanceof ConfiguredFeature<?, ?> c) return getID(c);
        if (object instanceof Potion c) return getID(c);
        if (object instanceof MobEffect c) return getID(c);
        if (object instanceof Supplier<?> s) return getID(s.get());
        if (object instanceof SoftFluid s) return getID(s);
        if (object instanceof MapDecorationType<?, ?> s) return getID(s);
        if (object instanceof CreativeModeTab t) return getID(t);
        if (object instanceof DamageType t) return getID(t);
        if (object instanceof StatType t) return getID(t);
        throw new UnsupportedOperationException("Unsupported class type " + object.getClass()+". Expected a registry entry for a call to Utils.getID()");
    }

    public static <T> boolean isTagged(T entry, Registry<T> registry, TagKey<T> tag) {
        return registry.getHolder(registry.getId(entry)).map(h -> h.is(tag)).orElse(false);
    }

    //very hacky
    public static RegistryAccess hackyGetRegistryAccess() {
        var s = PlatHelper.getCurrentServer();
        if (s != null) return s.registryAccess();
        if (PlatHelper.getPhysicalSide().isClient()) {
            var level = Minecraft.getInstance().level;
            if (level != null) return level.registryAccess();
            throw new UnsupportedOperationException("Failed to get registry access: level was null");
        }
        throw new UnsupportedOperationException("Failed to get registry access. This is a bug");
    }

    public static <T> Registry<T> hackyGetRegistry(ResourceKey<Registry<T>> registry) {
        return hackyGetRegistryAccess().registryOrThrow(registry);
    }

    /**
     * Copies block properties without keeping stupid lambdas that could include references to the wrong blockstate properties
     */
    public static BlockBehaviour.Properties copyPropertySafe(Block blockBehaviour) {
        var p = BlockBehaviour.Properties.copy(blockBehaviour);
        BlockState state = blockBehaviour.defaultBlockState();
        p.lightLevel(s -> state.getLightEmission());
        p.offsetType(BlockBehaviour.OffsetType.NONE);
        p.isValidSpawn((blockState, blockGetter, blockPos, object) -> false);
        p.mapColor(blockBehaviour.defaultMapColor());
        //TODO: this isnt safe anymore...
        return p;
    }

    @Deprecated(forRemoval = true)
    public static HitResult rayTrace(LivingEntity entity, Level world, ClipContext.Block blockMode, ClipContext.Fluid fluidMode) {
        return rayTrace(entity, world, blockMode, fluidMode, ForgeHelper.getReachDistance(entity));
    }

    // use entity.clip
    @Deprecated(forRemoval = true)
    public static HitResult rayTrace(Entity entity, Level world, ClipContext.Block blockMode, ClipContext.Fluid fluidMode, double range) {
        Vec3 startPos = entity.getEyePosition();
        Vec3 ray = entity.getViewVector(1).scale(range);
        Vec3 endPos = startPos.add(ray);
        ClipContext context = new ClipContext(startPos, endPos, blockMode, fluidMode, entity);
        return world.clip(context);
    }

    public static void awardAdvancement(ServerPlayer sp, ResourceLocation name) {
        awardAdvancement(sp, name, "unlock");
    }

    public static void awardAdvancement( ServerPlayer sp, ResourceLocation name, String unlockProp) {
        Advancement advancement = sp.getServer().getAdvancements().getAdvancement(name);
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
     * This is needed as vanilla handles most of its block altering actions from the item class which calls this.
     * In a Block class this should be called instead to allow adventure mode to work properly
     */
    @Deprecated(forRemoval = true)
    public static boolean mayBuild(Player player, BlockPos pos) {
        if (player instanceof ServerPlayer sp) {
            return !player.blockActionRestricted(player.level(), pos, sp.gameMode.getGameModeForPlayer());
        } else {
            return !player.blockActionRestricted(player.level(), pos, Minecraft.getInstance().gameMode.getPlayerMode());
        }
    }

    /**
     * Call when placing or modifying a block outside of the use methods. Those are already covered by vanilla
     */
    public static boolean mayPerformBlockAction(Player player, BlockPos pos) {
        if (player instanceof ServerPlayer sp) {
            return !player.blockActionRestricted(player.level(), pos, sp.gameMode.getGameModeForPlayer());
        } else {
            return !player.blockActionRestricted(player.level(), pos, Minecraft.getInstance().gameMode.getPlayerMode());
        }
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
    public static <K, V, C extends BaseMapCodec<K,V> & Codec<Map<K, V>>> C optionalMapCodec(final Codec<K> keyCodec, final Codec<V> elementCodec){
        throw new AssertionError();
    }

}