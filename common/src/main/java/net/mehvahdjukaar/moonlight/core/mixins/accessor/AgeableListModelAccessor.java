package net.mehvahdjukaar.moonlight.core.mixins.accessor;

import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AgeableListModel.class)
public interface AgeableListModelAccessor {

    @Invoker("bodyParts")
    Iterable<ModelPart> moonlight$invokeBodyParts();

}
