package net.mehvahdjukaar.moonlight.api.client.gui.widget;

import net.mehvahdjukaar.moonlight.api.client.gui.GuiHelper;
import net.mehvahdjukaar.moonlight.api.client.gui.misc.ConfigGuiColors;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.component.DataComponents;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ARGB;
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
 * A decorative strip of items panning slowly, showcasing the mod items.
 */
public class ItemCarouselWidget extends AbstractWidget {

    private static final int ICON = 16;
    private static final int SPACE_BETWEEN_ICONS = 6;
    private static final int CELL = ICON + SPACE_BETWEEN_ICONS;
    private static final float SPEED = 14f;        // px per second
    private static final float SCROLL_IMPULSE = 220f;  // px per second added by one wheel notch
    private static final float MAX_FLING = 900f;
    private static final float FLING_DECAY = 4f;   // fraction of the fling shed per second
    private static final int FADE_WIDTH = 24;
    private static final int MAX_ITEMS_CAP = 256;

    private static final Map<String, List<ItemStack>> MOD_ITEMS = new HashMap<>();
    private static boolean cacheIsDisplayOnly;

    private final List<ItemStack> items;
    private final double span;                     // px of one full loop
    private int background = ConfigGuiColors.TILE_BG;
    private @Nullable Integer outline = null;

    private double offset;
    private float speed = SPEED;
    private float fling;
    private long lastMs = -1;
    private int hoveredIndex = -1;

    public ItemCarouselWidget(int x, int y, int width, int height, List<ItemStack> items) {
        super(x, y, width, height, Component.empty());
        this.items = items;
        this.span = items.size() * (double) CELL;
        this.active = false;
    }

    @Nullable
    public static ItemCarouselWidget forMod(String modId, int x, int y, int width, int height) {
        List<ItemStack> items = itemsOf(modId);
        return items.isEmpty() ? null : new ItemCarouselWidget(x, y, width, height, items);
    }

    public static List<ItemStack> itemsOf(String modId) {
        boolean bound = Utils.areItemComponentsBound();
        if (bound && cacheIsDisplayOnly) {
            MOD_ITEMS.clear();
            cacheIsDisplayOnly = false;
        }
        List<ItemStack> cached = MOD_ITEMS.get(modId);
        if (cached != null) return cached;
        List<ItemStack> found = collect(modId);
        MOD_ITEMS.put(modId, found);
        cacheIsDisplayOnly = !bound;
        return found;
    }

    private static List<ItemStack> collect(String modId) {
        Level level = Minecraft.getInstance().level;
        FeatureFlagSet features = level == null ? FeatureFlags.DEFAULT_FLAGS : level.enabledFeatures();
        List<ItemStack> found = new ArrayList<>();
        for (Map.Entry<ResourceKey<Item>, Item> e : BuiltInRegistries.ITEM.entrySet()) {
            if (!e.getKey().identifier().getNamespace().equals(modId)) continue;
            Item item = e.getValue();
            if (!item.isEnabled(features) || !I18n.exists(item.getDescriptionId())) continue;
            ItemStack stack = Utils.displayStack(item);
            if (stack.isEmpty() || hasNoModel(stack)) continue;
            found.add(stack);
            if (found.size() >= MAX_ITEMS_CAP) break;
        }
        return List.copyOf(found);
    }

    private static boolean hasNoModel(ItemStack stack) {
        Identifier modelId = stack.get(DataComponents.ITEM_MODEL);
        // the model manager would log a warning for a missing model
        return modelId == null || !Minecraft.getInstance().getModelManager().bakedItemStackModels.containsKey(modelId);
    }

    public ItemCarouselWidget background(int argb) {
        this.background = argb;
        return this;
    }

    public ItemCarouselWidget withOutline(int argb) {
        this.outline = argb;
        return this;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int right = this.getX() + this.width;
        int bottom = this.getY() + this.height;
        graphics.fill(this.getX(), this.getY(), right, bottom, this.background);
        if (this.outline != null) graphics.outline(this.getX(), this.getY(), this.width, this.height, this.outline);
        if (this.items.isEmpty()) return;

        advance(this.isHovered);

        int fade = Math.min(FADE_WIDTH, this.width / 3);
        int firstCell = Mth.floor(this.offset / CELL);
        double shift = this.offset - firstCell * (double) CELL; // [0, CELL)
        int wholeShift = (int) shift;
        float subShift = (float) (shift - wholeShift);
        int iconY = this.getY() + (this.height - ICON) / 2;
        int hovered = -1;

        graphics.enableScissor(this.getX(), this.getY(), right, bottom);
        graphics.pose().pushMatrix();
        graphics.pose().translate(-subShift, 0f);
        for (int i = 0, cells = this.width / CELL + 2; i <= cells; i++) {
            int x = this.getX() + i * CELL + SPACE_BETWEEN_ICONS / 2 - wholeShift;
            int index = Math.floorMod(firstCell + i, this.items.size());
            graphics.fakeItem(this.items.get(index), x, iconY);
            float drawnX = x - subShift;
            if (this.isHovered && mouseX >= drawnX && mouseX < drawnX + ICON
                    && mouseX >= this.getX() + fade && mouseX < right - fade) {
                hovered = index;
            }
        }
        graphics.pose().popMatrix();
        graphics.disableScissor();

        int transparent = ARGB.color(0, this.background);
        GuiHelper.fillGradientHorizontal(graphics, this.getX(), this.getY(), this.getX() + fade, bottom, this.background, transparent);
        GuiHelper.fillGradientHorizontal(graphics, right - fade, this.getY(), right, bottom, transparent, this.background);

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
        this.fling = Mth.lerp(Math.min(1f, dt * FLING_DECAY), this.fling, 0f);
        double moved = (this.offset + (this.speed + this.fling) * dt) % this.span;
        this.offset = moved < 0 ? moved + this.span : moved;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.items.isEmpty()) return false;
        this.fling = Mth.clamp(this.fling - (float) scrollY * SCROLL_IMPULSE, -MAX_FLING, MAX_FLING);
        return true;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.visible && mouseX >= this.getX() && mouseY >= this.getY()
                && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;
    }

    @Override
    protected boolean isValidClickButton(MouseButtonInfo buttonInfo) {
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }
}