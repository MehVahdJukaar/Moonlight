package net.mehvahdjukaar.selene.mixins;

import com.google.common.collect.Maps;
import net.mehvahdjukaar.selene.Selene;
import net.mehvahdjukaar.selene.map.CustomDecoration;
import net.mehvahdjukaar.selene.map.ExpandedMapData;
import net.mehvahdjukaar.selene.map.CustomDecorationType;
import net.mehvahdjukaar.selene.map.MapDecorationHandler;
import net.mehvahdjukaar.selene.map.markers.DummyMapWorldMarker;
import net.mehvahdjukaar.selene.map.markers.MapWorldMarker;
import net.mehvahdjukaar.selene.network.NetworkHandler;
import net.mehvahdjukaar.selene.network.ClientBoundSyncCustomMapDecorationPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapBanner;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.fmllegacy.network.PacketDistributor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;


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

    @Shadow
    public byte[] colors;
    @Final
    @Shadow
    public boolean locked;
    @Shadow
    @Final
    private Map<String, MapBanner> bannerMarkers;
    //new decorations (stuff that gets rendered)

    public Map<String, CustomDecoration> customDecorations = Maps.newLinkedHashMap();

    //world markers

    private final Map<String, MapWorldMarker<?>> customMapMarkers = Maps.newHashMap();

    @Override
    public Map<String, CustomDecoration> getCustomDecorations() {
        return customDecorations;
    }

    @Override
    public Map<String, MapWorldMarker<?>> getCustomMarkers() {
        return customMapMarkers;
    }

    public <D extends CustomDecoration> void addCustomDecoration(MapWorldMarker<D> marker) {
        D decoration = marker.createDecorationFromMarker(scale, x, z, dimension, locked);
        if (decoration != null) {
            this.customDecorations.put(marker.getMarkerId(), decoration);
        }
    }

    @Inject(method = "locked", at = @At("RETURN"), cancellable = true)
    public void locked(CallbackInfoReturnable<MapItemSavedData> cir) {
        MapItemSavedData data = cir.getReturnValue();
        if (data instanceof ExpandedMapData) {
            this.customMapMarkers.putAll(((ExpandedMapData) data).getCustomMarkers());
            this.customDecorations.putAll(((ExpandedMapData) data).getCustomDecorations());
        }
    }

    @Inject(method = "tickCarriedBy", at = @At("TAIL"), cancellable = true)
    public void tickCarriedBy(Player player, ItemStack stack, CallbackInfo ci) {
        CompoundTag compoundnbt = stack.getTag();
        if (compoundnbt != null && compoundnbt.contains("CustomDecorations", 9)) {
            ListTag listnbt = compoundnbt.getList("CustomDecorations", 10);
            //for exploration maps
            for (int j = 0; j < listnbt.size(); ++j) {
                CompoundTag com = listnbt.getCompound(j);
                if (!this.decorations.containsKey(com.getString("id"))) {
                    String name = com.getString("type");
                    //TODO: add more checks
                    CustomDecorationType<CustomDecoration, ?> type = (CustomDecorationType<CustomDecoration, ?>) MapDecorationHandler.get(name);
                    if (type != null) {
                        MapWorldMarker<CustomDecoration> dummy = new DummyMapWorldMarker(type, com.getInt("x"), com.getInt("z"));
                        this.addCustomDecoration(dummy);
                    } else {
                        Selene.LOGGER.warn("Failed to load map decoration " + name + ". Skipping it");

                    }
                }
            }
        }
    }

    @Inject(method = "load", at = @At("RETURN"), cancellable = true)
    private static void load(CompoundTag compound, CallbackInfoReturnable<MapItemSavedData> cir) {
        MapItemSavedData data = cir.getReturnValue();
        if (compound.contains("customMarkers") && data instanceof ExpandedMapData mapData) {
            ListTag listNBT = compound.getList("customMarkers", 10);

            for (int j = 0; j < listNBT.size(); ++j) {
                MapWorldMarker<?> marker = MapDecorationHandler.readWorldMarker(listNBT.getCompound(j));
                if (marker != null) {
                    mapData.getCustomMarkers().put(marker.getMarkerId(), marker);
                    mapData.addCustomDecoration(marker);
                }
            }
        }
    }

    @Inject(method = "save", at = @At("RETURN"), cancellable = true)
    public void save(CompoundTag p_189551_1_, CallbackInfoReturnable<CompoundTag> cir) {
        CompoundTag com = cir.getReturnValue();

        ListTag listNBT = new ListTag();

        for (MapWorldMarker<?> marker : this.customMapMarkers.values()) {
            CompoundTag com2 = new CompoundTag();
            com2.put(marker.getTypeId(), marker.saveToNBT(new CompoundTag()));
            listNBT.add(com2);
        }
        com.put("customMarkers", listNBT);
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
            List<MapWorldMarker<?>> markers = MapDecorationHandler.getMarkersFromWorld(world, pos);
            boolean changed = false;
            for (MapWorldMarker<?> marker : markers) {
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

    @Inject(method = "checkBanners", at = @At("TAIL"), cancellable = true)
    public void checkBanners(BlockGetter world, int x, int z, CallbackInfo ci) {
        Iterator<MapWorldMarker<?>> iterator = this.customMapMarkers.values().iterator();

        while (iterator.hasNext()) {
            MapWorldMarker<?> marker = iterator.next();
            if (marker.getPos().getX() == x && marker.getPos().getZ() == z) {
                MapWorldMarker<?> newMarker = marker.getType().getWorldMarkerFromWorld(world, marker.getPos());
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

    @Inject(method = "getUpdatePacket", at = @At("RETURN"), cancellable = true)
    public void getUpdatePacket(int pMapId, Player pPlayer, CallbackInfoReturnable<Packet<?>> cir) {
        Packet<?> packet = cir.getReturnValue();
        if (pPlayer instanceof ServerPlayer serverPlayer && packet instanceof ClientboundMapItemDataPacket) {
            NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new ClientBoundSyncCustomMapDecorationPacket(pMapId, this.scale, this.locked, null, this.customDecorations.values().toArray(new CustomDecoration[0])));
        }
    }

    public int getVanillaDecorationSize(){
        return this.decorations.size();
    }

}
