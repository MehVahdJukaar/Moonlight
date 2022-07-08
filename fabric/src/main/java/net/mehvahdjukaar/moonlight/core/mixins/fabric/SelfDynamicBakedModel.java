package net.mehvahdjukaar.moonlight.core.mixins.fabric;

import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.mehvahdjukaar.moonlight.api.client.model.DynamicBakedModel;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Supplier;

@Mixin(DynamicBakedModel.class)
public interface SelfDynamicBakedModel extends FabricBakedModel, DynamicBakedModel {

    @Override
    public default boolean isVanillaAdapter() {
        return false;
    }

    @Override
    default void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
        var attachment = ((RenderAttachedBlockView) blockView).getBlockEntityRenderAttachment(pos);
        if(attachment instanceof ExtraModelData data){
            var list = this.getBlockQuads(state, context.getEmitter().cullFace(),randomSupplier.get(), data);
        }
    }

    @Override
    default void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
        var list = this.getItemQuads(stack,randomSupplier.get());
        //TODO: now what?
    }
}
