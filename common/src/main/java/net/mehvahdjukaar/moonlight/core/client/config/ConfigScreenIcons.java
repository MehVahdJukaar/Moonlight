package net.mehvahdjukaar.moonlight.core.client.config;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mehvahdjukaar.moonlight.api.client.gui.ConfigScreenExtensions;
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

public final class ConfigScreenIcons {

    private static final Map<ResourceLocation, ItemStack> CACHE = new HashMap<>();

    @Deprecated(forRemoval = true)
    public static void registerOverride(ResourceLocation id, Supplier<ItemStack> stack) {
        ConfigScreenExtensions.registerIcon(id, stack);
        CACHE.remove(id);
    }

    public static ItemStack resolve(@Nullable ResourceLocation id) {
        if (id == null) return ItemStack.EMPTY;
        ItemStack cached = CACHE.get(id);
        if (cached != null) return cached;
        ItemStack resolved = compute(id);
        CACHE.put(id, resolved);
        return resolved;
    }

    private static ItemStack compute(ResourceLocation id) {
        Supplier<ItemStack> override = ConfigScreenExtensions.iconOverride(id);
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

    public static boolean has(@Nullable ResourceLocation id) {
        return id != null && !resolve(id).isEmpty();
    }

    public static boolean render(GuiGraphics graphics, @Nullable ResourceLocation id, int x, int y) {
        ItemStack stack = resolve(id);
        if (stack.isEmpty()) return false;
        graphics.renderItem(stack, x, y);
        return true;
    }

    private static final int PERIOD = 36; // phase wraps here: a full Y spin, or two pulse cycles

    public static boolean renderAnimated(GuiGraphics graphics, @Nullable ResourceLocation id, int x, int y,
                                         float phase, boolean lit) {
        ItemStack stack = resolve(id);
        if (stack.isEmpty()) return false;

        if (!lit) RenderSystem.setShaderColor(0.35f, 0.35f, 0.35f, 1f);
        RenderUtil.renderGuiItemRelative(graphics.pose(), stack, x, y,
                Minecraft.getInstance().getItemRenderer(),
                (pose, model) -> animate(pose, model, phase), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        if (!lit) RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
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
