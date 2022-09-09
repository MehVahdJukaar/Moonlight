package net.mehvahdjukaar.moonlight.forge;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.client.ItemDecoratorHandler;
import net.minecraftforge.client.event.RegisterItemDecorationsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.conditions.ConditionContext;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkHooks;

/**
 * Author: MehVahdJukaar
 */
@Mod(Moonlight.MOD_ID)
public class MoonlightForge {
    public static final String MOD_ID = Moonlight.MOD_ID;

    public MoonlightForge() {
        Moonlight.commonInit();

        MinecraftForge.EVENT_BUS.register(this);

    }

    @SubscribeEvent
    public static void onDataSync(OnDatapackSyncEvent event) {
        SoftFluidRegistry.onDataLoad();
        //send syncing packets
        if (event.getPlayer() != null) {
            SoftFluidRegistry.onDataSyncToPlayer(event.getPlayer(),true);
        } else {
            for (var p : event.getPlayerList().getPlayers()) {
                SoftFluidRegistry.onDataSyncToPlayer(p,true);
            }
        }
    }
}

