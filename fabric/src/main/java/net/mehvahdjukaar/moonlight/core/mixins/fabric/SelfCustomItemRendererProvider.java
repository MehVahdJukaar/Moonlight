package net.mehvahdjukaar.moonlight.core.mixins.fabric;

import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.mehvahdjukaar.moonlight.api.client.ICustomItemRendererProvider;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ICustomItemRendererProvider.class)
public interface SelfCustomItemRendererProvider extends ICustomItemRendererProvider{

    default void registerFabricRenderer() {
        BuiltinItemRendererRegistry.INSTANCE.register(this, (BuiltinItemRendererRegistry.DynamicItemRenderer) this.createRenderer());
    }
}
