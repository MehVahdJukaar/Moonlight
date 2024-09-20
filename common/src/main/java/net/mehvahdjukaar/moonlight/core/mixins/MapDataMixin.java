package net.mehvahdjukaar.moonlight.core.mixins;

import com.google.common.collect.Maps;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.mehvahdjukaar.moonlight.api.MoonlightRegistry;
import net.mehvahdjukaar.moonlight.api.map.CustomMapData;
import net.mehvahdjukaar.moonlight.api.map.ExpandedMapData;
import net.mehvahdjukaar.moonlight.api.map.MLMapDecorationsComponent;
import net.mehvahdjukaar.moonlight.api.map.decoration.MLMapDecoration;
import net.mehvahdjukaar.moonlight.api.map.decoration.MLMapMarker;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.map.MapDataInternal;
import net.mehvahdjukaar.moonlight.core.misc.IHoldingPlayerExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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
import java.util.function.Consumer;


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
    public Map<String, MLMapDecoration> moonlight$customDecorations = Maps.newLinkedHashMap();

    //world markers (stuff that gets saved)
    @Unique
    private final Map<String, MLMapMarker<?>> moonlight$customMapMarkers = Maps.newHashMap();

    //custom data that can be stored in maps
    @Unique
    public final Map<ResourceLocation, CustomMapData<?>> moonlight$customData = new LinkedHashMap<>();

    @Override
    public void ml$setCustomDecorationsDirty() {
        this.setDirty();
        carriedBy.forEach(h -> ((IHoldingPlayerExtension) h).moonlight$setCustomMarkersDirty());
    }

    @Override
    public <H extends CustomMapData.DirtyCounter> void ml$setCustomDataDirty(
            CustomMapData.Type<?> type, Consumer<H> dirtySetter) {
        this.setDirty();
        carriedBy.forEach(h -> ((IHoldingPlayerExtension) h)
                .moonlight$setCustomDataDirty(type, dirtySetter));

    }

    @Override
    public Map<ResourceLocation, CustomMapData<?>> ml$getCustomData() {
        return moonlight$customData;
    }

    @Override
    public Map<String, MLMapDecoration> ml$getCustomDecorations() {
        return moonlight$customDecorations;
    }

    @Override
    public Map<String, MLMapMarker<?>> ml$getCustomMarkers() {
        return moonlight$customMapMarkers;
    }

    @Override
    public int ml$getVanillaDecorationSize() {
        return this.decorations.size();
    }

    @Override
    public <M extends MLMapMarker<?>> void ml$addCustomMarker(M marker) {
        var decoration = marker.createDecorationFromMarker((MapItemSavedData) (Object) this);
        if (decoration != null) {
            this.moonlight$customDecorations.put(marker.getMarkerUniqueId(), decoration);
            if (marker.shouldSave()) {
                this.moonlight$customMapMarkers.put(marker.getMarkerUniqueId(), marker);
            }
            //so packet is sent
            ml$setCustomDecorationsDirty();
        }
    }

    @Override
    public boolean ml$removeCustomMarker(String key) {
        moonlight$customDecorations.remove(key);
        if (moonlight$customMapMarkers.containsKey(key)) {
            moonlight$customMapMarkers.remove(key);
            ml$setCustomDecorationsDirty();
            return true;
        }
        return false;
    }

    @Override
    public MapItemSavedData ml$copy() {
        MapItemSavedData newData = MapItemSavedData.load(this.save(
                new CompoundTag(), Utils.hackyGetRegistryAccess()), Utils.hackyGetRegistryAccess());
        newData.setDirty();
        return newData;
    }

    @Override
    public void ml$resetCustomDecoration() {
        if (!bannerMarkers.isEmpty() || !moonlight$customMapMarkers.isEmpty()) {
            ml$setCustomDecorationsDirty();
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
    public boolean ml$toggleCustomDecoration(LevelAccessor world, BlockPos pos) {
        if (world.isClientSide()) {
            List<MLMapMarker<?>> markers = MapDataInternal.getMarkersFromWorld(world, pos);
            return !markers.isEmpty();
        }

        double d0 = pos.getX() + 0.5D;
        double d1 = pos.getZ() + 0.5D;
        int i = 1 << this.scale;
        double d2 = (d0 - this.centerX) / i;
        double d3 = (d1 - this.centerZ) / i;
        if (d2 >= -63.0D && d3 >= -63.0D && d2 <= 63.0D && d3 <= 63.0D) {
            List<MLMapMarker<?>> markers = MapDataInternal.getMarkersFromWorld(world, pos);

            boolean changed = false;
            for (MLMapMarker<?> marker : markers) {
                if (marker != null) {
                    //toggle
                    String id = marker.getMarkerUniqueId();
                    if (marker.equals(this.moonlight$customMapMarkers.get(id))) {
                        ml$removeCustomMarker(id);
                    } else {
                        this.ml$addCustomMarker(marker);
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
            expandedMapData.ml$getCustomMarkers().putAll(this.ml$getCustomMarkers());
            expandedMapData.ml$getCustomDecorations().putAll(this.ml$getCustomDecorations());
        }
        moonlight$copyCustomData(data);
    }

    @Inject(method = "scaled", at = @At("RETURN"))
    public void scaled(CallbackInfoReturnable<MapItemSavedData> cir) {
        MapItemSavedData data = cir.getReturnValue();
        moonlight$copyCustomData(data);
    }

    @Unique
    private void moonlight$copyCustomData(MapItemSavedData data) {
        if (data instanceof ExpandedMapData ed) {
            for (var entry : this.moonlight$customData.entrySet()) {
                CustomMapData<?> customData = entry.getValue();
                if (customData.persistOnCopyOrLock()) {
                    CompoundTag t = new CompoundTag();
                    customData.save(t);
                    ed.ml$getCustomData().get(entry.getKey()).load(t);
                }
            }
        }
    }


    @Inject(method = "tickCarriedBy", at = @At("TAIL"))
    public void tickCarriedBy(Player player, ItemStack stack, CallbackInfo ci) {
        //for exploration maps. Decoration assigned to an item instead of a map directly
        MLMapDecorationsComponent customDecoComponent = stack.get(MoonlightRegistry.CUSTOM_MAP_DECORATIONS.get());
        if (customDecoComponent != null) {

            if (!this.moonlight$customMapMarkers.keySet().containsAll(customDecoComponent.decorations().keySet())) {
                customDecoComponent.decorations().forEach((string, entry) -> {
                    if (!this.decorations.containsKey(string)) {
                        this.ml$addCustomMarker(entry);
                    }
                });
            }
        }
    }

    @Inject(method = "load", at = @At("RETURN"))
    private static void load(CompoundTag compound, HolderLookup.Provider
            registries, CallbackInfoReturnable<MapItemSavedData> cir) {
        MapItemSavedData data = cir.getReturnValue();
        if (compound.contains("customMarkers") && data instanceof ExpandedMapData mapData) {
            ListTag listNBT = compound.getList("customMarkers", 10);

            RegistryOps<Tag> registryOps = registries.createSerializationContext(NbtOps.INSTANCE);

            for (int j = 0; j < listNBT.size(); ++j) {
                MLMapMarker.REFERENCE_CODEC.parse(registryOps, listNBT.getCompound(j))
                        .resultOrPartial(string -> Moonlight.LOGGER.warn("Failed to parse moonlight map marker: '{}'", string))
                        .ifPresent(marker -> {
                            mapData.ml$getCustomMarkers().put(marker.getMarkerUniqueId(), marker);
                            mapData.ml$addCustomMarker(marker);
                        });
            }

            mapData.ml$getCustomData().values().forEach(customMapData -> customMapData.load(compound));
        }
    }

    @Inject(method = "save", at = @At("RETURN"))
    public void save(CompoundTag tag, HolderLookup.Provider registries, CallbackInfoReturnable<CompoundTag> cir) {
        CompoundTag com = cir.getReturnValue();

        ListTag listNBT = new ListTag();

        RegistryOps<Tag> registryOps = registries.createSerializationContext(NbtOps.INSTANCE);

        for (MLMapMarker<?> marker : this.moonlight$customMapMarkers.values()) {
            if (marker.shouldSave()) {
                listNBT.add(MLMapMarker.REFERENCE_CODEC.encodeStart(registryOps, marker).getOrThrow());
            }
        }
        com.put("customMarkers", listNBT);

        this.moonlight$customData.forEach((s, o) -> o.save(tag));

    }

    @Inject(method = "checkBanners", at = @At("TAIL"))
    public void checkCustomDeco(BlockGetter world, int x, int z, CallbackInfo ci) {
        List<String> toRemove = new ArrayList<>();
        List<MLMapMarker<?>> toAdd = new ArrayList<>();
        for (var e : this.moonlight$customMapMarkers.entrySet()) {
            var marker = e.getValue();
            if (marker.getPos().getX() == x && marker.getPos().getZ() == z) {
                if (marker.shouldRefreshFromWorld()) {
                    MLMapMarker<?> newMarker = marker.getType().value().createMarkerFromWorld(world, marker.getPos());
                    String id = e.getKey();
                    if (newMarker == null) {
                        toRemove.add(id);
                    } else if (!Objects.equals(marker, newMarker)) {
                        toRemove.add(id);
                        toAdd.add(newMarker);
                    }
                }
            }
        }
        toRemove.forEach(this::ml$removeCustomMarker);
        toAdd.forEach(this::ml$addCustomMarker);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void initCustomData(int i, int j, byte b, boolean bl, boolean bl2, boolean bl3,
                               ResourceKey<Level> resourceKey, CallbackInfo ci) {
        for (var d : MapDataInternal.CUSTOM_MAP_DATA_TYPES.getValues()) {
            moonlight$customData.put(d.id(), d.factory().get());
        }
    }

    @ModifyReturnValue(method = "isExplorationMap", at = @At("RETURN"))
    public boolean ml$isExplorationMap(boolean original) {
        if (original) return true;
        for (var mapDecoration : this.moonlight$customMapMarkers.values()) {
            if (mapDecoration.preventsExtending()) {
                return true;
            }
        }
        return false;
    }
}
