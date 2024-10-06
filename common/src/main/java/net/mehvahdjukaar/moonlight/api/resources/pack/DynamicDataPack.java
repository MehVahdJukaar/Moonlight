package net.mehvahdjukaar.moonlight.api.resources.pack;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import io.netty.util.internal.UnstableApi;
import net.mehvahdjukaar.moonlight.api.resources.RPUtils;
import net.mehvahdjukaar.moonlight.api.resources.ResType;
import net.mehvahdjukaar.moonlight.api.resources.SimpleTagBuilder;
import net.minecraft.Util;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

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
        ResourceLocation loc = ResType.TAGS.getPath(tagId.withPath(tagPath + "/" + tagId.getPath()));
        //merge tags
        if (this.resources.containsKey(loc)) {
            var r = resources.get(loc);
            try (var stream = new ByteArrayInputStream(r)) {
                var oldTag = RPUtils.deserializeJson(stream);
                builder.addFromJson(oldTag);
            } catch (Exception ignored) {
            }
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
        this.addLootTable(block, createSingleItemTable(block)
                .setParamSet(LootContextParamSets.BLOCK));
    }

    public void addLootTable(Block block, LootTable.Builder table) {
        this.addLootTable(block.getLootTable().location(), table.build());
    }

    public void addLootTable(ResourceLocation id, LootTable table) {
        this.addJson(id, LootDataType.TABLE.codec.encodeStart(JsonOps.INSTANCE, table).getOrThrow(), ResType.LOOT_TABLES);
    }

    protected static LootTable.Builder createSingleItemTable(ItemLike itemLike) {
        return LootTable.lootTable()
                .withPool(
                        LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0F))
                                .add(LootItem.lootTableItem(itemLike)).unwrap());
    }

    public void addRecipe(RecipeHolder<?> holder) {
        addRecipe(holder.value(), holder.id());
    }

    @UnstableApi
    public void addRecipe(Recipe<?> recipe, ResourceLocation id) {
        this.addRecipeNoAdvancement(recipe, id);

        //Advancement.Builder.recipeAdvancement().parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT);
        // ResourceLocation advancementId = recipe.getAdvancementId();
        //if (advancementId != null) {
        //  this.addJson(recipe.getAdvancementId(), recipe.serializeAdvancement(), ResType.ADVANCEMENTS);
        //}
    }

    @UnstableApi
    public void addRecipeNoAdvancement(Recipe<?> recipe, ResourceLocation id) {
        this.addJson(id, RPUtils.writeRecipe(recipe), ResType.RECIPES);
    }



}
