package net.mehvahdjukaar.moonlight.api.integration;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.set.BlockSetAPI;
import net.mehvahdjukaar.moonlight.api.set.leaves.LeavesType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

//place for all known weird hardcoded wood types from mods that aren't getting detected
@SuppressWarnings("SameParameterValue")
public class CompatWoodTypes {

    public static void init() {

        // Botania
        BlockSetAPI.addBlockTypeFinder(WoodType.class,
                advancedWoodFinder("botania", "livingwood", "livingwood_planks",
                        "log", "",
                        "log", ""));

        BlockSetAPI.addBlockTypeFinder(WoodType.class,
                advancedWoodFinder("botania", "dreamwood", "dreamwood_planks",
                        "log", "",
                        "log", ""));

        // Caverns-And-Chasms
        var ccAzalea = simpleWoodFinder("caverns_and_chasms", "azalea");
        ccAzalea.addChild("leaves", new ResourceLocation("minecraft:azalea_leaves"));
        BlockSetAPI.addBlockTypeFinder(WoodType.class, ccAzalea);

        // The Outer End
        BlockSetAPI.addBlockTypeFinder(WoodType.class,
                advancedWoodFinder("outer_end", "azure", "azure_planks",
                        "stem", "pith",
                        "stripped_stem", "stripped_pith"));

        // Upgrade Aquatic
        BlockSetAPI.addBlockTypeFinder(WoodType.class,
                advancedWoodFinder("upgrade_aquatic", "driftwood", "driftwood_planks",
                        "log", "",
                        "log", ""));
        // Atmospheric
        BlockSetAPI.addBlockTypeFinder(WoodType.class,
                advancedWoodFinder("atmospheric", "grimwood", "grimwood_planks",
                        "log", "",
                        "log", ""));

        BlockSetAPI.addBlockTypeFinder(WoodType.class,
                advancedWoodFinder("atmospheric", "rosewood", "rosewood_planks",
                        "log", "",
                        "log", ""));

        // Deeper And Darker
        BlockSetAPI.addBlockTypeFinder(WoodType.class,
                altSimpleStemFinder("deeperdarker", "blooming", "bloom"));

        // Eternal Tales
        BlockSetAPI.addBlockTypeFinder(WoodType.class,
                advancedWoodFinder("eternal_tales", "comets", "comets_planks", "log", "wood", "striped_comets_log", ""));

        BlockSetAPI.addBlockTypeFinder(WoodType.class,
                advancedWoodFinder("eternal_tales", "purgatorium", "purgatorium_planks", "log", "wood", "stripped_log", ""));

        // Blocks +
        BlockSetAPI.addBlockTypeFinder(WoodType.class,
                simpleStemFinder("blocksplus", "chorus"));
        BlockSetAPI.addBlockTypeFinder(WoodType.class,
                simpleWoodFinder( "blocksplus", "bamboo"));
        BlockSetAPI.addBlockTypeFinder(WoodType.class,
                simpleStemFinder( "blocksplus", "mushroom"));

        // Integrated Dynamics
        BlockSetAPI.addBlockTypeFinder(WoodType.class,
                advancedWoodFinder("integrateddynamics", "menril", "menril_planks",
                        "log", "wood",
                        "log_stripped", "wood_stripped"));

        // Domum Oranmentum
        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(new ResourceLocation("domum_ornamentum:cactus"),
                new ResourceLocation("domum_ornamentum:green_cactus_extra"), new ResourceLocation("cactus")));
        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(new ResourceLocation("domum_ornamentum:cactus_extra"),
                new ResourceLocation("domum_ornamentum:cactus_extra"), new ResourceLocation("cactus")));

        // Better End
        BlockSetAPI.addBlockTypeFinder(WoodType.class,
                advancedWoodFinder("betterend", "end_lotus", "end_lotus_planks",
                        "log", "bark",
                        "stripped_log", "stripped_bark"));
        BlockSetAPI.addBlockTypeFinder(WoodType.class,
                advancedWoodFinder("betterend", "jellyshroom", "jellyshroom_planks",
                        "log", "bark",
                        "stripped_log", "stripped_bark"));

        BlockSetAPI.addBlockTypeFinder(WoodType.class,
                advancedWoodFinder("betterend", "lucernia", "lucernia_planks",
                        "log", "bark",
                        "stripped_log", "stripped_bark"));
        BlockSetAPI.addBlockTypeFinder(WoodType.class,
                advancedWoodFinder("betterend", "mossy_glowshroom", "mossy_glowshroom_planks",
                        "log", "bark",
                        "stripped_log", "stripped_bark"));
        BlockSetAPI.addBlockTypeFinder(WoodType.class,
                advancedWoodFinder("betterend", "pythadendron", "pythadendron_planks",
                        "log", "bark",
                        "stripped_log", "stripped_bark"));
        BlockSetAPI.addBlockTypeFinder(WoodType.class,
                advancedWoodFinder("betterend", "dragon_tree", "dragon_tree_planks",
                        "log", "bark",
                        "stripped_log", "stripped_bark"));
        BlockSetAPI.addBlockTypeFinder(WoodType.class,
                advancedWoodFinder("betterend", "helix_tree", "helix_tree_planks",
                        "log", "bark",
                        "stripped_log", "stripped_bark"));
        BlockSetAPI.addBlockTypeFinder(WoodType.class,
                advancedWoodFinder("betterend", "lacugrove", "lacugrove_planks",
                        "log", "bark",
                        "stripped_log", "stripped_bark"));
        BlockSetAPI.addBlockTypeFinder(WoodType.class,
                advancedWoodFinder("betterend", "tenanea", "tenanea_planks",
                        "log", "bark",
                        "stripped_log", "stripped_bark"));
        BlockSetAPI.addBlockTypeFinder(WoodType.class,
                advancedWoodFinder("betterend", "umbrella_tree", "umbrella_tree_planks",
                        "log", "bark",
                        "stripped_log", "stripped_bark"));

        // Better Nether
        BlockSetAPI.addBlockTypeFinder(WoodType.class,
                advancedWoodFinder("betternether", "anchor_tree", "anchor_tree_planks",
                        "log", "bark",
                        "stripped_log", "stripped_bark"));
        BlockSetAPI.addBlockTypeFinder(WoodType.class,
                advancedWoodFinder("betternether", "mushroom_fir", "mushroom_fir_planks",
                        "log", "bark",
                        "stripped_log", "stripped_bark"));
        BlockSetAPI.addBlockTypeFinder(WoodType.class,
                advancedWoodFinder("betternether", "nether_sakura", "nether_sakura_planks",
                        "log", "bark",
                        "stripped_log", "stripped_bark"));
        BlockSetAPI.addBlockTypeFinder(WoodType.class,
                advancedWoodFinder("betternether", "rubeus", "rubeus_planks",
                        "log", "bark",
                        "stripped_log", "stripped_bark"));
        BlockSetAPI.addBlockTypeFinder(WoodType.class,
                advancedWoodFinder("betternether", "stalagnate", "stalagnate_planks",
                        "log", "bark",
                        "stripped_log", "stripped_bark"));
        BlockSetAPI.addBlockTypeFinder(WoodType.class,
                advancedWoodFinder("betternether", "wart", "wart_planks",
                        "log", "bark",
                        "stripped_log", "stripped_bark"));
        BlockSetAPI.addBlockTypeFinder(WoodType.class,
                advancedWoodFinder("betternether", "willow", "willow_planks",
                        "log", "bark",
                        "stripped_log", "stripped_bark"));

        // Jaden's Nether Expansion
        BlockSetAPI.addBlockTypeFinder(WoodType.class,
                advancedWoodFinder("netherexp", "cerebrage_claret", "claret_planks",
                        "stem", "hyphae",
                        "netherexp:stripped_claret_stem", "netherexp:stripped_claret_hyphae")
        );

        // Piglin Ruins
        BlockSetAPI.addBlockTypeFinder(WoodType.class,
                mediumWoodFinder("piglin_ruins", "ominous", "stalk_block", "")
        );


        // Unusual End
        var chorus_cane = altMediumWoodFinder("unusualend", "chorus_cane", "chorus_nest", "block", "");
        chorus_cane.addChild("fence", new ResourceLocation("unusualend:chorus_nest_mosaic_fence"));
        BlockSetAPI.addBlockTypeFinder(WoodType.class, chorus_cane);


        // Spectrum (FABRIC)
        BlockSetAPI.addBlockTypeFinder(WoodType.class,
                altSimpleStemFinder("spectrum", "ivory_noxcap", "ivory_noxwood"));
        BlockSetAPI.addBlockTypeFinder(WoodType.class,
                altSimpleStemFinder("spectrum", "slate_noxcap", "slate_noxwood"));
        BlockSetAPI.addBlockTypeFinder(WoodType.class,
                altSimpleStemFinder("spectrum", "ebony_noxcap", "ebony_noxwood"));
        BlockSetAPI.addBlockTypeFinder(WoodType.class,
                altSimpleStemFinder("spectrum", "chestnut_noxcap", "chestnut_noxwood"));

        // Ars Nouveau - Do not add other WoodTypes blc it would create too many block variants using archwood_planks
                        // The WoodTypes below all are using the same planks. There is no solutions
        BlockSetAPI.addBlockTypeFinder(WoodType.class,
                altSimpleWoodFinder("ars_nouveau", "blue_archwood", "archwood"));

