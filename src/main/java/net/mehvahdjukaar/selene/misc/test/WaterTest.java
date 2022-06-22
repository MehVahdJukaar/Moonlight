package net.mehvahdjukaar.selene.misc.test;


import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;

//TODO: make foamy water fluid
@OnlyIn(Dist.CLIENT)
public class WaterTest {
    private static final float MAX_FLUID_HEIGHT = 0.8888889F;
    private final TextureAtlasSprite[] lavaIcons = new TextureAtlasSprite[2];
    private final TextureAtlasSprite[] waterIcons = new TextureAtlasSprite[2];
    private TextureAtlasSprite waterOverlay;

    protected void setupSprites() {
        this.lavaIcons[0] = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(Blocks.LAVA.defaultBlockState()).getParticleIcon();
        this.lavaIcons[1] = ModelBakery.LAVA_FLOW.sprite();
        this.waterIcons[0] = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(Blocks.WATER.defaultBlockState()).getParticleIcon();
        this.waterIcons[1] = ModelBakery.WATER_FLOW.sprite();
        this.waterOverlay = ModelBakery.WATER_OVERLAY.sprite();
    }

    private static boolean isNeighborSameFluid(BlockGetter p_110974_, BlockPos p_110975_, Direction p_110976_, FluidState p_110977_) {
        BlockPos blockpos = p_110975_.relative(p_110976_);
        FluidState fluidstate = p_110974_.getFluidState(blockpos);
        return fluidstate.getType().isSame(p_110977_.getType());
    }

