package net.mehvahdjukaar.moonlight.api.util.codec;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.BaseMapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;

public class CodecUtils {


    public static final Codec<Boolean> MOD_LOADED_CODEC = Codec.STRING.xmap(PlatHelper::isModLoaded, b -> "");

    public static <A> MapCodec<A> safeOptFieldOf(Codec<A> c, String name, Supplier<A> defaultValue) {
        return Codec.optionalField(name, c, false).xmap(
                (o) -> o.orElse(defaultValue.get()),
                (a) -> Objects.equals(a, defaultValue.get()) ? Optional.empty() : Optional.of(a));
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

    public static <T> Codec<T> remapNamespaceCodec(Registry<T> reg, String oldNamespace, String newNamespace) {
        return ResourceLocation.CODEC.flatXmap(
                rl -> {
                    T value = reg.get(rl);
                    if (value == null && rl.getNamespace().equals(oldNamespace)) {
                        rl = ResourceLocation.fromNamespaceAndPath(newNamespace, rl.getPath());
                        value = reg.get(rl);
                    }
                    if (value == null) {
                        ResourceLocation finalRl = rl;
                        return DataResult.error(() -> "Unknown registry key in " + reg.key() + ": " + finalRl);
                    }
                    return DataResult.success(value);
                },
                t -> DataResult.success(reg.getKey(t)));
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

    public static final Codec<AABB> AABB_CODEC = RecordCodecBuilder.create(i -> i.group(
                    Vec3.CODEC.fieldOf("from").forGetter(AABB::getMinPosition),
                    Vec3.CODEC.fieldOf("to").forGetter(AABB::getMaxPosition)
            ).apply(i, AABB::new)
    );


    public static <A> Codec<List<A>> lenientListOrSingleCodec(final Codec<A> elementCodec) {
        return Codec.either(lenientListCodec(elementCodec), elementCodec)
                .xmap(either -> either.map(Function.identity(), List::of), Either::left);
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

    public static <A, B> LenientUnboundedMapCodec<A, B> lenientUnboundedMap(Codec<A> keyCodec, Codec<B> elementCodec) {
        return new LenientUnboundedMapCodec<>(keyCodec, elementCodec);
    }

    public static <A> MapCodec<A> lenientWithLog(Codec<A> elementCodec, String name, A defaultValue) {
        return LenientCodecWithLog.of(elementCodec, name, defaultValue);
    }

    public static <A> MapCodec<Optional<A>> lenientWithLog(Codec<A> elementCodec, String name) {
        return LenientCodecWithLog.of(elementCodec, name);
    }

    public static <B> MapCodec<Optional<B>> optionalAlias(Codec<B> codec, String primaryName, String alias) {
        //first lenient so we can go to second
        return AlternativeMapCodec.optionalAlias(codec, primaryName, alias);
    }

    public static <B> MapCodec<B> alias(Codec<B> codec, String primaryName, String alias) {
        return AlternativeMapCodec.alias(codec, primaryName, alias);
    }

    public static <A, B> Codec<Either<A, B>> eitherLeft(Codec<A> leftCodec) {
        return new EitherLeftCodec<>(leftCodec);
    }

    public static final Codec<ItemStack> ITEM_OR_STACK = Codec.withAlternative(ItemStack.SINGLE_ITEM_CODEC, BuiltInRegistries.ITEM.byNameCodec(),
            Item::getDefaultInstance);

    public static final Codec<List<ItemStack>> ITEMSTACK_OR_ITEMSTACK_LIST = singleOrList(ITEM_OR_STACK);

    public static final Codec<Supplier<List<ItemStack>>> ITEMSTACK_HOLDER_SET = RegistryCodecs.homogeneousList(Registries.ITEM)
            .xmap(l -> () -> l.stream().map(Holder::value).map(ItemStack::new).toList(), s -> HolderSet.direct(s.get().stream().map(ItemStack::getItemHolder).toList()));

    public static final Codec<Supplier<List<ItemStack>>> ITEMSTACK_OR_LIST_OR_HOLDER_SET =
            Codec.withAlternative(
                    ITEMSTACK_OR_ITEMSTACK_LIST.xmap(l -> () -> l, Supplier::get),
                    ITEMSTACK_HOLDER_SET);


    //when both can succeed at the same time
    public static <A, B extends A, C extends A> Codec<A> bestAlternative(
            Codec<B> first, Codec<C> second, BiPredicate<B, C> chooseFirst) {

        return new BestAlternativeCodec<>(first, second, chooseFirst);
    }

    @SafeVarargs
    public static <A> Codec<A> alternatives(Codec<? extends A>... codecs) {
        return new AlternativeCodec<>(codecs);
    }

    public static <A> Codec<List<A>> singleOrList(Codec<A> elementCodec) {
        return Codec.withAlternative(elementCodec.listOf(), elementCodec, List::of);
    }
}
