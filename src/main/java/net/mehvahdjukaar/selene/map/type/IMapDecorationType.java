package net.mehvahdjukaar.selene.map.type;

import net.mehvahdjukaar.selene.map.CustomMapDecoration;
import net.mehvahdjukaar.selene.map.markers.MapBlockMarker;
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
