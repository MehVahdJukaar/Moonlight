package net.mehvahdjukaar.moonlight.api.resources.pack;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.serialization.JsonOps;
import net.mehvahdjukaar.moonlight.api.misc.ThrowingSupplier;
import net.mehvahdjukaar.moonlight.api.resources.*;
import net.mehvahdjukaar.moonlight.api.resources.assets.LangBuilder;
import net.mehvahdjukaar.moonlight.api.resources.textures.SpriteUtils;
import net.mehvahdjukaar.moonlight.api.resources.textures.TextureImage;
import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ResourceSink {

    private final String packNamespace;
    private final String packId;
    private final Map<Identifier, byte[]> resources = new HashMap<>();
    private final Map<TagKey<?>, SimpleTagBuilder> tags = new HashMap<>();

    public ResourceSink(String packNamespace, String packId) {
        this.packNamespace = packNamespace;
        this.packId = packId;
    }

    protected void addBytes(Identifier id, byte[] bytes) {
        this.resources.put(id, Preconditions.checkNotNull(bytes));
    }


    public void addResource(StaticResource resource) {
        this.addBytes(resource.location, resource.data);
    }

    private void addJson(Identifier path, JsonElement json) {
        try {
            this.addBytes(path, RPUtils.serializeJson(json).getBytes());
        } catch (IOException e) {
            Moonlight.LOGGER.error("Failed to write JSON {} to resource pack.", path, e);
        }
    }

    public void addJson(Identifier location, JsonElement json, ResType resType) {
        this.addJson(resType.getPath(location), json);
    }

    public void addBytes(Identifier location, byte[] bytes, ResType resType) {
        this.addBytes(resType.getPath(location), bytes);
    }


    //equivalent safer version of the old method with same name
    public void addAndCloseTexture(Identifier path, Supplier<TextureImage> image) {
        try (TextureImage img = image.get()) {
            addTexture(path, img);
        }
    }

    /**
     * Adds a new textures and closes the passed native image
     * Last boolean is for textures that aren't stitched so won't be cleared automatically after stitching
     * Use it for textures such as entity textures of GUI.
     * You must close the texture yourself now
     */
    public void addTexture(Identifier path, TextureImage texture) {
        try {
            NativeImage image = texture.getImage();
            if (!texture.isAllocated()) {
                Moonlight.crashIfInDev("Tried to save a non allocated texture image at " + path + " \nDid you close it too early?");
                return;
            }
            this.addBytes(path, SpriteUtils.toPngBytes(image), ResType.TEXTURES);
            if (texture.getMcMeta() != null) {
                this.addJson(path, texture.getMcMeta().toJson(), ResType.MCMETA);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void addBlockModel(Identifier modelLocation, JsonElement model) {
        this.addJson(modelLocation, model, ResType.BLOCK_MODELS);
    }

    public void addItemModel(Identifier modelLocation, JsonElement model) {
        this.addJson(modelLocation, model, ResType.ITEM_MODELS);
    }

    /** itemId is the item id, not a model path. */
    public void addItemModelDefinition(Identifier itemId, JsonElement definition) {
        this.addJson(itemId, definition, ResType.ITEMS);
    }

    public void addBlockState(Identifier modelLocation, JsonElement model) {
        this.addJson(modelLocation, model, ResType.BLOCKSTATES);
    }

    public void addLang(Identifier langName, JsonElement language) {
        this.addJson(langName, language, ResType.LANG);
    }

    public void addLang(Identifier langName, LangBuilder builder) {
        this.addJson(langName, builder.build(), ResType.LANG);
    }


    public void addTag(SimpleTagBuilder builder, ResourceKey<? extends Registry<?>> reg) {
        TagKey key = TagKey.create((ResourceKey) reg, builder.getId());
        this.tags.merge(key, builder, (a, b) -> {
            a.merge(b);
            return a;
        });
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
        block.getLootTable().ifPresent(key -> this.addLootTable(key.identifier(), table.build()));
    }

    public void addLootTable(Identifier id, LootTable table) {
        this.addJson(id, LootDataType.TABLE.codec().encodeStart(JsonOps.INSTANCE, table).getOrThrow(), ResType.LOOT_TABLES);
    }

    protected static LootTable.Builder createSingleItemTable(ItemLike itemLike) {
        return LootTable.lootTable()
                .withPool(
                        LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0F))
                                .add(LootItem.lootTableItem(itemLike)).unwrap());
    }

    public void addRecipe(RecipeHolder<?> holder) {
        addRecipe(holder.value(), holder.id().identifier());
    }

    public void addRecipe(Recipe<?> recipe, Identifier id) {
        this.addRecipeNoAdvancement(recipe, id);

        //Advancement.Builder.recipeAdvancement().parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT);
        // Identifier advancementId = recipe.getAdvancementId();
        //if (advancementId != null) {
        //  this.addJson(recipe.getAdvancementId(), recipe.serializeAdvancement(), ResType.ADVANCEMENTS);
        //}
    }

    public void addRecipeNoAdvancement(Recipe<?> recipe, Identifier id) {
        this.addJson(id, RPUtils.writeRecipe(recipe), ResType.RECIPES);
    }


    public void addResourceIfNotPresent(ResourceManager manager, StaticResource resource) {
        if (!alreadyHasAssetAtLocation(manager, resource.location)) {
            this.addResource(resource);
        }
    }

    public void addJsonUnlessPresent(ResourceManager manager, Identifier path, ThrowingSupplier<JsonElement> jsonSupplier) {
        if (!alreadyHasAssetAtLocation(manager, path)) {
            try {
                this.addJson(path, jsonSupplier.get());
            } catch (Exception e) {
                Moonlight.LOGGER.error("Failed to write JSON {} to resource pack.", path, e);
            }
        }
    }

    public boolean alreadyHasTextureAtLocation(ResourceManager manager, Identifier res) {
        return alreadyHasAssetAtLocation(manager, res, ResType.TEXTURES);
    }

    public void addTextureIfNotPresent(ResourceManager manager, Identifier res, Supplier<TextureImage> textureSupplier) {
        if (!alreadyHasTextureAtLocation(manager, res)) {
            try (TextureImage textureImage = textureSupplier.get()) {
                this.addTexture(res, textureImage);
            } catch (Exception e) {
                Moonlight.LOGGER.error("Failed to generate texture {}: {}", res, e);
            }
        }
    }

    public void addTextureUnlessPresent(ResourceManager manager, Identifier res, ThrowingSupplier<TextureImage> textureSupplier) {
        if (!alreadyHasTextureAtLocation(manager, res)) {
            try (TextureImage textureImage = textureSupplier.get()) {
                this.addTexture(res, textureImage);
            } catch (Exception e) {
                Moonlight.LOGGER.error("Failed to generate texture {}: {}", res, e);
            }
        }
    }

    public boolean alreadyHasAssetAtLocation(ResourceManager manager, Identifier res, ResType type) {
        return alreadyHasAssetAtLocation(manager, type.getPath(res));
    }

    public boolean alreadyHasAssetAtLocation(ResourceManager manager, Identifier res) {
        var resource = manager.getResource(res);
        return resource.filter(value -> !value.sourcePackId().equals(this.packId)).isPresent();
    }

    /**
     * This is a handy method for dynamic resource pack since it allows to specify the name of an existing resource
     * that will then be copied and modified replacing a certain keyword in it with another.
     * This is useful when adding new woodtypes as one can simply manually add a default wood json and provide the method with the
     * default woodtype name and the target name
     * The target location will the one of this pack while its path will be the original one modified following the same principle as the json itself
     *
     * @param resource    target resource that will be copied, modified and saved back
     * @param keyword     keyword to replace
     * @param replaceWith word to replace the keyword with
     */
    public void addSimilarJsonResource(ResourceManager manager, StaticResource resource, String keyword, String replaceWith) throws NoSuchElementException {
        addSimilarJsonResource(manager, resource, s -> s.replace(keyword, replaceWith));
    }

    public void addSimilarJsonResource(ResourceManager manager, StaticResource resource, Function<String, String> textTransform) throws NoSuchElementException {
        addSimilarJsonResource(manager, resource, textTransform, textTransform);
    }

    public void addSimilarJsonResource(ResourceManager manager, StaticResource resource, Function<String, String> textTransform, Function<String, String> pathTransform) throws NoSuchElementException {
        Identifier fullPath = resource.location;

        //calculates new path
        StringBuilder builder = new StringBuilder();
        String[] partial = fullPath.getPath().split("/");
        for (int i = 0; i < partial.length; i++) {
            if (i != 0) builder.append("/");
            if (i == partial.length - 1) {
                builder.append(pathTransform.apply(partial[i]));
            } else builder.append(partial[i]);
        }
        //adds modified under my namespace
        Identifier newRes = Identifier.fromNamespaceAndPath(this.packNamespace, builder.toString());
        if (!alreadyHasAssetAtLocation(manager, newRes)) {
            String fullText = resource.asString();
            fullText = textTransform.apply(fullText);

            this.addBytes(newRes, fullText.getBytes());
        }
    }

    public void copyResource(ResourceManager manager, Identifier from, Identifier to, boolean lenient) {
        var resource = manager.getResource(from);
        if (resource.isPresent()) {
            var s = StaticResource.of(resource.get(), from);
            this.addBytes(to, s.data);
        } else {
            if (lenient) {
                Moonlight.LOGGER.info("Resource {} not found for copying to {}", from, to);
            } else {
                throw new NoSuchElementException("Resource " + from + " not found for copying to " + to);
            }
        }
    }

    public <T extends BlockType> void addBlockTypeSwapRecipe(ResourceManager manager, Identifier originalRecipeId,
                                                             T originalMat, T destinationMat, Identifier baseID) {
        StaticResource originalResource = StaticResource.getOrThrow(manager,
                ResType.RECIPES.getPath(originalRecipeId));
        JsonObject originalJson = originalResource.toJson();
        Recipe<?> originalRecipe = RPUtils.readRecipe(originalJson);
        RecipeHolder<?> newRecipe = RecipeTemplate.makeSimilarRecipe(originalRecipe, originalMat, destinationMat, baseID);
        JsonElement newJson = RPUtils.writeRecipe(newRecipe.value());
        //copy conditions
        if (newJson instanceof JsonObject jo) {
            for (var e : originalJson.entrySet()) {
                var key = e.getKey();
                if (key.contains("condition") && !jo.has(key)) {
                    jo.add(key, e.getValue());
                }
            }
        }
        Identifier newRecipeId = newRecipe.id().identifier();
        if (!alreadyHasAssetAtLocation(manager, newRecipeId)) this.addJson(newRecipeId, newJson, ResType.RECIPES);
    }


    // a bit ugly here, also it means resource sink is not reusable in other contexts that much
    public static void acceptSinks(IEditablePackResources pack, Collection<ResourceSink> sinks) {

        Multimap<TagKey<?>, SimpleTagBuilder> tags = HashMultimap.create();
        for (ResourceSink sink : sinks) {
            for (var r : sink.resources.entrySet()) {
                pack.addResource(r.getKey(), r.getValue());
            }
            for (var e : sink.tags.entrySet()) {
                tags.put(e.getKey(), e.getValue());
            }
        }
        //special stuff for tags since they are weird and merged

        //adds tags
        ResourceSink dummy = new ResourceSink("dummy", "dummy");
        for (var key : tags.keySet()) {
            var it = tags.get(key).iterator();
            if (it.hasNext()) {
                SimpleTagBuilder builder = it.next();
                while (it.hasNext()) {
                    builder.merge(it.next());
                }

                Identifier tagId = builder.getId();
                Identifier loc = ResType.TAGS.getPath(tagId.withPath(
                        key.registry().identifier().getPath() + "/" + tagId.getPath()));

                dummy.addJson(loc, builder.serializeToJson(), ResType.GENERIC);
            }
        }
        dummy.resources.forEach(pack::addResource);
    }

    public void appendItemToEnchantment(ResourceManager manager, ResourceKey<Enchantment> ench, Item... items) {
        Identifier id = ench.identifier();
        try (var model = manager.getResourceOrThrow(ResType.ENCHANTMENTS.getPath(id)).open()) {
            JsonObject json = RPUtils.deserializeJson(model);
            JsonElement supportedItems = json.get("supported_items");
            SimpleTagBuilder tb = SimpleTagBuilder.of(id.withPrefix("enchantable/"));
            String tagName = tb.getTagString();
            if (supportedItems instanceof JsonArray arr) {
                for (var a : arr) {
                    if (a.isJsonPrimitive()) {
                        String asString = a.getAsString();
                        if (!tagName.equals(asString)) tb.add(asString);
                    }
                }
            } else if (supportedItems.isJsonPrimitive()) {
                String asString = supportedItems.getAsString();
                if (!tagName.equals(asString)) tb.add(asString);
            }
            for (Item item : items) {
                tb.addEntry(item);
            }
            json.addProperty("supported_items", tagName);

            this.addJson(id, json, ResType.ENCHANTMENTS);
            this.addTag(tb, Registries.ITEM);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
