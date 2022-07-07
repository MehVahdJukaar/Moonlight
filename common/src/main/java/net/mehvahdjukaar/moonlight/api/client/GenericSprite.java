package net.mehvahdjukaar.moonlight.api.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;
import java.util.function.Supplier;

public record GenericSprite(float minX, float minY, float maxX, float maxY,
                            VertexConsumer vertexConsumer) {

    public GenericSprite entity(MultiBufferSource bufferSource, ResourceLocation texture, Function<ResourceLocation, RenderType> entitySupplier) {
        var v = bufferSource.getBuffer(entitySupplier.apply(texture));
        return new GenericSprite(0, 0, 1, 1, v);
    }

    public GenericSprite block(MultiBufferSource bufferSource, ResourceLocation texture, Supplier<RenderType> blockSupplier) {
        var sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(texture);
        var v = bufferSource.getBuffer(blockSupplier.get());
        return new GenericSprite(sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1(), v);
    }
}
