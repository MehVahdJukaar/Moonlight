package net.mehvahdjukaar.moonlight.api.map;

import com.mojang.serialization.Codec;
import net.mehvahdjukaar.moonlight.api.map.decoration.MLMapMarker;
import net.minecraft.Util;

import java.util.Map;
import java.util.Set;

public record MLMapDecorationsComponent(Map<String, MLMapMarker<?>> decorations) {

    public static final Codec<MLMapDecorationsComponent> CODEC = Codec.unboundedMap(Codec.STRING, MLMapMarker.CODEC)
            .xmap(MLMapDecorationsComponent::new, d -> d.decorations);

    public static final MLMapDecorationsComponent EMPTY = new MLMapDecorationsComponent(Map.of());

    public MLMapDecorationsComponent copyAndAdd(MLMapMarker<?> marker) {
        return new MLMapDecorationsComponent(Util.copyAndPut(this.decorations, marker.getMarkerUniqueId(), marker));
    }

    public void addToMapIfAbsent(Set<String> strings, ExpandedMapData mapDataMixin) {
        for (var d : this.decorations.entrySet()) {
            if (!strings.contains(d.getKey())) {
                mapDataMixin.ml$addCustomMarker(d.getValue());
            }
        }
    }
}
