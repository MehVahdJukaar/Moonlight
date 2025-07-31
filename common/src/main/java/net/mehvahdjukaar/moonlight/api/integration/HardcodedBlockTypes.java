package net.mehvahdjukaar.moonlight.api.integration;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.set.BlockSetAPI;
import net.mehvahdjukaar.moonlight.api.set.leaves.LeavesType;
import net.mehvahdjukaar.moonlight.api.set.leaves.LeavesTypeRegistry;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.ApiStatus;

//TODO: move out of api package
//place for all known weird hardcoded wood types from mods that aren't getting detected

// SO this has become unmanageable, its shouldnt be that every time a new weird wood is found this mod has to be updated
// EC is the main mod that uses this so this shit when its found can be added there instead for basically same effect
// also the sheer amount of stuff here is huge, if there are some common conventions those shouldbe added to the wood type reg instead
@ApiStatus.Internal
public class HardcodedBlockTypes {

    public static void init() {

        WoodTypeRegistry woodReg = WoodTypeRegistry.INSTANCE;
        LeavesTypeRegistry leafReg = LeavesTypeRegistry.INSTANCE;

        // Mofu's Better End
        //TODO:replace all with stuff like this
        woodReg.addSimpleFinder("mofus_better_end_", "weepingstar")
                .childBlockSuffix(LEAVES, "_leaf");

        //why stripped log?
        advancedWoodFinder("mofus_better_end_", "weepingstar", "weepingstar_planks", "weepingstar_log",
                "STRIPPED_LOG-weepingstar_stripped_log", "LEAVES-weepingstar_leaf");

        woodReg.addSimpleFinder("mofus_better_end_", "frost_root")
                .planksSuffix("_plank");

        advancedWoodFinder("mofus_better_end_", "frost_root", "frost_root_plank", "frost_root_log",
                "STRIPPED_LOG-stripped_frost_root_log");


        // Burnt
        mediumWoodFinder("burnt", "smoldering_bamboo", "smoldering_bamboo_block", "");

        //TODO: why is this needed?
        // Botania
        advancedWoodFinder("botania", "livingwood", "livingwood_planks", "livingwood_log",
                "STRIPPED_LOG-stripped_livingwood_log");

        advancedWoodFinder("botania", "dreamwood", "dreamwood_planks", "dreamwood_log",
                "STRIPPED_LOG-stripped_dreamwood_log");

        // Caverns-And-Chasms
        simpleWoodFinder("caverns_and_chasms", "azalea", "azalea_leaves");

        // The Outer End
        advancedWoodFinder("outer_end", "azure", "azure_planks", "azure_stem",
                "STRIPPED_LOG-azure_stripped_stem", "WOOD-azure_pith", "STRIPPED_WOOD-azure_stripped_pith");

        //TODO: also handled. remove
        /*
        // Upgrade Aquatic
        advancedWoodFinder("upgrade_aquatic", "driftwood", "driftwood_planks", "driftwood_log",
                "STRIPPED_LOG-stripped_driftwood_log", "WOOD-driftwood", "STRIPPED_WOOD-stripped_driftwood");

        // Atmospheric
        advancedWoodFinder("atmospheric", "grimwood", "grimwood_planks", "grimwood_log",
                "STRIPPED_LOG-stripped_grimwood_log", "WOOD-grimwood", "STRIPPED_WOOD-stripped_grimwood");

        advancedWoodFinder("atmospheric", "rosewood", "rosewood_planks", "rosewood_log",
                "STRIPPED_LOG-stripped_rosewood_log", "WOOD-rosewood", "STRIPPED_WOOD-stripped_rosewood");
*/

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
        woodReg.addSimpleFinder("domum_ornamentum", "cactus")
                .planks("green_cactus_extra")
                .log(() -> Blocks.CACTUS);
        woodReg.addSimpleFinder("domum_ornamentum", "cactus_extra")
                .planks("cactus_extra")
                .log(() -> Blocks.CACTUS);


        //TODO:all barks are also taken care of. remove
        /*
        // Better End
        advancedWoodFinder("betterend", "end_lotus", "end_lotus_planks", "end_lotus_log",
                "STRIPPED_LOG-end_lotus_stripped_log", "WOOD-end_lotus_bark", "STRIPPED_WOOD-end_lotus_stripped_bark");

        advancedWoodFinder("betterend", "jellyshroom", "jellyshroom_planks", "jellyshroom_log",
                "STRIPPED_LOG-jellyshroom_stripped_log", "WOOD-jellyshroom_bark", "STRIPPED_WOOD-jellyshroom_stripped_bark");

        advancedWoodFinder("betterend", "lucernia", "lucernia_planks", "lucernia_log",
                "STRIPPED_LOG-lucernia_stripped_log", "WOOD-lucernia_bark", "STRIPPED_WOOD-lucernia_stripped_bark");

        advancedWoodFinder("betterend", "mossy_glowshroom", "mossy_glowshroom_planks", "mossy_glowshroom_log",
                "STRIPPED_LOG-mossy_glowshroom_stripped_log", "WOOD-mossy_glowshroom_bark", "STRIPPED_WOOD-mossy_glowshroom_stripped_bark");

        advancedWoodFinder("betterend", "pythadendron", "pythadendron_planks", "pythadendron_log",
                "STRIPPED_LOG-pythadendron_stripped_log", "WOOD-pythadendron_bark", "STRIPPED_WOOD-pythadendron_stripped_bark");

        advancedWoodFinder("betterend", "dragon_tree", "dragon_tree_planks", "dragon_tree_log",
                "STRIPPED_LOG-dragon_tree_stripped_log", "WOOD-dragon_tree_bark", "STRIPPED_WOOD-dragon_tree_stripped_bark");

        advancedWoodFinder("betterend", "helix_tree", "helix_tree_planks", "helix_tree_log",
                "STRIPPED_LOG-helix_tree_stripped_log", "WOOD-helix_tree_bark", "STRIPPED_WOOD-helix_tree_stripped_bark");

        advancedWoodFinder("betterend", "lacugrove", "lacugrove_planks", "lacugrove_log",
                "STRIPPED_LOG-lacugrove_stripped_log", "WOOD-lacugrove_bark", "STRIPPED_WOOD-lacugrove_stripped_bark");

        advancedWoodFinder("betterend", "tenanea", "tenanea_planks", "tenanea_log",
                "STRIPPED_LOG-tenanea_stripped_log", "WOOD-tenanea_bark", "STRIPPED_WOOD-tenanea_stripped_bark");

        advancedWoodFinder("betterend", "umbrella_tree", "umbrella_tree_planks", "umbrella_tree_log",
                "STRIPPED_LOG-umbrella_tree_stripped_log", "WOOD-umbrella_tree_bark", "STRIPPED_WOOD-umbrella_tree_stripped_bark");


        // Better Nether
        advancedWoodFinder("betternether", "anchor_tree", "anchor_tree_planks", "anchor_tree_log",
                "STRIPPED_LOG-anchor_tree_stripped_log", "WOOD-anchor_tree_bark", "STRIPPED_WOOD-anchor_tree_stripped_bark");

        advancedWoodFinder("betternether", "mushroom_fir", "mushroom_fir_planks", "mushroom_fir_log",
                "STRIPPED_LOG-mushroom_fir_stripped_log", "WOOD-mushroom_fir_bark", "STRIPPED_WOOD-mushroom_fir_stripped_bark");

        advancedWoodFinder("betternether", "nether_sakura", "nether_sakura_planks", "nether_sakura_log",
                "STRIPPED_LOG-nether_sakura_stripped_log", "WOOD-nether_sakura_bark", "STRIPPED_WOOD-nether_sakura_stripped_bark");

        advancedWoodFinder("betternether", "rubeus", "rubeus_planks", "rubeus_log",
                "STRIPPED_LOG-rubeus_stripped_log", "WOOD-rubeus_bark", "STRIPPED_WOOD-rubeus_stripped_bark");

        advancedWoodFinder("betternether", "stalagnate", "stalagnate_planks", "stalagnate_log",
                "STRIPPED_LOG-stalagnate_stripped_log", "WOOD-stalagnate_bark", "STRIPPED_WOOD-stalagnate_stripped_bark");

        advancedWoodFinder("betternether", "wart", "wart_planks", "wart_log",
                "STRIPPED_LOG-wart_stripped_log", "WOOD-wart_bark", "STRIPPED_WOOD-wart_stripped_bark");

        advancedWoodFinder("betternether", "willow", "willow_planks", "willow_log",
                "STRIPPED_LOG-willow_stripped_log", "WOOD-willow_bark", "STRIPPED_WOOD-willow_stripped_bark");
*/

        // Jaden's Nether Expansion
        woodReg.addSimpleFinder("netherexp", "claret")
                .log("cerebrage_claret_stem")
                .childBlock(WOOD, "cerebrage_claret_hyphae");

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
        woodReg.addSimpleFinder("blue_skies", "crystallized");

        // Darker Depths
        // needed because it has stone properties
        woodReg.addSimpleFinder("darkerdepths", "petrified");

        // Pokecube Legends
        //TODO: is this needed?
        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "pokecube_legends", "concrete", "concrete_planks", "concrete_log"));

        // Terraqueous
        //TODO: are these even woods???
        woodReg.addSimpleFinder("terraqueous", "storm_cloud")
                .planks("storm_cloud")
                .log("storm_cloud_column");
        woodReg.addSimpleFinder("terraqueous", "light_cloud")
                .planks("light_cloud")
                .log("light_cloud_column");
        woodReg.addSimpleFinder("terraqueous", "dense_cloud")
                .planks("dense_cloud")
                .log("dense_cloud_column");

        // Rats
        //TODO:shouldn this get detected?
        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "rats", "pirat", "pirat_planks", "pirat_log"));

        // Oh The Biomes You'll Go
        mediumWoodFinder("byg", "embur", "embur_pedu", "embur_pedu_top");

        // mcreator mod with typos...
        // Nethers Exoticism
        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "nethers_exoticism", "jabuticaba", "jaboticaba_planks", "jabuticaba_log"));

        // My Nether's Delight
        mediumWoodFinder("mynethersdelight", "powdery", "powdery_block", "");

        // Nourished End
        mediumWoodFinder("nourished_end", "verdant", "verdant_stalk", "verdant_hyphae");

        advancedWoodFinder("nourished_end", "cerulean", "cerulean_planks", "cerulean_stem_thick",
                "STRIPPED_LOG-cerulean_stem_stripped", "WOOD-cerulean_hyphae", "STRIPPED_WOOD-stripped_cerulean_hyphae");

        // Gardens Of The Dead
        simpleStemFinder("gardens_of_the_dead", "soulblight");

        mediumWoodFinder("gardens_of_the_dead", "whistlecane", "whistlecane_block", "whistlecane_wood");

        // Desolation
        woodReg.addSimpleFinder("desolation", "charred")
                .planks("charred_planks")
                .log("charredlog");
        //TODO:huh? is it inverted?
        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple("desolation",
                "charred", "charredlog", "charred_planks")); //huh? inverted?

        // Damn Of Time Builder
        //TODO: the names here seem completely off or inverted
        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple("dawnoftimebuilder",
                "waxed_oak", "waxed_oak_log_stripped", "waxed_oak_planks"));
        //here too
        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple("dawnoftimebuilder",
                "charred_spruce", "charred_spruce_log_stripped", "charred_spruce_planks"));

        // Habitat
        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "habitat", "fairy_ring_mushroom", "fairy_ring_mushroom_planks", "enhanced_fairy_ring_mushroom_stem"));

        // Ecologics
        woodReg.addSimpleFinder("ecologics", "flowering_azalea")
                .child(LEAVES, () -> Blocks.FLOWERING_AZALEA_LEAVES);

        woodReg.addSimpleFinder("ecologics", "azalea")
                .child(LEAVES, () -> Blocks.AZALEA_LEAVES);

        // Quark
        woodReg.addSimpleFinder("quark", "azalea")
                .child(LEAVES, () -> Blocks.AZALEA_LEAVES);


