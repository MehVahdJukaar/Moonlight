package net.mehvahdjukaar.moonlight.api.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.ItemLike;

import java.util.function.Supplier;

//ISTER provider
public interface ICustomItemRendererProvider extends ItemLike {

    @Environment(EnvType.CLIENT)
    Supplier<ItemStackRenderer> getRendererFactory();

    /**
     * Register this for fabric. does nothing for forge
     */
    default void registerFabricRenderer() {

    }
}
