package net.mehvahdjukaar.moonlight.api.client.model.fabric;

import net.mehvahdjukaar.moonlight.api.client.model.CustomGeometry;
import net.mehvahdjukaar.moonlight.core.mixins.fabric.BlockModelAccessor;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class BlockModelWithCustomGeo extends BlockModel {

    private final CustomGeometry geometry;

    public BlockModelWithCustomGeo(CustomGeometry geometry) {
        super(null, List.of(), Map.of(), true, null,
                ItemTransforms.NO_TRANSFORMS, List.of());
        this.geometry = geometry;
    }

    public BlockModelWithCustomGeo(BlockModel original, CustomGeometry geometry) {
        super(((BlockModelAccessor) original).getParentLocation(), List.of(),
                ((BlockModelAccessor) original).getTextureMap(), original.hasAmbientOcclusion(),
                original.getGuiLight(), original.getTransforms(), original.getOverrides());
        this.geometry = geometry;
    }

    public CustomGeometry getCustomGeometry() {
        return this.geometry;
    }

}
