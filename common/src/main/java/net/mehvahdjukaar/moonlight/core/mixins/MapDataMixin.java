package net.mehvahdjukaar.moonlight.core.mixins;

import com.google.common.collect.Maps;
import net.mehvahdjukaar.moonlight.api.map.CustomMapData;
import net.mehvahdjukaar.moonlight.api.map.CustomMapDecoration;
import net.mehvahdjukaar.moonlight.api.map.ExpandedMapData;
import net.mehvahdjukaar.moonlight.api.map.markers.MapBlockMarker;
import net.mehvahdjukaar.moonlight.api.map.type.MapDecorationType;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.map.MapDataInternal;
import net.mehvahdjukaar.moonlight.core.misc.IHoldingPlayerExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
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
    public byte scale;

    @Final
    @Shadow
    Map<String, net.minecraft.world.level.saveddata.maps.MapDecoration> decorations;

    @Shadow
    @Final
    private Map<String, MapBanner> bannerMarkers;

    @Shadow
    public int centerX;
    @Shadow
    public int centerZ;

    @Shadow
    @Final
    private List<MapItemSavedData.HoldingPlayer> carriedBy;
    //new decorations (stuff that gets rendered)
    @Unique
    public Map<String, CustomMapDecoration> moonlight$customDecorations = Maps.newLinkedHashMap();

    //world markers (stuff that gets saved)
    @Unique
    private final Map<String, MapBlockMarker<?>> moonlight$customMapMarkers = Maps.newHashMap();

    //custom data that can be stored in maps
    @Unique
    public final Map<ResourceLocation, CustomMapData> moonlight$customData = new LinkedHashMap<>();

    @Override
    public void setCustomDecorationsDirty() {
        this.setDirty();
        carriedBy.forEach(h -> ((IHoldingPlayerExtension) h).moonlight$setCustomMarkersDirty());
    }

    @Override
    public void setCustomDataDirty() {
        this.setDirty();
        carriedBy.forEach(h -> ((IHoldingPlayerExtension) h).moonlight$setCustomDataDirty());
    }


    @Override
    public Map<ResourceLocation, CustomMapData> getCustomData() {
        return moonlight$customData;
    }

    @Override
    public Map<String, CustomMapDecoration> getCustomDecorations() {
        return moonlight$customDecorations;
    }

    @Override
    public Map<String, MapBlockMarker<?>> getCustomMarkers() {
        return moonlight$customMapMarkers;
    }

    @Override
    public int getVanillaDecorationSize() {
        return this.decorations.size();
    }

    @Override
    public <M extends MapBlockMarker<?>> void addCustomMarker(M marker) {
        var decoration = marker.createDecorationFromMarker((MapItemSavedData) (Object)this);
        if (decoration != null) {
            this.moonlight$customDecorations.put(marker.getMarkerId(), decoration);
            if(marker.shouldSave()) {
                this.moonlight$customMapMarkers.put(marker.getMarkerId(), marker);
            }
            //so packet is sent
            setCustomDecorationsDirty();
        }
    }

    @Override
    public boolean removeCustomMarker(String key){
        if(moonlight$customMapMarkers.containsKey(key)){
            moonlight$customMapMarkers.remove(key);
            setCustomDecorationsDirty();
            return true;
        }
        moonlight$customDecorations.remove(key);
        return false;
    }

    @Override
    public MapItemSavedData copy() {
        MapItemSavedData newData = MapItemSavedData.load(this.save(new CompoundTag()));
        newData.setDirty();
        return newData;
    }

    @Override
    public void resetCustomDecoration() {
        if (!bannerMarkers.isEmpty() || !moonlight$customMapMarkers.isEmpty()) {
            setCustomDecorationsDirty();
        }
        for (String key : this.moonlight$customMapMarkers.keySet()) {
            this.moonlight$customDecorations.remove(key);
        }
        this.moonlight$customMapMarkers.clear();
        for (String key : this.bannerMarkers.keySet()) {
            this.decorations.remove(key);
        }
        this.bannerMarkers.clear();
    }

    /**
     * @param world level
     * @param pos   world position where a marker providing block could be
     * @return true if a marker was toggled
     */
    @Override
    public boolean toggleCustomDecoration(LevelAccessor world, BlockPos pos) {
        if (world.isClientSide()) {
            List<MapBlockMarker<?>> markers = MapDataInternal.getMarkersFromWorld(world, pos);
            return !markers.isEmpty();
        }

        double d0 = pos.getX() + 0.5D;
        double d1 = pos.getZ() + 0.5D;
        int i = 1 << this.scale;
        double d2 = (d0 - this.centerX) / i;
        double d3 = (d1 - this.centerZ) / i;
        if (d2 >= -63.0D && d3 >= -63.0D && d2 <= 63.0D && d3 <= 63.0D) {
            List<MapBlockMarker<?>> markers = MapDataInternal.getMarkersFromWorld(world, pos);

            boolean changed = false;
            for (MapBlockMarker<?> marker : markers) {
                if (marker != null) {
                    //toggle
                    String id = marker.getMarkerId();
                    if (marker.equals(this.moonlight$customMapMarkers.get(id))) {
                        removeCustomMarker(id);
                    } else {
                        this.addCustomMarker(marker);
                    }
                    changed = true;
                }
            }
            return changed;
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
            expandedMapData.getCustomData().putAll(this.moonlight$customData);
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

                        MapDecorationType<? extends CustomMapDecoration, ?> type = MapDataInternal.get(name);
                        if (type != null) {
                            BlockPos pos = new BlockPos(com.getInt("x"), 64, com.getInt("z"));
                            MapBlockMarker<?> marker = type.createEmptyMarker();
                            marker.setPos(pos);
                            this.addCustomMarker(marker);
                        } else {
                            Moonlight.LOGGER.warn("Failed to load map decoration " + name + ". Skipping it");
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "load", at = @At("RETURN"))
    private static void load(CompoundTag compound, CallbackInfoReturnable<MapItemSavedData> cir) {
        MapItemSavedData data = cir.getReturnValue();
        if (compound.contains("customMarkers") && data instanceof ExpandedMapData mapData) {
            ListTag listNBT = compound.getList("customMarkers", 10);

            for (int j = 0; j < listNBT.size(); ++j) {
                MapBlockMarker<?> marker = MapDataInternal.readWorldMarker(listNBT.getCompound(j));
                if (marker != null) {
                    mapData.getCustomMarkers().put(marker.getMarkerId(), marker);
                    mapData.addCustomMarker(marker);
                }
            }

            var customData = mapData.getCustomData();
            customData.clear();
            MapDataInternal.CUSTOM_MAP_DATA_TYPES.forEach((s, type) -> {
                CustomMapData i = type.factory().apply(compound);
                if (i != null) customData.put(s, i);
            });
        }
    }

    @Inject(method = "save", at = @At("RETURN"))
    public void save(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        CompoundTag com = cir.getReturnValue();

        ListTag listNBT = new ListTag();

        for (MapBlockMarker<?> marker : this.moonlight$customMapMarkers.values()) {
            if(marker.shouldSave()) {
                CompoundTag com2 = new CompoundTag();
                com2.put(marker.getTypeId(), marker.saveToNBT(new CompoundTag()));
                listNBT.add(com2);
            }
        }
        com.put("customMarkers", listNBT);

        this.moonlight$customData.forEach((s, o) -> o.save(tag));

    }

    @Inject(method = "checkBanners", at = @At("TAIL"))
    public void checkBanners(BlockGetter world, int x, int z, CallbackInfo ci) {
        Iterator<MapBlockMarker<?>> iterator = this.moonlight$customMapMarkers.values().iterator();

        while (iterator.hasNext()) {
            MapBlockMarker<?> marker = iterator.next();
            if (marker.getPos().getX() == x && marker.getPos().getZ() == z) {
                if(marker.shouldRefresh()) {
                    MapBlockMarker<?> newMarker = marker.getType().getWorldMarkerFromWorld(world, marker.getPos());
                    String id = marker.getMarkerId();
                    if (newMarker == null) {
                        iterator.remove();
                        this.removeCustomMarker(id);
                    } else if (Objects.equals(marker, newMarker)) {
                        this.removeCustomMarker(id);
                        this.addCustomMarker(newMarker);
                    }
                }
            }
        }
    }

}
