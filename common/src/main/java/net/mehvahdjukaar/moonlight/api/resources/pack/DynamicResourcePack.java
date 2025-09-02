package net.mehvahdjukaar.moonlight.api.resources.pack;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import io.netty.util.internal.UnstableApi;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.resources.RPUtils;
import net.mehvahdjukaar.moonlight.api.resources.ResType;
import net.mehvahdjukaar.moonlight.api.resources.SimpleTagBuilder;
import net.mehvahdjukaar.moonlight.api.resources.StaticResource;
import net.mehvahdjukaar.moonlight.api.resources.assets.LangBuilder;
import net.mehvahdjukaar.moonlight.api.resources.textures.TextureImage;
import net.mehvahdjukaar.moonlight.core.CompatHandler;
import net.mehvahdjukaar.moonlight.core.MoonlightClient;
import net.mehvahdjukaar.moonlight.core.integration.ModernFixCompat;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;

@Deprecated(forRemoval = true)
public abstract class DynamicResourcePack extends InMemoryPackResources {

    protected static final Logger LOGGER = LogManager.getLogger();

    protected final Pack.Position position;

    public final String mainNamespace;
    public final ResourceLocation resourcePackName;
    private boolean needsClearingNonStatic = false;
    boolean addToStatic = false;

    protected DynamicResourcePack(ResourceLocation name, PackType type) {
        this(name, type, Pack.Position.TOP, false);
    }

    protected DynamicResourcePack(ResourceLocation name, PackType type, Pack.Position position, boolean fixed, boolean hidden) {
        this(name, type, position, hidden);
    }

    protected DynamicResourcePack(ResourceLocation name, PackType type, Pack.Position position, boolean hidden) {
        super(makeInfo(name), type, hidden);
        this.position = position;
        this.mainNamespace = name.getNamespace();
        this.resourcePackName = name;
    }

    private static PackLocationInfo makeInfo(ResourceLocation name) {
        return new PackLocationInfo(
                name.toString(),    // id
                Component.translatable(LangBuilder.getReadableName(name.toString())), // title
                PackSource.BUILT_IN,
                Optional.empty() //no clue what this is
        );
    }


    /**
     * Marks this texture as non-clearable.
     * By default, all textures will be cleared after texture atlases have been created
     * Call this for textures that are not on an atlas.
     */
    public void markNotClearable(ResourceLocation texturePath) {
    }

    public void unMarkNotClearable(ResourceLocation staticResources) {
    }

    public ResourceLocation id() {
        return resourcePackName;
    }

    @Override
    public String toString() {
        return packId();
    }

    public Component getTitle() {
        return location().title();
    }

    /**
     * Registers this pack. Call on mod init
     */
    //shit code here
    public void registerPack() {
        PackType packType = this.getPackType();
        DynamicResourcePack p = this;
        if (packType == PackType.CLIENT_RESOURCES) {
            if (MoonlightClient.maybeMergeLegacyPack(this)) {
                return;
            }
        }
        RegHelper.registerResourcePack(packType, () ->
                Pack.readMetaAndCreate(
                        this.location(),
                        new Pack.ResourcesSupplier() {
                            @Override
                            public PackResources openPrimary(PackLocationInfo location) {
                                return p;
                            }

                            @Override
                            public PackResources openFull(PackLocationInfo location, Pack.Metadata metadata) {
                                return p;
                            }
                        },// pack supplier
                        packType,
                        new PackSelectionConfig(
                                true,    // required -- this MAY need to be true for the pack to be enabled by default
                                Pack.Position.TOP,
                                false // fixed position
                        )
                ));
    }

    //@Override

    public FileNotFoundException makeFileNotFoundException(String path) {
        return new FileNotFoundException(String.format("'%s' in ResourcePack '%s'", path, this.resourcePackName));
    }


    @Deprecated(forRemoval = true)
    public void removeResource(ResourceLocation res) {
        synchronized (this) {
            this.searchTrie.remove(res);
            this.resources.remove(res);
        }
    }


    // Called after texture have been stitched. Only keeps needed stuff
    public final void clearNonStatic() {
        //not used anymore
    }

    // Called after each reload
    public void clearAllContent() {
        synchronized (this) {
            this.searchTrie.clear();
            this.resources.clear();
            this.needsClearingNonStatic = true; //clear non static after dynamic textures have been stitched
        }
    }

