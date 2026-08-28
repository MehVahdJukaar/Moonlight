package net.mehvahdjukaar.moonlight.api.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Stacks several post effects at once, one per group, where vanilla only holds a single post effect id.
 * Effect ids are the post_effect/<id>.json paths.
 */
public class PostShadersHelper {

    public record Group(Identifier id, float priority) {
        public static final Group DEFAULT = new Group(Identifier.withDefaultNamespace("default"), 0);
        public static final Group SPECTATOR_SHADERS = new Group(Identifier.withDefaultNamespace("spectator_shaders"), 1);
    }

    public static final EffectStack SCREEN = new EffectStack();

    /** Sets the screen post effect of a group, or clears it when null. Groups are ordered by priority. */
    public static void toggleEffect(@Nullable Identifier newPost, Group group) {
        SCREEN.toggle(newPost, group);
    }

    /** Ordered post effects, at most one per group. */
    public static class EffectStack {

        private final Map<Group, Identifier> byGroup = new HashMap<>();
        private List<Identifier> ordered = List.of();

        /** Returns true if the stack changed. */
        public boolean toggle(@Nullable Identifier effect, Group group) {
            Identifier old = effect == null ? byGroup.remove(group) : byGroup.put(group, effect);
            if (Objects.equals(old, effect)) return false;
            this.ordered = byGroup.entrySet().stream()
                    .sorted(Comparator.comparingDouble(e -> e.getKey().priority()))
                    .map(Map.Entry::getValue)
                    .toList();
            return true;
        }

        public boolean isEmpty() {
            return ordered.isEmpty();
        }

        public List<Identifier> effects() {
            return ordered;
        }

        public void process(RenderTarget target, GraphicsResourceAllocator resourcePool) {
            if (ordered.isEmpty()) return;
            ShaderManager shaderManager = Minecraft.getInstance().getShaderManager();
            for (Identifier effect : ordered) {
                PostChain chain = shaderManager.getPostChain(effect, LevelTargetBundle.MAIN_TARGETS);
                if (chain != null) chain.process(target, resourcePool);
            }
        }
    }

    @ApiStatus.Internal
    public static void processScreenEffects(GraphicsResourceAllocator resourcePool) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        SCREEN.process(mc.getMainRenderTarget(), resourcePool);
    }
}
