package net.mehvahdjukaar.moonlight.api.client.model;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

@FunctionalInterface
public interface CustomGeometry {

    default BakedModel bakeModel(ModelBaker modelBaker, Function<Material, TextureAtlasSprite> spriteGetter,
                                 ModelState transform, ResourceLocation location) {
        return bake(modelBaker, spriteGetter, transform, location);
    }

    //remove and use above in 1.21
    @Deprecated(forRemoval = true)
    CustomBakedModel bake(ModelBaker modelBaker, Function<Material, TextureAtlasSprite> spriteGetter,
                          ModelState transform, ResourceLocation location);

}
