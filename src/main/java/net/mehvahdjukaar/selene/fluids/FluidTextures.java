package net.mehvahdjukaar.selene.fluids;

import net.mehvahdjukaar.selene.Selene;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FluidTextures {

    private static final String MOD_ID = Selene.MOD_ID;

    //minecraft
    public static final ResourceLocation WATER_TEXTURE = new ResourceLocation("minecraft:block/water_still");
    public static final ResourceLocation FLOWING_WATER_TEXTURE = new ResourceLocation("minecraft:block/water_flow");
    public static final ResourceLocation LAVA_TEXTURE = new ResourceLocation("minecraft:block/lava_still");
    public static final ResourceLocation SLIME_TEXTURE = new ResourceLocation("minecraft:block/slime_block");

    //blocks (to stitch)
    public static final ResourceLocation MILK_TEXTURE = new ResourceLocation(MOD_ID, "blocks/milk_liquid");
    public static final ResourceLocation POTION_TEXTURE = new ResourceLocation(MOD_ID, "blocks/potion_still");
    public static final ResourceLocation POTION_TEXTURE_FLOW = new ResourceLocation(MOD_ID, "blocks/potion_flow");
    public static final ResourceLocation HONEY_TEXTURE = new ResourceLocation(MOD_ID, "blocks/honey_liquid");
    public static final ResourceLocation DRAGON_BREATH_TEXTURE = new ResourceLocation(MOD_ID, "blocks/dragon_breath_liquid");
    public static final ResourceLocation XP_TEXTURE = new ResourceLocation(MOD_ID, "blocks/xp_still");
    public static final ResourceLocation XP_TEXTURE_FLOW = new ResourceLocation(MOD_ID, "blocks/xp_flow");
    public static final ResourceLocation SOUP_TEXTURE = new ResourceLocation(MOD_ID, "blocks/soup_liquid");
    public static final ResourceLocation MAGMA_TEXTURE = new ResourceLocation(MOD_ID, "blocks/magma_still");
    public static final ResourceLocation MAGMA_TEXTURE_FLOW = new ResourceLocation(MOD_ID, "blocks/magma_flow");


    public static List<ResourceLocation> getTexturesToStitch() {
        return new ArrayList<>(Arrays.asList(MILK_TEXTURE, POTION_TEXTURE, POTION_TEXTURE_FLOW, HONEY_TEXTURE,
                DRAGON_BREATH_TEXTURE, SOUP_TEXTURE,MAGMA_TEXTURE,MAGMA_TEXTURE_FLOW, XP_TEXTURE, XP_TEXTURE_FLOW));
    }

}
