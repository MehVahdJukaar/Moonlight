package net.mehvahdjukaar.moonlight.api.client.model;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

public interface CustomGeometry {

    CustomBakedModel bake(ModelBakery modelBakery, Function<Material, TextureAtlasSprite> spriteGetter,
                          ModelState transform, ResourceLocation location);

}
