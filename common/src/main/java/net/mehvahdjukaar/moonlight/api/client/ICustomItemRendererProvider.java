package net.mehvahdjukaar.moonlight.api.client;

import net.mehvahdjukaar.candlelight.api.ClientOnly;
import net.minecraft.world.level.ItemLike;

import java.util.function.Supplier;

//ISTER provider
//TODO: deprecate and use events
@Deprecated(forRemoval = true)
public interface ICustomItemRendererProvider extends ItemLike {

    @ClientOnly
    Supplier<ItemStackRenderer> getRendererFactory();

}
