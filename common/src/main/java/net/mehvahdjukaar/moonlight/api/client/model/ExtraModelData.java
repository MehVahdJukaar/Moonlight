package net.mehvahdjukaar.moonlight.api.client.model;

import dev.architectury.injectables.annotations.ExpectPlatform;
import org.jetbrains.annotations.Nullable;

public interface ExtraModelData {

    public static final ExtraModelData EMPTY = ExtraModelData.builder().build();

    @ExpectPlatform
    static Builder builder() {
        throw new AssertionError();
    }

    @Nullable
    <T> T get(ModelDataKey<T> key);

    interface Builder {
        <A> Builder with(ModelDataKey<A> key, A data);

        ExtraModelData build();
    }
}


