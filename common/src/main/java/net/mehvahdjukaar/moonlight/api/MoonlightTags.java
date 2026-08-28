package net.mehvahdjukaar.moonlight.api;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class MoonlightTags {

    public static final TagKey<Block> SHEARABLE_TAG = TagKey.create(Registries.BLOCK, Identifier.parse("mineable/shear"));

}
