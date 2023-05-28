package net.mehvahdjukaar.moonlight.api.map.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.map.CustomMapDecoration;
import net.mehvahdjukaar.moonlight.api.map.MapDecorationRegistry;
import net.mehvahdjukaar.moonlight.api.map.markers.MapBlockMarker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

import org.jetbrains.annotations.Nullable;
import java.util.Optional;

//type itself can have 2 types: json defined or custom code defined
public abstract class MapDecorationType<D extends CustomMapDecoration, M extends MapBlockMarker<D>> {

    abstract boolean hasMarker();

    public ResourceLocation getCustomFactoryID() {
        return new ResourceLocation("");
    }

    @Nullable
    public abstract D loadDecorationFromBuffer(FriendlyByteBuf buffer);

    @Nullable
    public abstract M loadMarkerFromNBT(CompoundTag compound);

    @Nullable
    public abstract M getWorldMarkerFromWorld(BlockGetter reader, BlockPos pos);

    public int getDefaultMapColor(){
        return 1;
    }

    public Optional<HolderSet<Structure>> getAssociatedStructure(){
        return Optional.empty();
    }

}
