package net.mehvahdjukaar.moonlight.api.map.type;

import net.mehvahdjukaar.moonlight.api.map.markers.MapBlockMarker;
import net.mehvahdjukaar.moonlight.api.map.CustomMapDecoration;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;

import javax.annotation.Nullable;

public interface IMapDecorationType<D extends CustomMapDecoration, M extends MapBlockMarker<D>> {

    boolean hasMarker();

    ResourceLocation getId();

    @Nullable
    D loadDecorationFromBuffer(FriendlyByteBuf buffer);

    @Nullable
    M loadMarkerFromNBT(CompoundTag compound);

    @Nullable
    M getWorldMarkerFromWorld(BlockGetter reader, BlockPos pos);

}
