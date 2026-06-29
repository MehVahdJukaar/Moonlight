package net.mehvahdjukaar.moonlight.core.mixins;

import net.mehvahdjukaar.moonlight.api.client.model.IModelPartExtension;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ModelPart.class)
public class ModelPartMixin implements IModelPartExtension {

    //stored /4 just so we take up a bit less memory (one byte instead of an int)
    @Unique
    private byte moonlight$textWidth = (byte) (64 / 4);
    @Unique
    private byte moonlight$textHeight = (byte) (64 / 4);

    @Override
    public void moonlight$setDimensions(int texWidth, int texHeight) {
        this.moonlight$textWidth = (byte) (texWidth / 4);
        this.moonlight$textHeight = (byte) (texHeight / 4);
    }

    @Override
    public int moonlight$getTextHeight() {
        return this.moonlight$textHeight * 4;
    }

    @Override
    public int moonlight$getTextWidth() {
        return this.moonlight$textWidth * 4;
    }
}
