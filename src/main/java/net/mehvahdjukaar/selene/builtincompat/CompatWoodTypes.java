package net.mehvahdjukaar.selene.builtincompat;

import net.mehvahdjukaar.selene.block_set.BlockSetManager;
import net.mehvahdjukaar.selene.block_set.wood.WoodType;
import net.minecraft.world.level.block.Block;

public class CompatWoodTypes {

    public static void init() {

            // Pokecube Legends
        BlockSetManager.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "pokecube_legends", "concrete", "concrete_planks", "concrete_log"));

            // Terraqueous
        BlockSetManager.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "terraqueous", "storm_cloud", "storm_cloud", "storm_cloud_column"));
        BlockSetManager.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "terraqueous", "light_cloud", "light_cloud", "light_cloud_column"));
        BlockSetManager.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "terraqueous", "dense_cloud", "dense_cloud", "dense_cloud_column"));

        BlockSetManager.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "the_bumblezone", "beehive_beeswax", "beehive_beeswax", "filled_porous_honeycomb_block"));

            // Nourished End
        var verdant = WoodType.Finder.simple(
                "nourished_end", "verdant", "verdant_planks", "verdant_stalk");
        verdant.addChild("wood", "verdant_hyphae");
        verdant.addChild("stripped_wood", "stripped_verdant_hyphae");
        verdant.addChild("stripped_log", "stripped_verdant_stem");
        BlockSetManager.addBlockTypeFinder(WoodType.class, verdant);

        var cerulean = WoodType.Finder.simple(
                "nourished_end", "cerulean", "cerulean_planks", "cerulean_stem_thick");
        cerulean.addChild("stripped_wood", "stripped_cerulean_hyphae");
        cerulean.addChild("stripped_log", "cerulean_stem_stripped");
        BlockSetManager.addBlockTypeFinder(WoodType.class, cerulean);

            // Twigs
        var bamboo = WoodType.Finder.simple(
                "twigs", "bamboo", "stripped_bamboo_planks", "bundled_bamboo");

        bamboo.addChild("stripped_log", "stripped_bundled_bamboo");
        BlockSetManager.addBlockTypeFinder(WoodType.class, bamboo);

            // Habitat
        BlockSetManager.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "habitat", "fairy_ring_mushroom", "fairy_ring_mushroom_planks", "enhanced_fairy_ring_mushroom_stem"));

            // Ecologics
        var azalea = WoodType.Finder.simple(
                "ecologics", "flowering_azalea", "flowering_azalea_planks", "flowering_azalea_log");
        azalea.addChild("stripped_log", "stripped_azalea_log");
        BlockSetManager.addBlockTypeFinder(WoodType.class, azalea);

            // Phantasm
        var phantasm = WoodType.Finder.simple(
                "phantasm", "ebony","ebony_planks", "ebony_packed_stems");
        phantasm.addChild("stripped_log", "stripped_ebony_packed_stems");
        BlockSetManager.addBlockTypeFinder(WoodType.class, phantasm);


    }
}
