package net.mehvahdjukaar.moonlight.api.client;

import net.mehvahdjukaar.candlelight.api.PlatformImpl;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;


/**
 * extra data on an entity render state. Mere wrapper of the forge and fabric ones.
 * See ClientHelper.addEntityRenderDataRegistration
 */
public interface ExtraEntityRenderDataKey<T> {

    @PlatformImpl
    static <T> ExtraEntityRenderDataKey<T> create(Identifier id) {
        throw new AssertionError();
    }

    @Nullable
    T get(EntityRenderState state);

    void set(EntityRenderState state, @Nullable T data);

    @FunctionalInterface
    interface Extractor<E extends Entity, S extends EntityRenderState, T> {

        @Nullable
        T extract(E entity, S state, float partialTicks);
    }

}
