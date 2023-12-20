package net.mehvahdjukaar.moonlight.api.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

//ISTER provider
public interface ICustomItemRendererProvider extends ItemLike {

    @Environment(EnvType.CLIENT)
    Supplier<ItemStackRenderer> getRendererFactory();

}
