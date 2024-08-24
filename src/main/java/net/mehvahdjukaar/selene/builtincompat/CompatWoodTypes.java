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

            // Enlightened End
        var ceruleanStalk = WoodType.Finder.simple(
                "enlightened_end", "cerulean", "cerulean_planks", "cerulean_log");
        ceruleanStalk.addChild("stripped_log", "stripped_cerulean_stalk_block");
        BlockSetManager.addBlockTypeFinder(WoodType.class, ceruleanStalk);

        var indigoStem = WoodType.Finder.simple(
                "enlightened_end", "indigo", "indigo_planks", "indigo_stem");
        indigoStem.addChild("stripped_log", "stripped_indigo_stem");
        BlockSetManager.addBlockTypeFinder(WoodType.class, indigoStem);

            // Oh The Biomes You'll Go
        var sythianStem = WoodType.Finder.simple(
                "byg", "sythian", "sythian_planks", "sythian_stem");
        sythianStem.addChild("stripped_log", "stripped_sythian_stem");
        BlockSetManager.addBlockTypeFinder(WoodType.class, sythianStem);

        var bulbisStem = WoodType.Finder.simple(
                "byg", "bulbis", "bulbis_planks", "bulbis_stem");
        bulbisStem.addChild("stripped_log", "stripped_bulbis_stem");
        BlockSetManager.addBlockTypeFinder(WoodType.class, bulbisStem);

            // Enhanced Mushroom
        var brownMushroom = WoodType.Finder.simple(
                "enhanced_mushrooms", "brown_mushroom", "brown_mushroom_planks", "brown_mushroom_stem");
        brownMushroom.addChild("stripped_log", "stripped_brown_mushroom_stem");
        BlockSetManager.addBlockTypeFinder(WoodType.class, brownMushroom);

        var redMushroom = WoodType.Finder.simple(
                "enhanced_mushrooms", "red_mushroom", "red_mushroom_planks", "red_mushroom_stem");
        redMushroom.addChild("stripped_log", "stripped_red_mushroom_stem");
        BlockSetManager.addBlockTypeFinder(WoodType.class, redMushroom);

    }
}
