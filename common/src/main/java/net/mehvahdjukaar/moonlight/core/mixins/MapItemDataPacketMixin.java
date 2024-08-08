package net.mehvahdjukaar.moonlight.core.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.mehvahdjukaar.moonlight.api.map.ExpandedMapData;
import net.mehvahdjukaar.moonlight.api.map.decoration.MLMapDecoration;
import net.mehvahdjukaar.moonlight.api.map.decoration.MLMapMarker;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.CompatHandler;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.map.MapDataInternal;
import net.mehvahdjukaar.moonlight.core.misc.IMapDataPacketExtension;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

//I hope this won't break with mods. We need this as all data needs to be received at the same time
@Mixin(ClientboundMapItemDataPacket.class)
public abstract class MapItemDataPacketMixin implements IMapDataPacketExtension {

    @Shadow
    @Final
    private MapId mapId;

    @Nullable
    @Unique
    private List<MLMapDecoration> moonlight$customDecorations = null;
    @Nullable
    @Unique
    private CompoundTag moonlight$customData = null;
    @Unique
    private int moonlight$mapCenterX = 0;
    @Unique
    private int moonlight$mapCenterZ = 0;
    @Unique
    private ResourceLocation moonlight$dimension = Level.OVERWORLD.location();

    //new constructor expansion
    @Inject(method = "<init>(Lnet/minecraft/world/level/saveddata/maps/MapId;BZLjava/util/Optional;Ljava/util/Optional;)V",
            at = @At("RETURN"))
    private void moonlight$addExtraCenterAndDimension(MapId mapId, byte b, boolean bl, Optional optional, Optional optional2, CallbackInfo ci) {
        var server = PlatHelper.getCurrentServer();
        moonlight$dimension = null;
        // on server side we add extra data like this
        if (server != null && server.getLevel(Level.OVERWORLD) instanceof ServerLevel sl) {
            MapItemSavedData data = CompatHandler.getMapDataFromKnownKeys(sl, mapId);
            if (data != null) {
                this.moonlight$mapCenterX = data.centerX;
                this.moonlight$mapCenterZ = data.centerZ;
                this.moonlight$dimension = data.dimension.location();
            }
        }
    }

    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/network/codec/StreamCodec;composite(Lnet/minecraft/network/codec/StreamCodec;Ljava/util/function/Function;Lnet/minecraft/network/codec/StreamCodec;Ljava/util/function/Function;Lnet/minecraft/network/codec/StreamCodec;Ljava/util/function/Function;Lnet/minecraft/network/codec/StreamCodec;Ljava/util/function/Function;Lnet/minecraft/network/codec/StreamCodec;Ljava/util/function/Function;Lcom/mojang/datafixers/util/Function5;)Lnet/minecraft/network/codec/StreamCodec;"))
    private static StreamCodec<RegistryFriendlyByteBuf, ClientboundMapItemDataPacket> moonlight$modifyMapPacketCodec(
            StreamCodec<RegistryFriendlyByteBuf, ClientboundMapItemDataPacket> original) {
        return StreamCodec.composite(original, Function.identity(),
                MLMapDecoration.CODEC.apply(ByteBufCodecs.list()).apply(ByteBufCodecs::optional),
                p -> ((IMapDataPacketExtension) (Object) p).moonlight$getCustomDecorations(),
                ByteBufCodecs.OPTIONAL_COMPOUND_TAG,
                p -> ((IMapDataPacketExtension) (Object) p).moonlight$getCustomMapDataTag(),
                ResourceLocation.STREAM_CODEC, p -> ((IMapDataPacketExtension) (Object) p).moonlight$getDimension(),
                ByteBufCodecs.INT, p -> ((IMapDataPacketExtension) (Object) p).moonlight$getMapCenterX(),
                ByteBufCodecs.INT, p -> ((IMapDataPacketExtension) (Object) p).moonlight$getMapCenterZ(),
                (old, deco, tag, res, x, z) -> {
                    IMapDataPacketExtension ext = (IMapDataPacketExtension) (Object) old;
                    ext.moonlight$setCustomDecorations(deco);
                    ext.moonlight$setCustomMapDataTag(tag);
                    ext.moonlight$setDimension(res);
                    ext.moonlight$setMapCenter(x, z);
                    return old;
                }
        );
    }


    @Override
    public Optional<CompoundTag> moonlight$getCustomMapDataTag() {
        return Optional.ofNullable(moonlight$customData);
    }

    @Override
    public Optional<List<MLMapDecoration>> moonlight$getCustomDecorations() {
        return Optional.ofNullable(moonlight$customDecorations);
    }

    @Override
    public void moonlight$setCustomDecorations(Optional<List<MLMapDecoration>> deco) {
        moonlight$customDecorations = deco.map(List::copyOf).orElse(null);
    }

    @Override
    public void moonlight$setCustomMapDataTag(Optional<CompoundTag> tag) {
        moonlight$customData = tag.orElse(null);
    }

    @Override
    public ResourceLocation moonlight$getDimension() {
        return moonlight$dimension;
    }

    @Override
    public void moonlight$setDimension(ResourceLocation dim) {
        this.moonlight$dimension = dim;
    }

    @Override
    public int moonlight$getMapCenterX() {
        return this.moonlight$mapCenterX;
    }

    @Override
    public int moonlight$getMapCenterZ() {
        return this.moonlight$mapCenterZ;
    }

    @Override
    public void moonlight$setMapCenter(int x, int z) {
        this.moonlight$mapCenterX = x;
        this.moonlight$mapCenterZ = z;
    }

    @Inject(method = "applyToMap", at = @At("HEAD"))
    private void handleExtraData(MapItemSavedData mapData, CallbackInfo ci) {
        var serverDeco = this.moonlight$customDecorations;
        var serverData = this.moonlight$customData;

        mapData.centerX = this.moonlight$mapCenterX;
        mapData.centerZ = this.moonlight$mapCenterZ;
        mapData.dimension = ResourceKey.create(Registries.DIMENSION, this.moonlight$dimension);


        if (mapData instanceof ExpandedMapData ed) {
            Map<String, MLMapDecoration> decorations = ed.ml$getCustomDecorations();


            //mapData = MapItemSavedData.createForClient(message.scale, message.locked, Minecraft.getInstance().level.dimension());
            //Minecraft.getInstance().level.setMapData(string, mapData);

            if (serverDeco != null) {
                decorations.clear();
                int i;
                for (i = 0; i < serverDeco.size(); ++i) {
                    MLMapDecoration customDecoration = serverDeco.get(i);
                    if (customDecoration != null) decorations.put("icon-" + i, customDecoration);
                    else {
                        Moonlight.LOGGER.warn("Failed to load custom map decoration, skipping");
                    }
                }

            }
            if (serverData != null) {
                var customData = ed.ml$getCustomData();
                for (var v : customData.values()) {
                    v.loadFromUpdateTag(serverData);
                }
            }

            //adds dynamic todo use deco instead
            // aaa not optimal but needed for player like behavior
            // update immediately all the times
            for (MLMapMarker<?> m : MapDataInternal.getDynamicClient(mapId, mapData)) {
                var d = m.createDecorationFromMarker(mapData);
                if (d != null) {
                    decorations.put(m.getMarkerUniqueId(), d);
                }
            }
        }
    }
}
