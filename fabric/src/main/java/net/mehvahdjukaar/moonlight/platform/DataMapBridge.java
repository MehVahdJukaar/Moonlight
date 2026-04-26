package net.mehvahdjukaar.moonlight.platform;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.network.platform.ClientBoundSyncDataMapsPacket;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComposterBlock;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

// bro totally unneeded lol
public abstract class DataMapBridge<T, O> extends SimplePreparableReloadListener<List<JsonElement>> {

    public static final Map<String, Function<HolderLookup.Provider, DataMapBridge<?, ?>>> FACTORIES = new HashMap<>();
    private static final Map<String, DataMapBridge<?, ?>> SERVER_INSTANCES = new HashMap<>();
    private static final String ML_MARKER = "moonlight_parse_on_fabric";


    @ApiStatus.Internal
    public static void init() {
        register(BurnTimes.FUEL, BurnTimes::new);
        register(Compostables.COMPOSTABLES, Compostables::new);
        register(Oxidisables.OXIDISABLES, Oxidisables::new);
        register(Waxables.WAXABLES, Waxables::new);

        for (var f : FACTORIES.entrySet()) {
            String mapID = f.getKey();
            ResourceLocation reloadID = Moonlight.res(mapID);
            PlatHelper.addServerReloadListener(r -> {
                DataMapBridge<?, ?> instance = f.getValue().apply(r);
                SERVER_INSTANCES.put(mapID, instance);
                return instance;
            }, reloadID);
        }
    }

    @ApiStatus.Internal
    public static void onDataSyncToPlayer(ServerPlayer player, boolean isJoined) {
        for (var entry : SERVER_INSTANCES.entrySet()) {
            var map = entry.getValue();
            NetworkHelper.sendToClientPlayer(player, new ClientBoundSyncDataMapsPacket(map));
        }
    }

    public static void register(String path, Function<HolderLookup.Provider, DataMapBridge<?, ?>> factory) {
        FACTORIES.put(path, factory);
    }


    private static final Gson GSON = new Gson();
    public final String path;
    private final HolderLookup.Provider registryAccess;
    public final Codec<Map<HolderSet<O>, T>> mapCodec;
    public final StreamCodec<RegistryFriendlyByteBuf, Map<HolderSet<O>, T>> streamCodec;
    public final Map<HolderSet<O>, T> map = new HashMap<>();

    protected DataMapBridge(String path, HolderLookup.Provider registryAccess,
                            Codec<T> entryCodec, ResourceKey<? extends Registry<O>> reg) {
        this.path = path;
        this.registryAccess = registryAccess;

        //doesn't support replace or any fancy stuff like that
        this.mapCodec = RecordCodecBuilder.create(i -> i.group(
                Codec.unboundedMap(RegistryCodecs.homogeneousList(reg), entryCodec)
                        .fieldOf("values").forGetter(m -> m)
        ).apply(i, HashMap::new));

        this.streamCodec = ByteBufCodecs.map(i -> new HashMap<>(),
                ByteBufCodecs.holderSet(reg), ByteBufCodecs.fromCodec(entryCodec));
    }


