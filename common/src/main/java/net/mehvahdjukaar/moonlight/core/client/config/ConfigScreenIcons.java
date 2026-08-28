package net.mehvahdjukaar.moonlight.core.client.config;

import net.mehvahdjukaar.moonlight.api.client.gui.AnimatedGuiItem;
import net.mehvahdjukaar.moonlight.api.client.gui.ConfigScreenExtensions;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class ConfigScreenIcons {

    private static final int ICON_SIZE = 16;

    private static final Map<Identifier, ItemStack> CACHE = new HashMap<>();
    private static boolean cacheIsDisplayOnly;

    public static ItemStack resolve(@Nullable Identifier id) {
        if (id == null) return ItemStack.EMPTY;
        boolean bound = Utils.areItemComponentsBound();
        if (bound && cacheIsDisplayOnly) {
            CACHE.clear();
            cacheIsDisplayOnly = false;
        }
        ItemStack cached = CACHE.get(id);
        if (cached != null) return cached;
        ItemStack resolved = compute(id);
        CACHE.put(id, resolved);
        cacheIsDisplayOnly = !bound;
        return resolved;
    }

    private static ItemStack compute(Identifier id) {
        Supplier<ItemStack> override = ConfigScreenExtensions.iconOverride(id);
        // overrides build real stacks, which needs components bound. Before that fall through to the plain lookup
        if (override != null && Utils.areItemComponentsBound()) {
            ItemStack s = override.get();
            if (s != null && !s.isEmpty()) return s;
        }
        var item = BuiltInRegistries.ITEM.getOptional(id);
        if (item.isPresent() && item.get() != Items.AIR) {
            return Utils.displayStack(item.get());
        }
        var block = BuiltInRegistries.BLOCK.getOptional(id);
        if (block.isPresent() && block.get() != Blocks.AIR) {
            ItemStack s = Utils.displayStack(block.get());
            if (!s.isEmpty()) return s;
        }
        return ItemStack.EMPTY;
    }

    public static boolean has(@Nullable Identifier id) {
        return id != null && !resolve(id).isEmpty();
    }

    public static boolean render(GuiGraphicsExtractor graphics, @Nullable Identifier id, int x, int y) {
        ItemStack stack = resolve(id);
        if (stack.isEmpty()) return false;
        graphics.item(stack, x, y);
        return true;
    }

    private static final int PERIOD = 36;
    // tint instead of combinedLight: the gui lightmap is white outside a world
    private static final int DISABLED_TINT = 0xFF595959;

    public static boolean renderAnimated(GuiGraphicsExtractor graphics, @Nullable Identifier id, int x, int y,
                                         float phase, boolean lit) {
        ItemStack stack = resolve(id);
        if (stack.isEmpty()) return false;
        if (phase <= 0 && lit) {
            graphics.item(stack, x, y); // the cheap path: no transform, no tint, straight through the item atlas
        } else {
            AnimatedGuiItem.submit(graphics, stack, x, y, ICON_SIZE, lit ? -1 : DISABLED_TINT,
                    (pose, blockModel) -> animate(pose, blockModel, phase));
        }
        return true;
    }

    private static void animate(Matrix4f pose, boolean blockModel, float phase) {
        if (phase <= 0) return;
        if (blockModel) {
            pose.rotateY(phase * 10f * Mth.DEG_TO_RAD); // 0..360 over a period -> continuous spin while hovered
        } else {
            pose.scale(1 + 0.1f * Mth.sin(phase * Mth.DEG_TO_RAD * 20f)); // gentle throb for flat items
        }
    }

    public static final class Anim {
        private float phase;
        private long lastMs = -1;

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
