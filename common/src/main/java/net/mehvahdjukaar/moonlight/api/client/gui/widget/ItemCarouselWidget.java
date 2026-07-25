package net.mehvahdjukaar.moonlight.api.client.gui.widget;

import net.mehvahdjukaar.moonlight.api.client.gui.GuiHelper;
import net.mehvahdjukaar.moonlight.api.client.gui.misc.ConfigGuiColors;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A decorative strip of items panning slowly sideways and fading out into its own background at both edges. Meant to
 * show off what a mod adds on its config screens.
 * <p>
 * Not clickable (it never eats input), but hovering it names the item under the cursor and eases the panning to a
 * stop so there's time to actually read it.
 */
public class ItemCarouselWidget extends AbstractWidget {

    private static final int ICON = 16;
    private static final int GAP = 6;              // empty space between two icons
    private static final int CELL = ICON + GAP;
    private static final float SPEED = 14f;        // px per second
    private static final int FADE = 24;            // width of each edge fade
    private static final int MAX_ITEMS = 256;      // sanity cap for content-heavy mods

    private static final Map<String, List<ItemStack>> MOD_ITEMS = new HashMap<>();

    private final List<ItemStack> items;
    private final double span;                     // px of one full loop
    private int background = ConfigGuiColors.TILE_BG;
    private @Nullable Integer outline = null;

    private double offset;
    private float speed = SPEED;
    private long lastMs = -1;
    private int hoveredIndex = -1;

    public ItemCarouselWidget(int x, int y, int width, int height, List<ItemStack> items) {
        super(x, y, width, height, Component.empty());
        this.items = items;
        this.span = items.size() * (double) CELL;
        // inactive purely so it stays out of the tab order and lets clicks fall through to whatever is underneath;
        // rendering and the hover tooltip don't look at this flag
        this.active = false;
    }

    /** The carousel of every item {@code modId} registers, or null when it registers none. */
    @Nullable
    public static ItemCarouselWidget forMod(String modId, int x, int y, int width, int height) {
        List<ItemStack> items = itemsOf(modId);
        return items.isEmpty() ? null : new ItemCarouselWidget(x, y, width, height, items);
    }

    /**
     * The items in {@code modId}'s namespace worth showing off, in registry order (which is usually the author's own
     * grouping), skipping the ones that aren't finished content: feature flag gated items, items with no name in the
     * current language, and items with no model (they'd draw as the missing-model cube). Creative tab membership would
     * be the ideal filter, but the tabs stay empty until the player opens the creative menu, so it isn't usable here.
     */
    public static List<ItemStack> itemsOf(String modId) {
        return MOD_ITEMS.computeIfAbsent(modId, id -> {
            Level level = Minecraft.getInstance().level;
            FeatureFlagSet features = level == null ? FeatureFlags.DEFAULT_FLAGS : level.enabledFeatures();
            List<ItemStack> found = new ArrayList<>();
            for (Map.Entry<ResourceKey<Item>, Item> e : BuiltInRegistries.ITEM.entrySet()) {
                if (!e.getKey().location().getNamespace().equals(id)) continue;
                Item item = e.getValue();
                if (!item.isEnabled(features) || !I18n.exists(item.getDescriptionId())) continue;
                ItemStack stack = item.getDefaultInstance();
                if (stack.isEmpty() || hasNoModel(stack)) continue;
                found.add(stack);
                if (found.size() >= MAX_ITEMS) break;
            }
            return List.copyOf(found);
        });
    }

    private static boolean hasNoModel(ItemStack stack) {
        Minecraft mc = Minecraft.getInstance();
        return mc.getItemRenderer().getModel(stack, null, null, 0) == mc.getModelManager().getMissingModel();
    }

    /** The color the strip sits on and fades into at its edges. Must match whatever is behind it. */
    public ItemCarouselWidget background(int argb) {
        this.background = argb;
        return this;
    }

    /** Frames the strip with a 1px outline. Off by default. */
    public ItemCarouselWidget withOutline(int argb) {
        this.outline = argb;
        return this;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int right = this.getX() + this.width;
        int bottom = this.getY() + this.height;
        graphics.fill(this.getX(), this.getY(), right, bottom, this.background);
        if (this.outline != null) graphics.renderOutline(this.getX(), this.getY(), this.width, this.height, this.outline);
        if (this.items.isEmpty()) return;

        advance(this.isHovered);

        int fade = Math.min(FADE, this.width / 3);
        int firstCell = Mth.floor(this.offset / CELL);
        int shift = (int) Math.round(this.offset - firstCell * (double) CELL);
        int iconY = this.getY() + (this.height - ICON) / 2;
        int hovered = -1;

        graphics.enableScissor(this.getX(), this.getY(), right, bottom);
        for (int i = 0, cells = this.width / CELL + 2; i <= cells; i++) {
            int x = this.getX() + i * CELL + GAP / 2 - shift;
            int index = Math.floorMod(firstCell + i, this.items.size());
            graphics.renderFakeItem(this.items.get(index), x, iconY);
            // only the fully lit middle band gets a tooltip; items dissolving into the edges aren't really readable
            if (this.isHovered && mouseX >= x && mouseX < x + ICON && mouseX >= this.getX() + fade && mouseX < right - fade) {
                hovered = index;
            }
        }
        graphics.disableScissor();

        // guiOverlay skips the depth test, so the fade actually covers the items instead of being clipped by them
        int transparent = FastColor.ARGB32.color(0, this.background);
        RenderType overItems = RenderType.guiOverlay();
        GuiHelper.fillGradientHorizontal(graphics, overItems, this.getX(), this.getY(), this.getX() + fade, bottom, this.background, transparent);
        GuiHelper.fillGradientHorizontal(graphics, overItems, right - fade, this.getY(), right, bottom, transparent, this.background);

        if (hovered != this.hoveredIndex) {
            this.hoveredIndex = hovered;
            this.setTooltip(hovered < 0 ? null : Tooltip.create(this.items.get(hovered).getHoverName()));
        }
    }

    private void advance(boolean hovered) {
        long now = Util.getMillis();
        float dt = this.lastMs < 0 ? 0 : Math.min((now - this.lastMs) / 1000f, 0.1f); // clamp screen-reopen gaps
        this.lastMs = now;
        this.speed = Mth.lerp(Math.min(1f, dt * 6f), this.speed, hovered ? 0f : SPEED);
        this.offset = (this.offset + this.speed * dt) % this.span;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }
}