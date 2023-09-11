package net.mehvahdjukaar.moonlight.api.map.type;

import net.mehvahdjukaar.moonlight.api.map.CustomMapDecoration;
import net.mehvahdjukaar.moonlight.api.map.markers.MapBlockMarker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;

//type itself can have 2 types: json defined or custom code defined
public abstract class MapDecorationType<D extends CustomMapDecoration, M extends MapBlockMarker<D>> {

    abstract boolean hasMarker();

    public ResourceLocation getCustomFactoryID() {
        return new ResourceLocation("");
    }

    @Nullable
    public abstract D loadDecorationFromBuffer(FriendlyByteBuf buffer);

    public Set<D> getDynamicDecorations(Player player, MapItemSavedData data){
        return Set.of();
    }

    @Nullable
    public abstract M loadMarkerFromNBT(CompoundTag compound);

    @Nullable
    public abstract M getWorldMarkerFromWorld(BlockGetter reader, BlockPos pos);


    //used for commands. gives either marker default instance or dummy marker
    @NotNull
    public abstract MapBlockMarker<D> getDefaultMarker(BlockPos pos);

    public int getDefaultMapColor() {
        return 1;
    }

    public Optional<HolderSet<Structure>> getAssociatedStructure() {
        return Optional.empty();
    }

}
