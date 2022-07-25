package net.mehvahdjukaar.moonlight.core.mixins.fabric;

import com.mojang.datafixers.util.Either;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(BlockModel.class)
public interface BlockModelAccessor {

    @Accessor("textureMap")
    Map<String, Either<Material, String>> getTextureMap();

    @Accessor("parentLocation")
    ResourceLocation getParentLocation();
}
