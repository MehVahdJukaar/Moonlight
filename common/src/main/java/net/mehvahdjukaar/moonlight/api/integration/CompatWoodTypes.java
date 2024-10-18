package net.mehvahdjukaar.moonlight.api.integration;

import net.mehvahdjukaar.moonlight.api.set.BlockSetAPI;
import net.mehvahdjukaar.moonlight.api.set.leaves.LeavesType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

//place for all known weird hardcoded wood types from mods that aren't getting detected
public class CompatWoodTypes {

    public static void init() {

        // Domum Oranmentum
        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(ResourceLocation.parse("domum_ornamentum:cactus"),
                ResourceLocation.parse("domum_ornamentum:green_cactus_extra"), ResourceLocation.parse("cactus")));
        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(ResourceLocation.parse("domum_ornamentum:cactus_extra"),
                ResourceLocation.parse("domum_ornamentum:cactus_extra"), ResourceLocation.parse("cactus")));

        // spectrum
        BlockSetAPI.addBlockTypeFinder(WoodType.class, spectrumWood("ebony"));
        BlockSetAPI.addBlockTypeFinder(WoodType.class, spectrumWood("slate"));
        BlockSetAPI.addBlockTypeFinder(WoodType.class, spectrumWood("chestnut"));
        BlockSetAPI.addBlockTypeFinder(WoodType.class, spectrumWood("ivory"));

        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple("spectrum",
                "ebony_noxwood", "ebony_noxwood_planks", "ebony_noxcap_stem"));

        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple("spectrum",
                "chestnut_noxwood", "chestnut_noxwood_planks", "chestnut_noxcap_stem"));
        // Ars Nouveau
        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "ars_nouveau", "archwood", "archwood_planks", "blue_archwood_log"));

        // Blue Skies
        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "blue_skies", "crystallized", "crystallized_planks", "crystallized_log"));

        // Darker Depths
        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "darkerdepths", "petrified", "petrified_planks", "petrified_log"));

        // Pokecube Legends
        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "pokecube_legends", "concrete", "concrete_planks", "concrete_log"));

        // Terraqueous
        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "terraqueous", "storm_cloud", "storm_cloud", "storm_cloud_column"));
        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "terraqueous", "light_cloud", "light_cloud", "light_cloud_column"));
        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "terraqueous", "dense_cloud", "dense_cloud", "dense_cloud_column"));

        // Rats
        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "rats", "pirat", "pirat_planks", "pirat_log"));

        // Oh The Biomes You'll Go
        var embur = WoodType.Finder.simple(
                "byg", "embur", "embur_planks", "embur_pedu");
        embur.addChild("stripped_log", "stripped_embur_pedu");
        embur.addChild("wood", "embur_pedu_top");
        embur.addChild("stripped_wood", "stripped_embur_pedu_top");
        BlockSetAPI.addBlockTypeFinder(WoodType.class, embur);

        // mcreator mod with typos...
        // Nethers Exoticism
        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "nethers_exoticism", "jabuticaba", "jaboticaba_planks", "jabuticaba_log"));

        // My Nether's Delight
        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "mynethersdelight", "powdery", "powdery_planks", "block_of_powdery_cannon"
        ));

        // Nourished End
        var verdant = WoodType.Finder.simple(
                "nourished_end", "verdant", "verdant_planks", "verdant_stalk");
        verdant.addChild("wood", "verdant_hyphae");
        verdant.addChild("stripped_wood", "stripped_verdant_hyphae");
        verdant.addChild("stripped_log", "stripped_verdant_stem");
        BlockSetAPI.addBlockTypeFinder(WoodType.class, verdant);

        var cerulean = WoodType.Finder.simple(
                "nourished_end", "cerulean", "cerulean_planks", "cerulean_stem_thick");
        cerulean.addChild("stripped_wood", "stripped_cerulean_hyphae");
        cerulean.addChild("stripped_log", "cerulean_stem_stripped");
        BlockSetAPI.addBlockTypeFinder(WoodType.class, cerulean);

        // Gardens Of The Dead
        var soulblight = WoodType.Finder.simple("gardens_of_the_dead",
                "soulblight", "soulblight_planks", "soulblight_stem");
        cerulean.addChild("stripped_wood", "stripped_soulblight_hyphae");
        cerulean.addChild("wood", "soulblight_hyphae");
        cerulean.addChild("stripped_log", "stripped_soulblight_stem");
        BlockSetAPI.addBlockTypeFinder(WoodType.class, soulblight);

        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple("gardens_of_the_dead",
                "whistlecane", "whistlecane_block", "whistlecane"));

        // Desolation
        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple("desolation",
                "charred", "charredlog", "charred_planks"));

        // Damn Of Time Builder
        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple("dawnoftimebuilder",
                "waxed_oak", "waxed_oak_log_stripped", "waxed_oak_planks"));
        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple("dawnoftimebuilder",
                "charred_spruce", "charred_spruce_log_stripped", "charred_spruce_planks"));


        // Habitat
        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "habitat", "fairy_ring_mushroom", "fairy_ring_mushroom_planks", "enhanced_fairy_ring_mushroom_stem"));

        // Ecologics
        var floweringAzalea = WoodType.Finder.simple(
                "ecologics", "flowering_azalea", "flowering_azalea_planks", "flowering_azalea_log");
        floweringAzalea.addChild("stripped_log", "stripped_azalea_log");
        floweringAzalea.addChild("leaves", ResourceLocation.parse("minecraft:flowering_azalea_leaves"));
        BlockSetAPI.addBlockTypeFinder(WoodType.class, floweringAzalea);

        var azalea = WoodType.Finder.simple(
                "ecologics", "azalea", "azalea_planks", "azalea_log");
        azalea.addChild("leaves", ResourceLocation.parse("minecraft:azalea_leaves"));
        BlockSetAPI.addBlockTypeFinder(WoodType.class, azalea);

        // Quark
        var quarkAzalea = WoodType.Finder.simple(
                "quark", "azalea", "azalea_planks", "azalea_log");
        quarkAzalea.addChild("leaves", ResourceLocation.parse("minecraft:azalea_leaves"));

        BlockSetAPI.addBlockTypeFinder(WoodType.class, quarkAzalea);


