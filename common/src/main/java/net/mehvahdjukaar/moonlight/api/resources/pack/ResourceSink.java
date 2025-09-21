package net.mehvahdjukaar.moonlight.api.resources.pack;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.mehvahdjukaar.moonlight.api.misc.ThrowingSupplier;
import net.mehvahdjukaar.moonlight.api.resources.RPUtils;
import net.mehvahdjukaar.moonlight.api.resources.ResType;
import net.mehvahdjukaar.moonlight.api.resources.SimpleTagBuilder;
import net.mehvahdjukaar.moonlight.api.resources.StaticResource;
import net.mehvahdjukaar.moonlight.api.resources.assets.LangBuilder;
import net.mehvahdjukaar.moonlight.api.resources.textures.TextureImage;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
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
    private final Map<ResourceLocation, byte[]> resources = new HashMap<>();
    private final Map<TagKey<?>, SimpleTagBuilder> tags = new HashMap<>();

    public ResourceSink(String packNamespace, String packId) {
        this.packNamespace = packNamespace;
        this.packId = packId;
    }

    protected void addBytes(ResourceLocation id, byte[] bytes) {
        this.resources.put(id, Preconditions.checkNotNull(bytes));
    }


    public void addResource(StaticResource resource) {
        this.addBytes(resource.location, resource.data);
    }

    private void addJson(ResourceLocation path, JsonElement json) {
        try {
            this.addBytes(path, RPUtils.serializeJson(json).getBytes());
        } catch (IOException e) {
            Moonlight.LOGGER.error("Failed to write JSON {} to resource pack.", path, e);
        }
    }

    public void addJson(ResourceLocation location, JsonElement json, ResType resType) {
        this.addJson(resType.getPath(location), json);
    }

    public void addBytes(ResourceLocation location, byte[] bytes, ResType resType) {
        this.addBytes(resType.getPath(location), bytes);
    }


    @Deprecated(forRemoval = true)
    public void addAndCloseTexture(ResourceLocation path, TextureImage image) {
        addAndCloseTexture(path, image, true);
    }

    @Deprecated(forRemoval = true)
    public void addAndCloseTexture(ResourceLocation path, TextureImage image, boolean isOnAtlas) {
        try (image) {
            addTexture(path, image, isOnAtlas);
        } catch (Exception e) {
            Moonlight.LOGGER.warn("Failed to add image {} to resource pack {}.", path, this, e);
        }
    }

    //equivalent safer version of the old method with same name
    public void addAndCloseTexture(ResourceLocation path, Supplier<TextureImage> image) {
        try (TextureImage img = image.get()) {
            addTexture(path, img);
        }
    }

    @Deprecated(forRemoval = true)
    public void addTexture(ResourceLocation path, TextureImage image, boolean onAtlas) {
        addTexture(path, image);
    }

    /**
     * Adds a new textures and closes the passed native image
     * Last boolean is for textures that aren't stitched so won't be cleared automatically after stitching
     * Use it for textures such as entity textures of GUI.
     * You must close the texture yourself now
     */
    public void addTexture(ResourceLocation path, TextureImage image) {
        try {
            this.addBytes(path, image.getImage().asByteArray(), ResType.TEXTURES);
            if (image.getMcMeta() != null) {
                this.addJson(path, image.getMcMeta().toJson(), ResType.MCMETA);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated(forRemoval = true)
    public void markNotClearable(ResourceLocation path) {
    }

    public void addBlockModel(ResourceLocation modelLocation, JsonElement model) {
        this.addJson(modelLocation, model, ResType.BLOCK_MODELS);
    }

    public void addItemModel(ResourceLocation modelLocation, JsonElement model) {
        this.addJson(modelLocation, model, ResType.ITEM_MODELS);
    }

    public void addBlockState(ResourceLocation modelLocation, JsonElement model) {
        this.addJson(modelLocation, model, ResType.BLOCKSTATES);
    }

    public void addLang(ResourceLocation langName, JsonElement language) {
        this.addJson(langName, language, ResType.LANG);
    }

    public void addLang(ResourceLocation langName, LangBuilder builder) {
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

    public void addRecipe(Recipe<?> recipe, ResourceLocation id) {
        this.addRecipeNoAdvancement(recipe, id);

        //Advancement.Builder.recipeAdvancement().parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT);
        // ResourceLocation advancementId = recipe.getAdvancementId();
        //if (advancementId != null) {
        //  this.addJson(recipe.getAdvancementId(), recipe.serializeAdvancement(), ResType.ADVANCEMENTS);
        //}
    }

    public void addRecipeNoAdvancement(Recipe<?> recipe, ResourceLocation id) {
        this.addJson(id, RPUtils.writeRecipe(recipe), ResType.RECIPES);
    }


    public void addResourceIfNotPresent(ResourceManager manager, StaticResource resource) {
        if (!alreadyHasAssetAtLocation(manager, resource.location)) {
            this.addResource(resource);
        }
    }

    public void addJsonUnlessPresent(ResourceManager manager, ResourceLocation path, ThrowingSupplier<JsonElement> jsonSupplier) {
        if (!alreadyHasAssetAtLocation(manager, path)) {
            try {
                this.addJson(path, jsonSupplier.get());
            } catch (Exception e) {
                Moonlight.LOGGER.error("Failed to write JSON {} to resource pack.", path, e);
            }
        }
    }

    public boolean alreadyHasTextureAtLocation(ResourceManager manager, ResourceLocation res) {
        return alreadyHasAssetAtLocation(manager, res, ResType.TEXTURES);
    }

    @Deprecated(forRemoval = true)
    public void addTextureIfNotPresent(ResourceManager manager, String relativePath, Supplier<TextureImage> textureSupplier) {
        addTextureIfNotPresent(manager, relativePath, textureSupplier, true);
    }

    @Deprecated(forRemoval = true)
    public void addTextureIfNotPresent(ResourceManager manager, String relativePath, Supplier<TextureImage> textureSupplier, boolean isOnAtlas) {
        addTextureIfNotPresent(manager, (relativePath.contains(":") ? ResourceLocation.parse(relativePath) :
                ResourceLocation.fromNamespaceAndPath(this.packNamespace, relativePath)), textureSupplier);
    }

    public void addTextureIfNotPresent(ResourceManager manager, ResourceLocation res, Supplier<TextureImage> textureSupplier) {
        if (!alreadyHasTextureAtLocation(manager, res)) {
            try (TextureImage textureImage = textureSupplier.get()) {
                this.addTexture(res, textureImage);
            } catch (Exception e) {
                Moonlight.LOGGER.error("Failed to generate texture {}: {}", res, e);
            }
        }
    }

    public void addTextureUnlessPresent(ResourceManager manager, ResourceLocation res, ThrowingSupplier<TextureImage> textureSupplier) {
        if (!alreadyHasTextureAtLocation(manager, res)) {
            try (TextureImage textureImage = textureSupplier.get()) {
                this.addTexture(res, textureImage);
            } catch (Exception e) {
                Moonlight.LOGGER.error("Failed to generate texture {}: {}", res, e);
            }
        }
    }

    public boolean alreadyHasAssetAtLocation(ResourceManager manager, ResourceLocation res, ResType type) {
        return alreadyHasAssetAtLocation(manager, type.getPath(res));
    }

    public boolean alreadyHasAssetAtLocation(ResourceManager manager, ResourceLocation res) {
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
        ResourceLocation fullPath = resource.location;

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
        ResourceLocation newRes = ResourceLocation.fromNamespaceAndPath(this.packNamespace, builder.toString());
        if (!alreadyHasAssetAtLocation(manager, newRes)) {
            String fullText = resource.asString();
            fullText = textTransform.apply(fullText);

            this.addBytes(newRes, fullText.getBytes());
        }
    }

    public void copyResource(ResourceManager manager, ResourceLocation from, ResourceLocation to, boolean lenient) {
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


    /**
     * Utility method to add models overrides in a non-destructive way. Provided overrides will be added on top of whatever model is currently provided by vanilla or mod resources. IE: crossbows
     */
    public void appendModelOverride(ResourceManager manager, ResourceLocation modelRes,
                                    Consumer<RPUtils.OverrideAppender> modelConsumer) {
        JsonElement json = RPUtils.makeModelOverride(manager, modelRes, modelConsumer);
        this.addItemModel(modelRes, json);
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

                ResourceLocation tagId = builder.getId();
                ResourceLocation loc = ResType.TAGS.getPath(tagId.withPath(
                        key.registry().location().getPath() + "/" + tagId.getPath()));

                dummy.addJson(loc, builder.serializeToJson(), ResType.GENERIC);
            }
        }
        dummy.resources.forEach(pack::addResource);
    }

}
