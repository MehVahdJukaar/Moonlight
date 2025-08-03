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
// also the sheer amount of stuff here is huge, if there are some common conventions those should added to the wood type reg instead
//Reminder that this class like all others in the mod follow the mod own license. Aka if you need this, depend on the library
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
        woodReg.addSimpleFinder("burnt", "smoldering_bamboo")
                .logSuffix("_block");

        // Caverns-And-Chasms
        woodReg.addSimpleFinder("caverns_and_chasms", "azalea")
                .childBlockSuffix(LEAVES, "_leaves");

        // The Outer End
        woodReg.addSimpleFinder("outer_end", "azure")
                .childBlockSuffix(WOOD, "_pith")
                .childBlockSuffix(STRIPPED_WOOD, "_stripped_pith");

        // Deeper And Darker
        woodReg.addSimpleFinder("deeperdarker", "bloom")
                .log("blooming_stem")
                .childBlock(STRIPPED_LOG, "stripped_blooming_stem");

        // Blocks +
        //TODO: why needed?  - blc of sounds (need to check if it's been fixed)
        woodReg.addSimpleFinder("blocksplus", "chorus");
        woodReg.addSimpleFinder("blocksplus", "bamboo");
        woodReg.addSimpleFinder("blocksplus", "mushroom");

        // Integrated Dynamics
        //TODO: why needed? - blc of sounds (need to check if it's been fixed)
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
        woodReg.addSimpleFinder("unusualend", "chorus_nest")
                .planks("chorus_nest_planks")
                .log("chorus_cane_block")
                .childBlock(STRIPPED_LOG, "stripped_chorus_cane_block")
                .childBlock(FENCE, "chorus_nest_mosaic_fence");

        // Spectrum (FABRIC)
        woodReg.addSimpleFinder("spectrum", "ivory_noxwood")
                .log("ivory_noxcap_stem")
                .childBlock(STRIPPED_LOG, "stripped_ivory_noxcap_stem")
                .childBlock(WOOD, "ivory_noxcap_hyphae")
                .childBlock(STRIPPED_WOOD, "stripped_ivory_noxcap_hyphae");

        woodReg.addSimpleFinder("spectrum", "slate_noxwood")
                .log("slate_noxcap_stem")
                .childBlock(STRIPPED_LOG, "stripped_slate_noxcap_stem")
                .childBlock(WOOD, "slate_noxcap_hyphae")
                .childBlock(STRIPPED_WOOD, "stripped_slate_noxcap_hyphae");

        woodReg.addSimpleFinder("spectrum", "ebony_noxwood")
                .log("ebony_noxcap_stem")
                .childBlock(STRIPPED_LOG, "stripped_ebony_noxcap_stem")
                .childBlock(WOOD, "ebony_noxcap_hyphae")
                .childBlock(STRIPPED_WOOD, "stripped_ebony_noxcap_hyphae");

        woodReg.addSimpleFinder("spectrum", "chestnut_noxwood")
                .log("chestnut_noxcap_stem")
                .childBlock(STRIPPED_LOG, "stripped_chestnut_noxcap_stem")
                .childBlock(WOOD, "chestnut_noxcap_hyphae")
                .childBlock(STRIPPED_WOOD, "stripped_chestnut_noxcap_hyphae");

        // Ars Nouveau - Do not add other WoodTypes blc it would create too many block variants using archwood_planks
        // The WoodTypes below all are using the same planks. There is no solutions
        woodReg.addSimpleFinder("ars_nouveau", "blue_archwood")
                .planks("archwood_planks");

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
        //TODO: is this needed? - blc of sounds (need to check if it's been fixed)
        woodReg.addSimpleFinder("pokecube_legends", "concrete");

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
        //TODO: is this needed? - blc of sounds (need to check if it's been fixed)
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
        woodReg.addSimpleFinder("mynethersdelight", "powdery")
                .logSuffix("_block");

        // Nourished End
        woodReg.addSimpleFinder("nourished_end", "verdant")
                .logSuffix("_stalk")
                .childBlock(WOOD, "verdant_hyphae");

        woodReg.addSimpleFinder("nourished_end", "cerulean")
                .logSuffix("_stem_thick")
                .childBlockSuffix(STRIPPED_LOG, "_stem_stripped")
                .childBlockSuffix(WOOD, "_hyphae")
                .childBlockSuffix(STRIPPED_WOOD, "_hyphae");

        // Gardens Of The Dead
        woodReg.addSimpleFinder("gardens_of_the_dead", "soulblight");

        woodReg.addSimpleFinder("gardens_of_the_dead", "whistlecane")
                .planks("whistlecane_planks")
                .log("whistlecane_block")
                .childItem(STICK, "whistlecane");

        // Luminous Nether
        woodReg.addSimpleFinder("luminous_nether", "mushroom")
                .planks("mushroom_planks")
                .log("goldenstem")
                .childBlock(STRIPPED_LOG, "shredded_stem")
                .childBlock(WOOD, "goldmushroom")
                .childBlock(SAPLING, "golden_mushroom")
                .childItem(STICK, "whistlecane");

        // Desolation
        woodReg.addSimpleFinder("desolation", "charred")
                .log("charredlog");

        // Damn Of Time Builder
        woodReg.addSimpleFinder("dawnoftimebuilder", "waxed_oak")
                .log("waxed_oak_log_stripped")
                .planks("waxed_oak_planks");

        woodReg.addSimpleFinder("dawnoftimebuilder", "charred_spruce")
                .log("charred_spruce_log_stripped")
                .planks("charred_spruce_planks");

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

        //TODO: are these correct? oak? shouldnt it be left empty? - need to check
        leafReg.addLeavesToWoodMapping("mysticsbiomes:peony", "minecraft:oak");
        leafReg.addLeavesToWoodMapping("mysticsbiomes:hydrangea", "minecraft:oak");
        leafReg.addLeavesToWoodMapping("mysticsbiomes:budding_peony", "minecraft:oak");

        // Ecologics
        leafReg.addSimpleFinder("ecologics", "coconut")
                .childBlock(SAPLING, "coconut_seedling");

        // BIOMES O' PLENTY
        leafReg.addLeavesToWoodMapping("biomesoplenty:origin", "minecraft:oak");

        // BLUE SKIES
        //TODO why needed? - blc of sounds (need to check if it's been fixed)
        leafReg.addLeavesToWoodMapping("blue_skies", "crystallized", "crystallized");

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


    /// Known Children Keys
    private static final String LEAVES = "leaves";
    private static final String WOOD = "wood";
    private static final String STRIPPED_LOG = "stripped_log";
    private static final String STRIPPED_WOOD = "stripped_wood";
    private static final String SAPLING = "sapling";
    private static final String FENCE = "fence";
    private static final String STICK = "fence";


}