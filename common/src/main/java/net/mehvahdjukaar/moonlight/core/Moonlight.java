package net.mehvahdjukaar.moonlight.core;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.map.MapDecorationRegistry;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.set.leaves.LeavesTypeRegistry;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.moonlight.core.criteria_triggers.ModCriteriaTriggers;
import net.mehvahdjukaar.moonlight.core.loot_pool_entries.ModLootPoolEntries;
import net.mehvahdjukaar.moonlight.core.misc.VillagerAIInternal;
import net.mehvahdjukaar.moonlight.core.network.ModMessages;
import net.mehvahdjukaar.moonlight.core.set.BlockSetInternal;
import net.mehvahdjukaar.moonlight.core.set.BlocksColorInternal;
import net.mehvahdjukaar.moonlight.core.set.CompatTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Moonlight {

    public static final String MOD_ID = "moonlight";

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static final boolean HAS_BEEN_INIT = true;
    public static final TagKey<Block> SHEARABLE_TAG = TagKey.create(Registries.BLOCK, new ResourceLocation("mineable/shear"));

    public static ResourceLocation res(String name) {
        return new ResourceLocation(MOD_ID, name);
    }

    //called on mod creation
    public static void commonInit() {
        BlockSetInternal.registerBlockSetDefinition(WoodTypeRegistry.INSTANCE);
        BlockSetInternal.registerBlockSetDefinition(LeavesTypeRegistry.INSTANCE);
        //MoonlightEventsHelper.addListener( BlockSetInternal::addTranslations, AfterLanguageLoadEvent.class);
        CompatTypes.init();

        ModMessages.registerMessages();
        ModCriteriaTriggers.register();
        ModLootPoolEntries.register();

        VillagerAIInternal.init();
        SoftFluidRegistry.init();
        MapDecorationRegistry.init();

        //client init
        if (PlatHelper.getEnv().isClient()) {
            MoonlightClient.initClient();
        }

        PlatHelper.addCommonSetup(BlocksColorInternal::setup);
    }

}
