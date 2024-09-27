package net.mehvahdjukaar.moonlight.api.map;

import com.mojang.serialization.Codec;
import net.mehvahdjukaar.moonlight.api.map.decoration.MLMapMarker;
import net.minecraft.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class MLMapDecorationsComponent {

    public static final Codec<MLMapDecorationsComponent> CODEC = Codec.unboundedMap(Codec.STRING, MLMapMarker.REFERENCE_CODEC)
            .xmap(MLMapDecorationsComponent::new, d->d.decorations);

    public static final MLMapDecorationsComponent EMPTY = new MLMapDecorationsComponent(Map.of());

    private final Map<String, MLMapMarker<?>> decorations;

    public MLMapDecorationsComponent(Map<String, MLMapMarker<?>> decorations) {
        this.decorations = decorations;
    }

    public MLMapDecorationsComponent copyAndAdd(MLMapMarker<?> marker){
        return new MLMapDecorationsComponent(Util.copyAndPut(this.decorations, marker.getMarkerUniqueId(), marker));
    }

    public void addIfAbsent(Set<String> strings, ExpandedMapData mapDataMixin) {
        for (var d : this.decorations.entrySet()) {
            if (!strings.contains(d.getKey())) {
                mapDataMixin.ml$addCustomMarker(d.getValue());
            }
        }
    }
}