    private static boolean isFaceOccludedByState(BlockGetter p_110979_, Direction p_110980_, float p_110981_, BlockPos p_110982_, BlockState p_110983_) {
        if (p_110983_.canOcclude()) {
            VoxelShape voxelshape = Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, (double)p_110981_, 1.0D);
            VoxelShape voxelshape1 = p_110983_.getOcclusionShape(p_110979_, p_110982_);
            return Shapes.blockOccudes(voxelshape, voxelshape1, p_110980_);
        } else {
            return false;
        }
    }

    private static boolean isFaceOccludedByNeighbor(BlockGetter p_110969_, BlockPos p_110970_, Direction p_110971_, float p_110972_) {
        BlockPos blockpos = p_110970_.relative(p_110971_);
        BlockState blockstate = p_110969_.getBlockState(blockpos);
        return isFaceOccludedByState(p_110969_, p_110971_, p_110972_, blockpos, blockstate);
    }

    private static boolean isFaceOccludedBySelf(BlockGetter p_110960_, BlockPos p_110961_, BlockState p_110962_, Direction p_110963_) {
        return isFaceOccludedByState(p_110960_, p_110963_.getOpposite(), 1.0F, p_110961_, p_110962_);
    }

    public static boolean shouldRenderFace(BlockAndTintGetter p_110949_, BlockPos p_110950_, FluidState p_110951_, BlockState p_110952_, Direction p_110953_) {
        return !isFaceOccludedBySelf(p_110949_, p_110950_, p_110952_, p_110953_) && !isNeighborSameFluid(p_110949_, p_110950_, p_110953_, p_110951_);
    }

    public boolean tesselate(BlockAndTintGetter tintGetter, BlockPos pos, VertexConsumer vertexConsumer, FluidState fluidState) {
        TextureAtlasSprite[] sprites = ForgeHooksClient.getFluidSprites(tintGetter, pos, fluidState);
        BlockState blockstate = tintGetter.getBlockState(pos);
        int color = 1;// fluidState.getType().getAttributes().getColor(tintGetter, pos);
        float alpha = (float)(color >> 24 & 255) / 255.0F;
        float red = (float)(color >> 16 & 255) / 255.0F;
        float green = (float)(color >> 8 & 255) / 255.0F;
        float blue = (float)(color & 255) / 255.0F;
        boolean renderUp = !isNeighborSameFluid(tintGetter, pos, Direction.UP, fluidState);
        boolean renderDown = shouldRenderFace(tintGetter, pos, fluidState, blockstate, Direction.DOWN) && !isFaceOccludedByNeighbor(tintGetter, pos, Direction.DOWN, 0.8888889F);
        boolean renderNorth = shouldRenderFace(tintGetter, pos, fluidState, blockstate, Direction.NORTH);
        boolean renderSouth = shouldRenderFace(tintGetter, pos, fluidState, blockstate, Direction.SOUTH);
        boolean renderWest = shouldRenderFace(tintGetter, pos, fluidState, blockstate, Direction.WEST);
        boolean renderEast = shouldRenderFace(tintGetter, pos, fluidState, blockstate, Direction.EAST);
        if (!renderUp && !renderDown && !renderEast && !renderWest && !renderNorth && !renderSouth) {
            return false;
        } else {
            boolean flag7 = false;
            float shadeDown = tintGetter.getShade(Direction.DOWN, true);
            float shadeUp = tintGetter.getShade(Direction.UP, true);
            float shadeNorth = tintGetter.getShade(Direction.NORTH, true);
            float shadeWest = tintGetter.getShade(Direction.WEST, true);
            float myHeight = this.getWaterHeight(tintGetter, pos, fluidState.getType());
            float southHeight = this.getWaterHeight(tintGetter, pos.south(), fluidState.getType());
            float cornerHeight = this.getWaterHeight(tintGetter, pos.east().south(), fluidState.getType());
            float eastHeight = this.getWaterHeight(tintGetter, pos.east(), fluidState.getType());
            double chunkX = pos.getX() & 15;
            double chunkY = pos.getY() & 15;
            double chunkZ = pos.getZ() & 15;
            float minY = renderDown ? 0.001F : 0.0F;
            if (renderUp && !isFaceOccludedByNeighbor(tintGetter, pos, Direction.UP, Math.min(Math.min(myHeight, southHeight), Math.min(cornerHeight, eastHeight)))) {
                flag7 = true;
                myHeight -= 0.001F;
                southHeight -= 0.001F;
                cornerHeight -= 0.001F;
                eastHeight -= 0.001F;
                Vec3 flow = fluidState.getFlow(tintGetter, pos);
                float minU;
                float minU2;
                float maxU;
                float maxU2;
                float minV;
                float maxV;
                float maxV2;
                float minV2;
                //still
                if (flow.x == 0.0D && flow.z == 0.0D) {
                    TextureAtlasSprite stillTexture = sprites[0];
                    minU = stillTexture.getU(0.0D);
                    minV = stillTexture.getV(0.0D);
                    minU2 = minU;
                    minV2 = minV;
                    maxV = stillTexture.getV(16.0D);
                    maxU = stillTexture.getU(16.0D);
                    maxV2 = maxV;
                    maxU2 = maxU;
                } else {
                    TextureAtlasSprite textureatlassprite = sprites[1];
                    float f21 = (float) Mth.atan2(flow.z, flow.x) - ((float)Math.PI / 2F);
                    float f22 = Mth.sin(f21) * 0.25F;
                    float f23 = Mth.cos(f21) * 0.25F;
                    float f24 = 8.0F;
                    minU = textureatlassprite.getU((double)(8.0F + (-f23 - f22) * 16.0F));
                    minV = textureatlassprite.getV((double)(8.0F + (-f23 + f22) * 16.0F));
                    minU2 = textureatlassprite.getU((double)(8.0F + (-f23 + f22) * 16.0F));
                    maxV = textureatlassprite.getV((double)(8.0F + (f23 + f22) * 16.0F));
                    maxU = textureatlassprite.getU((double)(8.0F + (f23 + f22) * 16.0F));
                    maxV2 = textureatlassprite.getV((double)(8.0F + (f23 - f22) * 16.0F));
                    maxU2 = textureatlassprite.getU((double)(8.0F + (f23 - f22) * 16.0F));
                    minV2 = textureatlassprite.getV((double)(8.0F + (-f23 - f22) * 16.0F));
                }

                float textW = (minU + minU2 + maxU + maxU2) / 4.0F;
                float textH = (minV + maxV + maxV2 + minV2) / 4.0F;
                float f46 = (float)sprites[0].getWidth() / (sprites[0].getU1() - sprites[0].getU0());
                float f47 = (float)sprites[0].getHeight() / (sprites[0].getV1() - sprites[0].getV0());
                float f48 = 4.0F / Math.max(f47, f46);
                minU = Mth.lerp(f48, minU, textW);
                minU2 = Mth.lerp(f48, minU2, textW);
                maxU = Mth.lerp(f48, maxU, textW);
                maxU2 = Mth.lerp(f48, maxU2, textW);
                minV = Mth.lerp(f48, minV, textH);
                maxV = Mth.lerp(f48, maxV, textH);
                maxV2 = Mth.lerp(f48, maxV2, textH);
                minV2 = Mth.lerp(f48, minV2, textH);
                int j = this.getLightColor(tintGetter, pos);
                float f25 = shadeUp * red;
                float f26 = shadeUp * green;
                float f27 = shadeUp * blue;
                //render up
                this.vertex(vertexConsumer, chunkX + 0.0D, chunkY + (double)myHeight, chunkZ + 0.0D, f25, f26, f27, alpha, minU, minV, j);
                this.vertex(vertexConsumer, chunkX + 0.0D, chunkY + (double)southHeight, chunkZ + 1.0D, f25, f26, f27, alpha, minU2, maxV, j);
                this.vertex(vertexConsumer, chunkX + 1.0D, chunkY + (double)cornerHeight, chunkZ + 1.0D, f25, f26, f27, alpha, maxU, maxV2, j);
                this.vertex(vertexConsumer, chunkX + 1.0D, chunkY + (double)eastHeight, chunkZ + 0.0D, f25, f26, f27, alpha, maxU2, minV2, j);
                if (fluidState.shouldRenderBackwardUpFace(tintGetter, pos.above())) {
                    this.vertex(vertexConsumer, chunkX + 0.0D, chunkY + (double)myHeight, chunkZ + 0.0D, f25, f26, f27, alpha, minU, minV, j);
                    this.vertex(vertexConsumer, chunkX + 1.0D, chunkY + (double)eastHeight, chunkZ + 0.0D, f25, f26, f27, alpha, maxU2, minV2, j);
                    this.vertex(vertexConsumer, chunkX + 1.0D, chunkY + (double)cornerHeight, chunkZ + 1.0D, f25, f26, f27, alpha, maxU, maxV2, j);
                    this.vertex(vertexConsumer, chunkX + 0.0D, chunkY + (double)southHeight, chunkZ + 1.0D, f25, f26, f27, alpha, minU2, maxV, j);
                }
            }

            if (renderDown) {
                float f35 = sprites[0].getU0();
                float f36 = sprites[0].getU1();
                float f37 = sprites[0].getV0();
                float f39 = sprites[0].getV1();
                int i1 = this.getLightColor(tintGetter, pos.below());
                float f41 = shadeDown * red;
                float f42 = shadeDown * green;
                float f43 = shadeDown * blue;
                this.vertex(vertexConsumer, chunkX, chunkY + (double)minY, chunkZ + 1.0D, f41, f42, f43, alpha, f35, f39, i1);
                this.vertex(vertexConsumer, chunkX, chunkY + (double)minY, chunkZ, f41, f42, f43, alpha, f35, f37, i1);
                this.vertex(vertexConsumer, chunkX + 1.0D, chunkY + (double)minY, chunkZ, f41, f42, f43, alpha, f36, f37, i1);
                this.vertex(vertexConsumer, chunkX + 1.0D, chunkY + (double)minY, chunkZ + 1.0D, f41, f42, f43, alpha, f36, f39, i1);
                flag7 = true;
            }

            int k = this.getLightColor(tintGetter, pos);

            for(int l = 0; l < 4; ++l) {
                float f38;
                float f40;
                double d3;
                double d4;
                double d5;
                double d6;
                Direction direction;
                boolean flag8;
                if (l == 0) {
                    f38 = myHeight;
                    f40 = eastHeight;
                    d3 = chunkX;
                    d5 = chunkX + 1.0D;
                    d4 = chunkZ + (double)0.001F;
                    d6 = chunkZ + (double)0.001F;
                    direction = Direction.NORTH;
                    flag8 = renderNorth;
                } else if (l == 1) {
                    f38 = cornerHeight;
                    f40 = southHeight;
                    d3 = chunkX + 1.0D;
                    d5 = chunkX;
                    d4 = chunkZ + 1.0D - (double)0.001F;
                    d6 = chunkZ + 1.0D - (double)0.001F;
                    direction = Direction.SOUTH;
                    flag8 = renderSouth;
                } else if (l == 2) {
                    f38 = southHeight;
                    f40 = myHeight;
                    d3 = chunkX + (double)0.001F;
                    d5 = chunkX + (double)0.001F;
                    d4 = chunkZ + 1.0D;
                    d6 = chunkZ;
                    direction = Direction.WEST;
                    flag8 = renderWest;
                } else {
                    f38 = eastHeight;
                    f40 = cornerHeight;
                    d3 = chunkX + 1.0D - (double)0.001F;
                    d5 = chunkX + 1.0D - (double)0.001F;
                    d4 = chunkZ;
                    d6 = chunkZ + 1.0D;
                    direction = Direction.EAST;
                    flag8 = renderEast;
                }

                if (flag8 && !isFaceOccludedByNeighbor(tintGetter, pos, direction, Math.max(f38, f40))) {
                    flag7 = true;
                    BlockPos blockpos = pos.relative(direction);
                    TextureAtlasSprite textureatlassprite2 = sprites[1];
                    if (sprites[2] != null) {
                        if (tintGetter.getBlockState(blockpos).shouldDisplayFluidOverlay(tintGetter, blockpos, fluidState)) {
                            textureatlassprite2 = sprites[2];
                        }
                    }

                    float f49 = textureatlassprite2.getU(0.0D);
                    float f50 = textureatlassprite2.getU(8.0D);
                    float f28 = textureatlassprite2.getV((double)((1.0F - f38) * 16.0F * 0.5F));
                    float f29 = textureatlassprite2.getV((double)((1.0F - f40) * 16.0F * 0.5F));
                    float f30 = textureatlassprite2.getV(8.0D);
                    float f31 = l < 2 ? shadeNorth : shadeWest;
                    float f32 = shadeUp * f31 * red;
                    float f33 = shadeUp * f31 * green;
                    float f34 = shadeUp * f31 * blue;
                    this.vertex(vertexConsumer, d3, chunkY + (double)f38, d4, f32, f33, f34, alpha, f49, f28, k);
                    this.vertex(vertexConsumer, d5, chunkY + (double)f40, d6, f32, f33, f34, alpha, f50, f29, k);
                    this.vertex(vertexConsumer, d5, chunkY + (double)minY, d6, f32, f33, f34, alpha, f50, f30, k);
                    this.vertex(vertexConsumer, d3, chunkY + (double)minY, d4, f32, f33, f34, alpha, f49, f30, k);
                    if (textureatlassprite2 != this.waterOverlay) {
                        this.vertex(vertexConsumer, d3, chunkY + (double)minY, d4, f32, f33, f34, alpha, f49, f30, k);
                        this.vertex(vertexConsumer, d5, chunkY + (double)minY, d6, f32, f33, f34, alpha, f50, f30, k);
                        this.vertex(vertexConsumer, d5, chunkY + (double)f40, d6, f32, f33, f34, alpha, f50, f29, k);
                        this.vertex(vertexConsumer, d3, chunkY + (double)f38, d4, f32, f33, f34, alpha, f49, f28, k);
                    }
                }
            }

            return flag7;
        }
    }

    private void vertex(VertexConsumer p_110985_, double p_110986_, double p_110987_, double p_110988_, float p_110989_, float p_110990_, float p_110991_, float alpha, float p_110992_, float p_110993_, int p_110994_) {
        p_110985_.vertex(p_110986_, p_110987_, p_110988_).color(p_110989_, p_110990_, p_110991_, alpha).uv(p_110992_, p_110993_).uv2(p_110994_).normal(0.0F, 1.0F, 0.0F).endVertex();
    }

    private int getLightColor(BlockAndTintGetter p_110946_, BlockPos p_110947_) {
        int i = LevelRenderer.getLightColor(p_110946_, p_110947_);
        int j = LevelRenderer.getLightColor(p_110946_, p_110947_.above());
        int k = i & 255;
        int l = j & 255;
        int i1 = i >> 16 & 255;
        int j1 = j >> 16 & 255;
        return (k > l ? k : l) | (i1 > j1 ? i1 : j1) << 16;
    }

    private float getWaterHeight(BlockGetter p_110965_, BlockPos p_110966_, Fluid p_110967_) {
        int i = 0;
        float f = 0.0F;

        for(int j = 0; j < 4; ++j) {
            BlockPos blockpos = p_110966_.offset(-(j & 1), 0, -(j >> 1 & 1));
            if (p_110965_.getFluidState(blockpos.above()).getType().isSame(p_110967_)) {
                return 1.0F;
            }

            FluidState fluidstate = p_110965_.getFluidState(blockpos);
            if (fluidstate.getType().isSame(p_110967_)) {
                float f1 = fluidstate.getHeight(p_110965_, blockpos);
                if (f1 >= 0.8F) {
                    f += f1 * 10.0F;
                    i += 10;
                } else {
                    f += f1;
                    ++i;
                }
            } else if (!p_110965_.getBlockState(blockpos).getMaterial().isSolid()) {
                ++i;
            }
        }

        return f / (float)i;
    }
}

