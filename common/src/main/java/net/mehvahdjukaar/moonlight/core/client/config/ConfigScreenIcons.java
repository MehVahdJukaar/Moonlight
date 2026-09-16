package net.mehvahdjukaar.moonlight.core.client.config;

import net.mehvahdjukaar.moonlight.api.client.gui.misc.ConfigGuiColors;
import net.mehvahdjukaar.moonlight.api.client.gui.AnimatedGuiItem;
import net.mehvahdjukaar.moonlight.api.client.gui.ConfigScreenExtensions;
import net.mehvahdjukaar.moonlight.api.client.gui.FrameClock;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class ConfigScreenIcons {

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


    public static boolean renderAnimated(GuiGraphicsExtractor graphics, @Nullable Identifier id, int x, int y,
                                         float phase, boolean lit) {
        ItemStack stack = resolve(id);
        if (stack.isEmpty()) return false;
        if (phase <= 0 && lit) {
            graphics.item(stack, x, y);
        } else {
            AnimatedGuiItem.submit(graphics, stack, x, y, 16, lit ? CommonColors.WHITE : ConfigGuiColors.DISABLED,
                    (pose, blockModel) -> animate(pose, blockModel, phase));
        }
        return true;
    }

    private static void animate(Matrix4f pose, boolean blockModel, float phase) {
        if (phase <= 0) return;
        if (blockModel) {
            pose.rotateY(phase * 10f * Mth.DEG_TO_RAD);
        } else {
            pose.scale(1 + 0.1f * Mth.sin(phase * Mth.DEG_TO_RAD * 20f));
        }
    }

    public static final class Anim {
        private final FrameClock clock = new FrameClock();
        private float phase;

        public void update(boolean hovered) {
             int period = 36;

            phase += (hovered ? 20f : -40f) * clock.advance();
            if (phase < 0) phase = 0;
            else if (phase > period) phase -= period;
        }

        public float phase() {
            return phase;
        }
    }
}
