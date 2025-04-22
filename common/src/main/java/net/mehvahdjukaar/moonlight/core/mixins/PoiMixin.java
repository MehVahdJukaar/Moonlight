package net.mehvahdjukaar.moonlight.core.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Mixin(PoiType.class)
public class PoiMixin {

    @WrapOperation(method = "<init>",
            at = @At(value = "INVOKE", target = "Ljava/util/Set;copyOf(Ljava/util/Collection;)Ljava/util/Set;"))
    private Set<BlockState> moonlight$makePOIMutable(Collection<BlockState> coll, Operation<Set<BlockState>> original) {
        return new HashSet<>(original.call(coll));
    }
}
