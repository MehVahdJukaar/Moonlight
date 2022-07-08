package net.mehvahdjukaar.moonlight.core.mixins.forge;

import net.mehvahdjukaar.moonlight.api.client.model.DynamicBakedModel;
import net.mehvahdjukaar.moonlight.api.client.model.forge.ExtraModelDataImpl;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.extensions.IForgeBakedModel;
import net.minecraftforge.client.model.IDynamicBakedModel;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(DynamicBakedModel.class)
public interface SelfDynamicBakedModel extends IForgeBakedModel, IDynamicBakedModel, DynamicBakedModel {


    @Override
    default List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand,
                                     ModelData modelData, @Nullable RenderType type) {
        return this.getBlockQuads(state, side, rand, type, new ExtraModelDataImpl(modelData));
    }

    ;


}
