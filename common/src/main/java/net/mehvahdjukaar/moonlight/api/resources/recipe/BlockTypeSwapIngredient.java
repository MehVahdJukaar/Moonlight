package net.mehvahdjukaar.moonlight.api.resources.recipe;

import com.google.common.base.Preconditions;
import com.mojang.serialization.*;
import io.netty.handler.codec.DecoderException;
import net.mehvahdjukaar.candlelight.api.PlatformImpl;
import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.mehvahdjukaar.moonlight.api.set.BlockTypeRegistry;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public abstract class BlockTypeSwapIngredient<T extends BlockType> {


    @PlatformImpl
    public static <T extends BlockType> Ingredient create(Ingredient original, @NotNull T from, @NotNull T to) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static <T extends BlockType> BlockTypeSwapIngredient<T> create(Ingredient original, @NotNull T from, @NotNull T to, BlockTypeRegistry<T> reg) {
        throw new AssertionError();
    }

    protected final Ingredient inner;
    protected final T fromType;
    protected final T toType;
    protected final BlockTypeRegistry<T> registry;

    private List<ItemStack> items;

    protected BlockTypeSwapIngredient(Ingredient inner, T fromType, T toType, BlockTypeRegistry<T> reg) {
        super();
        Preconditions.checkNotNull(toType, "Found null to block type for BlockTypeSwapIngredient");
        Preconditions.checkNotNull(fromType, "Found null from block type for BlockTypeSwapIngredient");
        this.inner = inner;
        this.fromType = fromType;
        this.toType = toType;
        this.registry = reg;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BlockTypeSwapIngredient<?> ing && this.inner.equals(ing.inner)
                && this.fromType == ing.fromType && this.toType == ing.toType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(inner, fromType, toType);
    }

    public Ingredient getInner() {
        return inner;
    }

    public boolean test(ItemStack stack) {
        if (stack != null) {
            for (ItemStack itemStack : this.getMatchingStacks()) {
                if (itemStack.is(stack.getItem())) {
                    return true;
                }
            }
        }
        return false;
    }

    public final List<ItemStack> convertItems(List<ItemStack> toConvert) {
        List<ItemStack> newItems = new ArrayList<>();
        boolean success = false;
        for (ItemStack it : toConvert) {
            var type = this.registry.getBlockTypeOf(it.getItem());
            if (type != this.fromType) {
                break;
            } else {
                Item newItem = BlockType.changeItemType(it.getItem(), this.fromType, this.toType);
                if (newItem != null) {
                    newItems.add(it.transmuteCopy(newItem));
                    success = true;
                }
            }
        }
        if (!success) {
            newItems.addAll(toConvert);
        }
        return newItems;
    }

    public List<ItemStack> getMatchingStacks() {
        if (this.items == null) {
            this.items = convertItems(Arrays.asList(this.inner.getItems()));
        }
        return this.items;
    }


    public static final ResourceLocation ID = Moonlight.res("block_type_swap");

    public static final MapCodec<BlockTypeSwapIngredient<?>> CODEC = makeCodec(false);
    public static final MapCodec<BlockTypeSwapIngredient<?>> CODEC_NONEMPTY = makeCodec(true);
    public static final StreamCodec<RegistryFriendlyByteBuf, BlockTypeSwapIngredient<?>> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public BlockTypeSwapIngredient<?> decode(RegistryFriendlyByteBuf object) {
                    Ingredient inner = Ingredient.CONTENTS_STREAM_CODEC.decode(object);
                    BlockTypeRegistry<?> reg = BlockTypeRegistry.getRegistryStreamCodec().decode(object);
                    //this is slower but sends the full id so we have better error logging
                    var slowCodec = reg.getStreamCodecExplicit();
                    try {
                        BlockType from = slowCodec.decode(object);
                        BlockType to = slowCodec.decode(object);
                        return create(inner, from, to, (BlockTypeRegistry<? super BlockType>) reg);
                    } catch (DecoderException e) {
                        throw new RuntimeException("Failed to decode block type swap ingredient", e);
                    }
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, BlockTypeSwapIngredient<?> ing) {
                    Ingredient.CONTENTS_STREAM_CODEC.encode(buf, ing.inner);
                    BlockTypeRegistry.getRegistryStreamCodec().encode(buf, ing.registry);
                    StreamCodec streamCodec = ing.registry.getStreamCodecExplicit();
                    streamCodec.encode(buf, ing.fromType);
                    streamCodec.encode(buf, ing.toType);
                }
            };

    private static @NotNull MapCodec<BlockTypeSwapIngredient<?>> makeCodec(boolean nonEmpty) {
        return new MapCodec<>() {
            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return Stream.of("block_type", "from", "to", "ingredient").map(ops::createString);
            }

            @Override
            public <T> DataResult<BlockTypeSwapIngredient<?>> decode(DynamicOps<T> ops, MapLike<T> input) {
                var ingCodec = nonEmpty ? Ingredient.CODEC_NONEMPTY : Ingredient.CODEC;
                DataResult<Ingredient> ingResult = ingCodec.parse(ops, input.get(ops.createString("ingredient")));
                if (ingResult.isError()) {
                    return DataResult.error(() -> "Failed to decode inner ingredient: " + ingResult.error().get().message() + " on " + input);
                }
                Ingredient inner = ingResult.result().orElseThrow();
                T blockType = input.get(ops.createString("block_type"));
                DataResult<BlockTypeRegistry<?>> blockTypeResult = BlockTypeRegistry.getRegistryCodec().parse(ops, blockType);
                if (blockTypeResult.isError()) {
                    return DataResult.error(() -> "Failed to decode block type registry: " + blockType + " " + blockTypeResult.error().get().message() + " on " + input);
                }
                BlockTypeRegistry<?> reg = blockTypeResult.result().orElseThrow();
                T fromType = ops.createString("from");
                DataResult<?> fromResult = reg.getCodec().parse(ops, input.get(fromType));
                if (fromResult.isError()) {
                    return DataResult.error(() -> "Failed to decode 'from' block type: " + fromType + " " + fromResult.error().get().message() + " on " + input);
                }
                BlockType from = (BlockType) fromResult.result().orElseThrow();
                T toType = ops.createString("to");
                DataResult<?> toResult = reg.getCodec().parse(ops, input.get(toType));
                if (toResult.isError()) {
                    return DataResult.error(() -> "Failed to decode 'to' block type: " + toType + " " + toResult.error().get().message() + " on " + input);
                }
                BlockType to = (BlockType) toResult.result().orElseThrow();
                return DataResult.success(create(inner, from, to, (BlockTypeRegistry<? super BlockType>) reg));
            }

            @Override
            public <T> RecordBuilder<T> encode(BlockTypeSwapIngredient<?> ingr, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                var ingCodec = nonEmpty ? Ingredient.CODEC_NONEMPTY : Ingredient.CODEC;
                prefix.add(ops.createString("ingredient"), ingCodec.encodeStart(ops, ingr.inner));
                prefix.add(ops.createString("block_type"), BlockTypeRegistry.getRegistryCodec().encodeStart(ops, ingr.registry));
                Codec codec = ingr.registry.getCodec();
                prefix.add(ops.createString("from"), codec.encodeStart(ops, ingr.fromType));
                prefix.add(ops.createString("to"), codec.encodeStart(ops, ingr.toType));
                return prefix;
            }
        };
    }


}
