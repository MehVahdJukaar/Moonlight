package net.mehvahdjukaar.moonlight.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraftforge.client.IItemRenderProperties;
import net.minecraftforge.common.util.NonNullLazy;

import java.util.function.BiFunction;
import java.util.function.Consumer;

public class ClientUtils {
    public static void registerISTER(Consumer<IItemRenderProperties> consumer, BiFunction<BlockEntityRenderDispatcher, EntityModelSet, BlockEntityWithoutLevelRenderer> factory) {
        consumer.accept(new IItemRenderProperties() {
            final NonNullLazy<BlockEntityWithoutLevelRenderer> renderer = NonNullLazy.of(
                    () -> factory.apply(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                            Minecraft.getInstance().getEntityModels()));

            @Override
            public BlockEntityWithoutLevelRenderer getItemStackRenderer() {
                return renderer.get();
            }
        });
    }
}
