package net.mehvahdjukaar.moonlight.api.client.model.forge;

import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.moonlight.api.client.model.CustomGeometry;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

public class GeometryWrapper implements IUnbakedGeometry<GeometryWrapper> {

    private final CustomGeometry owner;

    public GeometryWrapper(CustomGeometry owner) {
        this.owner = owner;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext iGeometryBakingContext, ModelBaker baker,
                           Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState,
                           ItemOverrides itemOverrides, ResourceLocation modelLocation) {
        return owner.bake(baker, spriteGetter, modelState, modelLocation);
    }

}
