package net.mehvahdjukaar.moonlight.core.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.mehvahdjukaar.moonlight.api.map.ExpandedMapData;
import net.mehvahdjukaar.moonlight.api.map.decoration.MLMapMarker;
import net.mehvahdjukaar.moonlight.api.map.decoration.MLMapDecoration;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
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

import java.util.Collection;
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

    @Shadow public abstract Optional<MapItemSavedData.MapPatch> colorPatch();

    @Unique
    private Optional<List<MLMapDecoration>> moonlight$customDecorations = Optional.empty();
    @Unique
    private Optional<CompoundTag> moonlight$customData = Optional.empty();
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
        var level = PlatHelper.getCurrentServer().getLevel(Level.OVERWORLD);
        moonlight$dimension = null;
        if (level != null) {
            MapItemSavedData data = CompatHandler.getMapDataFromKnownKeys(level, mapId);
            if (data != null) {
                this.moonlight$mapCenterX = data.centerX;
                this.moonlight$mapCenterZ = data.centerZ;
                this.moonlight$dimension = data.dimension.location();
            }
        }
    }

    @ModifyReturnValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/StreamCodec;composite(Lnet/minecraft/network/codec/StreamCodec;Ljava/util/function/Function;Lnet/minecraft/network/codec/StreamCodec;Ljava/util/function/Function;Lnet/minecraft/network/codec/StreamCodec;Ljava/util/function/Function;Lnet/minecraft/network/codec/StreamCodec;Ljava/util/function/Function;Lnet/minecraft/network/codec/StreamCodec;Ljava/util/function/Function;Lcom/mojang/datafixers/util/Function5;)Lnet/minecraft/network/codec/StreamCodec;"))
    private static StreamCodec<RegistryFriendlyByteBuf, ClientboundMapItemDataPacket> moonlight$modifyMapPacketCodec(
            StreamCodec<RegistryFriendlyByteBuf, ClientboundMapItemDataPacket> original) {
        return StreamCodec.composite(original, Function.identity(),
                MLMapDecoration.CODEC.apply(ByteBufCodecs.list()).apply(ByteBufCodecs::optional),
                p -> ((IMapDataPacketExtension) (Object) p).moonlight$getCustomDecorations(),
                ByteBufCodecs.OPTIONAL_COMPOUND_TAG,
                p -> ((IMapDataPacketExtension) (Object) p).moonlight$getCustomMapDataTag(),
                (old, deco, tag) -> {
                    ((IMapDataPacketExtension) (Object) old).moonlight$setCustomDecorations(deco);
                    ((IMapDataPacketExtension) (Object) old).moonlight$setCustomMapDataTag(tag);
                    return old;
                }
        );
    }

    @Override
    public Optional<CompoundTag> moonlight$getCustomMapDataTag() {
        return moonlight$customData;
    }

    @Override
    public Optional<List<MLMapDecoration>> moonlight$getCustomDecorations() {
        return moonlight$customDecorations;
    }

    @Override
    public void moonlight$setCustomDecorations(Optional<List<MLMapDecoration>> deco) {
        moonlight$customDecorations = deco.map(List::copyOf);
    }

    @Override
    public void moonlight$setCustomMapDataTag(Optional<CompoundTag> tag) {
        moonlight$customData = tag;
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


        if (mapData instanceof ExpandedMapData ed) {
            Map<String, MLMapDecoration> decorations = ed.ml$getCustomDecorations();


            //mapData = MapItemSavedData.createForClient(message.scale, message.locked, Minecraft.getInstance().level.dimension());
            //Minecraft.getInstance().level.setMapData(string, mapData);

            if (serverDeco.isPresent()) {
                decorations.clear();
                int i;
                for (i = 0; i < serverDeco.get().size(); ++i) {
                    MLMapDecoration customDecoration = serverDeco.get().get(i);
                    if (customDecoration != null) decorations.put("icon-" + i, customDecoration);
                    else {
                        Moonlight.LOGGER.warn("Failed to load custom map decoration, skipping");
                    }
                }

            }
            if (serverData.isPresent()) {
                var customData = ed.ml$getCustomData();
                for (var v : customData.values()) {
                    v.loadFromUpdateTag(serverData.get());
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
