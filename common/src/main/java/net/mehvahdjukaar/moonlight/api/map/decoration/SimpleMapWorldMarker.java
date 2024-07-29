package net.mehvahdjukaar.moonlight.api.map.decoration;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;

import java.util.Optional;

/**
 * used to add decorations for decoration types that don't have a block marker (for structure decorations for example)
 * also used for json defined ones
 */
public class SimpleMapWorldMarker extends MLMapMarker<MLMapDecoration> {


    public SimpleMapWorldMarker(Holder<MLMapDecorationType<?,?>> type, BlockPos pos, Float rotation, Optional<Component> name) {
        super(type, pos, rotation, name, Optional.empty(), Optional.empty(), false);

    }
    public SimpleMapWorldMarker(Holder<MLMapDecorationType<?,?>> type, BlockPos pos, Float rotation, Optional<Component> name,
                                Optional<Boolean> shouldRefresh, Optional<Boolean> shouldSave, boolean preventsExtending) {
        super(type, pos, rotation, name, shouldRefresh, shouldSave, preventsExtending);
    }

    @Override
    protected MLMapDecoration doCreateDecoration(byte mapX, byte mapY, byte rot) {
        return new MLMapDecoration(this.getType(), mapX, mapY, rot, getDisplayName());
    }

    static final MapCodec<SimpleMapWorldMarker> DIRECT_CODEC = RecordCodecBuilder.mapCodec(i->
        baseCodecGroup(i).apply(i, SimpleMapWorldMarker::new));


}