//        BlockSetAPI.addBlockTypeFinder(WoodType.class,
//                generalWoodFinder(false, "ars_nouveau", "red_archwood", "archwood_planks", true));
//
//        BlockSetAPI.addBlockTypeFinder(WoodType.class,
//                generalWoodFinder(false, "ars_nouveau", "purple_archwood", "archwood_planks", true));
//
//        BlockSetAPI.addBlockTypeFinder(WoodType.class,
//                generalWoodFinder(false, "ars_nouveau", "green_archwood", "archwood_planks", true));

        // Ars Elemental
//        BlockSetAPI.addBlockTypeFinder(WoodType.class,
//                uniqueWoodFinder("ars_elemental", "yellow_archwood", "ars_nouveau:archwood_planks", "log", ""));

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
        BlockSetAPI.addBlockTypeFinder(WoodType.class,
                mediumWoodFinder("byg", "embur","pedu", "pedu_top"));

        // mcreator mod with typos...
        // Nethers Exoticism
        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "nethers_exoticism", "jabuticaba", "jaboticaba_planks", "jabuticaba_log"));

        // My Nether's Delight
        BlockSetAPI.addBlockTypeFinder(WoodType.class,
                mediumWoodFinder("mynethersdelight", "powdery", "block", ""));

        // Nourished End
        BlockSetAPI.addBlockTypeFinder(WoodType.class,
                mediumWoodFinder("nourished_end", "verdant", "stalk", "hyphae"));

        BlockSetAPI.addBlockTypeFinder(WoodType.class,
                advancedWoodFinder("nourished_end", "cerulean", "cerulean_planks",
                        "stem_thick", "hyphae",
                        "stem_stripped", ""));

        // Gardens Of The Dead
        BlockSetAPI.addBlockTypeFinder(WoodType.class,
                simpleStemFinder("gardens_of_the_dead", "soulblight"));

        BlockSetAPI.addBlockTypeFinder(WoodType.class,
                mediumWoodFinder("gardens_of_the_dead","whistlecane","block", ""));

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
        floweringAzalea.addChild("leaves", new ResourceLocation("minecraft:flowering_azalea_leaves"));
        BlockSetAPI.addBlockTypeFinder(WoodType.class, floweringAzalea);

        var azalea = WoodType.Finder.simple(
                "ecologics", "azalea", "azalea_planks", "azalea_log");
        azalea.addChild("leaves", new ResourceLocation("minecraft:azalea_leaves"));
        BlockSetAPI.addBlockTypeFinder(WoodType.class, azalea);

        // Quark
        var quarkAzalea = WoodType.Finder.simple(
                "quark", "azalea", "azalea_planks", "azalea_log");
        quarkAzalea.addChild("leaves", new ResourceLocation("minecraft:azalea_leaves"));
        BlockSetAPI.addBlockTypeFinder(WoodType.class, quarkAzalea);


