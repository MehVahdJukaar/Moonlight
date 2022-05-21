package net.mehvahdjukaar.selene.mixins;

import com.google.common.collect.Maps;
import net.mehvahdjukaar.selene.Selene;
import net.mehvahdjukaar.selene.map.*;
import net.mehvahdjukaar.selene.map.markers.DummyMapBlockMarker;
import net.mehvahdjukaar.selene.map.markers.MapBlockMarker;
import net.mehvahdjukaar.selene.network.ClientBoundSyncCustomMapDecorationPacket;
import net.mehvahdjukaar.selene.network.NetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.maps.MapBanner;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraftforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;


@Mixin(MapItemSavedData.class)
public abstract class MapDataMixin extends SavedData implements ExpandedMapData {


    @Final
    @Shadow
    public int x;

    @Final
    @Shadow
    public int z;

    @Final
    @Shadow
    public byte scale;

    @Final
    @Shadow
    Map<String, MapDecoration> decorations;

    @Final
    @Shadow
    public ResourceKey<Level> dimension;

    @Final
    @Shadow
    public boolean locked;

    @Shadow
    @Final
    private Map<String, MapBanner> bannerMarkers;

    //new decorations (stuff that gets rendered)
    @Unique
    public Map<String, CustomDecoration> customDecorations = Maps.newLinkedHashMap();

    //world markers
    @Unique
    private final Map<String, MapBlockMarker<?>> customMapMarkers = Maps.newHashMap();

    //custom data that can be stored in maps
    @Unique
    public final Map<String, CustomDataHolder.Instance<?>> customData = new HashMap<>();

    @Override
    public Map<String, CustomDataHolder.Instance<?>> getCustomData() {
        return customData;
    }

    @Override
    public Map<String, CustomDecoration> getCustomDecorations() {
        return customDecorations;
    }

    @Override
    public Map<String, MapBlockMarker<?>> getCustomMarkers() {
        return customMapMarkers;
    }

    @Override
    public int getVanillaDecorationSize() {
        return this.decorations.size();
    }

    @Override
    public <D extends CustomDecoration> void addCustomDecoration(MapBlockMarker<D> marker) {
        D decoration = marker.createDecorationFromMarker(scale, x, z, dimension, locked);
        if (decoration != null) {
            this.customDecorations.put(marker.getMarkerId(), decoration);
        }
    }

    @Override
    public MapItemSavedData copy() {
        MapItemSavedData newData = MapItemSavedData.load(this.save(new CompoundTag()));
        newData.setDirty();
        return newData;
    }

    @Override
    public void resetCustomDecoration() {

        for (String key : this.customMapMarkers.keySet()) {
            this.customDecorations.remove(key);
            this.customMapMarkers.remove(key);
        }
        for (String key : this.bannerMarkers.keySet()) {
            this.bannerMarkers.remove(key);
            this.decorations.remove(key);
        }
    }

    @Override
    public void toggleCustomDecoration(LevelAccessor world, BlockPos pos) {
        double d0 = (double) pos.getX() + 0.5D;
        double d1 = (double) pos.getZ() + 0.5D;
        int i = 1 << this.scale;
        double d2 = (d0 - (double) this.x) / (double) i;
        double d3 = (d1 - (double) this.z) / (double) i;
        if (d2 >= -63.0D && d3 >= -63.0D && d2 <= 63.0D && d3 <= 63.0D) {
            List<MapBlockMarker<?>> markers = MapDecorationHandler.getMarkersFromWorld(world, pos);
            boolean changed = false;
            for (MapBlockMarker<?> marker : markers) {
                if (marker != null) {
                    //toggle
                    String id = marker.getMarkerId();
                    if (this.customMapMarkers.containsKey(id) && this.customMapMarkers.get(id).equals(marker)) {
                        this.customMapMarkers.remove(id);
                        this.customDecorations.remove(id);
                    } else {
                        this.customMapMarkers.put(id, marker);
                        this.addCustomDecoration(marker);
                    }
                    changed = true;
                }
            }
            if (changed) this.setDirty();
        }
    }




    @Inject(method = "locked", at = @At("RETURN"))
    public void locked(CallbackInfoReturnable<MapItemSavedData> cir) {
        MapItemSavedData data = cir.getReturnValue();
        if (data instanceof ExpandedMapData expandedMapData) {
            expandedMapData.getCustomMarkers().putAll(this.getCustomMarkers());
            expandedMapData.getCustomDecorations().putAll(this.getCustomDecorations());
        }
    }

