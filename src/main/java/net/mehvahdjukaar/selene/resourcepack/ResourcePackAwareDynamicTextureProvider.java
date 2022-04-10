package net.mehvahdjukaar.selene.resourcepack;

import com.google.common.base.Stopwatch;
import com.mojang.blaze3d.platform.NativeImage;
import net.mehvahdjukaar.selene.textures.Respriter;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public abstract class ResourcePackAwareDynamicTextureProvider implements PreparableReloadListener {

    private boolean hasBeenInitialized;

    //creates this object and registers it
    public ResourcePackAwareDynamicTextureProvider() {
        ((ReloadableResourceManager) Minecraft.getInstance().getResourceManager())
                .registerReloadListener(this);
    }

    public abstract DynamicTexturePack getDynamicPack();

    public abstract Logger getLogger();

    public abstract boolean hasTexturePackSupport();

    public abstract void regenerateTextures(ResourceManager manager);

    public void generateStaticAssetsOnStartup(ResourceManager manager) {
    }

    ;

    @Override
    final public CompletableFuture<Void> reload(PreparationBarrier stage, ResourceManager manager,
                                                ProfilerFiller workerProfiler, ProfilerFiller mainProfiler,
                                                Executor workerExecutor, Executor mainExecutor) {
        Stopwatch watch = Stopwatch.createStarted();

        boolean resourcePackSupport = this.hasTexturePackSupport();

        if (!this.hasBeenInitialized) {
            this.hasBeenInitialized = true;
            generateStaticAssetsOnStartup(manager);
            if (!resourcePackSupport) {
                VanillaResourceManager vanillaManager = new VanillaResourceManager();
                this.regenerateTextures(vanillaManager);
                vanillaManager.close();
            }
        }

        //generate textures
        if (resourcePackSupport) {
            this.regenerateTextures(manager);
        }

        getLogger().info("Generated runtime client resources for pack {} in: {} ms",
                this.getDynamicPack().getName(),
                watch.elapsed().toMillis());

        return CompletableFuture.supplyAsync(() -> null, workerExecutor)
                .thenCompose(stage::wait)
                .thenAcceptAsync((noResult) -> {
                }, mainExecutor);
    }


    //some helper methods
    //TODO: cleanup and reorganize

    /**
     * recolors the template image with the color grabbed from the given image restrained to its mask, if possible
     */
    @Nullable
    protected NativeImage recolorFromVanilla(ResourceManager manager, NativeImage vanillaTexture, ResourceLocation vanillaMask,
                                             ResourceLocation templateTexture) {
        try (NativeImage scribbleMask = this.readImage(manager, vanillaMask);
             NativeImage template = readImage(manager, templateTexture)) {
            Respriter respriter = new Respriter(template);
            return respriter.recolorImage(vanillaTexture, scribbleMask);
        } catch (Exception ignored) {
        }
        return null;
    }

    protected boolean alreadyHasTextureAtLocation(ResourceManager manager, ResourceLocation res) {
        ResourceLocation fullRes = RPUtils.resPath(res, RPUtils.ResType.TEXTURES);
        if (manager.hasResource(fullRes)) {
            try (var r = manager.getResource(fullRes)) {
                return !r.getSourceName().equals(this.getDynamicPack().getName());
            } catch (IOException ignored) {
            }
        }
        return false;
    }

    protected NativeImage readImage(ResourceManager manager, ResourceLocation resourceLocation) throws IOException {
        return NativeImage.read(manager.getResource(resourceLocation).getInputStream());
    }

    @Nullable
    protected RPUtils.StaticResource getResOrLog(ResourceManager manager, ResourceLocation location) {
        try {
            return new RPUtils.StaticResource(manager.getResource(location));
        } catch (Exception e) {
            getLogger().error("Could not find resource {} while generating dynamic resource pack", location);
        }
        return null;
    }
}
