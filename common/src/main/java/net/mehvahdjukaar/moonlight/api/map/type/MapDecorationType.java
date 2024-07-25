package net.mehvahdjukaar.moonlight.api.map.type;

import net.mehvahdjukaar.moonlight.api.map.CustomMapDecoration;
import net.mehvahdjukaar.moonlight.api.map.markers.MapBlockMarker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

//type itself can have two types: json defined or custom code defined
public interface MapDecorationType<D extends CustomMapDecoration, M extends MapBlockMarker<D>> {

    /**
     * If this marker should be saved to disk
     */
    @ApiStatus.Internal
    boolean isFromWorld();

    default ResourceLocation getCustomFactoryID() {
        return ResourceLocation.parse("");
    }

    @Nullable
    D loadDecorationFromBuffer(FriendlyByteBuf buffer);

    M createEmptyMarker();

    @Nullable
    M load(CompoundTag compound, HolderLookup.Provider registries);

    @Nullable
    M getWorldMarkerFromWorld(BlockGetter reader, BlockPos pos);

    default int getDefaultMapColor() {
        return 1;
    }

    default Optional<HolderSet<Structure>> getAssociatedStructure() {
        return Optional.empty();
    }
}
