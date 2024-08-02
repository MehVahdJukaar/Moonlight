package net.mehvahdjukaar.moonlight.api;

import net.mehvahdjukaar.moonlight.api.item.additional_placements.BlockPlacerItem;
import net.mehvahdjukaar.moonlight.api.map.MLMapDecorationsComponent;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.util.PotionBottleType;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.criteria_triggers.GrindItemTrigger;
import net.mehvahdjukaar.moonlight.core.loot.OptionalItemPool;
import net.mehvahdjukaar.moonlight.core.loot.OptionalPropertyCondition;
import net.mehvahdjukaar.moonlight.core.misc.CaveFilter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.schedule.Schedule;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

import static net.mehvahdjukaar.moonlight.core.Moonlight.res;

public class MoonlightRegistry {

    @ApiStatus.Internal
    public static void init() {
    }


    public static final Supplier<PlacementModifierType<CaveFilter>> CAVE_MODIFIER = RegHelper.registerPlacementModifier(
            res("below_heightmaps"), CaveFilter.Type::new);

    public static final Supplier<BlockPlacerItem> BLOCK_PLACER = RegHelper.registerItem(
            res("placeable_item"), () -> new BlockPlacerItem(
                    Blocks.VOID_AIR, new Item.Properties()));

    public static final Supplier<LootPoolEntryType> LAZY_ITEM = RegHelper.registerLootPoolEntry(
            res("optional_item"), () -> OptionalItemPool.CODEC);

    public static final Supplier<LootItemConditionType> LAZY_PROPERTY = RegHelper.registerLootCondition(
            res("optional_block_state_property"), () -> OptionalPropertyCondition.CODEC);

    public static final Supplier<GrindItemTrigger> GRIND_TRIGGER = RegHelper.registerTriggerType(
            res("grind_item"), GrindItemTrigger::new);

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
