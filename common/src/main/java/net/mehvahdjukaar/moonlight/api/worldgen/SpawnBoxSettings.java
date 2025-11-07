package net.mehvahdjukaar.moonlight.api.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record SpawnBoxSettings(
        Map<MobCategory, Map<String, WeightedRandomList<MobSpawnSettings.SpawnerData>>> spawnOverrides) {

    private record Entry(MobCategory category, String name, WeightedRandomList<MobSpawnSettings.SpawnerData> spawns) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                MobCategory.CODEC.fieldOf("category").forGetter(Entry::category),
                Codec.STRING.fieldOf("name").forGetter(Entry::name),
                WeightedRandomList.codec(MobSpawnSettings.SpawnerData.CODEC).fieldOf("spawns").forGetter(Entry::spawns)
        ).apply(instance, Entry::new));
    }

    public static final Codec<SpawnBoxSettings> CODEC = Entry.CODEC.listOf().flatXmap(
            SpawnBoxSettings::fromEntries,
            spawnBoxSettings -> DataResult.success(spawnBoxSettings.toEntryList())
    );

    private static DataResult<SpawnBoxSettings> fromEntries(List<Entry> entries) {
        Map<MobCategory, Map<String, WeightedRandomList<MobSpawnSettings.SpawnerData>>> map = new HashMap<>();
        for (Entry e : entries) {
            map.computeIfAbsent(e.category, k -> new HashMap<>());
            var catMap = map.get(e.category);
            if (catMap.containsKey(e.name)) {
                return DataResult.error(() -> "Duplicate spawn box entry for category " + e.category + " and name " + e.name);
            } else {
                catMap.put(e.name, e.spawns);
            }
        }
        return DataResult.success(new SpawnBoxSettings(map));
    }

    private List<Entry> toEntryList() {
        return this.spawnOverrides.entrySet().stream().flatMap(catEntry ->
                catEntry.getValue().entrySet().stream().map(nameEntry ->
                        new Entry(catEntry.getKey(), nameEntry.getKey(), nameEntry.getValue())
                )
        ).toList();
    }

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