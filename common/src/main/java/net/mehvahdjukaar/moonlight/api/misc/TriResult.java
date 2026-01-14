package net.mehvahdjukaar.moonlight.api.misc;

import net.minecraft.world.InteractionResult;
import org.jetbrains.annotations.Nullable;

public class TriResult<T> {
    private final T object;
    private boolean isPass;

    private TriResult(boolean isPass, T object) {
        this.isPass = isPass;
        this.object = object;
    }

    public T getObject() {
        return this.object;
    }

    public static <T> TriResult<T> success(T type) {
        return new TriResult<>(false, type);
    }

    public static <T> TriResult<T> fail() {
        return new TriResult<>(false, null);
    }

    public static <T> TriResult<T> pass() {
        return new TriResult<>(true, null);
    }

    public T orElse(@Nullable T other) {
        return this.object != null ? this.object : other;
    }

    public boolean isSuccess() {
        return this.object  != null;
    }

    public boolean isPass(){
        return this.isPass;
    }

    public boolean isFail(){
        return !isPass && this.object == null;
    }

}
