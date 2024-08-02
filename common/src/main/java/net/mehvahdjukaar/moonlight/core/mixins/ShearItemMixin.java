package net.mehvahdjukaar.moonlight.core.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.moonlight.api.MoonlightRegistry;
import net.mehvahdjukaar.moonlight.api.MoonlightTags;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.ArrayList;
import java.util.List;

@Mixin(ShearsItem.class)
public class ShearItemMixin {

    @ModifyArg(method = "createToolProperties", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/component/Tool;<init>(Ljava/util/List;FI)V"))
    private static List<Tool.Rule> ml$addShearableTag(List<Tool.Rule> rules) {
        List<Tool.Rule> list = new ArrayList<>(rules);
        list.add(Tool.Rule.minesAndDrops(MoonlightTags.SHEARABLE_TAG, 2));
        return list;
    }

    @ModifyReturnValue(method = "mineBlock", at = @At("RETURN"))
    public boolean m$mineBlock(boolean original, @Local(argsOnly = true) BlockState state) {
        if (!original && state.is(MoonlightTags.SHEARABLE_TAG)) return true;
        return original;
    }
}
