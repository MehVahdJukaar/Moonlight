package net.mehvahdjukaar.moonlight.core.mixins.fabric;

import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.mehvahdjukaar.moonlight.api.client.model.IExtraModelDataProvider;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(IExtraModelDataProvider.class)
public interface SelfIDynamicModelProvider extends RenderAttachmentBlockEntity, IExtraModelDataProvider {

    @Override
    default Object getRenderAttachmentData() {
        return this.getExtraModelData();
    }

}
