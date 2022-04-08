package net.mehvahdjukaar.selene;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.selene.block_set.BlockSetManager;
import net.mehvahdjukaar.selene.block_set.leaves.LeavesType;
import net.mehvahdjukaar.selene.block_set.leaves.LeavesTypeRegistry;
import net.mehvahdjukaar.selene.block_set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.selene.block_set.wood.WoodType;
import net.mehvahdjukaar.selene.builtincompat.CompatWoodTypes;
import net.mehvahdjukaar.selene.data.ModCriteriaTriggers;
import net.mehvahdjukaar.selene.fluids.FluidContainerList;
import net.mehvahdjukaar.selene.fluids.SoftFluid;
import net.mehvahdjukaar.selene.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.selene.fluids.serialization.SoftFluidCodec;
import net.mehvahdjukaar.selene.network.NetworkHandler;
import net.mehvahdjukaar.selene.villager_ai.VillagerAIManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

@Mod(Selene.MOD_ID)
public class Selene {

    public static final String MOD_ID = "selene";

    public static final Logger LOGGER = LogManager.getLogger();

    public Selene() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        VillagerAIManager.SCHEDULES.register(bus);
        bus.addListener(Selene::init);
        MinecraftForge.EVENT_BUS.addListener(Selene::addJsonListener);
        BlockSetManager.registerBlockSetDefinition(WoodType.class, new WoodTypeRegistry());
        BlockSetManager.registerBlockSetDefinition(LeavesType.class, new LeavesTypeRegistry());
        CompatWoodTypes.init();
    }

    public static void addJsonListener(final AddReloadListenerEvent event) {
        event.addListener(Reloader.INSTANCE);
    }

    public static void init(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModCriteriaTriggers.init();
            NetworkHandler.registerMessages();
            VillagerAIManager.init();
            SoftFluidRegistry.init();
            ModSoftFluids.init();
        });

    }

    public static class Reloader extends SimpleJsonResourceReloadListener {
        public static Reloader INSTANCE = new Reloader();

        private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create(); //json object that will write stuff

        public Reloader() {
            super(GSON, "soft_fluids");
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {


            //  if (true) return;
            File folder = FMLPaths.GAMEDIR.get().resolve("recorded_songs").toFile();

            if (!folder.exists()) {
                folder.mkdir();
            }

            try {
                for(SoftFluid f : SoftFluidRegistry.getFluids()) {
                    try (FileWriter writer = new FileWriter(new File(folder,f.getRegistryName().getPath()+".json"))) {
                        writeToFile(f, writer);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void writeToFile(final SoftFluid obj, FileWriter writer) {
            var r = SoftFluidCodec.CODEC.encodeStart(JsonOps.INSTANCE, obj);
            r.result().ifPresent(a -> GSON.toJson(sortJson(a.getAsJsonObject()), writer));
        }

        private JsonObject sortJson(JsonObject jsonObject) {
            try {
                Map<String, JsonElement> joToMap = new TreeMap<>();
                jsonObject.entrySet().forEach(e -> {
                    var j = e.getValue();
                    if (j instanceof JsonObject jo) j = sortJson(jo);
                    joToMap.put(e.getKey(), j);
                });
                JsonObject sortedJSON = new JsonObject();
                joToMap.forEach(sortedJSON::add);
                return sortedJSON;
            } catch (Exception ignored) {
            }
            return jsonObject;
        }
    }

}
