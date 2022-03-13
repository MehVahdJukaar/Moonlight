package net.mehvahdjukaar.selene.resourcepack;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.mehvahdjukaar.selene.Selene;
import net.mehvahdjukaar.selene.data.BlockLootTableAccessor;
import net.mehvahdjukaar.selene.resourcepack.RPUtils.ResType;
import net.minecraft.core.Registry;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class DynamicDataPack extends DynamicResourcePack {

    public DynamicDataPack(ResourceLocation name, PackType type, Pack.Position position, boolean fixed, boolean hidden) {
        super(name, PackType.SERVER_DATA, position, fixed, hidden);
    }

    public DynamicDataPack(ResourceLocation name) {
        super(name, PackType.SERVER_DATA);
    }

    @Deprecated
    public enum TagType {
        BLOCKS, ITEMS, ENTITY_TYPES
    }

    @Deprecated
    public void addTag(ResourceLocation tagLocation, Collection<ResourceLocation> values, TagType... types) {
        for(var v : types){
            switch (v){
                case BLOCKS -> addTag(tagLocation, values, Registry.BLOCK_REGISTRY);
                case ITEMS -> addTag(tagLocation, values, Registry.ITEM_REGISTRY);
                case ENTITY_TYPES -> addTag(tagLocation, values, Registry.ENTITY_TYPE_REGISTRY);
            };
        }
    }

    public <T> void addTag(ResourceLocation tagLocation, Collection<ResourceLocation> values, ResourceKey<Registry<T>> type) {
        JsonObject json = new JsonObject();
        json.addProperty("replace", false);
        JsonArray array = new JsonArray();

        values.forEach(v -> array.add(v.toString()));
        json.add("values", array);
        this.addJson(new ResourceLocation(tagLocation.getNamespace(),
                type.location().getPath() +"/"+ tagLocation.getPath()), json, ResType.TAGS);

    }

    /**
     * Adds a simple loot table that only drops the block itself
     *
     * @param block block to be dropped
     */
    public void addSimpleBlockLootTable(Block block) {
        this.addJson(block.getLootTable(),
                LootTables.serialize(BlockLootTableAccessor.dropping(block).setParamSet(LootContextParamSets.BLOCK).build()),
                ResType.LOOT_TABLES);
    }

    public void addRecipe(FinishedRecipe recipe) {
        this.addJson(recipe.getId(), recipe.serializeRecipe(), ResType.RECIPES);
    }

    public void addRecipeWithAdvancement(FinishedRecipe recipe) {
        this.addRecipe(recipe);
        ResourceLocation advancementId = recipe.getAdvancementId();
        if (advancementId != null) {
            this.addJson(advancementId, recipe.serializeAdvancement(), ResType.ADVANCEMENTS);
        }
    }


}
