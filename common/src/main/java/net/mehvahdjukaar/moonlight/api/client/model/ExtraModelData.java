package net.mehvahdjukaar.moonlight.api.client.model;

import net.mehvahdjukaar.candlelight.api.PlatformImpl;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Just wraps forge and fabric ones
 */
public interface ExtraModelData {

    ExtraModelData EMPTY = ClassLoadingBs.getInstance();

    @PlatformImpl
    static Builder builder() {
        throw new AssertionError();
    }

    @Nullable <T> T get(ModelDataKey<T> key);

    Map<ModelDataKey<?>, Object> values();

    interface Builder {
        <A> Builder with(ModelDataKey<A> key, A data);

        ExtraModelData build();
    }

    default boolean isEmpty() {
        return this == EMPTY;
    }

    //prevents circular dependency when this class is loaded at the same time on 2 threads
    class ClassLoadingBs {
        private static class Holder {
            static final ExtraModelData INSTANCE = ExtraModelData.builder().build();
        }

        static ExtraModelData getInstance() {
            return Holder.INSTANCE;
        }
    }
}


