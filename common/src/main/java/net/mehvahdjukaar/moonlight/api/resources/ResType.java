package net.mehvahdjukaar.moonlight.api.resources;


import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

public enum ResType {
    GENERIC("%s"),
    TAGS("tags/%s.json"),
    LOOT_TABLES("loot_tables/%s.json"),
    BLOCK_LOOT_TABLES("loot_tables/blocks/%s.json"),
    RECIPES("recipes/%s.json"),
    ADVANCEMENTS("advancements/%s.json"),
    CONFIGURED_FEATURES("worldgen/configured_feature/%s.json"),
    STRUCTURE("worldgen/structure/%s.json"),
    STRUCTURE_SET("worldgen/structure_set/%s.json"),
    TEMPLATE_POOL("worldgen/template_pool/%s.json"),

    LANG("lang/%s.json"),
    TEXTURES("textures/%s.png"),
    BLOCK_TEXTURES("textures/block/%s.png"),
    ITEM_TEXTURES("textures/item/%s.png"),
    ENTITY_TEXTURES("textures/entity/%s.png"),
    PARTICLE_TEXTURES("textures/particle/%s.png"),
    MCMETA("textures/%s.png.mcmeta"),
    BLOCK_MCMETA("textures/block/%s.png.mcmeta"),
    ITEM_MCMETA("textures/item/%s.png.mcmeta"),
    MODELS("models/%s.json"),
    BLOCK_MODELS("models/block/%s.json"),
    ITEM_MODELS("models/item/%s.json"),
    BLOCKSTATES("blockstates/%s.json"),
    PARTICLES("particles/%s.json"),
    MOB_EFFECT_TEXTURES("mob_effect/%s.json"),
    JSON("%s.json"),
    PNG("%s.png");

    private final String loc;

    ResType(String loc) {
        this.loc = loc;
    }


    public ResourceLocation getPath(ResourceLocation relativeLocation) {
        return relativeLocation.withPath(String.format(this.loc, relativeLocation.getPath()));
    }

    public ResourceLocation getPath(String relativeLocation) {
        return this.getPath(ResourceLocation.parse(relativeLocation));
    }

    public static ResourceLocation getTagPath(TagKey<?> tag) {
     return    TAGS.getPath(tag.location().withPrefix(tag.registry().location().getPath() + "s/"));
    }

}