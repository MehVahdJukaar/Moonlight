package net.mehvahdjukaar.moonlight.api.client.gui;

import net.minecraft.client.gui.GuiGraphics;
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
 * The public hook for mods to extend Moonlight's native config UI for their own mod. Everything here is keyed by mod
 * id and meant to be called once from client setup.
 * <ul>
 *     <li>{@link #registerOverlay} draws over that mod's config-list screen (a banner, links, an extra tile, ...).</li>
 *     <li>{@link #registerIcon} maps a config {@code icon(...)} id to a custom {@link ItemStack} the default
 *     item/block lookup can't produce (a stack with data components, a made-up key, ...).</li>
 * </ul>
 */
public final class ConfigScreenExtensions {

    // ── per-mod overlays on the mod's config-list screen ──

    /** Something a mod paints over its config-list screen, with optional click handling. */
    public interface Overlay {
        void render(GuiGraphics graphics, Panel panel, int mouseX, int mouseY, float partialTick);

        /** Return true to consume the click (e.g. your own widget was hit). Default does nothing. */
        default boolean mouseClicked(Panel panel, double mouseX, double mouseY, int button) {
            return false;
        }
    }

    /** The content band of the config-list screen (between the header and footer bars), in screen pixels. */
    public record Panel(Screen screen, int left, int top, int right, int bottom) {
    }

    private static final Map<String, List<Overlay>> OVERLAYS = new HashMap<>();

    /** Adds an overlay to the config-list screen of {@code modId}. Call from client setup. */
    public static void registerOverlay(String modId, Overlay overlay) {
        OVERLAYS.computeIfAbsent(modId, k -> new ArrayList<>()).add(overlay);
    }

    @ApiStatus.Internal
    public static List<Overlay> overlaysFor(String modId) {
        return OVERLAYS.getOrDefault(modId, List.of());
    }

    // ── config icon overrides (formerly ConfigScreenIcons#registerOverride) ──

    private static final Map<ResourceLocation, Supplier<ItemStack>> ICON_OVERRIDES = new HashMap<>();

    /**
     * Binds a config {@code icon(...)} id to a custom stack, overriding the default item/block lookup. Call from
     * client setup (after registries are frozen). The {@code id} is whatever was passed to {@code icon(...)}.
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
