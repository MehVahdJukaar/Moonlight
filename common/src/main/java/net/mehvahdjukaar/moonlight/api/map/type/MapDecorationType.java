package net.mehvahdjukaar.moonlight.api.map.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.map.CustomMapDecoration;
import net.mehvahdjukaar.moonlight.api.map.MapDecorationRegistry;
import net.mehvahdjukaar.moonlight.api.map.markers.MapBlockMarker;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

import javax.annotation.Nullable;
import java.util.Optional;

public abstract class MapDecorationType<D extends CustomMapDecoration, M extends MapBlockMarker<D>> {

    //wish I knew how to do this better, but I can't partially dispatch an optional key
    @Deprecated
    public static final Codec<MapDecorationType<?, ?>> GENERIC_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.optionalFieldOf("custom", new ResourceLocation("")).forGetter(MapDecorationType::getCustomFactoryID),
            RuleTest.CODEC.optionalFieldOf("target_block").forGetter(b -> b instanceof SimpleDecorationType s ? s.getTarget() : Optional.empty())
    ).apply(instance, MapDecorationType::decode));

    @Deprecated
    private static MapDecorationType<?, ?> decode(ResourceLocation customFactory, Optional<RuleTest> ruleTest) {
        var f = MapDecorationRegistry.CODE_TYPES_FACTORIES.get(customFactory);
        if (f != null) return f.get();
        else return new SimpleDecorationType(ruleTest);
    }

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


}
