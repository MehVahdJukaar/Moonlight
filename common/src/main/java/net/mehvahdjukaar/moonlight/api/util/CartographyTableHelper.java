package net.mehvahdjukaar.moonlight.api.util;

import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Lets extra items go in the cartography table input slots.
 */
public class CartographyTableHelper {

    private static final ResourceLocation MAP_ICON = Moonlight.res("item/gui_slots/empty_slot_filled_map");
    private static final ResourceLocation PAPER_ICON = Moonlight.res("item/gui_slots/empty_slot_paper");
    private static final ResourceLocation GLASS_PANE_ICON = Moonlight.res("item/gui_slots/empty_slot_glass_pane");

    private static final List<SlotExtras> SLOTS = List.of(
            new SlotExtras(new ArrayList<>(), new ArrayList<>(List.of(MAP_ICON))),
            new SlotExtras(new ArrayList<>(), new ArrayList<>(List.of(PAPER_ICON, MAP_ICON, GLASS_PANE_ICON))));

    private static boolean hasCustomIcons = false;

    public static void addAllowedItem(int slot, Predicate<ItemStack> allowed) {
        SLOTS.get(slot).allowed.add(allowed);
    }

    /**
     * icon cycling in the empty slot. Block atlas sprite, so a texture under textures/item
     */
    public static void addSlotIcon(int slot, ResourceLocation icon) {
        List<ResourceLocation> icons = SLOTS.get(slot).icons;
        if (!icons.contains(icon)) icons.add(icon);
        hasCustomIcons = true;
    }

    @ApiStatus.Internal
    public static boolean isAllowed(int slot, ItemStack stack) {
        for (var allowed : SLOTS.get(slot).allowed) {
            if (allowed.test(stack)) return true;
        }
        return false;
    }

    @ApiStatus.Internal
    public static boolean hasCustomIcons() {
        return hasCustomIcons;
    }

    @ApiStatus.Internal
    public static List<ResourceLocation> getSlotIcons(int slot) {
        return SLOTS.get(slot).icons;
    }

    private record SlotExtras(List<Predicate<ItemStack>> allowed, List<ResourceLocation> icons) {
    }
}
