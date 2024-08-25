package net.mehvahdjukaar.moonlight.api.client.model.neoforge;

import net.mehvahdjukaar.moonlight.api.client.model.CustomGeometry;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

import java.util.function.Function;

public class GeometryWrapper implements IUnbakedGeometry<GeometryWrapper> {

    private final CustomGeometry owner;

    public GeometryWrapper(CustomGeometry owner) {
        this.owner = owner;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext iGeometryBakingContext, ModelBaker bakery,
                           Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides itemOverrides) {
        return owner.bake(bakery, spriteGetter, modelState);
    }
}
