package net.mehvahdjukaar.moonlight.api;

import com.mojang.serialization.MapCodec;
import net.mehvahdjukaar.moonlight.api.item.additional_placements.BlockPlacerItem;
import net.mehvahdjukaar.moonlight.api.map.MLMapDecorationsComponent;
import net.mehvahdjukaar.moonlight.api.misc.WorldSavedDataType;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.util.PotionBottleType;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.criteria_triggers.GrindItemTrigger;
import net.mehvahdjukaar.moonlight.core.loot.*;
import net.mehvahdjukaar.moonlight.core.misc.CaveFilter;
import net.mehvahdjukaar.moonlight.core.worldgen.EmptyBoxPoolElement;
import net.mehvahdjukaar.moonlight.core.worldgen.SpawnBoxBlock;
import net.mehvahdjukaar.moonlight.core.worldgen.SpawnBoxBlockEntity;
import net.mehvahdjukaar.moonlight.core.worldgen.SpawnBoxStructurePiece;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.schedule.Schedule;
import net.minecraft.world.item.GameMasterBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

import static net.mehvahdjukaar.moonlight.core.Moonlight.res;

public class MoonlightRegistry {

    @ApiStatus.Internal
    public static void init() {
    }

    public static final Registry<WorldSavedDataType<?>> WORLD_SAVED_DATA_TYPE_REGISTRY =
            RegHelper.registerRegistry(res("world_saved_data_type"), true);

    public static final Supplier<PlacementModifierType<CaveFilter>> CAVE_MODIFIER = RegHelper.registerPlacementModifier(
            res("below_heightmaps"), CaveFilter.Type::new);

    public static final Supplier<BlockPlacerItem> BLOCK_PLACER = RegHelper.registerItem(
            res("placeable_item"), () -> new BlockPlacerItem(
                    Blocks.VOID_AIR, new Item.Properties()));

    public static final Supplier<LootPoolEntryType> LAZY_ITEM = RegHelper.registerLootPoolEntry(
            res("optional_item"), () -> OptionalItemPoolEntry.CODEC);

    public static final Supplier<LootPoolEntryType> CONFIG_ITEM = RegHelper.registerLootPoolEntry(
            res("config_item"), () -> ConfigItemPoolEntry.CODEC);

    public static final Supplier<LootItemConditionType> LAZY_PROPERTY = RegHelper.registerLootCondition(
            res("optional_block_state_property"), () -> OptionalPropertyCondition.CODEC);

    public static final Supplier<GrindItemTrigger> GRIND_TRIGGER = RegHelper.registerTriggerType(
            res("grind_item"), GrindItemTrigger::new);

    @Deprecated(forRemoval = true)
    public static final Supplier<LootItemConditionType> ICONDITION_LOOT_CONDITION = RegHelper.registerLootCondition(
            Moonlight.res("iconditions"), () -> ResourceLootItemCondition.CODEC);

    public static final Supplier<LootItemConditionType> RESOURCE_CONDITION_LOOT_ITEM_CONDITION = RegHelper.registerLootCondition(
            Moonlight.res("load_conditions"), () -> ResourceLootItemCondition.CODEC);

    public static final Supplier<LootItemConditionType> PATTERN_MATCH_CONDITION = RegHelper.registerLootCondition(
            Moonlight.res("loot_table_id_pattern"), () -> PatternMatchLootItemCondition.CODEC);


    public static final Supplier<SpawnBoxBlock> SPAWN_BOX_BLOCK = RegHelper.registerBlock(
            Moonlight.res("spawn_box"), SpawnBoxBlock::new);

    public static final Supplier<Item> STRUCTURE_BLOCK = RegHelper.registerItem(
            Moonlight.res("spawn_box"), () -> new GameMasterBlockItem(
                    SPAWN_BOX_BLOCK.get(), new Item.Properties().rarity(Rarity.EPIC)));

    public static final Supplier<BlockEntityType<SpawnBoxBlockEntity>> SPAWN_BOX_BLOCK_ENTITY =
            RegHelper.registerBlockEntityType(Moonlight.res("spawn_box"),
                    SpawnBoxBlockEntity::new, SPAWN_BOX_BLOCK);

    public static final Supplier<StructurePieceType> SPAWN_BOX_PIECE = RegHelper.registerStructurePiece(
            Moonlight.res("spawn_box"), SpawnBoxStructurePiece::new);

    public static final Supplier<StructurePoolElementType<EmptyBoxPoolElement>> EMPTY_BOX_POOL_ELEMENT = RegHelper.registerStructurePoolElement(
            Moonlight.res("empty_box"), EmptyBoxPoolElement.CODEC);

    public static final Supplier<DataComponentType<PotionBottleType>> BOTTLE_TYPE = RegHelper.registerDataComponent(
            res("bottle_type"), () -> DataComponentType.<PotionBottleType>builder()
                    .persistent(PotionBottleType.CODEC)
                    .build()
    );

    public static final Supplier<DataComponentType<MLMapDecorationsComponent>> CUSTOM_MAP_DECORATIONS = RegHelper.registerDataComponent(
            res("custom_map_decorations"), () -> DataComponentType.<MLMapDecorationsComponent>builder()
                    .persistent(MLMapDecorationsComponent.CODEC)
                    .cacheEncoding()
                    .build()
    );

    //schedule to which all the tasks are registered to
    public static final Supplier<Schedule> CUSTOM_VILLAGER_SCHEDULE =
            RegHelper.registerSchedule(Moonlight.res("custom_villager_schedule"), Schedule::new);

}
