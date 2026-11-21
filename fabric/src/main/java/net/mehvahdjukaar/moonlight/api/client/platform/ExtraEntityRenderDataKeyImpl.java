package net.mehvahdjukaar.moonlight.api.client.platform;

import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.mehvahdjukaar.moonlight.api.client.ExtraEntityRenderDataKey;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public record ExtraEntityRenderDataKeyImpl<T>(RenderStateDataKey<T> key) implements ExtraEntityRenderDataKey<T> {

    public static <T> ExtraEntityRenderDataKey<T> create(Identifier id) {
        return new ExtraEntityRenderDataKeyImpl<>(RenderStateDataKey.create(id::toString));
    }

    @Nullable
    @Override
    public T get(EntityRenderState state) {
        return state.getData(key);
    }

    @Override
    public void set(EntityRenderState state, @Nullable T data) {
        state.setData(key, data);
    }

}