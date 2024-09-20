package net.mehvahdjukaar.moonlight.api.map;

import com.mojang.serialization.Codec;
import net.mehvahdjukaar.moonlight.api.map.decoration.MLMapMarker;
import net.minecraft.Util;

import java.util.Map;

public record MLMapDecorationsComponent(Map<String, MLMapMarker<?>> decorations) {

    public static final Codec<MLMapDecorationsComponent> CODEC = Codec.unboundedMap(Codec.STRING, MLMapMarker.REFERENCE_CODEC)
            .xmap(MLMapDecorationsComponent::new, MLMapDecorationsComponent::decorations);

    public MLMapDecorationsComponent withDecoration(String type, MLMapMarker<?> entry) {
        return new MLMapDecorationsComponent(Util.copyAndPut(this.decorations, type, entry));
    }

}
