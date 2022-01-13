package net.mehvahdjukaar.selene.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FluidsReloadListener extends SimpleJsonResourceReloadListener {

    public FluidsReloadListener(Gson gson, String s) {
        super(gson, s);
    }

    //TODO: finish this
    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager manager, ProfilerFiller profile) {

        jsons.forEach((key, input) -> {
            if (input != null) {

            }
        });

    }
}