    @Inject(method = "scaled", at = @At("RETURN"))
    public void scaled(CallbackInfoReturnable<MapItemSavedData> cir) {
        MapItemSavedData data = cir.getReturnValue();
        if (data instanceof ExpandedMapData expandedMapData) {
            expandedMapData.getCustomData().putAll(this.customData);
        }
    }

    //TODO: find a good way to have update packet (current one isnt fully working)
    @Inject(method = "tickCarriedBy", at = @At("TAIL"))
    public void tickCarriedBy(Player player, ItemStack stack, CallbackInfo ci) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            if (tag.contains("CustomDecorations", 9)) {
                ListTag listTag = tag.getList("CustomDecorations", 10);
                //for exploration maps
                for (int j = 0; j < listTag.size(); ++j) {
                    CompoundTag com = listTag.getCompound(j);
                    if (!this.decorations.containsKey(com.getString("id"))) {
                        String name = com.getString("type");

                        CustomDecorationType<? extends CustomDecoration, ?> type = MapDecorationHandler.get(name);
                        if (type != null) {
                            MapBlockMarker<CustomDecoration> dummy = new DummyMapBlockMarker(type, com.getInt("x"), com.getInt("z"));
                            this.addCustomDecoration(dummy);
                        } else {
                            Selene.LOGGER.warn("Failed to load map decoration " + name + ". Skipping it");
                        }
                    }
                }
            }
            //sends update packet
            Integer mapId = MapHelper.getMapId(stack, player, this);
            if (player instanceof ServerPlayer serverPlayer && mapId != null) {

                NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                        new ClientBoundSyncCustomMapDecorationPacket(mapId, this.scale, this.locked,
                                this.customDecorations.values().toArray(new CustomDecoration[0]),
                                this.customData.values().toArray(new CustomDataHolder.Instance[0])));
            }
        }
    }

    @Inject(method = "load", at = @At("RETURN"))
    private static void load(CompoundTag compound, CallbackInfoReturnable<MapItemSavedData> cir) {
        MapItemSavedData data = cir.getReturnValue();
        if (compound.contains("customMarkers") && data instanceof ExpandedMapData mapData) {
            ListTag listNBT = compound.getList("customMarkers", 10);

            for (int j = 0; j < listNBT.size(); ++j) {
                MapBlockMarker<?> marker = MapDecorationHandler.readWorldMarker(listNBT.getCompound(j));
                if (marker != null) {
                    mapData.getCustomMarkers().put(marker.getMarkerId(), marker);
                    mapData.addCustomDecoration(marker);
                }
            }

            var customData = mapData.getCustomData();
            customData.clear();
            MapDecorationHandler.CUSTOM_MAP_DATA_TYPES.forEach((s, o) -> {
                CustomDataHolder.Instance<?> i = o.create(compound);
                if (i != null) customData.put(s, i);
            });
        }

    }

    @Inject(method = "save", at = @At("RETURN"))
    public void save(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        CompoundTag com = cir.getReturnValue();

        ListTag listNBT = new ListTag();

        for (MapBlockMarker<?> marker : this.customMapMarkers.values()) {
            CompoundTag com2 = new CompoundTag();
            com2.put(marker.getTypeId(), marker.saveToNBT(new CompoundTag()));
            listNBT.add(com2);
        }
        com.put("customMarkers", listNBT);

        this.customData.forEach((s, o) -> o.save(tag));

    }

    @Inject(method = "checkBanners", at = @At("TAIL"))
    public void checkBanners(BlockGetter world, int x, int z, CallbackInfo ci) {
        Iterator<MapBlockMarker<?>> iterator = this.customMapMarkers.values().iterator();

        while (iterator.hasNext()) {
            MapBlockMarker<?> marker = iterator.next();
            if (marker.getPos().getX() == x && marker.getPos().getZ() == z) {
                MapBlockMarker<?> newMarker = marker.getType().getWorldMarkerFromWorld(world, marker.getPos());
                String id = marker.getMarkerId();
                if (newMarker == null) {
                    iterator.remove();
                    this.customDecorations.remove(id);
                } else if (Objects.equals(id, newMarker.getMarkerId()) && marker.shouldUpdate(newMarker)) {
                    newMarker.updateDecoration(this.customDecorations.get(id));
                }
            }
        }
    }

}
