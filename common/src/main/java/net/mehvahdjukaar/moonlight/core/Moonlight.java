package net.mehvahdjukaar.moonlight.core;

import net.mehvahdjukaar.moonlight.api.events.IDropItemOnDeathEvent;
import net.mehvahdjukaar.moonlight.api.events.MoonlightEventsHelper;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.map.MapDecorationRegistry;
import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.api.set.leaves.LeavesTypeRegistry;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.moonlight.api.util.fake_player.FakeServerPlayer;
import net.mehvahdjukaar.moonlight.core.criteria_triggers.ModCriteriaTriggers;
import net.mehvahdjukaar.moonlight.core.loot_pool_entries.ModLootPoolEntries;
import net.mehvahdjukaar.moonlight.core.misc.VillagerAIInternal;
import net.mehvahdjukaar.moonlight.core.network.ModMessages;
import net.mehvahdjukaar.moonlight.core.set.BlockSetInternal;
import net.mehvahdjukaar.moonlight.core.set.BlocksColorInternal;
import net.mehvahdjukaar.moonlight.core.set.CompatTypes;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Moonlight {

    public static final String MOD_ID = "moonlight";

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static final boolean HAS_BEEN_INIT = true;
    public static final TagKey<Block> SHEATABLE_TAG = TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation("mineable/shear"));

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
        if (PlatformHelper.getEnv().isClient()) {
            MoonlightClient.initClient();
        }

        PlatformHelper.addCommonSetup(BlocksColorInternal::setup);
        PlatformHelper.addCommonSetup(Moonlight::commonSetup);

    }

    private static void commonSetup() {
        BlocksColorInternal.setup();

        FakeServerPlayer.init();
    }

    @EventCalled
    public static void onPlayerCloned(Player oldPlayer, Player newPlayer, boolean wasDeath) {
        if (wasDeath && !oldPlayer.level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
            var inv = oldPlayer.getInventory();
            int i = 0;
            for (var v : inv.items) {
                if (v != ItemStack.EMPTY) {
                    IDropItemOnDeathEvent e = IDropItemOnDeathEvent.create(v, oldPlayer, false);
                    MoonlightEventsHelper.postEvent(e, IDropItemOnDeathEvent.class);
                    if (e.isCanceled()) {
                        newPlayer.getInventory().setItem(i, e.getReturnItemStack());
                    }
                }
                i++;
            }
        }
    }
}
