package net.mehvahdjukaar.moonlight.platform;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.mehvahdjukaar.moonlight.api.client.texture_renderer.DynamicTextureRenderer;
import net.mehvahdjukaar.moonlight.api.client.texture_renderer.RenderedTexturesManager;
import net.mehvahdjukaar.moonlight.api.misc.fake_level.FakeLevelManager;
import net.mehvahdjukaar.moonlight.core.MoonlightClient;
import net.mehvahdjukaar.moonlight.core.client.MLRenderTypes;
import net.mehvahdjukaar.moonlight.core.mixins.platform.ParticleEngineAccessor;
import net.minecraft.client.particle.ParticleRenderType;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MoonlightFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        //dont remove
        MoonlightFabric.commonSetup();
        ItemTooltipCallback.EVENT.register(MoonlightClient::onItemTooltip);
        ClientPlayConnectionEvents.DISCONNECT.register((clientPacketListener, minecraft) -> {
            FakeLevelManager.invalidateAll();
            RenderedTexturesManager.clearCache();
            DynamicTextureRenderer.clearCache();
        });
        PRE_CLIENT_SETUP_WORK.forEach(Runnable::run);
        CLIENT_SETUP_WORK.forEach(Runnable::run);

        PRE_CLIENT_SETUP_WORK.clear();
        CLIENT_SETUP_WORK.clear();

        List<ParticleRenderType> renderOrder = new ArrayList<>(ParticleEngineAccessor.getRENDER_ORDER());
        renderOrder.add(MLRenderTypes.PARTICLE_ADDITIVE_TRANSLUCENCY_RENDER_TYPE);
        ParticleEngineAccessor.setRENDER_ORDER(renderOrder);

        hasRunClientEntryPoint = true;
    }

    private static boolean hasRunClientEntryPoint = false;
    //to be honest I don't remember why i needed these 2 steps
    private static final Queue<Runnable> CLIENT_SETUP_WORK = new ConcurrentLinkedQueue<>();
    private static final Queue<Runnable> PRE_CLIENT_SETUP_WORK = new ConcurrentLinkedQueue<>();

    //not ideal. Ideally all should run from main initializer so all can be delegated

    public static void addClientTaskLate(Runnable r) {
        if (hasRunClientEntryPoint) {
            r.run();
        } else {
            CLIENT_SETUP_WORK.add(r);
        }
    }

    public static void addClientTask(Runnable r) {
        if (hasRunClientEntryPoint) {
            r.run();
        } else {
            PRE_CLIENT_SETUP_WORK.add(r);
        }
    }
}
