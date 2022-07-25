package net.mehvahdjukaar.moonlight.api.client.model.fabric;

import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.moonlight.api.client.model.CustomGeometry;
import net.mehvahdjukaar.moonlight.core.mixins.fabric.BlockModelAccessor;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.function.Function;

public class UnbakedModelWrapper extends BlockModel {

    private final CustomGeometry geometry;

    public UnbakedModelWrapper(CustomGeometry geometry) {
        super(null, List.of(), Map.of(), true, null,
                ItemTransforms.NO_TRANSFORMS, List.of());
        this.geometry = geometry;
    }

    public UnbakedModelWrapper(BlockModel original, CustomGeometry geometry) {
        super(((BlockModelAccessor) original).getParentLocation(), List.of(),
                ((BlockModelAccessor) original).getTextureMap(), original.hasAmbientOcclusion(),
                original.getGuiLight(), original.getTransforms(), original.getOverrides());
        this.geometry = geometry;
    }

    @Override
    public Collection<Material> getMaterials(Function<ResourceLocation, UnbakedModel> modelGetter,
                                             Set<Pair<String, String>> missingTextureErrors) {
        Set<Material> old = new HashSet<>(super.getMaterials(modelGetter, missingTextureErrors));
        old.addAll(geometry.getMaterials(modelGetter, missingTextureErrors));
        return old;
    }

    @Override
    public BakedModel bake(ModelBakery modelBakery, Function<Material, TextureAtlasSprite> spriteGetter,
                           ModelState transform, ResourceLocation location) {
        return geometry.bake(modelBakery, spriteGetter, transform, location);
    }


}
