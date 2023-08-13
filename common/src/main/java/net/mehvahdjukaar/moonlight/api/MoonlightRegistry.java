package net.mehvahdjukaar.moonlight.api;

import net.mehvahdjukaar.moonlight.api.item.additional_placements.AdditionalItemPlacementsAPI;
import net.mehvahdjukaar.moonlight.api.item.additional_placements.BlockPlacerItem;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.core.criteria_triggers.GrindItemTrigger;
import net.mehvahdjukaar.moonlight.core.loot.OptionalItemPool;
import net.mehvahdjukaar.moonlight.core.loot.OptionalPropertyCondition;
import net.mehvahdjukaar.moonlight.core.misc.CaveFilter;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

import static net.mehvahdjukaar.moonlight.core.Moonlight.res;

public class MoonlightRegistry {

    @ApiStatus.Internal
    public static void init(){
    }

    public static final TagKey<Block> SHEARABLE_TAG = TagKey.create(Registries.BLOCK, new ResourceLocation("mineable/shear"));

    public static final Supplier<PlacementModifierType<CaveFilter>> CAVE_MODIFIER = RegHelper.register(
            res("below_heightmaps"), CaveFilter.Type::new, Registries.PLACEMENT_MODIFIER_TYPE);

    public static final Supplier<BlockPlacerItem> BLOCK_PLACER = RegHelper.registerItem(
            res("placeable_item"), () -> new BlockPlacerItem(
                    Blocks.VOID_AIR, new Item.Properties()));

    public static final Supplier<LootPoolEntryType> LAZY_ITEM = RegHelper.register(
            res("optional_item"), () ->
                    new LootPoolEntryType(new OptionalItemPool.Serializer()), Registries.LOOT_POOL_ENTRY_TYPE);

    public static final Supplier<LootItemConditionType> LAZY_PROPERTY = RegHelper.register(
            res("optional_block_state_property"), () ->
                    new LootItemConditionType(new OptionalPropertyCondition.ConditionSerializer()), Registries.LOOT_CONDITION_TYPE);

    public static final GrindItemTrigger GRIND_TRIGGER = CriteriaTriggers.register(new GrindItemTrigger());

}
