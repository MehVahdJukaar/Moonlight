package net.mehvahdjukaar.moonlight.core.misc;

import it.unimi.dsi.fastutil.objects.Object2FloatArrayMap;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class CachingItemPropertyWrapperFunction implements ItemPropertyFunction {
    private final Map<ResourceLocation, Float> cache = new Object2FloatArrayMap<>();

    private ResourceLocation location = null;
    private Item item;

    @Override
    public float call(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int i) {
        return cache.computeIfAbsent(location,(j)-> {
            var p = ItemProperties.getProperty(item, location);
            if(p != null)p.call(itemStack, clientLevel, livingEntity, i);
            return Float.NEGATIVE_INFINITY;
        });
    }

    public void set(Item item, ResourceLocation location){
        this.item = item;
        this.location = location;
    }

    public void reset(){
        this.cache.clear();
    }
}
