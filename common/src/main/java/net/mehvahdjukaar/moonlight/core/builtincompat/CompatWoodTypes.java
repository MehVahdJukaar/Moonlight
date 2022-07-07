package net.mehvahdjukaar.moonlight.core.builtincompat;

import net.mehvahdjukaar.moonlight.api.set.BlockSetManager;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.minecraft.resources.ResourceLocation;

public class CompatWoodTypes {

    public static void init() {

        BlockSetManager.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(new ResourceLocation("domum_ornamentum:cactus"),
                new ResourceLocation("domum_ornamentum:green_cactus_extra"),new ResourceLocation("cactus")));
        BlockSetManager.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(new ResourceLocation("domum_ornamentum:cactus"),
                new ResourceLocation("domum_ornamentum:cactus_extra"),new ResourceLocation("cactus")));
        BlockSetManager.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "darkerdepths", "petrified", "petrified_planks", "petrified_log"));
        BlockSetManager.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "pokecube_legends", "concrete", "concrete_planks", "concrete_log"));
        BlockSetManager.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "terraqueous", "storm_cloud", "storm_cloud", "storm_cloud_column"));
        BlockSetManager.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "terraqueous", "light_cloud", "light_cloud", "light_cloud_column"));
        BlockSetManager.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "terraqueous", "dense_cloud", "dense_cloud", "dense_cloud_column"));
        BlockSetManager.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "the_bumblezone", "beehive_beeswax", "beehive_beeswax", "filled_porous_honeycomb_block"));
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

        var bamboo = WoodType.Finder.simple(
                "twigs", "bamboo", "stripped_bamboo_planks", "bundled_bamboo");

        bamboo.addChild("stripped_log", "stripped_bundled_bamboo");

        BlockSetManager.addBlockTypeFinder(WoodType.class, bamboo);

        BlockSetManager.addBlockTypeFinder(WoodType.class, WoodType.Finder.simple(
                "habitat", "fairy_ring_mushroom", "fairy_ring_mushroom_planks", "enhanced_fairy_ring_mushroom_stem"));

        var floweringAzalea = WoodType.Finder.simple(
                "ecologics", "flowering_azalea", "flowering_azalea_planks", "flowering_azalea_log");
        floweringAzalea.addChild("stripped_log", "stripped_azalea_log");
        floweringAzalea.addChild("leaves", new ResourceLocation("minecraft:flowering_azalea_leaves"));

        BlockSetManager.addBlockTypeFinder(WoodType.class, floweringAzalea);


        var azalea = WoodType.Finder.simple(
                "ecologics", "azalea", "azalea_planks", "azalea_log");
        azalea.addChild("leaves", new ResourceLocation("minecraft:azalea_leaves"));

        BlockSetManager.addBlockTypeFinder(WoodType.class, azalea);

        var quarkAzalea = WoodType.Finder.simple(
                "quark", "azalea", "azalea_planks", "azalea_log");
        quarkAzalea.addChild("leaves", new ResourceLocation("minecraft:azalea_leaves"));

        BlockSetManager.addBlockTypeFinder(WoodType.class, quarkAzalea);

    }
}
