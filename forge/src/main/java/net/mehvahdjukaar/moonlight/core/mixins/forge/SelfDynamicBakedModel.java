package net.mehvahdjukaar.moonlight.core.mixins.forge;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.client.model.DynamicBakedModel;
import net.mehvahdjukaar.moonlight.api.client.model.forge.ExtraModelDataImpl;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.extensions.IForgeBakedModel;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(DynamicBakedModel.class)
public interface SelfDynamicBakedModel extends IForgeBakedModel, IDynamicBakedModel, DynamicBakedModel {


    @Override
    default List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource randomSource, @NotNull IModelData data) {
        return this.getBlockQuads(state, direction, randomSource, new ExtraModelDataImpl(data));
    }


    default boolean doesHandlePerspectives() {
        return false;
    }


    default @NotNull IModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull IModelData modelData) {
        return modelData;
    }

    default boolean isLayered() {
        return false;
    }


}
