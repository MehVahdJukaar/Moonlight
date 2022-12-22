package net.mehvahdjukaar.moonlight.core.set;

import net.mehvahdjukaar.moonlight.api.set.BlockSetAPI;
import net.mehvahdjukaar.moonlight.api.set.leaves.LeavesType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.minecraft.resources.ResourceLocation;

public class CompatTypes {

    public static void init() {

        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(new ResourceLocation("domum_ornamentum:cactus"),
                new ResourceLocation("domum_ornamentum:green_cactus_extra"), new ResourceLocation("cactus")));
        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(new ResourceLocation("domum_ornamentum:cactus_extra"),
                    new ResourceLocation("domum_ornamentum:cactus_extra"), new ResourceLocation("cactus")));

        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "ars_nouveau", "archwood", "archwood_planks", "blue_archwood_log"));

        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "blue_skies", "crystallized", "crystallized_planks", "crystallized_log"));

        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "darkerdepths", "petrified", "petrified_planks", "petrified_log"));
        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "pokecube_legends", "concrete", "concrete_planks", "concrete_log"));
        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "terraqueous", "storm_cloud", "storm_cloud", "storm_cloud_column"));
        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "terraqueous", "light_cloud", "light_cloud", "light_cloud_column"));
        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "terraqueous", "dense_cloud", "dense_cloud", "dense_cloud_column"));

        var embur = WoodType.Finder.simple(
                "byg", "embur", "embur_planks", "embur_pedu");
        embur.addChild("stripped_log", "stripped_embur_pedu");
        embur.addChild("wood", "embur_pedu_top");
        embur.addChild("stripped_wood", "stripped_embur_pedu_top");
        BlockSetAPI.addBlockTypeFinder(WoodType.class, embur);


        //mcreator mod with typos...
        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "nethers_exoticism", "jabuticaba", "jaboticaba_planks", "jabuticaba_log"));


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

        var soulblight = WoodType.Finder.simple(
                "gardens_of_the_dead", "soulblight", "soulblight_planks", "soulblight_stem");
        cerulean.addChild("stripped_wood", "stripped_soulblight_hyphae");
        cerulean.addChild("wood", "soulblight_hyphae");
        cerulean.addChild("stripped_log", "stripped_soulblight_stem");
        BlockSetAPI.addBlockTypeFinder(WoodType.class, soulblight);


        var bamboo = WoodType.Finder.simple(
                "twigs", "bamboo", "stripped_bamboo_planks", "bundled_bamboo");

        bamboo.addChild("stripped_log", "stripped_bundled_bamboo");

        BlockSetAPI.addBlockTypeFinder(WoodType.class, bamboo);

        BlockSetAPI.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "habitat", "fairy_ring_mushroom", "fairy_ring_mushroom_planks", "enhanced_fairy_ring_mushroom_stem"));

        var floweringAzalea = WoodType.Finder.simple(
                "ecologics", "flowering_azalea", "flowering_azalea_planks", "flowering_azalea_log");
        floweringAzalea.addChild("stripped_log", "stripped_azalea_log");
        floweringAzalea.addChild("leaves", new ResourceLocation("minecraft:flowering_azalea_leaves"));

        BlockSetAPI.addBlockTypeFinder(WoodType.class, floweringAzalea);


        var azalea = WoodType.Finder.simple(
                "ecologics", "azalea", "azalea_planks", "azalea_log");
        azalea.addChild("leaves", new ResourceLocation("minecraft:azalea_leaves"));

        BlockSetAPI.addBlockTypeFinder(WoodType.class, azalea);

        var quarkAzalea = WoodType.Finder.simple(
                "quark", "azalea", "azalea_planks", "azalea_log");
        quarkAzalea.addChild("leaves", new ResourceLocation("minecraft:azalea_leaves"));

        BlockSetAPI.addBlockTypeFinder(WoodType.class, quarkAzalea);


        //leaves

        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "ars_nouveau", "blue_archwood", "blue_archwood_leaves", "ars_nouveau:archwood"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "ars_nouveau", "green_archwood", "green_archwood_leaves", "ars_nouveau:archwood"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "ars_nouveau", "purple_archwood", "purple_archwood_leaves", "ars_nouveau:archwood"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "ars_nouveau", "red_archwood", "red_archwood_leaves", "ars_nouveau:archwood"));

        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "biomesoplenty", "origin", "origin_leaves", "oak"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "biomesoplenty", "flowering_oak", "flowering_oak_leaves", "oak"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "biomesoplenty", "maple", "maple_leaves", "oak"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "biomesoplenty", "orange_autumn", "orange_autumn_leaves", "oak"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "biomesoplenty", "yellow_autumn", "yellow_autumn_leaves", "oak"));

        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "biomesoplenty", "rainbow_birch", "rainbow_birch_leaves", "birch"));

        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "blue_skies", "crystallized", "crystallized_leaves", "blue_skies:crystallized"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "blue_skies", "crescent_fruit", "crescent_fruit_leaves", "blue_skies:dusk"));

        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "pokecube_legends", "dyna_pastel_pink", "dyna_leaves_pastel_pink", "pokecube_legends:aged"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "pokecube_legends", "dyna_pink", "dyna_leaves_pink", "pokecube_legends:aged"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "pokecube_legends", "dyna_red", "dyna_leaves_red", "pokecube_legends:aged"));

        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "regions_unexlpored", "bamboo", "bamboo_leaves", "twigs:bamboo"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "regions_unexlpored", "golden_larch", "golden_larch_leaves", "regions_unexlpored:larch"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "regions_unexlpored", "dead_pine", "dead_pine_leaves", "regions_unexlpored:pine"));

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

        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "twilightforest", "beanstalk", "beanstalk_leaves", "twilightforest:twilight_oak"));
        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "twilightforest", "thorn", "thorn_leaves", "twilightforest:twilight_oak"));

        BlockSetAPI.addBlockTypeFinder(LeavesType.class, LeavesType.Finder.simple(
                "ulterlands", "souldrained", "souldrained_leaves", "oak"));
    }
}
