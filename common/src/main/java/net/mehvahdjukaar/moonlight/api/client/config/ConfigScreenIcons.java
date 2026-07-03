package net.mehvahdjukaar.moonlight.api.client.config;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mehvahdjukaar.moonlight.api.client.util.RenderUtil;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Client side resolver for the decorative icons attached to config rows via
 * {@link net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder#icon}. A config only ever stores an inert
 * icon id (a {@link ResourceLocation}); this turns that id into a rendered 16x16 item at screen render time, when
 * the item/block registries are populated. Resolution is memoized, so it's cheap to call every frame.
 * <p>
 * By default an id resolves to the matching item, else the matching block's item. Mods that need something a plain
 * lookup can't produce (a stack with data components, a made-up icon key, ...) can {@link #registerOverride register
 * an override} from their client setup.
 */
public final class ConfigScreenIcons {

    private static final Map<ResourceLocation, Supplier<ItemStack>> OVERRIDES = new HashMap<>();
    private static final Map<ResourceLocation, ItemStack> CACHE = new HashMap<>();

    /**
     * Binds an icon id to a custom stack, overriding the default item/block lookup. Call from client setup (after
     * registries are frozen). The {@code id} is whatever was passed to {@code icon(...)} in the config.
     */
    public static void registerOverride(ResourceLocation id, Supplier<ItemStack> stack) {
        OVERRIDES.put(id, stack);
        CACHE.remove(id);
    }

    /**
     * Resolves an icon id to a renderable stack: a registered override, else the matching item, else the matching
     * block's item, else {@link ItemStack#EMPTY}. Memoized.
     */
    public static ItemStack resolve(@Nullable ResourceLocation id) {
        if (id == null) return ItemStack.EMPTY;
        ItemStack cached = CACHE.get(id);
        if (cached != null) return cached;
        ItemStack resolved = compute(id);
        CACHE.put(id, resolved);
        return resolved;
    }

    private static ItemStack compute(ResourceLocation id) {
        Supplier<ItemStack> override = OVERRIDES.get(id);
        if (override != null) {
            ItemStack s = override.get();
            if (s != null && !s.isEmpty()) return s;
        }
        var item = BuiltInRegistries.ITEM.getOptional(id);
        if (item.isPresent() && item.get() != Items.AIR) {
            return item.get().getDefaultInstance();
        }
        var block = BuiltInRegistries.BLOCK.getOptional(id);
        if (block.isPresent() && block.get() != Blocks.AIR) {
            ItemStack s = new ItemStack(block.get());
            if (!s.isEmpty()) return s;
        }
        return ItemStack.EMPTY;
    }

    /** Whether {@code id} resolves to a drawable icon (so callers can decide layout before drawing). */
    public static boolean has(@Nullable ResourceLocation id) {
        return id != null && !resolve(id).isEmpty();
    }

    /**
     * Draws the resolved icon as a 16x16 GUI item at {@code (x, y)}. Returns {@code true} if something was drawn, so
     * the caller can fall back to a default (e.g. a folder sprite) when it wasn't.
     */
    public static boolean render(GuiGraphics graphics, @Nullable ResourceLocation id, int x, int y) {
        ItemStack stack = resolve(id);
        if (stack.isEmpty()) return false;
        graphics.renderItem(stack, x, y);
        return true;
    }

    // ===== hover animation (ported from the old Configured screen) =====

    private static final int PERIOD = 36; // phase wraps here: a full Y spin, or two pulse cycles

    /**
     * Draws the resolved icon at {@code (x, y)} with a hover animation: 3D block models spin about Y, flat items
     * pulse in scale. {@code phase} comes from an {@link Anim} the caller keeps per row. {@code lit} renders the
     * item fully bright (pass the row's enabled state; a disabled row draws it dark, like the label greys out).
     * Returns {@code true} if something was drawn.
     */
    public static boolean renderAnimated(GuiGraphics graphics, @Nullable ResourceLocation id, int x, int y,
                                         float phase, boolean lit) {
        ItemStack stack = resolve(id);
        if (stack.isEmpty()) return false;
        int light = lit ? LightTexture.FULL_BRIGHT : 0;
        RenderUtil.renderGuiItemRelative(graphics.pose(), stack, x, y,
                Minecraft.getInstance().getItemRenderer(),
                (pose, model) -> animate(pose, model, phase), light, OverlayTexture.NO_OVERLAY);
        return true;
    }

    private static void animate(PoseStack pose, BakedModel model, float phase) {
        if (phase <= 0) return;
        if (model.usesBlockLight()) {
            pose.mulPose(Axis.YP.rotationDegrees(phase * 10f)); // 0..360 over a period -> continuous spin while hovered
        } else {
            float scale = 1 + 0.1f * Mth.sin(phase * Mth.DEG_TO_RAD * 20f); // gentle throb for flat items
            pose.scale(scale, scale, scale);
        }
    }

    /**
     * Per-row hover animation phase, driven by wall-clock time so it needs no screen tick hook. Ramps up while the
     * row is hovered and decays back to rest otherwise, mirroring the old Configured screen's {@code +1/-2 per tick}.
     * Keep one instance per row and call {@link #update} each frame before reading {@link #phase}.
     */
    public static final class Anim {
        private float phase;
        private long lastMs = -1;

        /** Advances the phase toward spinning (hovered) or rest (not), based on elapsed real time. */
        public void update(boolean hovered) {
            long now = Util.getMillis();
            float dt = lastMs < 0 ? 0 : Math.min((now - lastMs) / 1000f, 0.1f); // clamp big gaps (e.g. screen reopen)
            lastMs = now;
            phase += (hovered ? 20f : -40f) * dt; // +1/-2 per 1/20s tick, expressed as a per-second rate
            if (phase < 0) phase = 0;
            else if (phase > PERIOD) phase -= PERIOD;
        }

        public float phase() {
            return phase;
        }
    }
}
