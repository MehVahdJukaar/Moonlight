package net.mehvahdjukaar.moonlight.api.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.map.type.MlMapDecorationType;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;

import java.util.Map;

public record MLMapDecorationsComponent(Map<String, MapDecorations.Entry> decorations) {

    public static final Codec<MLMapDecorationsComponent> CODEC = Codec.unboundedMap(Codec.STRING,
                    MLMapDecorationsComponent.Entry.CODEC)
            .xmap(MapDecorations::new, MapDecorations::decorations);

    public MapDecorations withDecoration(String type, MapDecorations.Entry entry) {
        return new MapDecorations(Util.copyAndPut(this.decorations, type, entry));
    }

    public record Entry(Holder<MlMapDecorationType<?,?>> type, double x, double z, float rotation) {
        public static final Codec<MapDecorations.Entry> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                                MlMapDecorationType.CODEC.fieldOf("type").forGetter(MapDecorations.Entry::type),
                                Codec.DOUBLE.fieldOf("x").forGetter(MapDecorations.Entry::x),
                                Codec.DOUBLE.fieldOf("z").forGetter(MapDecorations.Entry::z),
                                Codec.FLOAT.fieldOf("rotation").forGetter(MapDecorations.Entry::rotation)
                        )
                        .apply(instance, MapDecorations.Entry::new)
        );
    }
}
