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
        woodReg.addSimpleFinder("mofus_better_end_", "frost_root")
                .planksSuffix("_plank");

        // Burnt
        mediumWoodFinder("burnt", "smoldering_bamboo", "smoldering_bamboo_block", "");

        // Caverns-And-Chasms
        simpleWoodFinder("caverns_and_chasms", "azalea", "azalea_leaves");

        // The Outer End
        woodReg.addSimpleFinder("outer_end", "azure")
                .childBlockSuffix(WOOD, "_pith")
                .childBlockSuffix(STRIPPED_WOOD, "_stripped_pith");


        // Deeper And Darker
        simplePlanksStemFinder("deeperdarker", "blooming", "bloom_planks");

        // Blocks +
        woodReg.addSimpleFinder("blocksplus", "chorus");
        woodReg.addSimpleFinder("blocksplus", "bamboo");
        woodReg.addSimpleFinder("blocksplus", "mushroom");

        // Integrated Dynamics
        //TODO: why needed?
        woodReg.addSimpleFinder("integrateddynamics", "menril");

        // Domum Oranmentum
        woodReg.addSimpleFinder("domum_ornamentum", "cactus")
                .planks("green_cactus_extra")
                .log(() -> Blocks.CACTUS);
        woodReg.addSimpleFinder("domum_ornamentum", "cactus_extra")
                .planks("cactus_extra")
                .log(() -> Blocks.CACTUS);


        // Jaden's Nether Expansion
        woodReg.addSimpleFinder("netherexp", "claret")
                .log("cerebrage_claret_stem")
                .childBlock(WOOD, "cerebrage_claret_hyphae");

        // Piglin Ruins
        woodReg.addSimpleFinder("piglin_ruins", "ominous")
                .log("ominous_stalk_block");

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
        //TODO: is this needed?
        woodReg.addSimpleFinder("rats", "pirat");

        // Oh The Biomes You'll Go
        //TODO: shouldnt this need more stuff?
        woodReg.addSimpleFinder("byg", "embur")
                .planks("embur_pedu")
                .log("embur_pedu_top");

        // mcreator mod with typos...
        // Nethers Exoticism
        woodReg.addSimpleFinder("nethers_exoticism", "jabuticaba")
                .planks("jaboticaba_planks")
                .log("jabuticaba_log");

        // My Nether's Delight
        mediumWoodFinder("mynethersdelight", "powdery", "powdery_block", "");

        // Nourished End
        mediumWoodFinder("nourished_end", "verdant", "verdant_stalk", "verdant_hyphae");

        advancedWoodFinder("nourished_end", "cerulean", "cerulean_planks", "cerulean_stem_thick",
                "STRIPPED_LOG-cerulean_stem_stripped", "WOOD-cerulean_hyphae", "STRIPPED_WOOD-stripped_cerulean_hyphae");

        // Gardens Of The Dead
        woodReg.addSimpleFinder("gardens_of_the_dead", "soulblight");

        woodReg.addSimpleFinder("gardens_of_the_dead", "whistlecane")
                .planks("whistlecane_block")
                .log("whistlecane_wood");

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
        woodReg.addSimpleFinder("habitat", "fairy_ring_mushroom")
                .planks("fairy_ring_mushroom_planks")
                .log("enhanced_fairy_ring_mushroom_stem");

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

        // Ecologics
        leafReg.addSimpleFinder("ecologics", "coconut")
                .childBlock(SAPLING, "coconut_seedling");

        // BIOMES O' PLENTY
        leafReg.addLeavesToWoodMapping("biomesoplenty:origin", "minecraft:oak");

        // BLUE SKIES
        //TODO why needed?
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
                "aether_redux:crystal" : "aether:skyroot";

        //TODO: are these correct?
        leafReg.addLeavesToWoodMapping("aether:crystal", crystalLeavesWoodType);
        leafReg.addLeavesToWoodMapping("aether:crystal_fruit_leaves", crystalLeavesWoodType);

        // AETHER GENESIS
        leafReg.addLeavesToWoodMapping("aether_genesis:purple_crystal", crystalLeavesWoodType);
        leafReg.addLeavesToWoodMapping("aether_genesis:purple_crystal_fruit", crystalLeavesWoodType);


        // ANCIENT AETHER
        leafReg.addLeavesToWoodMapping("ancient_aether", "crystal_skyroot", "skyroot");
        leafReg.addLeavesToWoodMapping("ancient_aether", "enchanted_skyroot", "skyroot");
        leafReg.addLeavesToWoodMapping("ancient_aether", "skyroot_pine", "skyroot");
        leafReg.addLeavesToWoodMapping("ancient_aether", "blue_skyroot_pine", "skyroot");

        // ALEXSCAVES
        leafReg.addLeavesToWoodMapping("alexscaves:ancient_leaves", "minecraftjungle");
    }

    //known children keys

    private static final String LEAVES = "leaves";
    private static final String WOOD = "wood";
    private static final String STRIPPED_LOG = "stripped_log";
    private static final String STRIPPED_WOOD = "stripped_wood";
    private static final String SAPLING = "sampling";


}