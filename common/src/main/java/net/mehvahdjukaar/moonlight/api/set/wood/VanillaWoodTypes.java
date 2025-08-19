package net.mehvahdjukaar.moonlight.api.set.wood;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;

public class VanillaWoodTypes {

    public static final WoodType OAK = WoodTypeRegistry.INSTANCE.register(
            new WoodType(ResourceLocation.withDefaultNamespace("oak"), Blocks.OAK_PLANKS, Blocks.OAK_LOG));
    public static final WoodType SPRUCE = WoodTypeRegistry.INSTANCE.register(
            new WoodType(ResourceLocation.withDefaultNamespace("spruce"), Blocks.SPRUCE_PLANKS, Blocks.SPRUCE_LOG));
    public static final WoodType BIRCH = WoodTypeRegistry.INSTANCE.register(
            new WoodType(ResourceLocation.withDefaultNamespace("birch"), Blocks.BIRCH_PLANKS, Blocks.BIRCH_LOG));
    public static final WoodType JUNGLE = WoodTypeRegistry.INSTANCE.register(
            new WoodType(ResourceLocation.withDefaultNamespace("jungle"), Blocks.JUNGLE_PLANKS, Blocks.JUNGLE_LOG));
    public static final WoodType ACACIA = WoodTypeRegistry.INSTANCE.register(
            new WoodType(ResourceLocation.withDefaultNamespace("acacia"), Blocks.ACACIA_PLANKS, Blocks.ACACIA_LOG));
    public static final WoodType CHERRY = WoodTypeRegistry.INSTANCE.register(
            new WoodType(ResourceLocation.withDefaultNamespace("cherry"), Blocks.CHERRY_PLANKS, Blocks.CHERRY_LOG));
    public static final WoodType DARK_OAK = WoodTypeRegistry.INSTANCE.register(
            new WoodType(ResourceLocation.withDefaultNamespace("dark_oak"), Blocks.DARK_OAK_PLANKS, Blocks.DARK_OAK_LOG));
    public static final WoodType MANGROVE = WoodTypeRegistry.INSTANCE.register(
            new WoodType(ResourceLocation.withDefaultNamespace("mangrove"), Blocks.MANGROVE_PLANKS, Blocks.MANGROVE_LOG));
    public static final WoodType BAMBOO = WoodTypeRegistry.INSTANCE.register(
            new WoodType(ResourceLocation.withDefaultNamespace("bamboo"), Blocks.BAMBOO_PLANKS, Blocks.BAMBOO_BLOCK));
    public static final WoodType CRIMSON = WoodTypeRegistry.INSTANCE.register(
            new WoodType(ResourceLocation.withDefaultNamespace("crimson"), Blocks.CRIMSON_PLANKS, Blocks.CRIMSON_STEM));
    public static final WoodType WARPED = WoodTypeRegistry.INSTANCE.register(
            new WoodType(ResourceLocation.withDefaultNamespace("warped"), Blocks.WARPED_PLANKS, Blocks.WARPED_STEM));

    static {

        //Ugly ass code here
        MANGROVE.addChild("sapling", Blocks.MANGROVE_PROPAGULE);
        BAMBOO.addChild("sapling", Blocks.BAMBOO_SAPLING);
    }

    static {

        //Ugly ass code here
        MANGROVE.addChild("sapling", Blocks.MANGROVE_PROPAGULE);
    }

}
