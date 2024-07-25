package net.mehvahdjukaar.moonlight.api.map;

import com.mojang.serialization.Codec;
import net.minecraft.world.item.component.MapDecorations;

public class CustomMapDecorationsComponent {

    public static final Codec<CustomMapDecorationsComponent> CODEC = Codec.unboundedMap(Codec.STRING, CustomMapDecorationsComponent.Entry.CODEC)
            .xmap(MapDecorations::new, MapDecorations::decorations);
}
