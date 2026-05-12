package net.mehvahdjukaar.moonlight.api.misc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.map.MapDataRegistry;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.api.util.codec.AlternativeCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public record BlockAndItem(@Nullable Block block, @Nullable Item item) {

    public static BlockAndItem forBlock(Block block) {
        Item i = block.asItem();
        if (i == Items.AIR) i = null;
        return new BlockAndItem(block, i);
    }

    public static BlockAndItem forItem(Item item) {
        Block block = null;
        if (item instanceof BlockItem b) {
            block = b.getBlock();
        }
        return new BlockAndItem(block, item);
    }

    private static final Codec<BlockAndItem> DIRECT_CODEC = RecordCodecBuilder.create(i -> i.group(
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(BlockAndItem::block),
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(BlockAndItem::item)
    ).apply(i, BlockAndItem::new));


    public static final Codec<BlockAndItem> CODEC = new AlternativeCodec<>(DIRECT_CODEC,
            BuiltInRegistries.BLOCK.byNameCodec().xmap(BlockAndItem::forBlock, BlockAndItem::block),
            BuiltInRegistries.ITEM.byNameCodec().xmap(BlockAndItem::forItem, BlockAndItem::item));

}
