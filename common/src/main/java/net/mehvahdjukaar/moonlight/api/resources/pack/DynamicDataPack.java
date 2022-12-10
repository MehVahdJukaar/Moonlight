package net.mehvahdjukaar.moonlight.api.resources.pack;

import com.google.gson.JsonElement;
import net.mehvahdjukaar.moonlight.api.resources.RPUtils;
import net.mehvahdjukaar.moonlight.api.resources.ResType;
import net.mehvahdjukaar.moonlight.api.resources.SimpleTagBuilder;
import net.minecraft.data.loot.packs.VanillaBlockLoot;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

import java.io.ByteArrayInputStream;

public class DynamicDataPack extends DynamicResourcePack {

    public DynamicDataPack(ResourceLocation name, Pack.Position position, boolean fixed, boolean hidden) {
        super(name, PackType.SERVER_DATA, position, fixed, hidden);
    }

    public DynamicDataPack(ResourceLocation name) {
        super(name, PackType.SERVER_DATA);
    }


    public void addTag(SimpleTagBuilder builder, ResourceKey<?> type) {

        ResourceLocation tagId = builder.getId();
        String tagPath = type.location().getPath();
        if (tagPath.equals("block") || tagPath.equals("entity_type") || tagPath.equals("item")) tagPath = tagPath + "s";
        ResourceLocation loc = ResType.TAGS.getPath(new ResourceLocation(tagId.getNamespace(),
                tagPath + "/" + tagId.getPath()));
        //merge tags
        if (this.resources.containsKey(loc)) {
            var r = resources.get(loc);
            var oldTag = RPUtils.deserializeJson(new ByteArrayInputStream(r));
            builder.addFromJson(oldTag);
        }
        JsonElement json = builder.serializeToJson();
        this.addJson(loc, json, ResType.GENERIC);
    }

    /**
     * Adds a simple loot table that only drops the block itself
     *
     * @param block block to be dropped
     */
    public void addSimpleBlockLootTable(Block block) {
        this.addJson(block.getLootTable(),
                LootTables.serialize(createSingleItemTable(block)
                        .setParamSet(LootContextParamSets.BLOCK).build()),
                ResType.LOOT_TABLES);
    }


    protected static LootTable.Builder createSingleItemTable(ItemLike itemLike) {
        return LootTable.lootTable()
                .withPool(
                        LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0F))
                                .add(LootItem.lootTableItem(itemLike)).unwrap());
    }


    public void addRecipe(FinishedRecipe recipe) {
        this.addJson(recipe.getId(), recipe.serializeRecipe(), ResType.RECIPES);
        ResourceLocation advancementId = recipe.getAdvancementId();
        if (advancementId != null) {
            this.addJson(recipe.getAdvancementId(), recipe.serializeAdvancement(), ResType.ADVANCEMENTS);
        }
    }

    public void addRecipeNoAdvancement(FinishedRecipe recipe) {
        this.addJson(recipe.getId(), recipe.serializeRecipe(), ResType.RECIPES);
    }


}
