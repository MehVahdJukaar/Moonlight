package net.mehvahdjukaar.moonlight.core.mixins.fabric;

import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.mehvahdjukaar.moonlight.api.client.model.CustomBakedModel;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Supplier;

@Mixin(CustomBakedModel.class)
public interface SelfCustomBakedModel extends FabricBakedModel, CustomBakedModel, BakedModel {

    @Override
    public default boolean isVanillaAdapter() {
        return false;
    }

    @Override
    default void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
        var attachment = ((RenderAttachedBlockView) blockView).getBlockEntityRenderAttachment(pos);
        if(attachment instanceof ExtraModelData data){
            //TODO: finish this
            var list = this.getBlockQuads(state, context.getEmitter().cullFace(),randomSupplier.get(),
                    RenderType.cutout(), data);
        }
    }

    @Override
    default void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
       // var list = this.getItemQuads(stack,randomSupplier.get());
        //TODO: now what?
    }
}
