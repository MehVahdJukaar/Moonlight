package net.mehvahdjukaar.selene.data.test;


import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.selene.resourcepack.DynamicTexturePack;
import net.mehvahdjukaar.selene.resourcepack.PaletteSwapper;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;


public class PaletteTest extends DynamicTexturePack implements PreparableReloadListener {


    public PaletteTest(ResourceLocation name) {
        super(name);
        this.addNamespaces("farmersdelight");
    }

    private static String getPathFromLocation(PackType p_10227_, ResourceLocation p_10228_) {
        return String.format("%s/%s/%s", p_10227_.getDirectory(), p_10228_.getNamespace(), p_10228_.getPath());
    }


    // resource loading stuff

    @Override
    public CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier stage, ResourceManager manager,
                                          ProfilerFiller workerProfiler, ProfilerFiller mainProfiler,
                                          Executor workerExecutor, Executor mainExecutor) {
        // we need to ensure generated textures are ready before the texture stitchers load them
        // unfortunately, the texture stitchers run during the worker thread phase
        // so we need to generate our textures on the main thread, before the worker phase starts
        // (we are here)

        //generate textures
        this.generateAssets(manager, mainProfiler);

        // prepare = worker thread stuff
        return CompletableFuture.supplyAsync(() -> null, workerExecutor)
                // wait for off-thread phase to conclude
                .thenCompose(stage::wait)
                // then do stuff on main thread again
                .thenAcceptAsync((noResult) -> {
                }, mainExecutor);
    }

    // resource loading helpers

    protected void generateAssets(ResourceManager manager, ProfilerFiller profiler) {
        //LOGGER.info("Starting autopalette texture generation");
        // get all available packs (even unselected ones)
        Minecraft minecraft = Minecraft.getInstance();
        PackRepository packList = minecraft.getResourcePackRepository();
        Map<String, Pack> selectedPacks = packList.getSelectedPacks()
                .stream()
                .collect(Collectors.toMap(Pack::getId, info -> info));

        ResourceLocation originalTexture = getTexturePath(new ResourceLocation("minecraft","block/amethyst_block"));
        ResourceLocation destination = getTexturePath(new ResourceLocation("minecraft","block/emerald_block"));
        var generated = this.generateImage(originalTexture, destination, manager);

        generated.forEach(g -> this.addBytes(g.getFirst(), g.getSecond()));

        originalTexture = getTexturePath(new ResourceLocation("minecraft","block/lapis_block"));
        destination = getTexturePath(new ResourceLocation("minecraft","block/coal_block"));
        generated = this.generateImage(originalTexture, destination, manager);

        generated.forEach(g -> this.addBytes(g.getFirst(), g.getSecond()));

        originalTexture = getTexturePath(new ResourceLocation("minecraft","block/deepslate"));
        destination = getTexturePath(new ResourceLocation("minecraft","block/stone"));
        generated = this.generateImage(originalTexture, destination, manager);

        generated.forEach(g -> this.addBytes(g.getFirst(), g.getSecond()));

        originalTexture = getTexturePath(new ResourceLocation("minecraft","block/raw_copper_block"));
        destination = getTexturePath(new ResourceLocation("minecraft","block/mossy_cobblestone"));
        generated = this.generateImage(originalTexture, destination, manager);

        generated.forEach(g -> this.addBytes(g.getFirst(), g.getSecond()));



        originalTexture = getTexturePath(new ResourceLocation("minecraft","block/magma_block"));
        destination = getTexturePath(new ResourceLocation("minecraft","block/podzol_side"));
        generated = this.generateImage(originalTexture, destination, manager);

        generated.forEach(g -> this.addBytes(g.getFirst(), g.getSecond()));
    }

    public List<Pair<ResourceLocation, byte[]>> generateImage(
            ResourceLocation originalTexture, ResourceLocation newTexture, ResourceManager manager) {

        List<Pair<ResourceLocation, byte[]>> list = new ArrayList<>();

        //manager = Minecraft.getInstance().getResourceManager();

        try (InputStream inputStream = manager.getResource(originalTexture).getInputStream() ) {
            NativeImage image = NativeImage.read(inputStream);

            InputStream inputStream1 = manager.getResource(newTexture).getInputStream();

            NativeImage toImage = NativeImage.read(inputStream1);

            PaletteSwapper swapper = new PaletteSwapper(toImage);

            NativeImage transformedImage = swapper.recolorImage(image);

            list.add(Pair.of(newTexture, transformedImage.asByteArray()));

            //try getting metadata for animated textures
            ResourceLocation metadataLocation = getMetadataPath(originalTexture);

            if (manager.hasResource(metadataLocation)) {
                BufferedReader bufferedReader = null;

                try (InputStream metadataStream = manager.getResource(metadataLocation).getInputStream()) {
                    bufferedReader = new BufferedReader(new InputStreamReader(metadataStream, StandardCharsets.UTF_8));
                    JsonObject metadataJson = GsonHelper.parse(bufferedReader);

                    list.add(Pair.of(getMetadataPath(newTexture), metadataJson.toString().getBytes()));
                } finally {
                    IOUtils.closeQuietly(bufferedReader);
                }
            }
        } catch (IOException e) {
            //LOGGER.error("Cannot override texture {} in pack {} specified by override {}: error getting texture", parentTextureID, parentPackID, overrideID);
            e.printStackTrace();
        }
        return list;
    }



}
