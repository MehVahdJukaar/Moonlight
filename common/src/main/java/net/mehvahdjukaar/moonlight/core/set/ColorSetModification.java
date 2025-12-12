package net.mehvahdjukaar.moonlight.core.set;

import com.google.common.collect.Streams;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Keyable;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.misc.BlockAndItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class ColorSetModification {

    private static final String DEFAULT_KEY = "default";

    public static final Codec<ColorSetModification> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.simpleMap(Codec.STRING, BlockAndItem.CODEC, makeKeys()).fieldOf("colors").forGetter(ColorSetModification::toStringMap),
            Codec.BOOL.optionalFieldOf("replace", false).orElse(false).forGetter(cs -> cs.replace),
            ResourceLocation.CODEC.fieldOf("id").forGetter(cs -> cs.id)
    ).apply(instance, ColorSetModification::new));

    private static Keyable makeKeys() {
        var list = Streams.concat(Stream.of(DEFAULT_KEY), Arrays.stream(DyeColor.values()).map(DyeColor::getSerializedName))
                .toList();
        return new Keyable() {
            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return list.stream().map(ops::createString);
            }
        };
    }

    private final Map<DyeColor, BlockAndItem> colorsMap = new HashMap<>();
    private final ResourceLocation id;
    private final boolean replace;

    private ColorSetModification(Map<String, BlockAndItem> colorsMap, boolean replace, ResourceLocation id) {
        this.replace = replace;
        this.id = id;
        for (Map.Entry<String, BlockAndItem> entry : colorsMap.entrySet()) {
            String key = entry.getKey();
            DyeColor color = key.equals(DEFAULT_KEY) ? null : DyeColor.byName(key, null);
            this.colorsMap.put(color, entry.getValue());
        }
    }

    public ResourceLocation getId() {
        return id;
    }

    public boolean replace() {
        return replace;
    }

    public boolean hasItems() {
        return colorsMap.values().stream().anyMatch(bi -> bi.item() != null);
    }

    public boolean hasBlocks() {
        return colorsMap.values().stream().anyMatch(bi -> bi.block() != null);
    }

    //entry set method
    public Set<Map.Entry<DyeColor, BlockAndItem>> entrySet() {
        return colorsMap.entrySet();
    }

    private Map<String, BlockAndItem> toStringMap() {
        Map<String, BlockAndItem> map = new HashMap<>();
        for (Map.Entry<DyeColor, BlockAndItem> entry : colorsMap.entrySet()) {
            String key = entry.getKey() != null ? entry.getKey().getSerializedName() : DEFAULT_KEY;
            map.put(key, entry.getValue());
        }
        return map;
    }
}