//!! LEAVES
        // Mystic's Biomes
        leafReg.addLeavesToWoodMapping("mysticsbiomes", "yellow_maple", "white_maple");

        leafReg.addSimpleFinder("mysticsbiomes", "pink_cherry")
                .leavesSuffix("_blossoms");
        leafReg.addSimpleFinder("mysticsbiomes", "white_cherry")
                .leavesSuffix("_blossoms");
        leafReg.addSimpleFinder("mysticsbiomes", "strawberry")
                .leavesSuffix("_blossoms");
        leafReg.addSimpleFinder("mysticsbiomes", "peach")
                .leavesSuffix("_blossoms");
        leafReg.addSimpleFinder("mysticsbiomes", "jacaranda")
                .leavesSuffix("_blossoms");
        leafReg.addLeavesToWoodMapping("mysticsbiomes", "sea_shrub", "sea_foam");

//TODO: are these correct? oak? shouldnt it be left empty?
        leafReg.addLeavesToWoodMapping("mysticsbiomes:peony", "minecraft:oak");
        leafReg.addLeavesToWoodMapping("mysticsbiomes:hydrangea", "minecraft:oak");
        leafReg.addLeavesToWoodMapping("mysticsbiomes:budding_peony", "minecraft:oak");

        // Environmental
        //TODO: also taken care of. remove
     /*
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
     */

        // Ecologics
        leafReg.addSimpleFinder("ecologics", "coconut")
                .childBlock(SAPLING, "coconut_seedling");

        // Ars Nouveau
        //also done
        /*
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "ars_nouveau", "blue_archwood", "blue_archwood_leaves", "ars_nouveau:archwood"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "ars_nouveau", "green_archwood", "green_archwood_leaves", "ars_nouveau:archwood"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "ars_nouveau", "purple_archwood", "purple_archwood_leaves", "ars_nouveau:archwood"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "ars_nouveau", "red_archwood", "red_archwood_leaves", "ars_nouveau:archwood"));
*/
        // Ars Elemental
        //DONE TOO
        //BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
        //        "ars_elemental", "yellow_archwood", "yellow_archwood_leaves", "ars_nouveau:archwood"));

        // BIOMES O' PLENTY
        leafReg.addLeavesToWoodMapping("biomesoplenty:origin", "minecraft:oak");
        //TODO:  done too
        /*
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
*/
        // BLUE SKIES
        //TODO???
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "blue_skies", "crystallized", "crystallized_leaves", "blue_skies:crystallized"));
        leafReg.addLeavesToWoodMapping("blue_skies", "crescent_fruit", "dusk");


        // COLORFUL AZALEAS
        leafReg.addLeavesToWoodMapping("colorfulazaleas", "blue_azalea", "azule_azalea");
        leafReg.addLeavesToWoodMapping("colorfulazaleas", "blue_blooming_azalea", "azule_azalea");
        leafReg.addLeavesToWoodMapping("colorfulazaleas", "blue_flowering_azalea", "azule_azalea");

        leafReg.addLeavesToWoodMapping("colorfulazaleas", "orange_azalea", "tecal_azalea");
        leafReg.addLeavesToWoodMapping("colorfulazaleas", "orange_blooming_azalea", "tecal_azalea");
        leafReg.addLeavesToWoodMapping("colorfulazaleas", "orange_flowering_azalea", "tecal_azalea");

        leafReg.addLeavesToWoodMapping("colorfulazaleas", "pink_azalea", "bright_azalea");
        leafReg.addLeavesToWoodMapping("colorfulazaleas", "pink_blooming_azalea", "bright_azalea");
        leafReg.addLeavesToWoodMapping("colorfulazaleas", "pink_flowering_azalea", "bright_azalea");

        leafReg.addLeavesToWoodMapping("colorfulazaleas", "purple_azalea", "walnut_azalea");
        leafReg.addLeavesToWoodMapping("colorfulazaleas", "purple_blooming_azalea", "walnut_azalea");
        leafReg.addLeavesToWoodMapping("colorfulazaleas", "purple_flowering_azalea", "walnut_azalea");

        leafReg.addLeavesToWoodMapping("colorfulazaleas", "red_azalea", "roze_azalea");
        leafReg.addLeavesToWoodMapping("colorfulazaleas", "red_blooming_azalea", "roze_azalea");
        leafReg.addLeavesToWoodMapping("colorfulazaleas", "red_flowering_azalea", "roze_azalea");

        leafReg.addLeavesToWoodMapping("colorfulazaleas", "white_azalea", "titanium_azalea");
        leafReg.addLeavesToWoodMapping("colorfulazaleas", "white_blooming_azalea", "titanium_azalea");
        leafReg.addLeavesToWoodMapping("colorfulazaleas", "white_flowering_azalea", "titanium_azalea");

        leafReg.addLeavesToWoodMapping("colorfulazaleas", "yellow_azalea", "fiss_azalea");
        leafReg.addLeavesToWoodMapping("colorfulazaleas", "yellow_blooming_azalea", "fiss_azalea");
        leafReg.addLeavesToWoodMapping("colorfulazaleas", "yellow_flowering_azalea", "fiss_azalea");

        // POKECUBE LEGENDS
        leafReg.addSimpleFinder("pokecube_legends", "dyna_pastel_pink")
                .leaves("dyna_leaves_pastel_pink")
                .equivalentWood("pokecube_legends:aged");
        leafReg.addSimpleFinder("pokecube_legends", "dyna_pink")
                .leaves("dyna_leaves_pink")
                .equivalentWood("pokecube_legends:aged");
        leafReg.addSimpleFinder("pokecube_legends", "dyna_red")
                .leaves("dyna_leaves_red")
                .equivalentWood("pokecube_legends:aged");

        // REGIONS UNEXPLORED
        leafReg.addLeavesToWoodMapping("regions_unexplored:bamboo", "minecraft:jungle");
        //BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
        //      "regions_unexplored", "golden_larch", "golden_larch_leaves", "regions_unexplored:larch"));

        // TERRAQUEOUS
        //TODO:why is this needed?
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
        leafReg.addLeavesToWoodMapping("twilightforest", "beanstalk", "twilight_oak");
        leafReg.addLeavesToWoodMapping("twilightforest", "thorn", "twilight_oak");

        // ULTERLANDS
        leafReg.addLeavesToWoodMapping("ulterlands:souldrained", "minecraft:oak");

        // AETHER
        leafReg.addLeavesToWoodMapping("aether", "golden_oak", "skyroot");
        leafReg.addLeavesToWoodMapping("aether", "holiday", "skyroot");
        leafReg.addLeavesToWoodMapping("aether", "decorated_holiday", "skyroot");

        // Aether Redux replaces the skyroot logs in crystal trees with their own crystal logs
        String crystalLeavesWoodType = PlatHelper.isModLoaded("aether_redux") ?
        "aether_redux:crystal" :  "aether:skyroot";

        //TODO: are these correct?
        leafReg.addLeavesToWoodMapping("aether:crystal", crystalLeavesWoodType);
        leafReg.addLeavesToWoodMapping("aether:crystal_fruit_leaves", crystalLeavesWoodType);

        //TODO: why is this needed?
        // AETHER REDUX
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "aether_redux", "fieldsproot", "fieldsproot_leaves", "aether_redux:fieldsproot"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "aether_redux", "gilded_oak", "gilded_oak_leaves", "aether:skyroot"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "aether_redux", "blighted_skyroot", "blighted_skyroot_leaves", "aether:skyroot"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "aether_redux", "purple_glacia", "purple_glacia_leaves", "aether_redux:glacia"));

        // AETHER GENESIS
        //TODO: taken care of remove
        /*
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "aether_genesis", "blue_skyroot", "blue_skyroot_leaves", "aether:skyroot"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "aether_genesis", "dark_blue_skyroot", "dark_blue_skyroot_leaves", "aether:skyroot"));
        */
        leafReg.addLeavesToWoodMapping("aether_genesis:purple_crystal", crystalLeavesWoodType);
        leafReg.addLeavesToWoodMapping("aether_genesis:purple_crystal_fruit", crystalLeavesWoodType);


        // DEEP AETHER
        //TODO: taken care of
        /*
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "deep_aether", "flowering_roseroot", "flowering_roseroot_leaves", "deep_aether:roseroot"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "deep_aether", "blue_roseroot", "blue_roseroot_leaves", "deep_aether:roseroot"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "deep_aether", "flowering_blue_roseroot", "flowering_blue_roseroot_leaves", "deep_aether:roseroot"));
*/

        // ANCIENT AETHER
        leafReg.addLeavesToWoodMapping("ancient_aether", "crystal_skyroot", "skyroot");
        leafReg.addLeavesToWoodMapping("ancient_aether", "enchanted_skyroot", "skyroot");
        leafReg.addLeavesToWoodMapping("ancient_aether", "skyroot_pine", "skyroot");
        leafReg.addLeavesToWoodMapping("ancient_aether", "blue_skyroot_pine", "skyroot");



        // AUTUMNITY
        //TODO: taken care of remove
        /*
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "autumnity", "yellow_maple", "yellow_maple_leaves", "autumnity:maple"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "autumnity", "orange_maple", "orange_maple_leaves", "autumnity:maple"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "autumnity", "red_maple", "red_maple_leaves", "autumnity:maple"));
*/
        // ALEXSCAVES
        leafReg.addLeavesToWoodMapping("alexscaves:ancient_leaves", "minecraftjungle");
    }

    /*
      Below is to simplify the amount of works to add undetected WoodType
    */

    /**
     * @param modId       The ID of the mod that WoodType is from
     * @param nameWood    Name of WoodType without "_log"
     * @param childrenIds Example: "FENCE-oak_fence", "LEAVES-oak_leaves", "GATE_FENCE-oak_gate_fence"
     *
     *                    <p>Note: The following are also included in childrenIds:</p>
     *                    <ul>
     *                        <li>stripped_log</li>
     *                        <li>wood</li>
     *                        <li>stripped_wood</li>
     *                    </ul>
     */
    /*
    private static void simpleWoodFinder(String modId, String nameWood, String... childrenIds) {
        List<@NotNull String> standardChildren = List.of(
                "STRIPPED_LOG-stripped_" + nameWood + "_log",
                "WOOD-" + nameWood + "_wood",
                "STRIPPED_WOOD-stripped_" + nameWood + "_wood");

        String[] combined = Stream.concat(standardChildren.stream(), Arrays.stream(childrenIds)).toArray(String[]::new);

        advancedWoodFinder(false, modId, nameWood, nameWood + "_planks", nameWood + "_log", combined);
    }*/

    /**
     * <p>Using Planks' name instead of Log's name to detect WoodType's children</p>
     *
     * @param planksId Id of the planks
     *                 Same as {@link HardcodedBlockTypes#simpleWoodFinder(String, String, String...)}
     */
    /*
    private static void simplePlanksWoodFinder(String modId, String nameWood, String planksId, String... childrenIds) {
        List<@NotNull String> standardChildren = List.of(
                "STRIPPED_LOG-stripped_" + nameWood + "_log",
                "WOOD-" + nameWood + "_wood",
                "STRIPPED_WOOD-stripped_" + nameWood + "_wood");

        String[] combined = Stream.concat(standardChildren.stream(), Arrays.stream(childrenIds)).toArray(String[]::new);

        advancedWoodFinder(true, modId, nameWood, planksId, nameWood + "_log", combined);
    }*/

    /**
     * @param modId       The ID of the mod that WoodType is from
     * @param nameStem    Name of WoodType without "_stem"
     * @param childrenIds Example: "FENCE-oak_fence", "LEAVES-oak_leaves", "GATE_FENCE-oak_gate_fence"
     *
     *                    <p>Note: The following are also included in childrenIds:</p>
     *                    <ul>
     *                        <li>stripped_log</li>
     *                        <li>wood</li>
     *                        <li>stripped_wood</li>
     *                    </ul>
     */
    /*
    private static void simpleStemFinder(String modId, String nameStem, String... childrenIds) {
        List<@NotNull String> standardChildren = List.of(
                "STRIPPED_LOG-stripped_" + nameStem + "_stem",
                "WOOD-" + nameStem + "_hyphae",
                "STRIPPED_WOOD-stripped_" + nameStem + "_hyphae");

        String[] combined = Stream.concat(standardChildren.stream(), Arrays.stream(childrenIds)).toArray(String[]::new);

        advancedWoodFinder(false, modId, nameStem, nameStem + "_planks", nameStem + "_stem", combined);
    }
*/
    /**
     * <p>Using Planks' name instead of Stem's name to detect WoodType's children</p>
     *
     * @param planksId Id of the planks
     *                 Same as {@link HardcodedBlockTypes#simpleStemFinder(String, String, String...)}
     */
    /*
    private static void simplePlanksStemFinder(String modId, String nameStem, String planksId, String... childrenIds) {
        List<@NotNull String> standardChildren = List.of(
                "STRIPPED_LOG-stripped_" + nameStem + "_log",
                "WOOD-" + nameStem + "_wood",
                "STRIPPED_WOOD-stripped_" + nameStem + "_wood");

        String[] combined = Stream.concat(standardChildren.stream(), Arrays.stream(childrenIds)).toArray(String[]::new);

        advancedWoodFinder(true, modId, nameStem, planksId, nameStem + "_stem", combined);
    }*/

    /**
     * @param modId       The ID of the mod that WoodType is from
     * @param nameWood    Name of WoodType without "_log"
     * @param logId       Id of the log
     * @param childrenIds Example: "FENCE-oak_fence", "LEAVES-oak_leaves", "GATE_FENCE-oak_gate_fence"
     *
     *                    <p>Note: The following are also included in childrenIds:</p>
     *                    <ul>
     *                        <li>stripped_log</li>
     *                        <li>wood</li>
     *                        <li>stripped_wood</li>
     *                    </ul>
     */
    /*
    private static void mediumWoodFinder(String modId, String nameWood, String logId, String woodId, String... childrenIds) {
        String wood = "WOOD-" + woodId;
        String stripped_wood = "STRIPPED_WOOD-stripped_" + woodId;

        List<@NotNull String> standardChildren = new ArrayList<>(List.of("STRIPPED_LOG-stripped_" + logId));

        if (!woodId.isEmpty()) {
            standardChildren.add(wood);
            standardChildren.add(stripped_wood);
        }

        String[] combined = Stream.concat(standardChildren.stream(), Arrays.stream(childrenIds)).toArray(String[]::new);

        advancedWoodFinder(false, modId, nameWood, nameWood + "_planks", logId, combined);
    }*/

    /**
     * @param useNamePlanks Using Planks' name instead of log's name to detect WoodType's children
     * @param modId         The ID of the mod that WoodType is from
     * @param nameWoodType  Name of WoodType without "_log" or "_stem"
     * @param planksId      Id of the planks
     * @param logId         Id of the log
     * @param childrenIds   Example: "FENCE-oak_fence", "LEAVES-oak_leaves", "GATE_FENCE-oak_gate_fence"
     */
    /*
    private static void advancedWoodFinder(boolean useNamePlanks, String modId, String nameWoodType, String planksId, String logId, String... childrenIds) {
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

    private static void advancedWoodFinder(String modId, String nameWoodType, String planksId, String logId, String... childrenIds) {
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
*/

    //known children keys

    private static final String LEAVES = "leaves";
    private static final String WOOD = "wood";
    private static final String SAPLING = "sampling";


}