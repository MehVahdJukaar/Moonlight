package net.mehvahdjukaar.moonlight.core.mixins;

import it.unimi.dsi.fastutil.objects.Object2FloatArrayMap;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.misc.CachingItemPropertyWrapperFunction;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(ItemOverrides.class)
public class ItemOverridesCacheMixin {

    @Unique
    private static final CachingItemPropertyWrapperFunction DUMMY = new CachingItemPropertyWrapperFunction();

    @Redirect(method = "resolve", at = @At(target = "Lnet/minecraft/client/renderer/item/ItemProperties;getProperty(Lnet/minecraft/world/item/Item;Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/item/ItemPropertyFunction;", value = "INVOKE"))
    public ItemPropertyFunction resolveWithCache(Item item, ResourceLocation location) {
        if (Moonlight.test) {
            DUMMY.set(item, location);
            return DUMMY;
        } else {
            return ItemProperties.getProperty(item, location);
        }
    }

    @Inject(method = "resolve", at = @At(
            value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;"))
    public void resetDummy(BakedModel model, ItemStack stack, ClientLevel level, LivingEntity entity, int seed, CallbackInfoReturnable<BakedModel> cir) {
        DUMMY.reset();
    }
}

