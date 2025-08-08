package net.mehvahdjukaar.moonlight.api.set.leaves;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;

public class VanillaLeavesTypes {

    public static final LeavesType OAK = LeavesTypeRegistry.INSTANCE.register(
            new LeavesType(ResourceLocation.parse("oak"), Blocks.OAK_LEAVES));
    public static final LeavesType SPRUCE = LeavesTypeRegistry.INSTANCE.register(
            new LeavesType(ResourceLocation.parse("spruce"), Blocks.SPRUCE_LEAVES));
    public static final LeavesType BIRCH = LeavesTypeRegistry.INSTANCE.register(
            new LeavesType(ResourceLocation.parse("birch"), Blocks.BIRCH_LEAVES));
    public static final LeavesType JUNGLE = LeavesTypeRegistry.INSTANCE.register(
            new LeavesType(ResourceLocation.parse("jungle"), Blocks.JUNGLE_LEAVES));
    public static final LeavesType ACACIA = LeavesTypeRegistry.INSTANCE.register(
            new LeavesType(ResourceLocation.parse("acacia"), Blocks.ACACIA_LEAVES));
    public static final LeavesType CHERRY = LeavesTypeRegistry.INSTANCE.register(
            new LeavesType(ResourceLocation.parse("cherry"), Blocks.CHERRY_LEAVES));
    public static final LeavesType DARK_OAK = LeavesTypeRegistry.INSTANCE.register(
            new LeavesType(ResourceLocation.parse("dark_oak"), Blocks.DARK_OAK_LEAVES));
    public static final LeavesType MANGROVE = LeavesTypeRegistry.INSTANCE.register(
            new LeavesType(ResourceLocation.parse("mangrove"), Blocks.MANGROVE_LEAVES));

}
