package net.mehvahdjukaar.moonlight.api.platform.configs;

import net.mehvahdjukaar.moonlight.api.platform.configs.options.ConfigReloadType;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

@ApiStatus.Internal
public interface IConfigValue<T> extends Supplier<T> {

    boolean setValue(T value);

    ConfigReloadType reloadType();

    boolean affectsDynamicPacks();
}
