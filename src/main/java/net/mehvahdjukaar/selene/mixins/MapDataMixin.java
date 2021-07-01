package net.mehvahdjukaar.selene.mixins;

import com.google.common.collect.Maps;
import net.mehvahdjukaar.selene.map.CustomDecoration;
import net.mehvahdjukaar.selene.map.CustomDecorationHolder;
import net.mehvahdjukaar.selene.map.CustomDecorationType;
import net.mehvahdjukaar.selene.map.MapDecorationHandler;
import net.mehvahdjukaar.selene.map.markers.DummyMapWorldMarker;
import net.mehvahdjukaar.selene.map.markers.MapWorldMarker;
import net.mehvahdjukaar.selene.network.NetworkHandler;
import net.mehvahdjukaar.selene.network.SyncCustomMapDecorationPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SMapDataPacket;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapBanner;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapDecoration;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.network.PacketDistributor;
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

@Mixin(MapData.class)
public abstract class MapDataMixin extends WorldSavedData implements CustomDecorationHolder {
    public MapDataMixin(String name) {
        super(name);
    }

    @Shadow
    public int x;

    @Shadow
    public int z;

    @Shadow
    public byte scale;

    @Final
    @Shadow
    public Map<String, MapDecoration> decorations;

    @Shadow
    public RegistryKey<World> dimension;

    @Shadow public byte[] colors;
    @Shadow public boolean locked;
    @Shadow @Final private Map<String, MapBanner> bannerMarkers;
    //new decorations (stuff that gets rendered)
    @Final
    public Map<String, CustomDecoration> customDecorations = Maps.newLinkedHashMap();

    //world markers
    @Final
    private Map<String, MapWorldMarker<?>> customMapMarkers = Maps.newHashMap();

    @Override
    public Map<String, CustomDecoration> getCustomDecorations() {
        return customDecorations;
    }

    @Override
    public Map<String, MapWorldMarker<?>> getCustomMarkers() {
        return customMapMarkers;
    }

    private <D extends CustomDecoration> void addCustomDecoration(MapWorldMarker<D> marker){
        D decoration = marker.createDecorationFromMarker(scale,x,z,dimension,locked);
        if(decoration!=null) {
            this.customDecorations.put(marker.getMarkerId(), decoration);
        }
    }

    @Inject(method = "lockData", at = @At("HEAD"), cancellable = true)
    public void lockData(MapData data, CallbackInfo ci){
        if(data instanceof CustomDecorationHolder){
            this.customMapMarkers.putAll(((CustomDecorationHolder) data).getCustomMarkers());
            this.customDecorations.putAll(((CustomDecorationHolder) data).getCustomDecorations());
        }
    }

    @Inject(method = "tickCarriedBy", at = @At("TAIL"), cancellable = true)
    public void tickCarriedBy(PlayerEntity player, ItemStack stack, CallbackInfo ci){
        CompoundNBT compoundnbt = stack.getTag();
        if (compoundnbt != null && compoundnbt.contains("CustomDecorations", 9)) {
            ListNBT listnbt = compoundnbt.getList("CustomDecorations", 10);
            //for exploration maps
            for(int j = 0; j < listnbt.size(); ++j) {
                CompoundNBT com = listnbt.getCompound(j);
                if (!this.decorations.containsKey(com.getString("id"))) {
                    CustomDecorationType<CustomDecoration, ?> type = (CustomDecorationType<CustomDecoration, ?>) MapDecorationHandler.get(com.getString("type"));
                    if(type!=null) {
                        MapWorldMarker<CustomDecoration> dummy = new DummyMapWorldMarker(type, com.getInt("x"), com.getInt("z"));
                        this.addCustomDecoration(dummy);
                    }
                }
            }
        }
    }

    @Inject(method = "load", at = @At("TAIL"), cancellable = true)
    public void load(CompoundNBT compound, CallbackInfo ci) {
        if(compound.contains("customMarkers")) {
            ListNBT listNBT = compound.getList("customMarkers", 10);

            for (int j = 0; j < listNBT.size(); ++j) {
                MapWorldMarker<?> marker = MapDecorationHandler.readWorldMarker(listNBT.getCompound(j));
                if(marker!=null) {
                    this.customMapMarkers.put(marker.getMarkerId(), marker);
                    this.addCustomDecoration(marker);
                }
            }
        }
    }

    @Inject(method = "save", at = @At("RETURN"), cancellable = true)
    public void save(CompoundNBT p_189551_1_, CallbackInfoReturnable<CompoundNBT> cir) {
        CompoundNBT com = cir.getReturnValue();

        ListNBT listNBT = new ListNBT();

        for(MapWorldMarker<?> marker : this.customMapMarkers.values()) {
            CompoundNBT com2 = new CompoundNBT();
            com2.put(marker.getTypeId(),marker.saveToNBT(new CompoundNBT()));
            listNBT.add(com2);
        }
        com.put("customMarkers", listNBT);
    }

    @Override
    public void resetCustomDecoration() {
        for(String key : this.customMapMarkers.keySet()) {
            this.customDecorations.remove(key);
            this.customMapMarkers.remove(key);
        }
        for(String key : this.bannerMarkers.keySet()) {
            this.bannerMarkers.remove(key);
            this.decorations.remove(key);
        }
    }

    @Override
    public void toggleCustomDecoration(IWorld world, BlockPos pos) {
        double d0 = (double)pos.getX() + 0.5D;
        double d1 = (double)pos.getZ() + 0.5D;
        int i = 1 << this.scale;
        double d2 = (d0 - (double)this.x) / (double)i;
        double d3 = (d1 - (double)this.z) / (double)i;
        if (d2 >= -63.0D && d3 >= -63.0D && d2 <= 63.0D && d3 <= 63.0D) {
            List<MapWorldMarker<?>> markers = MapDecorationHandler.getMarkersFromWorld(world,pos);
            boolean changed = false;
            for(MapWorldMarker<?> marker : markers) {
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
            if(changed) this.setDirty();
        }
    }

    @Inject(method = "checkBanners", at = @At("TAIL"), cancellable = true)
    public void checkBanners(IBlockReader world, int x, int z, CallbackInfo ci) {
        Iterator<MapWorldMarker<?>> iterator = this.customMapMarkers.values().iterator();

        while(iterator.hasNext()) {
            MapWorldMarker<?> marker = iterator.next();
            if (marker.getPos().getX() == x && marker.getPos().getZ() == z) {
                MapWorldMarker<?> newMarker = marker.getType().getWorldMarkerFromWorld(world,marker.getPos());
                String id = marker.getMarkerId();
                if (newMarker==null) {
                    iterator.remove();
                    this.customDecorations.remove(id);
                }
                else if(Objects.equals(id,newMarker.getMarkerId())&&marker.shouldUpdate(newMarker)){
                    newMarker.updateDecoration(this.customDecorations.get(id));
                }
            }


        }
    }

    @Inject(method = "getUpdatePacket", at = @At("RETURN"), cancellable = true)
    public void getUpdatePacket(ItemStack stack, IBlockReader reader, PlayerEntity playerEntity, CallbackInfoReturnable<IPacket<?>> cir) {
        IPacket<?> packet = cir.getReturnValue();
        if(playerEntity instanceof ServerPlayerEntity && packet instanceof SMapDataPacket) {
            NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) playerEntity),
                    new SyncCustomMapDecorationPacket(FilledMapItem.getMapId(stack), this.customDecorations.values().toArray(new CustomDecoration[0])));
        }
    }

}
