package net.mehvahdjukaar.selene.builtincompat;

import net.mehvahdjukaar.selene.block_set.BlockSetManager;
import net.mehvahdjukaar.selene.block_set.wood.WoodType;
import net.minecraft.resources.ResourceLocation;

public class CompatWoodTypes {

    public static void init(){
        BlockSetManager.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "pokecube_legends","concrete", "concrete_planks", "concrete_log"));
        BlockSetManager.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "terraqueous","storm_cloud", "storm_cloud", "storm_cloud_column"));
        BlockSetManager.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "terraqueous","light_cloud", "light_cloud", "light_cloud_column"));
        BlockSetManager.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "terraqueous","dense_cloud", "dense_cloud", "dense_cloud_column"));
        BlockSetManager.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "twigs","stripped_bamboo", "stripped_bamboo_planks" , "bundled_bamboo"));
        BlockSetManager.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                new ResourceLocation("culturaldelights:avocado"),
                new ResourceLocation("minecraft:jungle_planks"),
                new ResourceLocation("culturaldelights:avocado_log")));
    }
}
