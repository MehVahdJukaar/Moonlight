package net.mehvahdjukaar.moonlight.api.client.platform;

import net.mehvahdjukaar.moonlight.api.client.ExtraEntityRenderDataKey;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import org.jetbrains.annotations.Nullable;

public record ExtraEntityRenderDataKeyImpl<T>(ContextKey<T> key) implements ExtraEntityRenderDataKey<T> {

    public static <T> ExtraEntityRenderDataKey<T> create(Identifier id) {
        return new ExtraEntityRenderDataKeyImpl<>(new ContextKey<>(id));
    }

    @Nullable
    @Override
    public T get(EntityRenderState state) {
        return state.getRenderData(key);
    }

    @Override
    public void set(EntityRenderState state, @Nullable T data) {
        state.setRenderData(key, data);
    }

}
