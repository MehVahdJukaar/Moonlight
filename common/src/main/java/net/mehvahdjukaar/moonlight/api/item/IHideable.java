package net.mehvahdjukaar.moonlight.api.item;

import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

public interface IHideable extends FeatureElement {

    boolean isHidden();

    static boolean isHidden(ItemLike itemLike){
        if(itemLike instanceof IHideable h)return h.isHidden();
        else {
            Item item = itemLike.asItem();
            if(item instanceof IHideable h){
                return h.isHidden();
            }else if(item instanceof BlockItem bi && bi.getBlock() instanceof IHideable h){
                return h.isHidden();
            }
        }
        return false;
    }
}
