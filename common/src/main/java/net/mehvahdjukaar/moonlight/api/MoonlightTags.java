package net.mehvahdjukaar.moonlight.api;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class MoonlightTags {

    public static final TagKey<Block> SHEARABLE_TAG = TagKey.create(Registries.BLOCK, ResourceLocation.parse("mineable/shear"));
    public static final TagKey<Block> NON_RECOLORABLE_BLOCKS_TAG = TagKey.create(Registries.BLOCK, ResourceLocation.parse("non_recolorable"));
    public static final TagKey<Item> NON_RECOLORABLE_ITEMS_TAG = TagKey.create(Registries.ITEM, ResourceLocation.parse("non_recolorable"));

}
