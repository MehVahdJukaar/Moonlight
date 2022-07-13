package net.mehvahdjukaar.moonlight.core.mixins.forge;

import net.mehvahdjukaar.moonlight.api.client.ICustomItemRendererProvider;
import net.mehvahdjukaar.moonlight.api.client.forge.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.util.NonNullLazy;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Consumer;

@Mixin(ICustomItemRendererProvider.class)
public interface SelfCustomItemRendererProvider extends ICustomItemRendererProvider{

    default void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            final NonNullLazy<BlockEntityWithoutLevelRenderer> renderer = NonNullLazy.of(() -> createRenderer());

            public BlockEntityWithoutLevelRenderer getItemStackRenderer() {
                return renderer.get();
            }
        });
    }
}
