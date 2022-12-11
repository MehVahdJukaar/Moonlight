package net.mehvahdjukaar.moonlight.api.client.model;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

public interface CustomGeometry {

    CustomBakedModel bake(ModelBaker modelBaker, Function<Material, TextureAtlasSprite> spriteGetter,
                          ModelState transform, ResourceLocation location);

}
