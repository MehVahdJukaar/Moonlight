package net.mehvahdjukaar.moonlight.api.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;

import java.util.Map;

public record SpawnBoxSettings(Map<MobCategory, Map<String, WeightedRandomList<MobSpawnSettings.SpawnerData>>> spawnOverrides) {
    public static final Codec<SpawnBoxSettings> CODEC = Codec.simpleMap(MobCategory.CODEC,
                    Codec.unboundedMap(Codec.STRING, WeightedRandomList.codec(MobSpawnSettings.SpawnerData.CODEC)),
                    StringRepresentable.keys(MobCategory.values()))
            .codec()
            .xmap(SpawnBoxSettings::new, SpawnBoxSettings::spawnOverrides);

    public boolean hasCategory(MobCategory category) {
        return this.spawnOverrides.containsKey(category);
    }

    public WeightedRandomList<MobSpawnSettings.SpawnerData> get(String boxID, MobCategory category) {
        var map = this.spawnOverrides.get(category);
        if (map != null) {
            return map.get(boxID);
        }
        return null;
    }

    public boolean isEmpty() {
        return this.spawnOverrides.isEmpty();
    }

    public static final SpawnBoxSettings EMPTY = new SpawnBoxSettings(Map.of());
}