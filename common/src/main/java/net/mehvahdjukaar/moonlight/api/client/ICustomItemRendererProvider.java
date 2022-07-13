package net.mehvahdjukaar.moonlight.api.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.ItemLike;

//ISTER provider
public interface ICustomItemRendererProvider extends ItemLike {

    @Environment(EnvType.CLIENT)
    ItemStackRenderer createRenderer();

    /**
     * Register this for fabric. does nothing for forge
     */
    default void registerFabricRenderer() {

    }
}
