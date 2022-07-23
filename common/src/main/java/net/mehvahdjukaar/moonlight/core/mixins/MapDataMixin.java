package net.mehvahdjukaar.moonlight.core.mixins;

import com.google.common.collect.Maps;
import net.mehvahdjukaar.moonlight.api.map.*;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.api.map.markers.GenericMapBlockMarker;
import net.mehvahdjukaar.moonlight.api.map.markers.MapBlockMarker;
import net.mehvahdjukaar.moonlight.api.map.type.MapDecorationType;
import net.mehvahdjukaar.moonlight.core.network.ClientBoundSyncCustomMapDecorationMessage;
import net.mehvahdjukaar.moonlight.core.network.ModMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.maps.MapBanner;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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
    Map<String, net.minecraft.world.level.saveddata.maps.MapDecoration> decorations;

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
    public Map<String, CustomMapDecoration> customDecorations = Maps.newLinkedHashMap();

    //world markers
    @Unique
    private final Map<String, MapBlockMarker<?>> customMapMarkers = Maps.newHashMap();

    //custom data that can be stored in maps
    @Unique
    public final Map<ResourceLocation, CustomDataHolder.Instance<?>> customData = new HashMap<>();

    @Override
    public Map<ResourceLocation, CustomDataHolder.Instance<?>> getCustomData() {
        return customData;
    }

    @Override
    public Map<String, CustomMapDecoration> getCustomDecorations() {
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
    public <D extends CustomMapDecoration> void addCustomDecoration(MapBlockMarker<D> marker) {
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

    /**
     * @param world level
     * @param pos world position where a marker providing block could be
     * @return true if a marker was toggled
     */
    @Override
    public boolean toggleCustomDecoration(LevelAccessor world, BlockPos pos) {
        if(world.isClientSide()){
            List<MapBlockMarker<?>> markers = MapDecorationRegistry.getMarkersFromWorld(world, pos);
            return !markers.isEmpty();
        }

        double d0 = (double) pos.getX() + 0.5D;
        double d1 = (double) pos.getZ() + 0.5D;
        int i = 1 << this.scale;
        double d2 = (d0 - (double) this.x) / (double) i;
        double d3 = (d1 - (double) this.z) / (double) i;
        if (d2 >= -63.0D && d3 >= -63.0D && d2 <= 63.0D && d3 <= 63.0D) {
            List<MapBlockMarker<?>> markers = MapDecorationRegistry.getMarkersFromWorld(world, pos);

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
            if (changed) {
                this.setDirty();
                return true;
            }
        }
        return false;
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

                        MapDecorationType<? extends CustomMapDecoration, ?> type = MapDecorationRegistry.get(name);
                        if (type != null) {
                            MapBlockMarker<CustomMapDecoration> dummy = new GenericMapBlockMarker(type, com.getInt("x"), com.getInt("z"));
                            this.addCustomDecoration(dummy);
                        } else {
                            Moonlight.LOGGER.warn("Failed to load map decoration " + name + ". Skipping it");
                        }
                    }
                }
            }
            //sends update packet
            Integer mapId = MapHelper.getMapId(stack, player, this);
            if (player instanceof ServerPlayer serverPlayer && mapId != null) {

                ModMessages.CHANNEL.sendToClientPlayer(serverPlayer,
                        new ClientBoundSyncCustomMapDecorationMessage(mapId, this.scale, this.locked,
                                this.customDecorations.values().toArray(new CustomMapDecoration[0]),
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
                MapBlockMarker<?> marker = MapDecorationRegistry.readWorldMarker(listNBT.getCompound(j));
                if (marker != null) {
                    mapData.getCustomMarkers().put(marker.getMarkerId(), marker);
                    mapData.addCustomDecoration(marker);
                }
            }

            var customData = mapData.getCustomData();
            customData.clear();
            MapDecorationRegistry.CUSTOM_MAP_DATA_TYPES.forEach((s, o) -> {
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
