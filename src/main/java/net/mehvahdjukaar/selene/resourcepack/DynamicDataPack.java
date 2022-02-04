package net.mehvahdjukaar.selene.resourcepack;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.mehvahdjukaar.selene.data.BlockLootTableAccessor;
import net.mehvahdjukaar.selene.resourcepack.RPUtils.ResType;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.event.server.ServerStartedEvent;

import java.util.Collection;
import java.util.Locale;

public class DynamicDataPack extends DynamicResourcePack {

    public DynamicDataPack(ResourceLocation name) {
        super(name, PackType.SERVER_DATA);
    }

    /**
     * Needs to be called to register the pack. Call from forge event
     *
     * @param event ServerStartedEvent event
     */
    public void register(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        PackRepository packs = server.getPackRepository();
        this.addPackToRepository(packs);
        try {
            server.reloadResources(packs.getSelectedIds()).get();
        } catch (Exception e) {
            LOGGER.error("Failed to reload test resource packs.", e);
        }
    }

    public enum TagType {
        BLOCKS, ITEMS, ENTITY_TYPES
    }

    public void addTag(ResourceLocation tagLocation, Collection<ResourceLocation> values, TagType... types) {
        JsonObject json = new JsonObject();
        json.addProperty("replace", false);
        JsonArray array = new JsonArray();

        values.forEach(v -> array.add(v.toString()));
        json.add("values", array);

        for (TagType type : types) {
            this.addJson(new ResourceLocation(tagLocation.getNamespace(),
                    type.name().toLowerCase(Locale.ROOT) +"/"+ tagLocation.getPath()), json, ResType.TAGS);
        }
    }

    public void addLootTable(ResourceLocation lootTableLocation, JsonElement lootTable) {
        this.addJson(lootTableLocation, lootTable, ResType.LOOT_TABLES);
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
