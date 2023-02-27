package net.mehvahdjukaar.moonlight.api.misc;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class RegistryAccessJsonReloadListener extends SimpleJsonResourceReloadListener {

    private static final List<RegistryAccessJsonReloadListener> INSTANCES = new ArrayList<>();

    @ApiStatus.Internal
    public static void runReloads(RegistryAccess access){
        for(var v : INSTANCES){
            if(v.jsonMap != null) {
                v.parse(v.jsonMap, access);
                v.jsonMap = null;
            }
        }
    }

    @Nullable
    private Map<ResourceLocation, JsonElement> jsonMap;

    protected RegistryAccessJsonReloadListener(Gson gson, String string) {
        super(gson, string);
        INSTANCES.add(this);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        this.jsonMap = object;
    }

    public abstract void parse(Map<ResourceLocation, JsonElement> jsonMap, RegistryAccess access);

}
