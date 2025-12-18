package net.mehvahdjukaar.moonlight.api.integration;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.set.leaves.LeavesTypeRegistry;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.moonlight.api.util.INamedSupplier;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.ApiStatus;

import static net.mehvahdjukaar.moonlight.api.set.wood.VanillaWoodChildKeys.*;

//TODO: move out of api package
//place for all known weird hardcoded wood types from mods that aren't getting detected

// SO this has become unmanageable, its shouldnt be that every time a new weird wood is found this mod has to be updated
// EC is the main mod that uses this so this shit when its found can be added there instead for basically same effect
// also the sheer amount of stuff here is huge, if there are some common Conventions those should added to the wood type reg instead
//Reminder that this class like all others in the mod follow the mod own license. Aka if you need this, depend on the library
@ApiStatus.Internal
public class HardcodedBlockTypes {

    public static final INamedSupplier<WoodType> BURNT;

    public static void init() {}

    /* Defintion of REASONS:
     *
     * PlANKS-NAME: Children are using planks' name instead of log's name
     *
     * Spelling Convention: a typo in the Id, no underscore
     *
     * Naming-Convention: blocks has unique names that doesn't have "_wood", "_log", "_planks" or has different affix like "_block"
     *
     * Associated WoodType: Leaves have no WoodType, must be included
     * Associated LeavesType: Wood have no LeavesType, must be included
     *
     * 2-Words: WoodType or LeavesType having 2-words instead of 1-words for a name
     */
    static {
        WoodTypeRegistry woodReg = WoodTypeRegistry.INSTANCE;
        LeavesTypeRegistry leafReg = LeavesTypeRegistry.INSTANCE;

        // Abundant Atmosphere - REASON: Naming-Convention, 2-Words
        woodReg.addSimpleFinder("abundant_atmosphere", "red_bamboo")
                .log("red_bamboo_block")
                .childBlock(STRIPPED_LOG, "stripped_red_bamboo_block");

        // Dungeon's Delight - REASON: Naming-Convention, PLANKS-NAME
        woodReg.addSimpleFinder("dungeonsdelight", "wormwood")
                .log("wormroots_block");

        // Sniffed Out - REASON: Naming-Convention
        woodReg.addSimpleFinder("sniffed_out", "vessel")
                .log("crude_vessel_stem")
                .childBlock(WOOD, "crude_vessel_cuticle")
                .childBlock(STRIPPED_WOOD, "stripped_vessel_cuticle");

        // Mofu's Better End - REASON: Naming-Convention
        woodReg.addSimpleFinder("mofus_better_end_", "weepingstar")
                .childBlockSuffix(LEAVES, "_leaf");

        woodReg.addSimpleFinder("mofus_better_end_", "frost_root")
                .planksSuffix("_plank");

        // Burnt - REASON: Naming-Convention
        BURNT = woodReg.addSimpleFinder("burnt", "smoldering_bamboo")
                .logSuffix("_block")
                .build();

        // Caverns-And-Chasms - REASON: ???
        woodReg.addSimpleFinder("caverns_and_chasms", "azalea")
                .childBlockSuffix(LEAVES, "_leaves");

        // The Outer End - REASON: Naming-Convention
        woodReg.addSimpleFinder("outer_end", "azure")
                .childBlockSuffix(WOOD, "_pith")
                .childBlockSuffix(STRIPPED_WOOD, "_stripped_pith");

        // Deeper And Darker
        woodReg.addSimpleFinder("deeperdarker", "bloom")
                .log("blooming_stem")
                .childBlock(STRIPPED_LOG, "stripped_blooming_stem");

        // Blocks + - REASON: ???
        //TODO: why needed?  - blc of sounds (need to check if it's been fixed)
        woodReg.addSimpleFinder("blocksplus", "chorus");
        woodReg.addSimpleFinder("blocksplus", "bamboo");
        woodReg.addSimpleFinder("blocksplus", "mushroom");

        // Integrated Dynamics - REASON: ???
        //TODO: why needed? - blc of sounds (need to check if it's been fixed)
        woodReg.addSimpleFinder("integrateddynamics", "menril");

        // Domum Oranmentum - REASON: Associated LOG
        woodReg.addSimpleFinder("domum_ornamentum", "cactus")
                .planks("green_cactus_extra")
                .log(() -> Blocks.CACTUS);
        woodReg.addSimpleFinder("domum_ornamentum", "cactus_extra")
                .planks("cactus_extra")
                .log(() -> Blocks.CACTUS);


        // Jaden's Nether Expansion - REASON: PLANKS-NAME, Naming-Convention
        woodReg.addSimpleFinder("netherexp", "claret")
                .log("cerebrage_claret_stem")
                .childBlock(WOOD, "cerebrage_claret_hyphae");

        // Piglin Ruins - REASON: PLANKS-NAME, Naming-Convention
        woodReg.addSimpleFinder("piglin_ruins", "ominous")
                .log("ominous_stalk_block");

        // Unusual End - REASON: PLANKS-NAME, Naming-Convention
        woodReg.addSimpleFinder("unusualend", "chorus_nest")
                .planks("chorus_nest_planks")
                .log("chorus_cane_block")
                .childBlock(STRIPPED_LOG, "stripped_chorus_cane_block")
                .childBlock(FENCE, "chorus_nest_mosaic_fence");

        // Spectrum (FABRIC) - REASON: PLANKS-NAME, Naming-Convention
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

        // Ars Nouveau - REASON: PLANKS-NAME
        if (!PlatHelper.isModLoaded("archwood_good")) { // skip if mod is loaded
            woodReg.addSimpleFinder("ars_nouveau", "archwood")
                    .log("blue_archwood_log")
                    .childBlock(STRIPPED_LOG, "stripped_blue_archwood_log")
                    .childBlock(WOOD, "blue_archwood_wood")
                    .childBlock(STRIPPED_WOOD, "stripped_blue_archwood_wood")
                    .childBlock(LEAVES, "blue_archwood_leaves")
                    .childBlock(SAPLING, "blue_archwood_sapling");
        }

    /// Do not add other WoodTypes blc it would create too many block variants using archwood_planks
    /// The WoodTypes below all are using the same planks. There is no solutions
//        BlockSetAPI.addBlockTypeFinder(WoodType.class,
//                generalWoodFinder(false, "ars_nouveau", "red_archwood", "archwood_planks", true));
//
//        BlockSetAPI.addBlockTypeFinder(WoodType.class,
//                generalWoodFinder(false, "ars_nouveau", "purple_archwood", "archwood_planks", true));
//
//        BlockSetAPI.addBlockTypeFinder(WoodType.class,
//                generalWoodFinder(false, "ars_nouveau", "green_archwood", "archwood_planks", true));

        // Ars Elemental - REASON: same as above
//        BlockSetAPI.addBlockTypeFinder(WoodType.class,
//                uniqueWoodFinder("ars_elemental", "yellow_archwood", "ars_nouveau:archwood_planks", "log", ""));

        // Blue Skies - REASON: ???
        woodReg.addSimpleFinder("blue_skies", "crystallized");

        // Darker Depths - REASON: needed because it has stone properties
        woodReg.addSimpleFinder("darkerdepths", "petrified");

        // Pokecube Legends - REASON: ???
        //TODO: is this needed? - blc of sounds (need to check if it's been fixed)
        woodReg.addSimpleFinder("pokecube_legends", "concrete");

        // Terraqueous - REASON: ???
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

        // Rats - REASON: ???
        //TODO: is this needed? - blc of sounds (need to check if it's been fixed)
        woodReg.addSimpleFinder("rats", "pirat");

        // Oh The Biomes You'll Go - REASON: Naming-Convention
        //TODO: shouldnt this need more stuff?
        woodReg.addSimpleFinder("byg", "embur")
                .planks("embur_pedu")
                .log("embur_pedu_top");

        // Nethers Exoticism - REASON: mcreator mod with typos
        woodReg.addSimpleFinder("nethers_exoticism", "jabuticaba")
                .planks("jaboticaba_planks")
                .log("jabuticaba_log");

        // My Nether's Delight - REASON: Naming-Convention
        woodReg.addSimpleFinder("mynethersdelight", "powdery")
                .logSuffix("_block");

        // Nourished End - REASON: Naming-Convention
        woodReg.addSimpleFinder("nourished_end", "verdant")
                .logSuffix("_stalk")
                .childBlock(WOOD, "verdant_hyphae");

        woodReg.addSimpleFinder("nourished_end", "cerulean")
                .logSuffix("_stem_thick")
                .childBlockSuffix(STRIPPED_LOG, "_stem_stripped")
                .childBlockSuffix(WOOD, "_hyphae")
                .childBlockSuffix(STRIPPED_WOOD, "_hyphae");

        // Gardens Of The Dead - REASON: Naming-Convention
        woodReg.addSimpleFinder("gardens_of_the_dead", "whistlecane")
                .planks("whistlecane_planks")
                .log("whistlecane_block")
                .childItem(STICK, "whistlecane");

        // Luminous Nether - REASON: Associated WoodType, Naming-Convention, Spelling Convention
        woodReg.addSimpleFinder("luminous_nether", "mushroom")
                .planks("mushroom_planks")
                .log("goldenstem")
                .childBlock(STRIPPED_LOG, "shredded_stem")
                .childBlock(WOOD, "goldmushroom")
                .childBlock(SAPLING, "golden_mushroom")
                .childItem(STICK, "whistlecane");

        // Desolation - REASON: Spelling Convention
        woodReg.addSimpleFinder("desolation", "charred")
                .log("charredlog");

        // Damn Of Time Builder - REASON: Associated LOG
        woodReg.addSimpleFinder("dawnoftimebuilder", "waxed_oak")
                .log("waxed_oak_log_stripped")
                .planks("waxed_oak_planks");

        woodReg.addSimpleFinder("dawnoftimebuilder", "charred_spruce")
                .log("charred_spruce_log_stripped")
                .planks("charred_spruce_planks");

        // Habitat - REASON: Associated LOG, Naming-Convention
        woodReg.addSimpleFinder("habitat", "fairy_ring_mushroom")
                .planks("fairy_ring_mushroom_planks")
                .log("enhanced_fairy_ring_mushroom_stem");

        // Ecologics - REASON: Associated LeavesType
        woodReg.addSimpleFinder("ecologics", "flowering_azalea")
                .child(LEAVES, () -> Blocks.FLOWERING_AZALEA_LEAVES);

        woodReg.addSimpleFinder("ecologics", "azalea")
                .child(LEAVES, () -> Blocks.AZALEA_LEAVES);

        // Quark - REASON: Associated LeavesType
        woodReg.addSimpleFinder("quark", "azalea")
                .child(LEAVES, () -> Blocks.AZALEA_LEAVES);


//!! LEAVES
        // Oh The Biomes We've Gone - REASON: Associated WoodType
        leafReg.addSimpleFinder("biomeswevegone","flowering_palo_verde")
                .childBlock(LOG, "palo_verde_log");

        leafReg.addLeavesToWoodMapping("biomeswevegone", "araucaria", "pine");
        leafReg.addLeavesToWoodMapping("biomeswevegone", "holly_berry", "holly");

        leafReg.addLeavesToWoodMapping("biomeswevegone:firecracker", "minecraft:oak");
        leafReg.addLeavesToWoodMapping("biomeswevegone:yucca", "minecraft:oak");
        leafReg.addLeavesToWoodMapping("biomeswevegone:ripe_yucca", "minecraft:oak");
        leafReg.addLeavesToWoodMapping("biomeswevegone:flowering_yucca", "minecraft:oak");
        leafReg.addLeavesToWoodMapping("biomeswevegone:orchard", "minecraft:oak");
        leafReg.addLeavesToWoodMapping("biomeswevegone:ripe_orchard", "minecraft:oak");
        leafReg.addLeavesToWoodMapping("biomeswevegone:flowering_orchard", "minecraft:oak");

        // Luminous Nether - REASON: Associated WoodType
        leafReg.addLeavesToWoodMapping("luminous_nether:ash", "luminous_nether:withered");

        // FruitFul FUn - REASON: Associated WoodType
        leafReg.addLeavesToWoodMapping("fruitfulfun:apple", "minecraft:oak");
        leafReg.addLeavesToWoodMapping("fruitfulfun:pomegranate", "minecraft:jungle");
        leafReg.addLeavesToWoodMapping("fruitfulfun", "grapefruit", "citrus");
        leafReg.addLeavesToWoodMapping("fruitfulfun", "lemon", "citrus");
        leafReg.addLeavesToWoodMapping("fruitfulfun", "tangerine", "citrus");
        leafReg.addLeavesToWoodMapping("fruitfulfun", "lime", "citrus");
        leafReg.addLeavesToWoodMapping("fruitfulfun", "citron", "citrus");
        leafReg.addLeavesToWoodMapping("fruitfulfun", "pomelo", "citrus");
        leafReg.addLeavesToWoodMapping("fruitfulfun", "orange", "citrus");

        // Mystic's Biomes - REASON: Associated WoodType
        leafReg.addLeavesToWoodMapping("mysticsbiomes", "yellow_maple", "white_maple");

        //REASON: Naming-Convention
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

        // Environmental - REASON: Associated WoodType
        leafReg.addLeavesToWoodMapping("environmental", "pink_wisteria", "wisteria");
        leafReg.addLeavesToWoodMapping("environmental", "blue_wisteria", "wisteria");
        leafReg.addLeavesToWoodMapping("environmental", "purple_wisteria", "wisteria");
        leafReg.addLeavesToWoodMapping("environmental", "white_wisteria", "wisteria");
        leafReg.addLeavesToWoodMapping("environmental", "cheerful_plum", "plum");
        leafReg.addLeavesToWoodMapping("environmental", "moody_plum", "plum");

        // Ecologics - REASON: Associated SAPLING
        leafReg.addSimpleFinder("ecologics", "coconut")
                .childBlock(SAPLING, "coconut_seedling");

        // BIOMES O' PLENTY - REASON: Associated WoodType
        leafReg.addLeavesToWoodMapping("biomesoplenty:origin", "minecraft:oak");

        // BLUE SKIES - REASON: ???
        //TODO why needed?
        leafReg.addLeavesToWoodMapping("blue_skies", "crystallized", "crystallized");
        //REASON: Associated WoodType
        leafReg.addLeavesToWoodMapping("blue_skies", "crescent_fruit", "dusk");

        // COLORFUL AZALEAS - REASON: Associated WoodType, Naming-Convention
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

        // POKECUBE LEGENDS - REASON: Naming-Convention
        leafReg.addSimpleFinder("pokecube_legends", "dyna_pastel_pink")
                .leaves("dyna_leaves_pastel_pink")
                .equivalentWood("pokecube_legends:aged");
        leafReg.addSimpleFinder("pokecube_legends", "dyna_pink")
                .leaves("dyna_leaves_pink")
                .equivalentWood("pokecube_legends:aged");
        leafReg.addSimpleFinder("pokecube_legends", "dyna_red")
                .leaves("dyna_leaves_red")
                .equivalentWood("pokecube_legends:aged");

        // REGIONS UNEXPLORED - REASON: Associated WoodType
        leafReg.addLeavesToWoodMapping("regions_unexplored:bamboo", "minecraft:jungle");

        // THE TWILIGHT FOREST - REASON: Associated WoodType, Naming-Convention
        leafReg.addLeavesToWoodMapping("twilightforest", "beanstalk", "twilight_oak");
        leafReg.addLeavesToWoodMapping("twilightforest", "thorn", "twilight_oak");

        // ULTERLANDS - REASON: Associated WoodType
        leafReg.addLeavesToWoodMapping("ulterlands:souldrained", "minecraft:oak");

        // AETHER - REASON: Associated WoodType

        // Replaces LeavesType's Associated WoodType if a mod is installed
        String skyroot_or_crystal = PlatHelper.isModLoaded("aether_redux") ?
                "aether_redux:crystal" : "aether:skyroot";
        String skyroot_or_glacia = PlatHelper.isModLoaded("aether_redux") ?
                "aether_redux:glacia" : "aether:skyroot";
        leafReg.addLeavesToWoodMapping("aether:crystal", skyroot_or_crystal);
        leafReg.addLeavesToWoodMapping("aether:crystal_fruit", skyroot_or_crystal);

        leafReg.addLeavesToWoodMapping("aether", "golden_oak", "skyroot");
        leafReg.addLeavesToWoodMapping("aether:holiday", skyroot_or_glacia);
        leafReg.addLeavesToWoodMapping("aether:decorated_holiday", skyroot_or_glacia);

        leafReg.addLeavesToWoodMapping("aether:crystal", skyroot_or_crystal);
        leafReg.addLeavesToWoodMapping("aether:crystal_fruit", skyroot_or_crystal);
        leafReg.addLeavesToWoodMapping("aether", "gilded_oak", "skyroot");
        leafReg.addLeavesToWoodMapping("aether_redux:gilded_oak", "aether:skyroot");

        // Aether Redux - REASON: Associated WoodType
        leafReg.addLeavesToWoodMapping("aether_redux:blighted_skyroot", "aether:skyroot");

        // AETHER GENESIS - REASON: Associated WoodType
        leafReg.addLeavesToWoodMapping("aether_genesis:purple_crystal", skyroot_or_crystal);
        leafReg.addLeavesToWoodMapping("aether_genesis:purple_crystal_fruit", skyroot_or_crystal);

        // ANCIENT AETHER - REASON: Associated WoodType
        leafReg.addLeavesToWoodMapping("ancient_aether:crystal_skyroot", "aether:skyroot");
        leafReg.addLeavesToWoodMapping("ancient_aether:enchanted_skyroot", "aether:skyroot");
        leafReg.addLeavesToWoodMapping("ancient_aether:skyroot_pine", "aether:skyroot");
        leafReg.addLeavesToWoodMapping("ancient_aether:blue_skyroot_pine", "aether:skyroot");
        leafReg.addLeavesToWoodMapping("ancient_aether:wyndcaps_holiday_tree", "aether:skyroot");

        // AUTUMNITY - REASON: Associated WoodType
        leafReg.addLeavesToWoodMapping("autumnity", "yellow_maple", "maple");
        leafReg.addLeavesToWoodMapping("autumnity", "orange_maple", "maple");
        leafReg.addLeavesToWoodMapping("autumnity", "red_maple", "maple");

        // ALEX'S CAVES - REASON: Associated WoodType
        leafReg.addLeavesToWoodMapping("alexscaves:ancient", "minecraft:jungle");
    }




}