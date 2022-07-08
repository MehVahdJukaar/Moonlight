package net.mehvahdjukaar.moonlight.api.client.model;

import dev.architectury.injectables.annotations.ExpectPlatform;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface ExtraModelData {

    @ExpectPlatform
    static Builder builder() {
        throw new AssertionError();
    }

    @Nullable
    <T> T getData(ModelDataKey<T> key);

    interface Builder {
        <A> Builder withProperty(ModelDataKey<A> key, A data);

        ExtraModelData build();
    }
}


