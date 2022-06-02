package net.mehvahdjukaar.selene.resourcepack.resources;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TagBuilder {

    private final ResourceLocation id;
    private final List<String> entries = new ArrayList<>();

    private boolean replace = false;

    private TagBuilder(ResourceLocation location) {
        this.id = location;
    }

    public static TagBuilder of(ResourceLocation location) {
        return new TagBuilder(location);
    }

    public static TagBuilder of(TagKey<?> key) {
        return new TagBuilder(key.location());
    }


    public ResourceLocation getId() {
        return id;
    }

    public TagBuilder setReplace(boolean replace) {
        this.replace = replace;
        return this;
    }

    public TagBuilder add(ResourceLocation entry) {
        return add(entry.toString());
    }

    public TagBuilder add(String entry) {
        this.entries.add(entry);
        return this;
    }

    public TagBuilder addAll(ResourceLocation... locations) {
        entries.forEach(this::add);
        return this;
    }

    public TagBuilder addAll(String... locations) {
        entries.forEach(this::add);
        return this;
    }

    public <V extends IForgeRegistryEntry<V>, T extends ForgeRegistryEntry<V>> TagBuilder addEntries(Collection<T> entries) {
        entries.forEach(e -> this.add(e.getRegistryName()));
        return this;
    }

    public TagBuilder addTag(ResourceLocation resourceLocation) {
        return this.add("#" + resourceLocation.toString());
    }

    public TagBuilder addTag(TagKey<?> tagKey) {
        return this.add("#" + tagKey.location());
    }

    public TagBuilder addTag(TagBuilder otherBuilder) {
        return this.add("#" + otherBuilder.getId().toString());
    }

    public JsonElement build() {
        JsonObject json = new JsonObject();
        json.addProperty("replace", this.replace);
        JsonArray array = new JsonArray();

        entries.forEach(array::add);
        json.add("values", array);
        String tagPath = id.getPath();
        if (tagPath.equals("block") || tagPath.equals("entity_type") || tagPath.equals("item")) tagPath = tagPath + "s";
        return json;
    }

    //TODO: add optional entries

}