    private static final boolean MODERN_FIX = CompatHandler.MODERNFIX && ModernFixCompat.areLazyResourcesOn();

    private boolean modernFixHack(String s) {
        return s.startsWith("model") || s.startsWith("blockstate");
    }


    @Deprecated(forRemoval = true)
    public void addResource(StaticResource resource) {
        this.addResource(resource.location, resource.data);
    }

    @Deprecated(forRemoval = true)
    private void addJson(ResourceLocation path, JsonElement json) {
        try {
            this.addResource(path, RPUtils.serializeJson(json).getBytes());
        } catch (IOException e) {
            LOGGER.error("Failed to write JSON {} to resource pack {}.", path, this.resourcePackName, e);
        }
    }

    @Deprecated(forRemoval = true)
    public void addJson(ResourceLocation location, JsonElement json, ResType resType) {
        this.addJson(resType.getPath(location), json);
    }

    @Deprecated(forRemoval = true)
    public void addBytes(ResourceLocation location, byte[] bytes, ResType resType) {
        this.addResource(resType.getPath(location), bytes);
    }


    @Deprecated(forRemoval = true)
    public void addTag(SimpleTagBuilder builder, ResourceKey<?> type) {

        ResourceLocation tagId = builder.getId();
        String tagPath = type.location().getPath();
        ResourceLocation loc = ResType.TAGS.getPath(tagId.withPath(tagPath + "/" + tagId.getPath()));
        //merge tags
        //not needed anymore actually
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
    @Deprecated(forRemoval = true)
    public void addSimpleBlockLootTable(Block block) {
        this.addLootTable(block, createSingleItemTable(block)
                .setParamSet(LootContextParamSets.BLOCK));
    }

    @Deprecated(forRemoval = true)
    public void addLootTable(Block block, LootTable.Builder table) {
        this.addLootTable(block.getLootTable().location(), table.build());
    }

    @Deprecated(forRemoval = true)
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

    @Deprecated(forRemoval = true)
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

    @Deprecated(forRemoval = true)
    @UnstableApi
    public void addRecipeNoAdvancement(Recipe<?> recipe, ResourceLocation id) {
        this.addJson(id, RPUtils.writeRecipe(recipe), ResType.RECIPES);
    }


    @Deprecated(forRemoval = true)
    public void addAndCloseTexture(ResourceLocation path, TextureImage image) {
        addAndCloseTexture(path, image, true);
    }

    /**
     * Adds a new textures and closes the passed native image
     * Last boolean is for textures that aren't stitched so won't be cleared automatically after stitching
     * Use it for textures such as entity textures of GUI
     */
    @Deprecated(forRemoval = true)
    public void addAndCloseTexture(ResourceLocation path, TextureImage image, boolean isOnAtlas) {
        try (image) {
            this.addBytes(path, image.getImage().asByteArray(), ResType.TEXTURES);
            if (!isOnAtlas) this.markNotClearable(ResType.TEXTURES.getPath(path));
            if (image.getMcMeta() != null) {
                this.addJson(path, image.getMcMeta().toJson(), ResType.MCMETA);
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to add image {} to resource pack {}.", path, this, e);
        }
    }

    @Deprecated(forRemoval = true)
    public void addBlockModel(ResourceLocation modelLocation, JsonElement model) {
        this.addJson(modelLocation, model, ResType.BLOCK_MODELS);
    }

    @Deprecated(forRemoval = true)
    public void addItemModel(ResourceLocation modelLocation, JsonElement model) {
        this.addJson(modelLocation, model, ResType.ITEM_MODELS);
    }

    @Deprecated(forRemoval = true)
    public void addBlockState(ResourceLocation modelLocation, JsonElement model) {
        this.addJson(modelLocation, model, ResType.BLOCKSTATES);
    }

    @Deprecated(forRemoval = true)
    public void addLang(ResourceLocation langName, JsonElement language) {
        this.addJson(langName, language, ResType.LANG);
    }

    @Deprecated(forRemoval = true)
    public void addLang(ResourceLocation langName, LangBuilder builder) {
        this.addJson(langName, builder.build(), ResType.LANG);
    }

    @Deprecated(forRemoval = true)
    public void setGenerateDebugResources(boolean generateDebugResources) {
    }

    @Deprecated(forRemoval = true)
    public void setClearOnReload(boolean canBeCleared) {
    }
}