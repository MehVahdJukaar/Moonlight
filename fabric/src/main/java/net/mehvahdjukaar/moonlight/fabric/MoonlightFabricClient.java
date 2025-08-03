package net.mehvahdjukaar.moonlight.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.impl.resource.conditions.ResourceConditionsImpl;
import net.mehvahdjukaar.moonlight.core.client.MLRenderTypes;
import net.mehvahdjukaar.moonlight.core.mixins.fabric.ParticleEngineAccessor;
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
        MLFabricSetupCallbacks.CLIENT_SETUP.forEach(Runnable::run);
        MLFabricSetupCallbacks.CLIENT_SETUP.clear();

        PRE_CLIENT_SETUP_WORK.forEach(Runnable::run);
        CLIENT_SETUP_WORK.forEach(Runnable::run);
        PRE_CLIENT_SETUP_WORK.clear();
        CLIENT_SETUP_WORK.clear();

        List<ParticleRenderType> renderOrder = new ArrayList<>(ParticleEngineAccessor.getRENDER_ORDER());
        renderOrder.add(MLRenderTypes.PARTICLE_ADDITIVE_TRANSLUCENCY_RENDER_TYPE);
        ParticleEngineAccessor.setRENDER_ORDER(renderOrder);
    }

    public static Queue<Runnable> CLIENT_SETUP_WORK = new ConcurrentLinkedQueue<>();
    public static Queue<Runnable> PRE_CLIENT_SETUP_WORK = new ConcurrentLinkedQueue<>();

}