// LEAVES
        // Ecologics
        var coconut = LeavesType.Finder.simple("ecologics", "coconut", "coconut_leaves", "ecologics:coconut");
        coconut.addChild("sapling", ResourceLocation.parse("ecologics:coconut_seedling"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, coconut);

        // ARS NOUVEAU
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "ars_nouveau", "blue_archwood", "blue_archwood_leaves", "ars_nouveau:archwood"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "ars_nouveau", "green_archwood", "green_archwood_leaves", "ars_nouveau:archwood"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "ars_nouveau", "purple_archwood", "purple_archwood_leaves", "ars_nouveau:archwood"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "ars_nouveau", "red_archwood", "red_archwood_leaves", "ars_nouveau:archwood"));

        // BIOMES O' PLENTY
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "biomesoplenty", "origin", "origin_leaves", "oak"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "biomesoplenty", "flowering_oak", "flowering_oak_leaves", "oak"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "biomesoplenty", "maple", "maple_leaves", "oak"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "biomesoplenty", "orange_autumn", "orange_autumn_leaves", "dark_oak"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "biomesoplenty", "yellow_autumn", "yellow_autumn_leaves", "birch"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "biomesoplenty", "rainbow_birch", "rainbow_birch_leaves", "birch"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "biomesoplenty", "snowblossom", "snowblossom_leaves", "cherry"));

        // BLUE SKIES
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "blue_skies", "crystallized", "crystallized_leaves", "blue_skies:crystallized"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "blue_skies", "crescent_fruit", "crescent_fruit_leaves", "blue_skies:dusk"));

        // COLORFUL AZALEAS
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "colorfulazaleas", "blue_azalea", "blue_azalea_leaves", "colorfulazaleas:azule_azalea"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "colorfulazaleas", "blue_blooming_azalea", "blue_blooming_azalea_leaves", "colorfulazaleas:azule_azalea"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "colorfulazaleas", "blue_flowering_azalea", "blue_flowering_azalea_leaves", "colorfulazaleas:azule_azalea"));

        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "colorfulazaleas", "orange_azalea", "orange_azalea_leaves", "colorfulazaleas:tecal_azalea"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "colorfulazaleas", "orange_blooming_azalea", "orange_blooming_azalea_leaves", "colorfulazaleas:tecal_azalea"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "colorfulazaleas", "orange_flowering_azalea", "orange_flowering_azalea_leaves", "colorfulazaleas:tecal_azalea"));

        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "colorfulazaleas", "pink_azalea", "pink_azalea_leaves", "colorfulazaleas:bright_azalea"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "colorfulazaleas", "pink_blooming_azalea", "pink_blooming_azalea_leaves", "colorfulazaleas:bright_azalea"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "colorfulazaleas", "pink_flowering_azalea", "pink_flowering_azalea_leaves", "colorfulazaleas:bright_azalea"));

        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "colorfulazaleas", "purple_azalea", "purple_azalea_leaves", "colorfulazaleas:walnut_azalea"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "colorfulazaleas", "purple_blooming_azalea", "purple_blooming_azalea_leaves", "colorfulazaleas:walnut_azalea"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "colorfulazaleas", "purple_flowering_azalea", "purple_flowering_azalea_leaves", "colorfulazaleas:walnut_azalea"));

        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "colorfulazaleas", "red_azalea", "red_azalea_leaves", "colorfulazaleas:roze_azalea"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "colorfulazaleas", "red_blooming_azalea", "red_blooming_azalea_leaves", "colorfulazaleas:roze_azalea"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "colorfulazaleas", "red_flowering_azalea", "red_flowering_azalea_leaves", "colorfulazaleas:roze_azalea"));

        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "colorfulazaleas", "white_azalea", "white_azalea_leaves", "colorfulazaleas:titanium_azalea"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "colorfulazaleas", "white_blooming_azalea", "white_blooming_azalea_leaves", "colorfulazaleas:titanium_azalea"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "colorfulazaleas", "white_flowering_azalea", "white_flowering_azalea_leaves", "colorfulazaleas:titanium_azalea"));

        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "colorfulazaleas", "yellow_azalea", "yellow_azalea_leaves", "colorfulazaleas:fiss_azalea"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "colorfulazaleas", "yellow_blooming_azalea", "yellow_blooming_azalea_leaves", "colorfulazaleas:fiss_azalea"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "colorfulazaleas", "yellow_flowering_azalea", "yellow_flowering_azalea_leaves", "colorfulazaleas:fiss_azalea"));

        // POKECUBE LEGENDS
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "pokecube_legends", "dyna_pastel_pink", "dyna_leaves_pastel_pink", "pokecube_legends:aged"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "pokecube_legends", "dyna_pink", "dyna_leaves_pink", "pokecube_legends:aged"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "pokecube_legends", "dyna_red", "dyna_leaves_red", "pokecube_legends:aged"));

        // REGIONS UNEXPLORED
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "regions_unexplored", "bamboo", "bamboo_leaves", "jungle"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "regions_unexplored", "golden_larch", "golden_larch_leaves", "regions_unexplored:larch"));

        // TERRAQUEOUS
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "terraqueous", "apple", "apple_leaves", "terraqueous:apple"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "terraqueous", "banana", "banana_leaves", "terraqueous:banana"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "terraqueous", "cherry", "cherry_leaves", "terraqueous:cherry"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "terraqueous", "coconut", "coconut_leaves", "terraqueous:coconut"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "terraqueous", "lemon", "lemon_leaves", "terraqueous:lemon"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "terraqueous", "mango", "mango_leaves", "terraqueous:mango"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "terraqueous", "mulberry", "mulberry_leaves", "terraqueous:mulberry"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "terraqueous", "orange", "orange_leaves", "terraqueous:orange"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "terraqueous", "peach", "peach_leaves", "terraqueous:peach"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "terraqueous", "pear", "pear_leaves", "terraqueous:pear"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "terraqueous", "plum", "plum_leaves", "terraqueous:plum"));

        // THE TWILIGHT FOREST
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "twilightforest", "beanstalk", "beanstalk_leaves", "twilightforest:twilight_oak"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "twilightforest", "thorn", "thorn_leaves", "twilightforest:twilight_oak"));

        // ULTERLANDS
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "ulterlands", "souldrained", "souldrained_leaves", "oak"));

        // CHIPPED
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "apple_acacia", "apple_acacia_leaves", "acacia"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "apple_acacia", "apple_acacia_leaves", "acacia"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "cherry_acacia", "cherry_acacia_leaves", "acacia"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "dead_acacia", "dead_acacia_leaves", "acacia"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "frosted_acacia", "frosted_acacia_leaves", "acacia"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "golden_acacia", "golden_acacia_leaves", "acacia"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "golden_cherry_acacia", "golden_cherry_acacia_leaves", "acacia"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "magenta_flower_acacia", "magenta_flower_acacia_leaves", "acacia"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "orange_acacia", "orange_acacia_leaves", "acacia"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "red_acacia", "red_acacia_leaves", "acacia"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "white_flower_acacia", "white_flower_acacia_leaves", "acacia"));

        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "apple_birch", "apple_birch_leaves", "birch"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "cherry_birch", "cherry_birch_leaves", "birch"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "dead_birch", "dead_birch_leaves", "birch"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "frosted_birch", "frosted_birch_leaves", "birch"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "golden_birch", "golden_birch_leaves", "birch"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "golden_cherry_birch", "golden_cherry_birch_leaves", "birch"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "magenta_flower_birch", "magenta_flower_birch_leaves", "birch"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "orange_birch", "orange_birch_leaves", "birch"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "red_birch", "red_birch_leaves", "birch"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "white_flower_birch", "white_flower_birch_leaves", "birch"));

        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "apple_dark_oak", "apple_dark_oak_leaves", "dark_oak"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "cherry_dark_oak", "cherry_dark_oak_leaves", "dark_oak"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "dead_dark_oak", "dead_dark_oak_leaves", "dark_oak"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "frosted_dark_oak", "frosted_dark_oak_leaves", "dark_oak"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "golden_dark_oak", "golden_dark_oak_leaves", "dark_oak"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "golden_cherry_dark_oak", "golden_cherry_dark_oak_leaves", "dark_oak"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "magenta_flower_dark_oak", "magenta_flower_dark_oak_leaves", "dark_oak"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "orange_dark_oak", "orange_dark_oak_leaves", "dark_oak"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "red_dark_oak", "red_dark_oak_leaves", "dark_oak"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "white_flower_dark_oak", "white_flower_dark_oak_leaves", "dark_oak"));

        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "apple_jungle", "apple_jungle_leaves", "jungle"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "cherry_jungle", "cherry_jungle_leaves", "jungle"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "dead_jungle", "dead_jungle_leaves", "jungle"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "frosted_jungle", "frosted_jungle_leaves", "jungle"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "golden_jungle", "golden_jungle_leaves", "jungle"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "golden_cherry_jungle", "golden_cherry_jungle_leaves", "jungle"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "magenta_flower_jungle", "magenta_flower_jungle_leaves", "jungle"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "orange_jungle", "orange_jungle_leaves", "jungle"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "red_jungle", "red_jungle_leaves", "jungle"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "white_flower_jungle", "white_flower_jungle_leaves", "jungle"));

        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "apple_oak", "apple_oak_leaves", "oak"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "cherry_oak", "cherry_oak_leaves", "oak"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "dead_oak", "dead_oak_leaves", "oak"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "frosted_oak", "frosted_oak_leaves", "oak"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "golden_oak", "golden_oak_leaves", "oak"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "golden_cherry_oak", "golden_cherry_oak_leaves", "oak"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "magenta_flower_oak", "magenta_flower_oak_leaves", "oak"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "orange_oak", "orange_oak_leaves", "oak"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "red_oak", "red_oak_leaves", "oak"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "white_flower_oak", "white_flower_oak_leaves", "oak"));

        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "apple_spruce", "apple_spruce_leaves", "spruce"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "cherry_spruce", "cherry_spruce_leaves", "spruce"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "dead_spruce", "dead_spruce_leaves", "spruce"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "frosted_spruce", "frosted_spruce_leaves", "spruce"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "golden_spruce", "golden_spruce_leaves", "spruce"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "golden_cherry_spruce", "golden_cherry_spruce_leaves", "spruce"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "magenta_flower_spruce", "magenta_flower_spruce_leaves", "spruce"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "orange_spruce", "orange_spruce_leaves", "spruce"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "red_spruce", "red_spruce_leaves", "spruce"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "chipped", "white_flower_spruce", "white_flower_spruce_leaves", "spruce"));
    }

    private static WoodType.@NotNull Finder spectrumWood(String name) {
        var w = WoodType.Finder.simple("spectrum",
                name + "_noxwood", name + "_noxwood_planks", "_noxcap_stem");
        w.addChild("stripped_log", "stripped_" + name + "_noxcap_stem");
        w.addChild("wood", name + "_noxcap_hyphae");
        w.addChild("stripped_wood", "stripped_" + name + "_noxcap_hyphae");
        return w;
    }
}
