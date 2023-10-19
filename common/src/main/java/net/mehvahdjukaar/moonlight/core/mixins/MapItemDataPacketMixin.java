package net.mehvahdjukaar.moonlight.core.mixins;

import com.mojang.datafixers.util.Pair;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.EncoderException;
import net.mehvahdjukaar.moonlight.api.map.CustomMapDecoration;
import net.mehvahdjukaar.moonlight.api.map.ExpandedMapData;
import net.mehvahdjukaar.moonlight.api.map.markers.MapBlockMarker;
import net.mehvahdjukaar.moonlight.api.map.type.MapDecorationType;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.map.MapDataInternal;
import net.mehvahdjukaar.moonlight.core.misc.IMapDataPacketExtension;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

//I hope this won't break with mods. We need this as all data needs to be received at the same time
@Mixin(ClientboundMapItemDataPacket.class)
public class MapItemDataPacketMixin implements IMapDataPacketExtension {

    @Shadow
    @Final
    @Nullable
    private MapItemSavedData.MapPatch colorPatch;
    @Shadow
    @Final
    private int mapId;
    @Unique
    private CustomMapDecoration[] moonlight$customDecorations = null;
    @Unique
    private CompoundTag moonlight$customData = null;
    @Unique
    private int moonlight$mapCenterX = 0;
    @Unique
    private int moonlight$mapCenterZ = 0;
    @Unique
    private ResourceLocation moonlight$dimension = Level.OVERWORLD.location();

    @Unique
    @Nullable
    private Pair<Boolean, Integer> moonlight$tfData = null;


    //new constructor expansion
    @Inject(method = "<init>(IBZLjava/util/Collection;Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData$MapPatch;)V",
            at = @At("RETURN"))
    private void addExtraCenterAndDimension(int mapId, byte b, boolean bl, Collection collection, MapItemSavedData.MapPatch mapPatch, CallbackInfo ci) {
        var level = PlatHelper.getCurrentServer().getLevel(Level.OVERWORLD);
        moonlight$dimension = null;
        if (level != null) {
            MapItemSavedData data = Moonlight.getMapDataFromKnownKeys(level, mapId);
            if (data != null) {
                this.moonlight$mapCenterX = data.centerX;
                this.moonlight$mapCenterZ = data.centerZ;
                this.moonlight$dimension = data.dimension.location();
            }
        }
    }


    @Inject(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V",
            at = @At("RETURN"))
    private void readExtraData(FriendlyByteBuf buf, CallbackInfo ci) {
        //we always need to send enough data to create the correct map type because we dont know if client has it
        if (buf.readBoolean()) {
            moonlight$dimension = buf.readResourceLocation();
            moonlight$mapCenterX = buf.readVarInt();
            moonlight$mapCenterZ = buf.readVarInt();
        }
        if (buf.readBoolean()) {
            this.moonlight$customDecorations = new CustomMapDecoration[buf.readVarInt()];
            for (int m = 0; m < moonlight$customDecorations.length; ++m) {
                MapDecorationType<?, ?> type = MapDataInternal.get(buf.readResourceLocation());
                if (type != null) {
                    moonlight$customDecorations[m] = type.loadDecorationFromBuffer(buf);
                }
            }
        }
        if (buf.readBoolean()) {
            //TODO: I really could have merged the 2 systems
            this.moonlight$customData = buf.readNbt(); //readCompressedNbt(buf);
        }
        if (buf.readBoolean()) {
            boolean first = buf.readBoolean();
            int second = buf.readVarInt();
            moonlight$tfData = Pair.of(first, second);
        }
    }

    @Inject(method = "write", at = @At("RETURN"))
    private void writeExtraData(FriendlyByteBuf buf, CallbackInfo ci) {
        buf.writeBoolean(moonlight$dimension != null);
        if (moonlight$dimension != null) {
            buf.writeResourceLocation(moonlight$dimension);
            buf.writeVarInt(moonlight$mapCenterX);
            buf.writeVarInt(moonlight$mapCenterZ);
        }

        buf.writeBoolean(moonlight$customDecorations != null);
        if (moonlight$customDecorations != null) {
            buf.writeVarInt(moonlight$customDecorations.length);

            for (CustomMapDecoration decoration : moonlight$customDecorations) {
                buf.writeResourceLocation(Utils.getID(decoration.getType()));
                decoration.saveToBuffer(buf);
            }
        }

        buf.writeBoolean(moonlight$customData != null);
        if (moonlight$customData != null) {
            buf.writeNbt(moonlight$customData);
           // writeCompressedNbt(buf, moonlight$customData);
        }

        buf.writeBoolean(moonlight$tfData != null);
        if (moonlight$tfData != null) {
            buf.writeBoolean(moonlight$tfData.getFirst());
            buf.writeVarInt(moonlight$tfData.getSecond());
        }
    }

