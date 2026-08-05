package net.mehvahdjukaar.moonlight.api.client;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Allows one to add and remove post shader effects in an ordered, grouped and non-destructive way.
 */
public class PostShadersHelper {

    public record Group(ResourceLocation id, float priority) {
        public static final Group DEFAULT = new Group(ResourceLocation.withDefaultNamespace("default"), 0);
        public static final Group SPECTATOR_SHADERS = new Group(ResourceLocation.withDefaultNamespace("spectator_shaders"), 1);
    }

    /**
     * Use instead of loadEffect: adds a post-effect non-destructively, so multiple mods can work together.
     *
     * @param newPost post-effect. Null to remove it
     * @param group   effect group, used for priority and mutual exclusivity
     */
    public static void toggleEffect(@Nullable ResourceLocation newPost, Group group) {
        GameRenderer gr = Minecraft.getInstance().gameRenderer;
        try {
            RenderTarget target = gr.postEffect != null ? gr.postEffect.screenTarget : Minecraft.getInstance().getMainRenderTarget();
            gr.postEffect = refreshComposite(gr.postEffect, newPost, group, target);
            gr.effectActive = gr.postEffect != null;
        } catch (IOException ioexception) {
            //  LOGGER.warn("Failed to load shader: {}", resourceLocation, ioexception);
            gr.effectActive = false;
        } catch (JsonSyntaxException jsonsyntaxexception) {
            //   LOGGER.warn("Failed to parse shader: {}", resourceLocation, jsonsyntaxexception);
            gr.effectActive = false;
        }
    }

    public static @Nullable PostChain refreshComposite(@Nullable PostChain currentChain,
                                                       @Nullable ResourceLocation newPost,
                                                       Group group, RenderTarget mainTarget) throws IOException {
        PostChain newChain;
        if (currentChain == null) {
            if (newPost == null) return null;
            newChain = ComposedPostChain.create(newPost, group, mainTarget);
        } else if (currentChain instanceof ComposedPostChain cpc) {
            newChain = cpc.with(newPost, group);
        } else {
            // another mod set gr.postEffect directly. Empty passes mean the chain was already closed, so treat it
            // like a null one instead of wrapping dead GL state
            if (currentChain.passes.isEmpty()) {
                if (newPost == null) return null;
                newChain = ComposedPostChain.create(newPost, group, mainTarget);
            } else {
                // actively rendering external chain: absorb it as DEFAULT and apply our change on top
                ComposedPostChain wrapped = ComposedPostChain.wrap(currentChain, Group.DEFAULT);
                newChain = (newPost == null) ? wrapped : wrapped.with(newPost, group);
                if (newChain == null) newChain = wrapped;
            }
        }
        return newChain;
    }

    private static final class ComposedPostChain extends PostChain {

        private final Map<Group, PostChain> chainsPerGroup = new HashMap<>();

        private ComposedPostChain(TextureManager textureManager, ResourceProvider resourceProvider,
                                  RenderTarget screenTarget, ResourceLocation resourceLocation) throws IOException, JsonSyntaxException {
            super(textureManager, resourceProvider, screenTarget, resourceLocation);
        }


        @Override
        public void close() {
            for (PostChain sub : chainsPerGroup.values()) {
                sub.close();
            }
            chainsPerGroup.clear();
            passes.clear(); // references are now dead
            customRenderTargets.clear();
            fullSizedTargets.clear();
        }


        //prevent it from loading normally
        public void load(@NotNull TextureManager textureManager, @NotNull ResourceLocation resourceLocation) throws IOException, JsonSyntaxException {
        }

        private static ComposedPostChain wrap(PostChain vanillaChain, Group group) throws IOException {
            Minecraft mc = Minecraft.getInstance();
            TextureManager tm = mc.getTextureManager();
            ResourceManager rm = mc.getResourceManager();
            var cc = new ComposedPostChain(tm, rm, vanillaChain.screenTarget, ResourceLocation.parse(vanillaChain.getName()));
            cc.addSubChain(vanillaChain, group);
            return cc;
        }

        private static ComposedPostChain create(ResourceLocation postEffect, Group group, RenderTarget mainTarget) throws IOException {
            Minecraft mc = Minecraft.getInstance();
            TextureManager tm = mc.getTextureManager();
            ResourceManager rm = mc.getResourceManager();
            PostChain vanillaChain = new PostChain(tm, rm, mainTarget, postEffect);
            vanillaChain.resize(mainTarget.width, mainTarget.height);

            return wrap(vanillaChain, group);
        }

        private @Nullable ComposedPostChain with(@Nullable ResourceLocation newEffect, Group group) throws IOException {
            Minecraft mc = Minecraft.getInstance();
            TextureManager tm = mc.getTextureManager();
            ResourceManager rm = mc.getResourceManager();

            //check if it's already good
            if (newEffect != null) {
                PostChain existing = this.chainsPerGroup.get(group);
                if (existing != null && existing.getName().equals(newEffect.toString())) {
                    return this;
                }
            }
            // copy existing groups
            Map<Group, PostChain> newGroups = new HashMap<>(this.chainsPerGroup);
            if (newEffect == null) {
                PostChain removed = newGroups.remove(group);
                if (removed != null) removed.close();
                if (newGroups.isEmpty()) {
                    // if no groups left, return null to indicate no post-chain needed
                    return null;
                }
            } else {
                PostChain newChain = new PostChain(tm, rm, this.screenTarget, newEffect);
                newChain.resize(this.screenTarget.width, this.screenTarget.height);
                // close the old sub-chain for this group so its GL programs are freed
                PostChain old = newGroups.put(group, newChain);
                if (old != null) old.close();
            }

            // sort groups by priority
            List<Map.Entry<Group, PostChain>> ordered =
                    newGroups.entrySet().stream()
                            .sorted((a, b) -> Float.compare(a.getKey().priority(), b.getKey().priority()))
                            .toList();

            ResourceLocation newName = ordered.size() == 1 ?
                    ResourceLocation.parse(ordered.getFirst().getValue().getName()) :
                    ResourceLocation.withDefaultNamespace("composed/" +
                            ordered.stream()
                            .map(e -> e.getValue().getName()
                                      .replace(":", "_"))
                            .reduce((a, b) -> a + "_" + b).orElse("empty"));

            // create new composed chain (empty base)
            ComposedPostChain result = new ComposedPostChain(
                    tm, rm,
                    this.screenTarget,
                    newName
            );
            // rebuild passes + targets in order
            for (var entry : ordered) {
                PostChain pc = entry.getValue();
                result.addSubChain(pc, entry.getKey());
            }

            result.resize(this.screenTarget.width, this.screenTarget.height);

            return result;

        }

        private void addSubChain(PostChain chain, Group group) {
            this.chainsPerGroup.put(group, chain);

            this.passes.addAll(chain.passes);
            this.customRenderTargets.putAll(chain.customRenderTargets);
            this.fullSizedTargets.addAll(chain.fullSizedTargets);

            this.time = chain.time;
            this.lastStamp = chain.lastStamp;


        }
    }


}
