package net.mehvahdjukaar.moonlight.core.mixins.fabric;

import net.mehvahdjukaar.moonlight.api.client.model.CustomGeometry;
import net.mehvahdjukaar.moonlight.api.client.model.fabric.BlockModelWithCustomGeo;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin(BlockModel.class)
public class BlockModelMixin {

    // makes models with a custom parent loader also use its geometry baking
    @Inject(at = @At("HEAD"), cancellable = true,
            method = "bake(Lnet/minecraft/client/resources/model/ModelBaker;Lnet/minecraft/client/renderer/block/model/BlockModel;Ljava/util/function/Function;Lnet/minecraft/client/resources/model/ModelState;Lnet/minecraft/resources/ResourceLocation;Z)Lnet/minecraft/client/resources/model/BakedModel;")
    void moonlight$bakeCustomGeo(ModelBaker baker, BlockModel model, Function<Material, TextureAtlasSprite> spriteGetter,
                                 ModelState state, ResourceLocation location, boolean guiLight3d,
                                 CallbackInfoReturnable<BakedModel> cir) {
        CustomGeometry geo = getParentGeoRecursive(model);
        if (geo != null) {
            cir.setReturnValue(geo.bakeModel(baker, spriteGetter, state, location));
        }
    }

    @Unique
    private static CustomGeometry getParentGeoRecursive(BlockModel model) {
        if (model != null) {
            if (model instanceof BlockModelWithCustomGeo w) {
                return w.getCustomGeometry();
            }
            return getParentGeoRecursive(model.parent);
        }
        return null;
    }


}