    @Override
    protected final List<JsonElement> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        List<JsonElement> output = new ArrayList<>();
        var m = resourceManager.listResourceStacks(path, r->true);
        var list = new ArrayList<Resource>();
        for (var res : m.values()) {
            list.addAll(res);
        }
        for (var res : list) {
            try (Reader reader = res.openAsReader()) {
                JsonElement jsonElement = GsonHelper.fromJson(GSON, reader, JsonElement.class);
                output.add(jsonElement);
            } catch (IllegalArgumentException | IOException | JsonParseException var14) {
                Moonlight.LOGGER.error("Couldn't parse data file {} from {}", res, path);
            }
        }
        return output;
    }

    @Override
    protected final void apply(List<JsonElement> jsons, ResourceManager resourceManager, ProfilerFiller profiler) {
        this.map.clear();
        var ops = RegistryOps.create(JsonOps.INSTANCE, registryAccess);
        for (var entry : jsons) {
            if (entry instanceof JsonObject jo && jo.has(ML_MARKER)) {
                var parsed = this.mapCodec.parse(ops, jo).getOrThrow();
                this.map.putAll(parsed);
            }
        }
        applyData();
    }

    public final void applyData() {
        for (var entry : this.map.entrySet()) {
            for (var holder : entry.getKey()) {
                var object = holder.value();
                var data = entry.getValue();
                this.applyEntry(object, data);
            }
        }
    }

    protected abstract void applyEntry(O object, T dataEntry);

    public void encode(RegistryFriendlyByteBuf buf) {
        streamCodec.encode(buf, this.map);
    }

    public void decode(RegistryFriendlyByteBuf buf) {
        this.map.clear();
        this.map.putAll(streamCodec.decode(buf));
    }


    protected static class BurnTimes extends DataMapBridge<BurnTimes.FurnaceFuel, Item> {
        protected static final String FUEL = "data_maps/item/furnace_fuels.json";

        protected BurnTimes(HolderLookup.Provider registryAccess) {
            super(FUEL, registryAccess, FurnaceFuel.CODEC, Registries.ITEM);
        }

        @Override
        protected void applyEntry(Item object, FurnaceFuel dataEntry) {
            FuelRegistry.INSTANCE.add(object, dataEntry.burnTime);
        }

        protected record FurnaceFuel(int burnTime) {
            public static final Codec<FurnaceFuel> CODEC = Codec.withAlternative(
                    RecordCodecBuilder.create(in -> in.group(
                            ExtraCodecs.POSITIVE_INT.fieldOf("burn_time").forGetter(FurnaceFuel::burnTime)).apply(in, FurnaceFuel::new)),
                    ExtraCodecs.POSITIVE_INT.xmap(FurnaceFuel::new, FurnaceFuel::burnTime));
        }
    }

    protected static class Compostables extends DataMapBridge<Compostables.Compostable, Item> {
        protected static final String COMPOSTABLES = "data_maps/item/compostables.json";

        protected Compostables(HolderLookup.Provider registryAccess) {
            super(COMPOSTABLES, registryAccess, Compostable.CODEC, Registries.ITEM);
        }

        @Override
        protected void applyEntry(Item object, Compostable dataEntry) {
            ComposterBlock.COMPOSTABLES.put(object, dataEntry.chance);
        }

        protected record Compostable(float chance, boolean canVillagerCompost) {
            public static final Codec<Compostable> CODEC = Codec.withAlternative(
                    RecordCodecBuilder.create(in -> in.group(
                            Codec.floatRange(0f, 1f).fieldOf("chance").forGetter(Compostable::chance),
                            Codec.BOOL.optionalFieldOf("can_villager_compost", false).forGetter(Compostable::canVillagerCompost)).apply(in, Compostable::new)),
                    Codec.floatRange(0f, 1f).xmap(Compostable::new, Compostable::chance));

            public Compostable(float chance) {
                this(chance, false);
            }
        }

    }

    protected static class Oxidisables extends DataMapBridge<Oxidisables.Oxidizable, Block> {
        protected static final String OXIDISABLES = "data_maps/block/oxidizables.json";

        protected Oxidisables(HolderLookup.Provider registryAccess) {
            super(OXIDISABLES, registryAccess, Oxidizable.CODEC, Registries.BLOCK);
        }

        @Override
        protected void applyEntry(Block object, Oxidizable dataEntry) {
            OxidizableBlocksRegistry.registerOxidizableBlockPair(object, dataEntry.nextOxidationStage);
        }

        public record Oxidizable(Block nextOxidationStage) {
            public static final Codec<Oxidizable> CODEC = Codec.withAlternative(
                    RecordCodecBuilder.create(in -> in.group(
                            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("next_oxidation_stage").forGetter(Oxidizable::nextOxidationStage)).apply(in, Oxidizable::new)),
                    BuiltInRegistries.BLOCK.byNameCodec()
                            .xmap(Oxidizable::new, Oxidizable::nextOxidationStage));
        }

    }

    protected static class Waxables extends DataMapBridge<Waxables.Waxable, Block> {
        protected static final String WAXABLES = "data_maps/block/waxables.json";

        protected Waxables(HolderLookup.Provider registryAccess) {
            super(WAXABLES, registryAccess, Waxable.CODEC, Registries.BLOCK);
        }

        @Override
        protected void applyEntry(Block object, Waxable dataEntry) {
            OxidizableBlocksRegistry.registerWaxableBlockPair(object, dataEntry.waxed);
        }

        public record Waxable(Block waxed) {
            public static final Codec<Waxable> CODEC = Codec.withAlternative(
                    RecordCodecBuilder.create(in -> in.group(
                            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("waxed").forGetter(Waxable::waxed)).apply(in, Waxable::new)),
                    BuiltInRegistries.BLOCK.byNameCodec()
                            .xmap(Waxable::new, Waxable::waxed));
        }
    }


}
