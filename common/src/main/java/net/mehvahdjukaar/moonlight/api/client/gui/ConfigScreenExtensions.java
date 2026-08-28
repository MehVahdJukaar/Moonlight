package net.mehvahdjukaar.moonlight.api.client.gui;

import net.mehvahdjukaar.moonlight.api.client.gui.widget.MediaButton;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.Identifier;
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
        void render(GuiGraphicsExtractor graphics, Panel panel, int mouseX, int mouseY, float partialTick);

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

    private static final Map<Identifier, Supplier<ItemStack>> ICON_OVERRIDES = new HashMap<>();

    /**
     * Ties a config icon(...) id to a stack of your choice instead of the usual item/block lookup. Call it after
     * registries are frozen.
     */
    public static void registerIcon(Identifier id, Supplier<ItemStack> stack) {
        ICON_OVERRIDES.put(id, stack);
    }

    @ApiStatus.Internal
    @Nullable
    public static Supplier<ItemStack> iconOverride(Identifier id) {
        return ICON_OVERRIDES.get(id);
    }

    public enum Side {LEFT, RIGHT}

    public record FooterLink(MediaButton.MediaIcon icon, String url) {
    }

    private static final Map<String, List<FooterLink>> LINKS = new HashMap<>();

    /** Adds a link button next to Back. Patreon and ko-fi go left, anything else joins the mod page buttons.
     * Curseforge, Modrinth, GitHub, Discord, YouTube and Twitter urls are read from the loader metadata already. */
    public static void registerLink(String modId, MediaButton.MediaIcon icon, String url) {
        LINKS.computeIfAbsent(modId, k -> new ArrayList<>()).add(new FooterLink(icon, url));
    }

    @ApiStatus.Internal
    public static List<FooterLink> linksFor(String modId) {
        return LINKS.getOrDefault(modId, List.of());
    }

    @FunctionalInterface
    public interface FooterButton {
        Button create(Screen screen, int x, int y);
    }

    public record FooterButtonEntry(Side side, FooterButton factory) {
    }

    private static final Map<String, List<FooterButtonEntry>> FOOTER_BUTTONS = new HashMap<>();

    public static void registerFooterButton(String modId, Side side, FooterButton factory) {
        FOOTER_BUTTONS.computeIfAbsent(modId, k -> new ArrayList<>()).add(new FooterButtonEntry(side, factory));
    }

    @ApiStatus.Internal
    public static List<FooterButtonEntry> footerButtonsFor(String modId) {
        return FOOTER_BUTTONS.getOrDefault(modId, List.of());
    }

    @ApiStatus.Internal
    public static boolean hasFooterExtras(String modId) {
        return !linksFor(modId).isEmpty() || !footerButtonsFor(modId).isEmpty();
    }
}
