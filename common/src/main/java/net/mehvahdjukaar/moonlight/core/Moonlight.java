package net.mehvahdjukaar.moonlight.core;

import net.mehvahdjukaar.moonlight.api.fluids.ModFlowingFluid;
import net.mehvahdjukaar.moonlight.api.client.ModFluidRenderProperties;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.map.MapDecorationRegistry;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.set.leaves.LeavesTypeRegistry;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.moonlight.core.criteria_triggers.ModCriteriaTriggers;
import net.mehvahdjukaar.moonlight.core.misc.VillagerAIInternal;
import net.mehvahdjukaar.moonlight.core.network.ModMessages;
import net.mehvahdjukaar.moonlight.core.set.BlockSetInternal;
import net.mehvahdjukaar.moonlight.core.set.BlocksColorInternal;
import net.mehvahdjukaar.moonlight.core.set.CompatTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public class Moonlight {

    public static final String MOD_ID = "moonlight";

    public static final Logger LOGGER = LogManager.getLogger();
    public static final boolean HAS_BEEN_INIT = true;

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
        VillagerAIInternal.init();
        ModCriteriaTriggers.register();
        SoftFluidRegistry.init();
        MapDecorationRegistry.init();

        //client init
        if (PlatformHelper.getEnv().isClient()) {
            MoonlightClient.initClient();
        }

        PlatformHelper.addCommonSetup(BlocksColorInternal::setup);
    }


    public static Supplier<Fluid> FLUID = RegHelper.registerFluid(res("test"), () -> new ModFlowingFluid(
            ModFlowingFluid.properties().canSwim(true),
            () -> (LiquidBlock) Blocks.WATER) {
        @Override
        public Fluid getFlowing() {
            return FLUID.get();
        }

        @Override
        public Fluid getSource() {
            return FLUID.get();
        }

        @Override
        protected int getSlopeFindDistance(LevelReader level) {
            return 0;
        }

        @Override
        protected int getDropOff(LevelReader level) {
            return 0;
        }

        @Override
        public int getAmount(FluidState state) {
            return 0;
        }

        @Override
        public Item getBucket() {
            return null;
        }

        @Override
        public int getTickDelay(LevelReader level) {
            return 0;
        }

        @Override
        protected float getExplosionResistance() {
            return 0;
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }

        @Override
        public ModFluidRenderProperties createRenderProperties() {
            return new ModFluidRenderProperties(new ResourceLocation("aa"), new ResourceLocation("bb"), 2);
        }
    });

}
