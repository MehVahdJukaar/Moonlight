package net.mehvahdjukaar.moonlight.api.integration;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.set.BlockSetAPI;
import net.mehvahdjukaar.moonlight.api.set.leaves.LeavesType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

//place for all known weird hardcoded wood types from mods that aren't getting detected
@SuppressWarnings("SameParameterValue")
public class CompatWoodTypes {

    public static void init() {

        // The Undergarden
        advancedWoodFinder("undergarden", "ancient_root", "ancient_root_planks", "ancient_root");

        // Mofu's Better End
        advancedWoodFinder("mofus_better_end_", "weepingstar", "weepingstar_planks", "weepingstar_log",
                "STRIPPED_LOG-weepingstar_stripped_log", "LEAVES-weepingstar_leaf");

        advancedWoodFinder("mofus_better_end_", "frost_root", "frost_root_plank", "frost_root_log",
                "STRIPPED_LOG-stripped_frost_root_log");

        // Burnt
        mediumWoodFinder("burnt", "smoldering_bamboo", "smoldering_bamboo_block", "");

        // Botania
        advancedWoodFinder("botania", "livingwood", "livingwood_planks", "livingwood_log",
                "STRIPPED_LOG-stripped_livingwood_log");

        advancedWoodFinder("botania", "dreamwood", "dreamwood_planks", "dreamwood_log",
                "STRIPPED_LOG-stripped_dreamwood_log");

        // Caverns-And-Chasms
        simpleWoodFinder("caverns_and_chasms", "azalea", "azalea_leaves");

        // The Outer End
        advancedWoodFinder("outer_end", "azure", "azure_planks", "_azurestem",
                "STRIPPED_LOG-azure_stripped_stem", "WOOD-azure_pith", "STRIPPED_WOOD-azure_stripped_pith");

        // Upgrade Aquatic
        advancedWoodFinder("upgrade_aquatic", "driftwood", "driftwood_planks", "driftwood_log",
                "STRIPPED_LOG-stripped_driftwood_log", "WOOD-driftwood", "STRIPPED_WOOD-stripped_driftwood");

        // Atmospheric
        advancedWoodFinder("atmospheric", "grimwood", "grimwood_planks", "grimwood_log",
                "STRIPPED_LOG-stripped_grimwood_log", "WOOD-grimwood", "STRIPPED_WOOD-stripped_grimwood");

        advancedWoodFinder("atmospheric", "rosewood", "rosewood_planks", "rosewood_log",
                "STRIPPED_LOG-stripped_rosewood_log", "WOOD-rosewood", "STRIPPED_WOOD-stripped_rosewood");

        // Deeper And Darker
        simplePlanksStemFinder("deeperdarker", "blooming", "bloom_planks");

        // Eternal Tales
        advancedWoodFinder("eternal_tales", "comets", "comets_planks", "comets_log",
                "STRIPPED_LOG-striped_comets_log", "WOOD-comets_wood", "STRIPPED_WOOD-stripped_comets_wood");

        advancedWoodFinder("eternal_tales", "purgatorium", "purgatorium_planks", "purgatorium_log",
                "STRIPPED_LOG-purgatorium_stripped_log", "WOOD-purgatorium_wood", "STRIPPED_WOOD-stripped_purgatorium_wood");

        // Blocks +
        simpleStemFinder("blocksplus", "chorus");
        simpleWoodFinder("blocksplus", "bamboo");
        simpleStemFinder("blocksplus", "mushroom");

        // Integrated Dynamics
        advancedWoodFinder("integrateddynamics", "menril", "menril_planks", "menril_log",
                "STRIPPED_LOG-menril_log_stripped", "WOOD-menril_wood", "STRIPPED_WOOD-menril_wood_stripped");

        // Domum Oranmentum
        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(new ResourceLocation("domum_ornamentum:cactus"),
                new ResourceLocation("domum_ornamentum:green_cactus_extra"), new ResourceLocation("cactus")));
        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(new ResourceLocation("domum_ornamentum:cactus_extra"),
                new ResourceLocation("domum_ornamentum:cactus_extra"), new ResourceLocation("cactus")));

        // Better End
        advancedWoodFinder("betterend", "end_lotus", "end_lotus_planks", "end_lotus_log",
                "STRIPPED_LOG-end_lotus_stripped_log", "WOOD-end_lotus_bark",  "STRIPPED_WOOD-end_lotus_stripped_bark");

        advancedWoodFinder("betterend", "jellyshroom", "jellyshroom_planks", "jellyshroom_log",
                "STRIPPED_LOG-jellyshroom_stripped_log", "WOOD-jellyshroom_bark",  "STRIPPED_WOOD-jellyshroom_stripped_bark");

        advancedWoodFinder("betterend", "lucernia", "lucernia_planks", "lucernia_log",
                "STRIPPED_LOG-lucernia_stripped_log", "WOOD-lucernia_bark",  "STRIPPED_WOOD-lucernia_stripped_bark");

        advancedWoodFinder("betterend", "mossy_glowshroom", "mossy_glowshroom_planks", "mossy_glowshroom_log",
                "STRIPPED_LOG-mossy_glowshroom_stripped_log", "WOOD-mossy_glowshroom_bark",  "STRIPPED_WOOD-mossy_glowshroom_stripped_bark");

        advancedWoodFinder("betterend", "pythadendron", "pythadendron_planks", "pythadendron_log",
                "STRIPPED_LOG-pythadendron_stripped_log", "WOOD-pythadendron_bark",  "STRIPPED_WOOD-pythadendron_stripped_bark");

        advancedWoodFinder("betterend", "dragon_tree", "dragon_tree_planks", "dragon_tree_log",
                "STRIPPED_LOG-dragon_tree_stripped_log", "WOOD-dragon_tree_bark",  "STRIPPED_WOOD-dragon_tree_stripped_bark");

        advancedWoodFinder("betterend", "helix_tree", "helix_tree_planks", "helix_tree_log",
                "STRIPPED_LOG-helix_tree_stripped_log", "WOOD-helix_tree_bark",  "STRIPPED_WOOD-helix_tree_stripped_bark");

        advancedWoodFinder("betterend", "lacugrove", "lacugrove_planks", "lacugrove_log",
                "STRIPPED_LOG-lacugrove_stripped_log", "WOOD-lacugrove_bark",  "STRIPPED_WOOD-lacugrove_stripped_bark");

        advancedWoodFinder("betterend", "tenanea", "tenanea_planks", "tenanea_log",
                "STRIPPED_LOG-tenanea_stripped_log", "WOOD-tenanea_bark",  "STRIPPED_WOOD-tenanea_stripped_bark");

        advancedWoodFinder("betterend", "umbrella_tree", "umbrella_tree_planks", "umbrella_tree_log",
                "STRIPPED_LOG-umbrella_tree_stripped_log", "WOOD-umbrella_tree_bark",  "STRIPPED_WOOD-umbrella_tree_stripped_bark");


        // Better Nether
        advancedWoodFinder("betternether", "anchor_tree", "anchor_tree_planks", "anchor_tree_log",
                "STRIPPED_LOG-anchor_tree_stripped_log", "WOOD-anchor_tree_bark",  "STRIPPED_WOOD-anchor_tree_stripped_bark");

        advancedWoodFinder("betternether", "mushroom_fir", "mushroom_fir_planks", "mushroom_fir_log",
                "STRIPPED_LOG-mushroom_fir_stripped_log", "WOOD-mushroom_fir_bark",  "STRIPPED_WOOD-mushroom_fir_stripped_bark");

        advancedWoodFinder("betternether", "nether_sakura", "nether_sakura_planks", "nether_sakura_log",
                "STRIPPED_LOG-nether_sakura_stripped_log", "WOOD-nether_sakura_bark",  "STRIPPED_WOOD-nether_sakura_stripped_bark");

        advancedWoodFinder("betternether", "rubeus", "rubeus_planks", "rubeus_log",
                "STRIPPED_LOG-rubeus_stripped_log", "WOOD-rubeus_bark",  "STRIPPED_WOOD-rubeus_stripped_bark");

        advancedWoodFinder("betternether", "stalagnate", "stalagnate_planks", "stalagnate_log",
                "STRIPPED_LOG-stalagnate_stripped_log", "WOOD-stalagnate_bark",  "STRIPPED_WOOD-stalagnate_stripped_bark");

        advancedWoodFinder("betternether", "wart", "wart_planks", "wart_log",
                "STRIPPED_LOG-wart_stripped_log", "WOOD-wart_bark",  "STRIPPED_WOOD-wart_stripped_bark");

        advancedWoodFinder("betternether", "willow", "willow_planks", "willow_log",
                "STRIPPED_LOG-willow_stripped_log", "WOOD-willow_bark",  "STRIPPED_WOOD-willow_stripped_bark");


        // Jaden's Nether Expansion
        advancedWoodFinder("netherexp", "claret", "claret_planks", "cerebrage_claret_stem",
                "STRIPPED_LOG-stripped_claret_stem", "WOOD-cerebrage_claret_hyphae", "STRIPPED_WOOD-stripped_claret_hyphae");

        // Piglin Ruins
        mediumWoodFinder("piglin_ruins", "ominous", "ominous_stalk_block", "");


        // Unusual End
        advancedWoodFinder(true, "unusualend", "chorus_cane", "chorus_nest_planks", "chorus_cane_block",
                "STRIPPED_LOG-stripped_chorus_cane_block", "FENCE-chorus_nest_mosaic_fence");

        // Spectrum (FABRIC)
        simplePlanksStemFinder("spectrum", "ivory_noxcap", "ivory_noxwood_planks");
        simplePlanksStemFinder("spectrum", "slate_noxcap", "slate_noxwood_planks");
        simplePlanksStemFinder("spectrum", "ebony_noxcap", "ebony_noxwood_planks");
        simplePlanksStemFinder("spectrum", "chestnut_noxcap", "chestnut_noxwood_planks");

        // Ars Nouveau - Do not add other WoodTypes blc it would create too many block variants using archwood_planks
                        // The WoodTypes below all are using the same planks. There is no solutions
        simplePlanksWoodFinder("ars_nouveau", "blue_archwood", "archwood_planks");

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
        mediumWoodFinder("byg", "embur","embur_pedu", "embur_pedu_top");

        // mcreator mod with typos...
        // Nethers Exoticism
        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "nethers_exoticism", "jabuticaba", "jaboticaba_planks", "jabuticaba_log"));

        // My Nether's Delight
        mediumWoodFinder("mynethersdelight", "powdery", "powdery_block", "");

        // Nourished End
        mediumWoodFinder("nourished_end", "verdant", "verdant_stalk", "verdant_hyphae");

        advancedWoodFinder("nourished_end", "cerulean", "cerulean_planks", "cerulean_stem_thick",
                "STRIPPED_LOG-cerulean_stem_stripped", "WOOD-cerulean_hyphae",  "STRIPPED_WOOD-stripped_cerulean_hyphae");

        // Gardens Of The Dead
        simpleStemFinder("gardens_of_the_dead", "soulblight");

        mediumWoodFinder("gardens_of_the_dead","whistlecane","whistlecane_block", "whistlecane_wood");

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
                "ars_elemental", "yellow_archwood", "yellow_archwood_leaves", "ars_nouveau:archwood"));

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
        if (PlatHelper.isModLoaded("aether_redux")) crystalLeavesWoodType = "aether_redux:crystal";

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
      Below is to simplify the amount of works to add undetected WoodType
    */

    /**
     * @param modId The ID of the mod that WoodType is from
     * @param nameWood Name of WoodType without "_log"
     * @param childrenIds Example: "FENCE-oak_fence", "LEAVES-oak_leaves", "GATE_FENCE-oak_gate_fence"
     *
     * <p>Note: The following are also included in childrenIds:</p>
     * <ul>
     *     <li>stripped_log</li>
     *     <li>wood</li>
     *     <li>stripped_wood</li>
     * </ul>
     */
    private static void simpleWoodFinder(String modId, String nameWood, String... childrenIds) {
        List<@NotNull String> standardChildren = List.of(
                "STRIPPED_LOG-stripped_"+nameWood+"_log",
                "WOOD-"+nameWood+"_wood",
                "STRIPPED_WOOD-stripped_"+nameWood+"_wood");

        String[] combined = Stream.concat(standardChildren.stream(), Arrays.stream(childrenIds)).toArray(String[]::new);

        advancedWoodFinder(false, modId, nameWood, nameWood+"_planks", nameWood+"_log", combined);
    }
    /**
     * <p>Using Planks' name instead of Log's name to detect WoodType's children</p>
     * @param planksId Id of the planks
     * Same as {@link CompatWoodTypes#simpleWoodFinder(String, String, String...)}
     */
    private static void simplePlanksWoodFinder(String modId, String nameWood, String planksId, String...childrenIds) {
        List<@NotNull String> standardChildren = List.of(
                "STRIPPED_LOG-stripped_"+nameWood+"_log",
                "WOOD-"+nameWood+"_wood",
                "STRIPPED_WOOD-stripped_"+nameWood+"_wood");

        String[] combined = Stream.concat(standardChildren.stream(), Arrays.stream(childrenIds)).toArray(String[]::new);

        advancedWoodFinder(true, modId, nameWood, planksId,nameWood+"_log", combined);
    }

    /**
     * @param modId The ID of the mod that WoodType is from
     * @param nameStem Name of WoodType without "_stem"
     * @param childrenIds Example: "FENCE-oak_fence", "LEAVES-oak_leaves", "GATE_FENCE-oak_gate_fence"
     *
     * <p>Note: The following are also included in childrenIds:</p>
     * <ul>
     *     <li>stripped_log</li>
     *     <li>wood</li>
     *     <li>stripped_wood</li>
     * </ul>
     */
    private static void simpleStemFinder(String modId, String nameStem, String... childrenIds) {
        List<@NotNull String> standardChildren = List.of(
                "STRIPPED_LOG-stripped_"+nameStem+"_stem",
                "WOOD-"+nameStem+"_hyphae",
                "STRIPPED_WOOD-stripped_"+nameStem+"_hyphae");

        String[] combined = Stream.concat(standardChildren.stream(), Arrays.stream(childrenIds)).toArray(String[]::new);

        advancedWoodFinder(false, modId, nameStem, nameStem+ "_planks", nameStem+"_stem", combined);
    }
    /**
     * <p>Using Planks' name instead of Stem's name to detect WoodType's children</p>
     * @param planksId Id of the planks
     * Same as {@link CompatWoodTypes#simpleStemFinder(String, String, String...)}
     */
    private static void simplePlanksStemFinder(String modId, String nameStem, String planksId, String...childrenIds) {
        List<@NotNull String> standardChildren = List.of(
                "STRIPPED_LOG-stripped_"+nameStem+"_log",
                "WOOD-"+nameStem+"_wood",
                "STRIPPED_WOOD-stripped_"+nameStem+"_wood");

        String[] combined = Stream.concat(standardChildren.stream(), Arrays.stream(childrenIds)).toArray(String[]::new);

        advancedWoodFinder(true, modId, nameStem, planksId,nameStem+"_stem", combined);
    }

    /**
     * @param modId The ID of the mod that WoodType is from
     * @param nameWood Name of WoodType without "_log"
     * @param logId Id of the log
     * @param childrenIds Example: "FENCE-oak_fence", "LEAVES-oak_leaves", "GATE_FENCE-oak_gate_fence"
     *
     * <p>Note: The following are also included in childrenIds:</p>
     * <ul>
     *     <li>stripped_log</li>
     *     <li>wood</li>
     *     <li>stripped_wood</li>
     * </ul>
     */
    private static void mediumWoodFinder(String modId, String nameWood, String logId, String woodId, String... childrenIds) {
        String wood = "WOOD-"+woodId;
        String stripped_wood = "STRIPPED_WOOD-stripped_"+woodId;

        List<@NotNull String> standardChildren = new ArrayList<>(List.of("STRIPPED_LOG-stripped_" + logId));

        if (!woodId.isEmpty()) {
            standardChildren.add(wood);
            standardChildren.add(stripped_wood);
        }

        String[] combined = Stream.concat(standardChildren.stream(), Arrays.stream(childrenIds)).toArray(String[]::new);

        advancedWoodFinder(false, modId, nameWood, nameWood+"_planks", logId, combined);
    }

    /**
     * @param useNamePlanks Using Planks' name instead of log's name to detect WoodType's children
     * @param modId The ID of the mod that WoodType is from
     * @param nameWoodType Name of WoodType without "_log" or "_stem"
     * @param planksId Id of the planks
     * @param logId Id of the log
     * @param childrenIds Example: "FENCE-oak_fence", "LEAVES-oak_leaves", "GATE_FENCE-oak_gate_fence"
     */
    private static void advancedWoodFinder(boolean useNamePlanks, String modId, String nameWoodType, String planksId, String logId, String ... childrenIds) {
        if (PlatHelper.isModLoaded(modId)) {
            if (useNamePlanks) nameWoodType = planksId.replace("_planks", "");

            WoodType.Finder woodFinder;
            if (planksId.contains(":")) // some addons like ars_elemental are using ars_nouveau's planks
                woodFinder = WoodType.Finder.simple(new ResourceLocation(modId, nameWoodType), new ResourceLocation(planksId), new ResourceLocation(modId, logId));
            else if (logId.contains(":"))
                woodFinder = WoodType.Finder.simple(new ResourceLocation(modId, nameWoodType), new ResourceLocation(modId, planksId), new ResourceLocation(logId));
            else // default
                woodFinder = WoodType.Finder.simple(modId, nameWoodType, planksId, logId);

            if (!Arrays.stream(childrenIds).toList().isEmpty()) {
                for (String currentChild : childrenIds) {
                    String childKey = getChildKeyFrom(currentChild);
                    String blockId = (currentChild.contains("-")) ? currentChild.split("-")[1] : currentChild;

                    ResourceLocation childId = (blockId.contains(":"))
                            ? new ResourceLocation(blockId)
                            : new ResourceLocation(modId, blockId);

                    if (currentChild.contains("-") && childKeySafe.contains(childKey))
                        woodFinder.addChild(childKey, childId);
                    else if (childKeySafe.contains(childKey))
                        woodFinder.addChild(childKey, currentChild);
                    else
                        Moonlight.LOGGER.warn("CompatWoodType: Incorrect childKey - {} for {}", childKey, childId);
                }
            }

            BlockSetAPI.addBlockTypeFinder(WoodType.class, woodFinder);
        }
    }

    private static void advancedWoodFinder(String modId, String nameWoodType, String planksId, String logId, String ... childrenIds) {
        advancedWoodFinder(false, modId, nameWoodType, planksId, logId, childrenIds);
    }

    /// Get the keyword from block: oak_trapdoor, key: trapdoor
    public static String getChildKeyFrom(String childBlock) {
        if (childBlock.contains("-")) {
            String key = childBlock.split("-")[0];
            if (key.equals(key.toUpperCase())) key = key.toLowerCase();
            return key;
        }

        String lastword = childBlock.substring(childBlock.lastIndexOf("_") + 1);
        return switch (lastword) {
            case "leaves", "leaf" -> "leaves";
            case "plank" -> "planks";
            case "fence_gate" -> "fence_gate";
            default -> lastword;
        };
    }

    protected static final Set<String> childKeySafe = Set.of(
            "log", "planks", "stripped_log", "wood", "stripped_wood",
            "slab", "stairs", "fence",
            "fence_gate", "door", "trapdoor",
            "button", "pressure_plate", "hanging_sign",
            "wall_hanging_sign", "sign", "wall_sign",
            "leaves"
    );

}