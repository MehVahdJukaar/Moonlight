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
import net.mehvahdjukaar.moonlight.core.worldgen.*;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.GameMasterBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

import static net.mehvahdjukaar.moonlight.core.Moonlight.res;

public class MoonlightRegistry {

    @ApiStatus.Internal
    public static void init() {
    }

    public static final Registry<WorldSavedDataType<?>> WORLD_SAVED_DATA_TYPE_REGISTRY =
            RegHelper.registerRegistry(res("world_saved_data_type"), true);

    public static final Supplier<PlacementModifierType<HeightRangeFilter>> HEIGHT_RANGE = RegHelper.registerPlacementModifier(
            res("height_range_filter"), HeightRangeFilter.CODEC);

    public static final Supplier<BlockPlacerItem> BLOCK_PLACER = RegHelper.registerItem(
            res("placeable_item"), p -> new BlockPlacerItem(Blocks.VOID_AIR, p));

    public static final Supplier<MapCodec<OptionalItemPoolEntry>> LAZY_ITEM = RegHelper.registerLootPoolEntry(
            res("optional_item"), () -> OptionalItemPoolEntry.CODEC);

    public static final Supplier<MapCodec<ConfigItemPoolEntry>> CONFIG_ITEM = RegHelper.registerLootPoolEntry(
            res("config_item"), () -> ConfigItemPoolEntry.CODEC);

    public static final Supplier<MapCodec<OptionalPropertyCondition>> LAZY_PROPERTY = RegHelper.registerLootCondition(
            res("optional_block_state_property"), () -> OptionalPropertyCondition.CODEC);

    public static final Supplier<GrindItemTrigger> GRIND_TRIGGER = RegHelper.registerTriggerType(
            res("grind_item"), GrindItemTrigger::new);

    public static final Supplier<MapCodec<ResourceLootItemCondition>> RESOURCE_CONDITION_LOOT_ITEM_CONDITION = RegHelper.registerLootCondition(
            Moonlight.res("load_conditions"), () -> ResourceLootItemCondition.CODEC);

    public static final Supplier<MapCodec<PatternMatchLootItemCondition>> PATTERN_MATCH_CONDITION = RegHelper.registerLootCondition(
            Moonlight.res("loot_table_id_pattern"), () -> PatternMatchLootItemCondition.CODEC);


    public static final Supplier<SpawnBoxBlock> SPAWN_BOX_BLOCK = RegHelper.registerBlock(
            Moonlight.res("spawn_box"), SpawnBoxBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.JIGSAW));

    public static final Supplier<Item> STRUCTURE_BLOCK = RegHelper.registerItem(
            Moonlight.res("spawn_box"), p -> new GameMasterBlockItem(SPAWN_BOX_BLOCK.get(), p),
            new Item.Properties().rarity(Rarity.EPIC).useBlockDescriptionPrefix());

    public static final Supplier<BlockEntityType<SpawnBoxBlockEntity>> SPAWN_BOX_BLOCK_ENTITY =
            RegHelper.registerBlockEntityType(Moonlight.res("spawn_box"),
                    SpawnBoxBlockEntity::new, SPAWN_BOX_BLOCK);

    public static final Supplier<StructurePieceType> SPAWN_BOX_PIECE = RegHelper.registerStructurePiece(
            Moonlight.res("spawn_box"), SpawnBoxStructurePiece::new);

    public static final Supplier<StructurePoolElementType<SpawnBoxPoolElement>> SPAWN_BOX_POOL_ELEMENT = RegHelper.registerStructurePoolElement(
            Moonlight.res("spawn_box"), SpawnBoxPoolElement.CODEC);


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

}
