package net.mehvahdjukaar.moonlight.api.item;

import net.mehvahdjukaar.moonlight.api.client.LoomItemRenderer;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * An item that acts like a banner inside a loom. It can be put in the banner slot, shift clicks into it,
 * and gets pattern layers added to it just like a banner would.
 */
public interface ILoomItem {

    Identifier  DEFAULT_LOOM_ICON = Moonlight.res("item/gui_slots/empty_slot_banner");
    /**
     * Color the pattern layers sit on top of. Only used by the vanilla preview, so when you have no renderer.
     */
    DyeColor getLoomBaseColor(ItemStack stack);

    /**
     * Icon for the loom empty slot, cycling with the banner one. GUI sprite, so a texture
     * under textures/gui/sprites like the smithing table ones. Null to stay out of the cycle
     */
    default Identifier getLoomSlotIcon() {
        return DEFAULT_LOOM_ICON;
    }

    @Nullable
    default Supplier<LoomItemRenderer> getLoomRenderer() {
        return null;
    }
}
