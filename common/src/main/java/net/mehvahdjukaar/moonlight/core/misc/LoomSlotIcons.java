package net.mehvahdjukaar.moonlight.core.misc;

import net.mehvahdjukaar.moonlight.api.item.ILoomItem;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

public class LoomSlotIcons {

    private static List<Identifier> icons = null;

    public static List<Identifier> get() {
        if (icons == null) {
            List<Identifier> found = new ArrayList<>();
            found.add(ILoomItem.DEFAULT_LOOM_ICON);
            for (Item item : BuiltInRegistries.ITEM) {
                if (item instanceof ILoomItem loom) {
                    Identifier icon = loom.getLoomSlotIcon();
                    //flags and the like share one icon across all their colors
                    if (icon != null && !found.contains(icon)) found.add(icon);
                }
            }
            icons = found;
        }
        return icons;
    }

    public static boolean hasCustom() {
        return get().size() > 1;
    }
}
