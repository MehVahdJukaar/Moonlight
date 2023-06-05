package net.mehvahdjukaar.moonlight.api.client.model;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.mehvahdjukaar.moonlight.api.client.util.VertexUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;
import java.util.function.Function;

public class RetexturedModel implements CustomBakedModel {


    private static final Map<ModelResourceLocation, RetexturedModel> parentCache = new Object2ObjectLinkedOpenHashMap<>();

    //TODO: reload this
    private static final Map<BlockState, Map<Direction, List<BakedQuad>>> modelCache = new IdentityHashMap<>();


    private final Block parent;
    private final boolean ambientOcclusion;
    private Function<String, TextureAtlasSprite> newSpriteGetter;
    private Map<Direction, List<String>> spriteOrder;
    private final TextureAtlasSprite particle;

    RetexturedModel(Block parent, Function<String, TextureAtlasSprite> newSpriteGetter,
                    Map<Direction, List<String>> spriteOrder, boolean ambientOcclusion) {
        this.parent = parent;
        this.newSpriteGetter = newSpriteGetter;
        this.spriteOrder = spriteOrder;
        this.particle = newSpriteGetter.apply("#particle");
        this.ambientOcclusion = ambientOcclusion;
    }

    @Override
    public List<BakedQuad> getBlockQuads(BlockState state, Direction side, RandomSource rand, RenderType renderType, ExtraModelData extraModelData) {
        if(state == null)state = parent.defaultBlockState();
        var map = modelCache.computeIfAbsent(state, s -> new HashMap<>());

        var v = map.get(side);
        if (v == null) {

            if(spriteOrder == null || !spriteOrder.containsKey(side)){
                map.put(side, List.of());
                return List.of();
            }

            var shaper = Minecraft.getInstance().getModelManager().getBlockModelShaper();

            BlockState parentState = parent.withPropertiesOf(state);
            var parentModel = shaper.getBlockModel(parentState);
            List<BakedQuad> quads = new ArrayList<>();

            try {

                var originalQuads = parentModel.getQuads(parentState, side, rand);
                if (!originalQuads.isEmpty()) {
                    var list = spriteOrder.get(side);
                    int i = 0;
                    for (var q : originalQuads) {
                        var textureName = list.get(i);

                        quads.add(VertexUtil.swapSprite(q, newSpriteGetter.apply(textureName)));
                        i++;
                    }
                }
            } catch (Exception ignored) {
            }
            spriteOrder.remove(side);
            if (map.size() == 7) newSpriteGetter = null;
            if (spriteOrder.isEmpty()){
                spriteOrder = null;
            }
            map.put(side, quads);
            return quads;
        } else return v;
    }

    @Override
    public TextureAtlasSprite getBlockParticle(ExtraModelData extraModelData) {
        return particle;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return ambientOcclusion;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

}