    @Override
    public void moonlight$sendCustomDecorations(Collection<CustomMapDecoration> decorations) {

        //packet will be passed to client no decoding. if we are on an integrated server we need to create new objects
        if (PlatHelper.getPhysicalSide().isClient()) {
            var buffer = new FriendlyByteBuf(Unpooled.buffer());
            decorations = decorations.stream().map(e -> {
                e.saveToBuffer(buffer);
                CustomMapDecoration d = e.getType().loadDecorationFromBuffer(buffer);
                return d;
            }).toList();
        }
        moonlight$customDecorations = decorations.toArray(CustomMapDecoration[]::new);
    }

    @Override
    public void moonlight$sendCustomMapDataTag(CompoundTag dataTag) {
        moonlight$customData = dataTag;
    }

    @Override
    public CompoundTag moonlight$getCustomMapDataTag() {
        return moonlight$customData;
    }

    @Override
    public MapItemSavedData.MapPatch moonlight$getColorPatch() {
        return colorPatch;
    }

    @Override
    public ResourceKey<Level> moonlight$getDimension() {
        return ResourceKey.create(Registries.DIMENSION, moonlight$dimension);
    }


    @Inject(method = "applyToMap", at = @At("HEAD"))
    private void handleExtraData(MapItemSavedData mapData, CallbackInfo ci) {
        var serverDeco = this.moonlight$customDecorations;
        var serverData = this.moonlight$customData;

        mapData.centerX = this.moonlight$mapCenterX;
        mapData.centerZ = this.moonlight$mapCenterZ;
        mapData.dimension = this.moonlight$getDimension();

        if (serverDeco != null || serverData != null) {

            if (mapData instanceof ExpandedMapData ed) {
                //mapData = MapItemSavedData.createForClient(message.scale, message.locked, Minecraft.getInstance().level.dimension());
                //Minecraft.getInstance().level.setMapData(string, mapData);

                if (serverDeco != null) {
                    Map<String, CustomMapDecoration> decorations = ed.getCustomDecorations();
                    decorations.clear();
                    int i;
                    for (i = 0; i < serverDeco.length; ++i) {
                        CustomMapDecoration customDecoration = serverDeco[i];
                        if (customDecoration != null) decorations.put("icon-" + i, customDecoration);
                        else {
                            Moonlight.LOGGER.warn("Failed to load custom map decoration, skipping");
                        }
                    }
                    //adds dynamic todo use deco instead
                    for (MapBlockMarker<?> m : MapDataInternal.getDynamicClient(mapId, mapData)) {
                        var d = m.createDecorationFromMarker(mapData);
                        if (d != null) {
                            decorations.put(m.getMarkerId(), d);
                        }
                    }
                }
                if (serverData != null) {
                    var customData = ed.getCustomData();
                    for (var v : customData.values()) {
                        v.loadUpdateTag(this.moonlight$customData);
                    }
                }
            }
        }
    }


    private static CompoundTag readCompressedNbt(FriendlyByteBuf buf) {
        int i = buf.readerIndex();
        byte b = buf.readByte();
        if (b == 0) {
            throw new EncoderException();
        } else {
            buf.readerIndex(i);
            try {
                return NbtIo.readCompressed(new ByteBufInputStream(buf));
            } catch (IOException var5) {
                throw new EncoderException(var5);
            }
        }
    }

    private static void writeCompressedNbt(FriendlyByteBuf buf, CompoundTag nbt) {
        if (nbt == null) {
            buf.writeByte(0);
        } else {
            try {
                NbtIo.writeCompressed(nbt, (new ByteBufOutputStream(buf)));
            } catch (IOException var3) {
                throw new EncoderException(var3);
            }
        }
    }
}