//!! LEAVES
        // Mystic's Biomes
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "mysticsbiomes", "yellow_maple", "yellow_maple_leaves", "mysticsbiomes:white_maple"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "mysticsbiomes", "orange_maple", "orange_maple_leaves", "mysticsbiomes:maple"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "mysticsbiomes", "pink_cherry", "pink_cherry_blossoms", "mysticsbiomes:cherry"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "mysticsbiomes", "white_cherry", "white_cherry_blossoms", "mysticsbiomes:cherry"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "mysticsbiomes", "strawberry", "strawberry_blossoms", "mysticsbiomes:strawberry"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "mysticsbiomes", "peach", "peach_leaves", "mysticsbiomes:peach"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "mysticsbiomes", "jacaranda", "jacaranda_blossoms", "mysticsbiomes:jacaranda"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "mysticsbiomes", "sea_shrub", "sea_shrub_leaves", "mysticsbiomes:sea_foam"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "mysticsbiomes", "tropical", "tropical_leaves", "mysticsbiomes:tropical"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "mysticsbiomes", "peony", "peony_leaves", "oak"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "mysticsbiomes", "hydrangea", "hydrangea_leaves", "oak"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "mysticsbiomes", "budding_peony", "budding_peony_leaves", "oak"));

        // Environmental
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "environmental", "pink_wisteria", "pink_wisteria_leaves", "environmental:wisteria"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "environmental", "blue_wisteria", "blue_wisteria_leaves", "environmental:wisteria"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "environmental", "purple_wisteria", "purple_wisteria_leaves", "environmental:wisteria"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "environmental", "white_wisteria", "white_wisteria_leaves", "environmental:wisteria"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "environmental", "cheerful_plum", "cheerful_plum_leaves", "environmental:plum"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "environmental", "moody_plum", "moody_plum_leaves", "environmental:plum"));

        // Ecologics
        var coconut = LeavesType.Finder.simple("ecologics", "coconut", "coconut_leaves", "ecologics:coconut");
        coconut.addChild("sapling", new ResourceLocation("ecologics:coconut_seedling"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, coconut);

        // Ars Nouveau
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "ars_nouveau", "blue_archwood", "blue_archwood_leaves", "ars_nouveau:archwood"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "ars_nouveau", "green_archwood", "green_archwood_leaves", "ars_nouveau:archwood"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "ars_nouveau", "purple_archwood", "purple_archwood_leaves", "ars_nouveau:archwood"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "ars_nouveau", "red_archwood", "red_archwood_leaves", "ars_nouveau:archwood"));
        
        // Ars Elemental
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "ars_nouveau", "yellow_archwood", "yellow_archwood_leaves", "ars_nouveau:archwood"));

        // BIOMES O' PLENTY
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "biomesoplenty", "origin", "origin_leaves", "oak"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "biomesoplenty", "flowering_oak", "flowering_oak_leaves", "oak"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "biomesoplenty", "red_maple", "red_maple_leaves", "maple"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "biomesoplenty", "orange_maple", "orange_maple_leaves", "maple"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "biomesoplenty", "yellow_maple", "yellow_maple_leaves", "maple"));
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

        // AETHER
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "aether", "golden_oak", "golden_oak_leaves", "aether:skyroot"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "aether", "holiday", "holiday_leaves", "aether:skyroot"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "aether", "decorated_holiday", "decorated_holiday_leaves", "aether:skyroot"));


        String crystalLeavesWoodType = "aether:skyroot";
        // Aether Redux replaces the skyroot logs in crystal trees with their own crystal logs
        if (PlatHelper.isModLoaded("aether_redux")){
            crystalLeavesWoodType = "aether_redux:crystal";
        }
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "aether", "crystal", "crystal_leaves", crystalLeavesWoodType));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "aether", "crystal_fruit", "crystal_fruit_leaves", crystalLeavesWoodType));

        // AETHER REDUX
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "aether_redux", "fieldsproot", "fieldsproot_leaves", "aether_redux:fieldsproot"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "aether_redux", "gilded_oak", "gilded_oak_leaves", "aether:skyroot"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "aether_redux", "blighted_skyroot", "blighted_skyroot_leaves", "aether:skyroot"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "aether_redux", "purple_glacia", "purple_glacia_leaves", "aether_redux:glacia"));

        // DEEP AETHER
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "deep_aether", "flowering_roseroot", "flowering_roseroot_leaves", "deep_aether:roseroot"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "deep_aether", "blue_roseroot", "blue_roseroot_leaves", "deep_aether:roseroot"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "deep_aether", "flowering_blue_roseroot", "flowering_blue_roseroot_leaves", "deep_aether:roseroot"));

        // ANCIENT AETHER
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "ancient_aether", "crystal_skyroot", "crystal_skyroot_leaves", "aether:skyroot"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "ancient_aether", "enchanted_skyroot", "enchanted_skyroot_leaves", "aether:skyroot"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "ancient_aether", "skyroot_pine", "skyroot_pine_leaves", "aether:skyroot"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "ancient_aether", "blue_skyroot_pine", "blue_skyroot_pine_leaves", "aether:skyroot"));

        // AETHER GENESIS
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "aether_genesis", "blue_skyroot", "blue_skyroot_leaves", "aether:skyroot"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "aether_genesis", "dark_blue_skyroot", "dark_blue_skyroot_leaves", "aether:skyroot"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "aether_genesis", "purple_crystal", "purple_crystal_leaves", crystalLeavesWoodType));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "aether_genesis", "purple_crystal_fruit", "purple_crystal_fruit_leaves", crystalLeavesWoodType));

        // AUTUMNITY
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "autumnity", "yellow_maple", "yellow_maple_leaves", "autumnity:maple"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "autumnity", "orange_maple", "orange_maple_leaves", "autumnity:maple"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "autumnity", "red_maple", "red_maple_leaves", "autumnity:maple"));

        // ALEXSCAVES
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "alexscaves", "ancient", "ancient_leaves", "jungle"));
    }

    /*
      Below just reduce the amount of works to add undetected wood
      Its the same as the code used to add the undetected wood:
        var undetectedWoodType = WoodType.Finder.simple("modId", "nameWood", "namePlanks", "nameLog");
        undetectedWoodType.addChild("stripped_log", "nameStrippedLog");
        undetectedWoodType.addChild("wood", "nameWood");
        undetectedWoodType.addChild("stripped_wood", "nameStrippedWood");
        BlockSetAPI.addBlockTypeFinder(WoodType.class, undetected);
    */

    /**
     * @param modId The ID of the mod that WoodType is from
     * @param nameWood Name of WoodType without "_log"
     * The rest of WoodType's children will be detected
     */
    private static WoodType.@NotNull Finder simpleWoodFinder(String modId, String nameWood) {
        return advancedWoodFinder(modId, nameWood, nameWood+ "_planks", "stem", "hyphae", "", "");
    }

    private static WoodType.@NotNull Finder simpleStemFinder(String modId, String nameStem) {
        return advancedWoodFinder(modId, nameStem, nameStem+ "_planks", "stem", "hyphae", "", "");
    }

    /**
     * MediumWoodFinder include log, wood
     * @param nameWood Name of WoodType without "_log"
     * @param suffixLog Example: "stem","block" or "TYPE_stem" or "stem_TYPE"
     * @param suffixWood Example: "wood", "hyphae" or "TYPE_wood" or "wood_TYPE"
     */
    private static WoodType.@NotNull Finder mediumWoodFinder(String modId, String nameWood, String suffixLog, String suffixWood) {
        return advancedWoodFinder(modId, nameWood, nameWood + "_planks", suffixLog, suffixWood, "", "");
    }

    private static WoodType.@NotNull Finder advancedWoodFinder(String modId, String nameWood, String planksId,
                                                               String suffixLog, String suffixWood, String suffixStrippedLog, String suffixStrippedWood) {
        return advancedWoodFinder(false, modId, nameWood, planksId, suffixLog, suffixWood, suffixStrippedLog, suffixStrippedWood);
    }

    /**
     * AdvancedWoodFinder include planks, log, wood, stripped_log, stripped_wood based on their unique Ids
     * @param useNamePlanks useNamePlanks instead of WoodType's name for children
     * @param modId The ID of mod where WoodType is from
     * @param nameWood Name of WoodType without the suffix, "_log"
     * @param planksId Example: "TYPE_planks" or "modId:TYPE_planks"
     * @param suffixLog Example: "stem","block" or "TYPE_stem" or "stem_TYPE"
     * @param suffixWood Example: "wood", "hyphae" or "TYPE_wood" or "wood_TYPE"
     * @param suffixStrippedLog Example: "stem","block" or "TYPE_stripped_log"
     * @param suffixStrippedWood Example: "wood", "hyphae" or "TYPE_stripped_wood"
     */
    private static WoodType.@NotNull Finder advancedWoodFinder(boolean useNamePlanks, String modId, String nameWood, String planksId,
                                                               String suffixLog, String suffixWood, String suffixStrippedLog, String suffixStrippedWood) {
        // Creating Ids of log & stripped_log
        String logPrefixed = (suffixStrippedLog.matches("(\\w+)?(striped|stripped)_\\w+")) ? "" : "stripped_";
        String logSuffixed = (suffixLog.isBlank()) ? "" : "_" + suffixLog;

        String log = (suffixLog.contains("_")) ? suffixLog : nameWood + logSuffixed;
        String stripped_log = (suffixStrippedLog.isBlank()) ? logPrefixed + log : logPrefixed + nameWood +"_"+ suffixStrippedLog;

        // Creating Ids of wood & stripped_wood
        String woodPrefixed = (suffixStrippedWood.matches("(\\w+)?(striped|stripped)_\\w+")) ? "" : "stripped_";
        String woodSuffixed = (suffixWood.isBlank()) ? "" : "_" + suffixWood;

        String wood = nameWood + woodSuffixed;
        String stripped_wood = (suffixStrippedWood.isBlank()) ? woodPrefixed + wood : woodPrefixed + nameWood +"_"+ suffixStrippedWood;

        if (useNamePlanks) nameWood = planksId.replace("_planks", "");

        // Adding blocks to WoodType
        WoodType.Finder wf;
        if (planksId.contains(":")) // some addons like ars_elemental are using ars_nouveau's planks
            wf = WoodType.Finder.simple(new ResourceLocation(modId, nameWood), new ResourceLocation(planksId), new ResourceLocation(modId, log));
        else
            wf = WoodType.Finder.simple(modId, nameWood, planksId, log);

        // WoodType.Finder has a null check for below, so don't worry about it
        wf.addChild("wood", wood);
        if (suffixStrippedLog.contains(":"))
            wf.addChild("stripped_log", new ResourceLocation(suffixStrippedLog));
        else
            wf.addChild("stripped_log", stripped_log);

        if (suffixStrippedWood.contains(":"))
            wf.addChild("stripped_wood", new ResourceLocation(suffixStrippedWood));
        else
            wf.addChild("stripped_wood", stripped_wood);

        return wf;
    }

    /**
     * ALTERNATIVE: Use Planks' name instead of log's name for WoodType's children
     * @param nameWood Name of WoodType without "_log"
     * @param namePlanks Name of Planks without "_planks"
    **/
    private static WoodType.@NotNull Finder altSimpleWoodFinder(String modId, String nameWood, String namePlanks) {
        String planksId = namePlanks + "_planks";

        return advancedWoodFinder(true, modId, nameWood, planksId, "log", "wood", "", "");

    }
    /**
     * Similar to altSimpleWoodFinder (above) but this is for Stem
     * @param nameWood Name of WoodType without "_stem"
     * @param namePlanks Name of Planks without "_planks"
     **/
    private static WoodType.@NotNull Finder altSimpleStemFinder(String modId, String nameWood, String namePlanks) {
        String planksId = namePlanks + "_planks";

        return advancedWoodFinder(true, modId, nameWood, planksId, "stem", "hyphae", "", "");
    }
    /**
     * Similar to AltSimpleWoodFinder (above) but included suffix for log and wood
     * @param nameWood Name of WoodType without "_log"
     * @param namePlanks Name of Planks without "_planks"
     * @param suffixLog Example: "stem","block" or "TYPE_log"
     * @param suffixWood Example: "wood", "hyphae" or "TYPE_wood"
    **/
    private static WoodType.@NotNull Finder altMediumWoodFinder(String modId, String nameWood, String namePlanks, String suffixLog, String suffixWood) {
        String planksId = namePlanks + "_planks";

        return advancedWoodFinder(true, modId, nameWood, planksId, suffixLog, suffixWood, "", "");

    }

}