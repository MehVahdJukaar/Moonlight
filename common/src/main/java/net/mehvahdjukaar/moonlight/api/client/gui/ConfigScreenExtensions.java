package net.mehvahdjukaar.moonlight.api.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Hook for mods to add their own stuff to Moonlight's config screens. Everything here is keyed by mod id and meant
 * to be called once, from client setup.
 */
public final class ConfigScreenExtensions {

    public interface Overlay {
        void render(GuiGraphics graphics, Panel panel, int mouseX, int mouseY, float partialTick);

        default boolean mouseClicked(Panel panel, double mouseX, double mouseY, int button) {
            return false;
        }
    }

    /** The content band of the config-list screen, between the header and footer bars, in screen pixels. */
    public record Panel(Screen screen, int left, int top, int right, int bottom) {
    }

    private static final Map<String, List<Overlay>> OVERLAYS = new HashMap<>();

    /** Adds an overlay to the config list screen of a mod. */
    public static void registerOverlay(String modId, Overlay overlay) {
        OVERLAYS.computeIfAbsent(modId, k -> new ArrayList<>()).add(overlay);
    }

    @ApiStatus.Internal
    public static List<Overlay> overlaysFor(String modId) {
        return OVERLAYS.getOrDefault(modId, List.of());
    }

    @FunctionalInterface
    public interface Showcase {
        AbstractWidget create(String modId, int x, int y, int width, int maxHeight);

        default boolean replacesCarousel() {
            return true;
        }
    }

    private static final Map<String, Showcase> SHOWCASES = new HashMap<>();

    /** Replaces the mod icon and item carousel on a mod's config list screen. */
    public static void registerShowcase(String modId, Showcase showcase) {
        SHOWCASES.put(modId, showcase);
    }

    @ApiStatus.Internal
    @Nullable
    public static Showcase showcaseFor(String modId) {
        return SHOWCASES.get(modId);
    }

    private static final Map<ResourceLocation, Supplier<ItemStack>> ICON_OVERRIDES = new HashMap<>();

    /**
     * Ties a config icon(...) id to a stack of your choice instead of the usual item/block lookup. Call it after
     * registries are frozen.
     */
    public static void registerIcon(ResourceLocation id, Supplier<ItemStack> stack) {
        ICON_OVERRIDES.put(id, stack);
    }

    @ApiStatus.Internal
    @Nullable
    public static Supplier<ItemStack> iconOverride(ResourceLocation id) {
        return ICON_OVERRIDES.get(id);
    }
}
